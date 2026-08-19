
# Backtest Module

---

### Build requirements
- Language: C++17
- Build tool: CMake
- Links against the Risk Module, see "The module is two stages" below.
  `risk.h` must therefore carry `extern "C"` guards so this C++ code can include
  the C header.

Everything exposed to Java must be wrapped in `extern "C"` so the C++ compiler
does not mangle the names. JNA looks functions up by their plain C name.

---

## In a nutshell:
The Backtest Module's primary objective is to simulate a strategy over
historical data and report four things about the result:
- Total return
- Annualized return
- Max Drawdown
- Sharpe Ratio

To do this, we require the following data:
```c
typedef struct
{
    const double* prices;        // Price matrix, day major (see layout below)
    int instruments;             // Number of instruments, 1 to 500
    int days;                    // Number of trading days, minimum 3
    const double* weights;       // Target weight per instrument, must sum to 1.0. NULL means equal weight
    const int* rebalance_days;   // Day indices where the portfolio is rebalanced. NULL means never
    int rebalance_count;         // Number of entries in rebalance_days
    double initial_capital;      // Starting portfolio value, e.g. 100000.0
    double risk_free_rate;       // E.g 0.02 for 2% yearly interest rate
    int periods_per_year;        // Number of stock market days per year (usually 252)
} BacktestInput;
```

---

## The module is two stages, not one

The simulation produces one number per day: the portfolio value on that day.
That series is the equity curve, and every output is derived from it.

```
prices matrix --> [ simulation ] --> equity curve --> [ statistics ] --> 4 outputs
     (2D)                               (1D)
```

The second stage is the Risk Module. Max drawdown and Sharpe ratio on an equity
curve are the exact same calculation as max drawdown and Sharpe ratio on a price
series, so once the simulation hands over the equity curve, half this module is
already written. Only the simulation stage is new work.

**How the reuse actually happens.** This module links the risk library and calls
`risk_compute` on the equity curve, then copies two of its three outputs into
`BacktestResult`. That keeps it one call for Java, at the cost of a real build
dependency between the two modules.

The alternative is worth knowing about: return only `total_return`,
`annualized_return` and the equity curve, and let Java call `risk_compute`
itself. That keeps the two modules completely independent and costs one extra
crossing. It also means `BacktestResult` no longer matches the shape the course
sketched. We take the linked version, but if the build dependency turns out to
be painful in Docker, switching is a small change.

---

## Specific needs for each variable:

**Total return**
- `prices`, `weights`, `rebalance_days`: not because the formula needs them,
  but because it needs the simulation to have run. Total return only reads the
  first and last point of the equity curve, but the path decides what the last
  point is.
- `initial_capital`: cancels out of the ratio, so it changes the equity curve's
  scale but not this number.
- Does not need the risk free rate or `periods_per_year`.

**Annualized return**
- Everything total return needs.
- `days` and `periods_per_year`: to know how many years the simulation covered.
  Turning a total return into a per year figure is the only thing they do here.
- Does not need the risk free rate.

**Max Drawdown**
- The equity curve. Nothing else.
- Same as in the Risk Module: a pure path measurement, no returns,
  no annualization, no rate.

**Sharpe Ratio**
- The equity curve, for the mean return and the volatility.
- `risk_free_rate`.
- `periods_per_year`, twice: to convert the annual risk free rate down to a
  per day rate, and to annualize the final figure.

### Summary

|                   | equity curve | `risk_free_rate` | `periods_per_year`         |
|-------------------|--------------|------------------|----------------------------|
| Total return      | yes          | no               | no                         |
| Annualized return | yes          | no               | yes, annualization only    |
| Max Drawdown      | yes          | no               | no                         |
| Sharpe Ratio      | yes          | yes              | yes                        |

Every row says "equity curve", not "prices". Nothing in the statistics stage
ever looks at an individual instrument. That is the whole point of the split.

### Why `instruments` and `days` are in the struct but not the lists above

Same reason `length` is in the Risk Module's struct. A C array does not carry
its own size, and this one is two dimensional, so it needs both extents to be
addressable at all.

---

## How `prices` is laid out

The matrix is **day major**: all instruments for one day sit next to each other.

```
index = day * instruments + instrument

day 0: [ instr 0 ][ instr 1 ][ instr 2 ] ... [ instr n-1 ]
day 1: [ instr 0 ][ instr 1 ][ instr 2 ] ... [ instr n-1 ]
...
```

This is not arbitrary. The simulation walks forward one day at a time and
touches every instrument on each step, so a day major layout means the loop
reads straight through memory in order. Instrument major would jump `days`
elements between every read and miss the cache on nearly every access.

This is the actual reason the module is native. Not raw arithmetic speed:
control over memory layout on a 500 x 1260 matrix.

Size for the full case: 500 instruments x 5 years x 252 days x 8 bytes is about
5 MB. Small enough to pass in a single call, which is what we want anyway,
because the JNA crossing should happen once per backtest and not once per
instrument.

---

## Strategies

The strategy is not a string. The course's original sketch, now in the git
history of `native/README.md`, passes `const char* strategy` and compares it
inside the module, which means a string parse and a `strcmp` chain in the hot
path, plus typos that only surface at runtime. We take the schedule as data
instead:

- `rebalance_days == NULL` is buy and hold. Capital is allocated once on day 0
  according to `weights` and never touched again.
- Otherwise `rebalance_days` lists the day indices where the portfolio is sold
  back to `weights`.

Monthly rebalancing then becomes an array Java hands us, which is correct
because **the module never sees dates**. It cannot know which day index is the
first trading day of March. Java has the calendar, so Java decides the
schedule and we execute it.

---

## Assumptions about the input data

These are things the module takes for granted. It cannot verify most of them,
so they are the Java side's responsibility. If any of them is violated,
the numbers come out wrong rather than failing loudly.

- `prices` is a fully rectangular matrix. Every instrument has a value on every
  day. There is no way to represent a hole. If an instrument was not listed yet
  at the start of the window, Java must either forward fill it or leave it out
  of the run entirely.

- Column `d` is the same calendar date for every instrument. A misaligned
  calendar silently correlates instruments that never traded on the same day.

- All prices are in one currency, already converted. That is the FX Module's
  job, and it has to happen before the matrix gets here. A series that switches
  currency mid window looks like a real price move.

- The base currency is part of the question, not a display setting. The user can
  switch it, and every one of the four outputs changes when they do, because a
  USD instrument valued in SEK also carries the USD/SEK movement. The same
  backtest in two base currencies gives two different and equally correct
  answers, so any cached result has to be keyed on base currency as well as on
  the instrument set, the weights and the schedule.

- Prices are adjusted for splits and dividends. An unadjusted series makes a
  dividend payout look like a loss.

- All prices are strictly positive and finite.

- `weights` sums to 1.0 and holds no negative entries. No shorting, no leverage.

- No cash flows during the simulation. The portfolio starts with
  `initial_capital` and is closed at the end. Nothing is deposited or withdrawn.

- Fractional shares are allowed and there are no transaction costs, no spread
  and no tax. This flatters every strategy, and it flatters rebalancing hardest,
  because rebalancing is exactly the strategy that pays fees in real life.
  Any comparison between buy and hold and a rebalancing schedule is optimistic
  until fees exist.

---

## Error handling

Same contract as the Risk Module: status as the `int` return value, results
through a caller allocated out parameter. The input arrives as a `const`
pointer to a struct rather than as flat arguments, because there are nine of
them. A pointer to a struct is safe across JNA. A struct passed *by value*
is the fragile case, and we avoid it in both directions.

```c
Entry point:
    int backtest_run(const BacktestInput* in,
                     BacktestResult*      out,
                     double*              equity_out);
```

`equity_out` is optional. If it is not NULL it must point to space for `days`
doubles, and the module writes the full equity curve into it so the frontend
can plot the line rather than just the summary. Pass NULL when only the four
numbers are wanted. Java owns the allocation either way, so there is nothing
to free.

### Status codes

Three bands, so the caller can branch on the sign alone:

| Value | Name | Meaning |
|---|---|---|
| `0`  | `BACKTEST_OK`                 | All four outputs valid. |
| `+1` | `BACKTEST_WARN_FLAT_EQUITY`   | The equity curve never moved, so Sharpe is undefined. The other three outputs are valid, `sharpe_ratio` is set to NaN. |
| `-1` | `BACKTEST_ERR_NULL`           | `in`, `out` or `in->prices` is NULL. |
| `-2` | `BACKTEST_ERR_TOO_SHORT`      | `days < 3`. |
| `-3` | `BACKTEST_ERR_BAD_PRICE`      | Some element is zero, negative, NaN or infinite. |
| `-4` | `BACKTEST_ERR_BAD_PERIODS`    | `periods_per_year < 1`. |
| `-5` | `BACKTEST_ERR_BAD_RATE`       | `risk_free_rate` is NaN, infinite, or `<= -1.0`. |
| `-6` | `BACKTEST_ERR_BAD_INSTRUMENTS`| `instruments < 1` or `> 500`. |
| `-7` | `BACKTEST_ERR_BAD_WEIGHTS`    | Weights do not sum to 1.0 within tolerance, or some weight is negative. |
| `-8` | `BACKTEST_ERR_BAD_SCHEDULE`   | A rebalance day index is out of range or the list is not sorted. |
| `-9` | `BACKTEST_ERR_BAD_CAPITAL`    | `initial_capital <= 0`. |

- **Negative means nothing was computed.** `out` and `equity_out` are left
  completely untouched, so the caller must not read them.
- **Zero or positive means `out` was written.** A positive code is a warning
  about one specific field, not a failure.
- Validation happens before any arithmetic, so a bad input never produces a
  half filled result.

A companion lookup turns a code into a human readable message:

```c
    const char* backtest_strerror(int code);
```

---

```c
Output:
    typedef struct
    {
        double total_return;
        double annualized_return;
        double max_drawdown;
        double sharpe_ratio;
    } BacktestResult;
```

The course's original sketch, which used to be in `native/README.md` and is now
only in the git history, returns `BacktestResult*` from a `malloc` inside the
module. We do not, for two reasons: it leaks unless Java remembers to call a
matching free, and a `static` struct instead of a `malloc` would make concurrent
calls from Spring request threads corrupt each other. Caller allocated out
parameters have neither problem.

```
Compiles as:
    Linux:   libbacktest.so
    Windows: backtest.dll
    MacOS:   libbacktest.dylib
```

```
File structure
/backtest
    /include
        backtest.h
    /src
        main.cpp (for testing)
        backtest.cpp
    CMakeLists.txt      <- links ../risk
```

---

## Where this module sits

Downstream of FX, and it calls into Risk on the way out.

```
holdings + history --> [ FX: convert to base currency ] --> price matrix
                                                                 |
                                                                 v
                                                          [ Backtest ]
                                                                 |
                                                          equity curve
                                                                 |
                                                                 v
                                                      [ Risk: stats stage ]
```

Two practical consequences of that ordering:

- FX has to convert the whole matrix, not one series at a time. 500 instruments
  over 1260 days is 630 000 conversions per run, and paying a JNA crossing for
  each one would cost more than the entire simulation. See `fx_convert_series`
  in the FX Module.

- The rebalancing feature in the app and the `rebalance_days` input here are the
  same idea seen from two ends. The app suggests a rebalance when drift passes a
  threshold; this module can measure whether that threshold would actually have
  helped over five years. Wiring the two together is the most interesting thing
  this module can do, and it is worth building the schedule input with that in
  mind rather than hardcoding "monthly".

Unlike the dashboard, a backtest is not polled. It is an expensive on demand
operation, so the result belongs in a cache keyed on the full input, and the
frontend should treat it as a request that takes a moment rather than something
that refreshes on a timer.

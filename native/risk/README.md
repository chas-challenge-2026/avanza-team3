
# Risk Module

---

### Build requirements
- Language: C
- Build tool: CMake

---

## In a nutshell:
The Risk Module's primary objective is to calculate
three things as efficiently as possible:
- Volatility
- Sharpe Ratio
- Max Drawdown

Each of them is wanted in two shapes: one number for the whole period, and one
number per day over a trailing window. See "Two shapes of output" below.

To do this, we require the following data:
```c
typedef struct
{
    const double* values;     // Closing asset value per day (index [x]-[y], oldest to newest)
    int length;               // Number of days in array, minimum 3 days
    double risk_free_rate;    // E.g 0.02 for 2% yearly interest rate
    int periods_per_year;     // Number of stock market days per year (usually 252)
} RiskInput;
```

---

## Specific needs for each variable:

**Volatility**
- `values`: The values themselves are not the input to the math,
  the daily returns derived from them are.
- `periods_per_year`: Used only to annualize the result.
  Daily volatility on its own needs nothing but the values.
- Does not need the risk free rate.

**Sharpe Ratio**
- `values`: Needed twice over: for the mean return and for the volatility.
- `risk_free_rate`: The return we subtract before dividing.
- `periods_per_year`: Also needed twice: to convert the annual risk free rate
  down to a per day rate, and to annualize the final figure.
- Sharpe is volatility plus two extra ingredients. Everything volatility needs,
  it needs too, so it comes almost for free once volatility is done.

**Max Drawdown**
- `values`. Nothing else.
- It is a pure path measurement over the raw values: no returns,
  no annualization, no rate. Just "how far below the running peak did we fall".

### Why `length` is in the struct but not in the lists above

`length` is not an input to the math. It is an input to the function,
because a C array does not carry its own size. All three metrics need it
for that reason alone.

### Summary

|               | `values` | `risk_free_rate` | `periods_per_year`      |
|---------------|----------|------------------|-------------------------|
| Volatility    | yes      | no               | yes, annualization only |
| Sharpe Ratio  | yes      | yes              | yes                     |
| Max Drawdown  | yes      | no               | no                      |

The rolling variants take one more input, `window`, and EWMA volatility takes
`lambda` instead of a window. Neither changes the table above.

---

## Two shapes of output

The dashboard card needs one number per metric: "volatility 18%". The chart
needs one number per day: what volatility looked like on each day of the last
year. These are different calls with very different costs.

- **Whole period.** One pass over `length` days, three scalars out.
- **Rolling.** A trailing window of `window` days, re-evaluated on every day.
  `length - window` values per metric. With `length = 1260` and `window = 252`
  that is 1008 outputs.

`window` is counted in **returns**, not in prices. A window of 252 covers 252
daily returns, which spans 253 calendar entries in `values`. This is the kind of
off by one that only shows up when a chart is one point short, so it is worth
fixing in one place and testing early.

`docs/v2-targets.md` is specific about which metric needs which shape, and it is
not "all of them rolling":

| Metric       | Shape the spec asks for | Notes |
|--------------|-------------------------|-------|
| Volatility   | Both, plus EWMA         | "Volatilitet (historisk, EWMA)" |
| Sharpe Ratio | Rolling, 252 day window | "rullande 1-årsperiod" |
| Max Drawdown | Whole period only       | Listed without "rullande" |

So `window` has one real default: 252, one trading year. And max drawdown never
needs a window at all, which matters more than it looks (see below).

Rolling is the expensive shape, and it is the actual reason this module is
native. Recomputing the window from scratch at every step is `O(n * w)`.

Five years of daily history is 1260 values, so 1259 returns, so with a 252 day
window there are 1008 windows to evaluate. Recomputing each one from scratch is
1008 x 252, about 254 000 operations per instrument and roughly 127 million
across 500 instruments. Updating the window incrementally instead, adding the
return that enters and removing the one that leaves, is `O(n)`: about 1260
operations per instrument and 630 000 across 500. Identical output, about 200
times less work.

That is an algorithmic win rather than a "C is faster than Java" claim, and it
is worth presenting it that way in the report.

### The catch with incremental variance

Maintaining variance by subtracting the leaving value from a running sum is
numerically fragile. The two quantities involved are large and nearly equal, so
cancellation can produce a tiny negative variance, and `sqrt` then hands back
NaN. Three usable mitigations, pick one before writing any code:

- Keep compensated (Kahan) running sums.
- Clamp a negative variance to zero. Hides the symptom, not the drift.
- Recompute the window from scratch every N steps so error cannot accumulate
  indefinitely. Cheap, and it caps the damage.

### Rolling max drawdown is not in scope, and that is a relief

The spec lists max drawdown without "rullande", so it is whole period only:
one running peak, one pass, nothing to remove. Nearly free.

Recorded here in case anyone proposes adding it later, because it is not the
small extension it looks like. You cannot remove a value from a running
maximum. If the peak is the element that just left the window, a single running
peak cannot tell you what the next highest one was. Doing it properly needs a
monotonic deque, amortized `O(1)` per step. Doing it naively collapses back to
`O(n * w)`. It is by a distance the hardest of the three, so it should be a
deliberate decision and not something that gets waved through as "same as the
others but rolling".

So `risk_rolling` outputs volatility and Sharpe, and nothing else.

### EWMA volatility

`docs/v2-targets.md` asks for EWMA alongside historical volatility. It is a
different weighting, not a different metric. Instead of a hard window where
every day counts equally and then drops off a cliff, the weights decay
exponentially, so recent days matter more and old ones fade out smoothly.

```
    variance_t = lambda * variance_(t-1) + (1 - lambda) * return_t^2
```

It takes one extra input, `lambda`. RiskMetrics uses 0.94 for daily data, which
gives an effective half life of about 11 days. It needs no window, it is
genuinely `O(n)` with one multiply and one add per step, and it has no
cancellation problem because nothing is ever subtracted. It is the cheapest of
the three volatility variants and the quickest to react to a change of regime.

---

## What "korrekt beräkning" refers to

The spec says "Sharpe-kvot (rullande 1-årsperiod, korrekt beräkning)". The last
two words point at a specific v1 bug in `docs/known-bugs.md`, and this module is
the fix for it. Two separate things are wrong in v1:

1. **Volatility is hardcoded to 0.15.** Every Sharpe number in v1 is built on a
   constant that has nothing to do with the instrument. Computing volatility
   from the actual history is the whole reason this module exists.

2. **Sharpe is computed per holding.** The course doc calls this meaningless.
   That overstates it slightly, per asset Sharpe is a real thing people quote,
   but the doc is the spec and the intent is right for this app: what the user
   wants to know is whether *their portfolio* is being paid for the risk it
   carries, and you cannot get that by averaging per holding numbers. Risk does
   not add up that way, because holdings are correlated.

The consequence for this module is an input requirement, not a formula change.
Portfolio level Sharpe needs a portfolio level series: one value per day for the
whole portfolio, time weighted so deposits and withdrawals do not register as
returns. Building that series is Java's job. This module cannot detect that it
was handed a per holding series instead, and will happily return the wrong
answer with full confidence.

---

## Assumptions about the input data

These are things the module takes for granted. It cannot verify most of them,
so they are the Java side's responsibility. If any of them is violated,
the numbers come out wrong rather than failing loudly.

- `values` is the closing price or NAV history
  of a single asset, oldest first, newest last.

- Currency conversion is the FX module's job, and it has to happen before the
  series reaches this module. Not just for tidiness: the volatility of a USD
  asset seen from SEK is not the volatility of the same asset seen from USD. It
  also contains the USD/SEK volatility and the correlation between the two.
  Since the user can switch base currency, the same instrument has a different
  and equally correct volatility in each base currency. There is no way to
  correct for it here after the fact.

- This is a price series, not an account balance.
  If a deposit of 50 000 SEK lands on day 300, the module reads it as a
  +40% daily return and every output becomes meaningless. If the Java side wants
  risk for a whole portfolio, it must send a time weighted (unitised) series,
  not the raw account value.

- An unadjusted series makes a dividend
  payout look like a price drop, which inflates both volatility and max drawdown.

- Weekends and holidays are simply absent
  from the array, which is why `periods_per_year` is 252 and not 365.
  Missing trading days are not detected and not interpolated.

- One array element equals one period. The module never sees
  dates, so it cannot tell a daily series from a weekly one. Whatever spacing
  the data has, `periods_per_year` must match it.

- A zero or negative price makes the return calculation divide by zero.

- The module holds no state and caches nothing. Portfolio values refresh by
  polling, so if Java calls on every poll it recomputes five years of history
  every time. The module is fast enough that this survives, but a cache on the
  Java side is the right answer, and its key has to include the base currency
  and the window, not just the instrument.

## Error handling

C has no exceptions, and every output field is a `double` where every possible
bit pattern is a legitimate value, so there is no sentinel we could return
inside the result. The status therefore comes back as the function's `int`
return value, and the results come back through a caller allocated
out parameter.

```c
Entry points:
    int risk_compute(const double* values,
                     int           length,
                     double        risk_free_rate,
                     int           periods_per_year,
                     RiskResult*   out);

    int risk_rolling(const double* values,
                     int           length,
                     int           window,
                     double        risk_free_rate,
                     int           periods_per_year,
                     double*       out_volatility,   // length - window entries, or NULL
                     double*       out_sharpe);      // length - window entries, or NULL

    int risk_ewma(const double* values,
                  int           length,
                  double        lambda,
                  int           periods_per_year,
                  double*       out_volatility);     // length - 1 entries
```

Java allocates every output array, so there is nothing to free. Passing NULL
for one of `risk_rolling`'s outputs means "do not compute this", which is worth
having because Sharpe costs more than volatility and a chart usually wants one
line and not both.

### Status codes

Three bands, so the caller can branch on the sign alone:

| Value | Name | Meaning |
|---|---|---|
| `0`  | `RISK_OK`                | All outputs valid. |
| `+1` | `RISK_WARN_FLAT_SERIES`  | Volatility is exactly zero, so Sharpe is undefined. `volatility` and `max_drawdown` are valid, `sharpe_ratio` is set to NaN. |
| `-1` | `RISK_ERR_NULL`          | `values` is NULL, or the call's result pointer is NULL. For `risk_rolling` that means both `out_volatility` and `out_sharpe` are NULL, since either one alone is a valid request. |
| `-2` | `RISK_ERR_TOO_SHORT`     | `length < 3`. |
| `-3` | `RISK_ERR_BAD_PRICE`     | Some element is zero, negative, NaN or infinite. |
| `-4` | `RISK_ERR_BAD_PERIODS`   | `periods_per_year < 1`. |
| `-5` | `RISK_ERR_BAD_RATE`      | `risk_free_rate` is NaN, infinite, or `<= -1.0`. |
| `-6` | `RISK_ERR_BAD_WINDOW`    | `window < 2`, or `window >= length`. |
| `-7` | `RISK_ERR_BAD_LAMBDA`    | `lambda` is not strictly between 0 and 1. |

- **Negative means nothing was computed.** `out` is left completely untouched,
  so the caller must not read it.
- **Zero or positive means `out` was written.** A positive code is a warning
  about one specific field, not a failure.
- Validation happens before any arithmetic, so a bad input never produces a
  half filled result.

On the rolling calls `RISK_WARN_FLAT_SERIES` means at least one window was
flat and the matching Sharpe entries are NaN. The rest of the array is valid.

A companion lookup turns a code into a human readable message:

```c
    const char* risk_strerror(int code);
```

---

```c
Output:
    typedef struct
    {
        double volatility;
        double sharpe_ratio;
        double max_drawdown;
    } RiskResult;
```

### One note for the Backtest Module

`risk.h` is included by `backtest.cpp`, which is C++. A C++ compiler mangles
function names unless told not to, so the declarations in `risk.h` have to be
wrapped:

```c
    #ifdef __cplusplus
    extern "C" {
    #endif
        ...declarations...
    #ifdef __cplusplus
    }
    #endif
```

Harmless when compiled as C, required when compiled as C++.

```
Compiles as:
    Linux:   librisk.so
    Windows: risk.dll
    MacOS:   librisk.dylib
```

```
File structure
/risk
    /include
        risk.h
    /src
        main.c (for testing)
        risk.c
    CMakeLists.txt
```

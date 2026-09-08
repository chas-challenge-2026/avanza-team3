
# native/ : the C and C++ modules

This folder holds three modules that the Java backend calls into. This file is
the plain language overview: what they are for, how they fit together, and the
rules they all follow. The README inside each folder is the precise technical
spec for that module.

---

## Vocabulary

**Instrument**: one thing you can own. A share, a fund. "Ericsson B" is an
instrument.

**Price series**: a list of what one instrument was worth, one number per
trading day, oldest first. This is the input to almost everything here.

**Trading day**: markets are closed on weekends and holidays, so a year has
about 252 trading days, not 365. That is why the number 252 appears everywhere.

**Return**: how much something changed in one day, as a fraction. 100 kr going
to 102 kr is a return of `0.02`, which is 2%.

**Volatility**: how much those daily returns bounce around. A savings account
has almost none. A single tech share has a lot. It is normally quoted as a
yearly percentage. High volatility means big swings, in both directions.

**Max drawdown**: the worst fall from a high point down to a later low point.
If something peaked at 150 kr and later fell to 100 kr before recovering, the
max drawdown is 33%. It answers "what is the worst it got while I held this".

**Risk free rate**: what you could earn with no risk at all, roughly a
government bond or a savings account. It is used as a baseline. If it is 2% a
year and your investment also returned 2%, you took on risk for nothing.

**Sharpe ratio**: your return above the risk free rate, divided by your
volatility. In plain terms: *were you paid enough for the bumpiness?* Higher is
better. Around 1.0 is decent, 2.0 is very good, and negative means you would
have done better leaving the money in a savings account.

**Window / rolling**: instead of one volatility number covering all five years,
you calculate it separately for every day using only the previous 252 days. That
gives you a line you can plot instead of a single figure. The "window" is how
many days back you look.

**Portfolio**: everything one user owns, taken together.

**Equity curve**: what a whole portfolio was worth on each day. It is exactly
the same shape as a price series, one number per day, just for a portfolio
instead of a single instrument. This turns out to matter a lot, see below.

**Backtest**: pretend you had followed some strategy five years ago, replay the
real historical prices, and see what would have happened.

**Rebalancing**: say your target is 60% shares and 40% bonds. Shares go up, so
now you are at 70/30. Rebalancing means selling some shares and buying bonds to
get back to 60/40.

**Base currency**: the currency the user wants their totals shown in. Apple is
priced in USD, but a Swedish user wants to see kronor.

**FX**: foreign exchange, meaning currency conversion. An **FX rate** is how
many of one currency you get per unit of another.

---

## The three modules

### risk/ (risk measures)

**In one sentence:** give it a list of daily values, get back volatility, Sharpe
ratio and max drawdown.

It is a pure calculator. It has no memory, no files, no network. Numbers go in,
numbers come out, and the same input always gives the same output. It is by far
the simplest of the three, and the only one that depends on nothing else, which
is why it is being built first.

It does not know what the numbers represent. They could be the price of one
share or the value of an entire portfolio. It just sees an array of doubles that
goes up and down.

### backtest/ (backtesting engine)

**In one sentence:** simulate an investment strategy against five years of real
historical prices and report how it would have gone.

It gets prices for up to 500 instruments across about 1260 trading days, plus a
starting amount and a strategy. It then steps forward one day at a time: buy on
day 1, watch prices move, occasionally rebalance, and keep track of what the
pretend portfolio is worth each day.

### fx/ (currency conversion)

**In one sentence:** what was one unit of currency A worth in currency B on a
given day?

It loads the European Central Bank's published exchange rate history into memory
at startup, and answers lookups against it. It can also fetch today's rate over
HTTP and cache it for a while.

This is the odd one out. The other two are pure calculators, while this one
holds data, talks to the network, and gives different answers at different
times.

---

## How they fit together

### backtest uses risk

This is the important connection, and it is not obvious until you see it.

The backtest has to report four numbers: total return, annualized return, max
drawdown, and Sharpe ratio. The first two come straight out of the simulation.
The other two do not.

But remember what the simulation produces on its way through: one value per day
for the pretend portfolio. That is an array of doubles, one per day. Which is
*exactly* what the risk module takes as input.

```
day 1: 100 000 kr
day 2: 100 340 kr
day 3:  99 810 kr
...
```

So the backtest does not need its own max drawdown or Sharpe code. It calls the
risk module on its own output:

```c
risk_compute(equity_curve, days, risk_free_rate, 252, &result);
```

The risk module has no idea that anything changed. It is the same reason you
would write one `average()` function and use it for both test scores and
temperatures rather than writing two.

**Consequence:** the backtest module is smaller than it looks. It is
"simulation" plus "risk module". Only the simulation half is new work.

### fx comes before both

Both risk and backtest refuse to deal with currency. They assume every number
they receive is already in one single currency. FX is the module that makes that
true.

This is not just tidiness. If a user holds Apple and views their portfolio in
kronor, the value they see moves for two reasons: the Apple share price moved,
and the USD/SEK rate moved. So the volatility of that holding measured in kronor
is genuinely different from its volatility measured in dollars. Both numbers are
correct, they just answer different questions. Since the user can switch base
currency in the app, every number downstream has to be recalculated when they
do.

### The whole picture

```
   raw prices + holdings
            |
            v
        [  fx  ]   convert everything into the user's base currency
            |
            +---------------------------+
            |                           |
            v                           v
       [  risk  ]                 [ backtest ]   simulate a strategy
   volatility, Sharpe,                  |
      max drawdown                 equity curve
                                        |
                                        v
                                   [  risk  ]   same module, second use
```

**Build order.** There are only two hard constraints, so several orders work:

1. **risk before backtest**, because backtest calls into it for two of its four
   outputs.
2. **fx before any realistic end to end test**, because until it exists nothing
   downstream can be fed correctly converted data. A stub that returns a rate of
   1.0 for every pair is enough to unblock the other two, so this constraint
   only bites at integration time, not while writing code.

`plan.md` orders the work risk, then backtest, then fx. That satisfies both
constraints as long as the fx stub exists early.

---

## Why C and C++ at all

Partly because the assignment says so. But there is one place where it genuinely
pays off, and it is worth knowing which argument to make.

**The good argument: rolling calculations.** To draw a volatility chart you need
a volatility number for every day, each one based on the previous 252 days. Five
years of history gives about 1008 of those windows. Done the obvious way, each
one means adding up 252 numbers, so that is about 254 000 operations per
instrument and roughly 127 million across 500 instruments.

Done incrementally, you keep a running total and on each new day you add the one
day entering the window and subtract the one leaving it. That is about 1260
operations per instrument, roughly 630 000 across 500. Same answer, about 200
times less work.

**The other good argument: doing bulk work in one call.** Converting 500
instruments × 1260 days into another currency is 630 000 conversions. If Java
called across into C once per conversion, the crossings alone would cost more
than the work. One call with the whole array is the point.

**Arguments not to lean on:** "C is faster than Java" is too vague to defend,
and the "garbage collector pressure" line in the course docs is weak, because a
Java `double[]` holds plain numbers and barely produces any garbage. Lead with
the algorithm.

---

## How Java calls into C

Your C code is compiled into a **shared library**: a single file full of
compiled functions. `risk.dll` on Windows, `librisk.so` on Linux.

**JNA** is a Java library that loads that file while the program is running and
lets Java call the functions inside it, without you writing any glue code in C.
Java says "load the library called risk, and I want to call `risk_compute`", and
then calls it like a normal Java method.

A few rules follow directly from how that works:

- **Functions must be plain C.** JNA finds them by their exact name. C++
  compilers rename functions behind your back (it is called name mangling), so
  anything C++ that Java needs to call has to be wrapped in `extern "C"` to stop
  that.

- **Arrays pass across cheaply.** A Java `double[]` maps onto a C `const
  double*` with no work on your part. This is the thing JNA does really well.

- **Java allocates, C fills in.** Never call `malloc` in C and hand the pointer
  back to Java, because nothing will ever free it and you have a memory leak.
  Java creates the space, passes a pointer, and C writes into it.

- **The return value is a status code, not a result.** See below.

**A practical gotcha:** you develop on Windows, but the app runs in Docker,
which is Linux. A `.dll` built on your machine is useless in the container. The
`.so` has to be built inside the Docker image, so the Dockerfile needs a build
step for it. Sort this out early, it is the kind of thing that eats a whole
afternoon the day before a deadline.

---

## Rules all three modules follow

Being consistent across the three is worth more than any individual choice here.

**1. Java owns all memory.** Every module writes its results into space the
caller provided. No module allocates anything it hands back.

**2. Every function that computes something returns an `int` status.** The
results come out through pointer arguments. This is because every result field
is a `double`, and every possible `double` is a legitimate answer, so there is
no special value that could mean "this failed".

The two exceptions are the ones that cannot fail in a way worth reporting:
`*_strerror` returns a string, and `fx_shutdown` returns nothing.

The `int` has three bands, so you can check the sign alone:

| | meaning |
|---|---|
| `0` | worked, everything valid |
| positive | worked, but with a caveat. Read the code to find out which |
| negative | failed, nothing was written, do not read the output |

**3. Every module has a `*_strerror(int)`** that turns a status code into a
readable message. It returns a fixed string, so there is nothing to free.

**4. No dates inside C.** The C modules never see calendars. They work with
array positions ("day 300"), and Java translates between dates and positions.
The one exception is fx, which needs dates to look rates up, and takes them as a
plain integer like `20260819` rather than a date type.

**5. One call per batch, not per item.** Every crossing from Java into C has a
cost. Pass the whole array once.

---

## Building

Each module has its own CMake project.

```bash
cd native/risk
cmake -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build
```

| Module   | Language | Output on Linux  | Output on Windows | Needs |
|----------|----------|------------------|-------------------|-------|
| risk     | C        | `librisk.so`     | `risk.dll`        | nothing |
| backtest | C++17    | `libbacktest.so` | `backtest.dll`    | links risk |
| fx       | C++17    | `libfx.so`       | `fx.dll`          | libcurl |

Two of those are not standalone. **backtest links against risk**, because it
uses it for two of its four outputs, so risk has to build first and `risk.h`
needs `extern "C"` guards so a C++ file can include it. **fx needs libcurl**,
which has to be installed in the Docker image and not only on the dev machine.

---

## A known error in the course documents

`docs/v2-targets.md` names the Java bridge class
`se.comerit.avanza.native.BacktestBridge`. **That package name does not
compile.** `native` is a reserved keyword in Java, and package names cannot
contain keywords, so `package se.comerit.avanza.native;` is a syntax error.

Use something else: `nativelib`, `nativebridge` or `jna` all work. Worth
agreeing on it with the Java side before anyone creates the folder.

---

## Where to read more

| File | What is in it |
|---|---|
| `risk/README.md`     | Exact inputs, formulas, rolling variants, status codes |
| `backtest/README.md` | Price matrix layout, strategies, simulation assumptions |
| `fx/README.md`       | ECB data quirks, cross rates, caching, thread safety |
| `../docs/v2-targets.md` | The course's own spec for all three |
| `../docs/known-bugs.md` | The v1 bugs these modules are meant to fix |

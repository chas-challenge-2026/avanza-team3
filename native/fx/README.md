
# FX Module

---

### Build requirements
- Language: C++17
- Build tool: CMake
- External: libcurl for the live fetch. Needs to be in the Docker image too,
  not just on the dev machine.

Everything exposed to Java must be wrapped in `extern "C"` so the C++ compiler
does not mangle the names. JNA looks functions up by their plain C name.

---

## In a nutshell:
The FX Module's primary objective is to answer one question as cheaply as
possible:

> what was one unit of currency A worth in currency B on a given day?

Everything it does is a variation on that:
- Historical rate for a specific date
- Latest available rate
- Converting an amount rather than just returning the rate
- Converting a whole time series in one call, each day at its own rate

To do this, we require the following data:
```c
typedef struct
{
    const char* from;   // ISO 4217 code, e.g. "USD"
    const char* to;     // ISO 4217 code, e.g. "SEK"
    int date;           // Trade date as YYYYMMDD, or 0 for the latest available rate
} FxQuery;
```

`FxQuery` documents what a lookup needs. It is **not** what gets passed across
the boundary: the entry points take these as flat arguments. See "Error
handling".

`date` is a plain `int` in `YYYYMMDD` form on purpose. `time_t` and `struct tm`
are both platform dependent widths and a nuisance to map through JNA, and
`20260819` is readable in a debugger.

---

## This module is the odd one out

Risk and Backtest are pure functions. Same input, same output, no state, safe
to call from any thread without thinking about it.

FX is none of those things. It holds a rate table, it holds a cache, it talks
to the network, and the same query returns different answers depending on when
you ask. That changes the design in three concrete ways, all covered below:
it has a lifecycle, it needs locking, and it has to report *which* date the
answer actually came from.

---

## Specific needs for each variable:

**Latest rate**
- `from` and `to`.
- The loaded rate table, and a live fetch on top of it if the cached live rate
  has expired. The table is always required, because a live fetch can fail and
  the newest table row is the fallback.
- No date, because "latest" means whatever the newest available row is.
- Requested by passing `date = 0`.

**Historical rate**
- `from` and `to`.
- `date`.
- A rate table that covers that date. This is the only operation that cannot
  be answered by a live fetch, because the live endpoint only knows today.
- A fallback rule, because the requested date very often has no published rate.
  See below.

**Converted amount**
- Everything the historical rate needs.
- The amount to convert.
- It is the rate operation times a scalar. There is no separate maths here, it
  exists purely so that Java does not have to do the multiplication in a loop
  over thousands of holdings and pay a JNA crossing for each one.

**Converted series**
- `from` and `to`.
- A value array and a matching date array, same length.
- The rate table, and the fallback rule, applied per element. Day 300 is
  converted at day 300's rate, not at today's rate.
- This is the bulk operation, and it is a different thing from calling the
  single conversion in a loop. See below.

### Summary

|                  | `from` / `to` | `date` | rate table | network |
|------------------|---------------|--------|------------|---------|
| Latest rate      | yes           | `0`    | yes        | yes, on cache miss |
| Historical rate  | yes           | yes    | yes        | no      |
| Converted amount | yes           | yes    | yes        | no      |
| Converted series | yes           | one per element | yes | no |

---

## The bulk conversion is the reason this module is native

The assignment calls for FX adjustment across large time series, and that is a
much stronger argument for native code than the live fetch is.

A backtest over 500 instruments and five years of daily history needs every
price converted into the user's base currency at that day's rate. That is
500 x 1260, about 630 000 conversions, and it happens again whenever the user
switches base currency. Done one call at a time across JNA, the boundary
crossings alone cost more than the entire backtest simulation. Done as one call
with two arrays in and one array out, it is a single crossing and a tight loop
over memory that is already resident.

There is a second win inside the loop. A time series is in date order, so the
lookup into the rate table walks forward monotonically. Instead of a binary
search per element, keep a cursor into the table and advance it. That turns
`O(n log m)` into `O(n + m)` and touches memory in order while doing it.

By contrast, the live rate fetch is the weakest part of the native case. HTTP
latency is measured in milliseconds and JVM overhead in microseconds, so
nothing is gained by doing the request in C++, and TLS, timeouts and retries are
all harder there. If time gets short, that is the piece to hand back to Java.
The historical table and the bulk conversion are the parts worth defending.

## Base currency is a runtime parameter

The user can switch base currency in the UI, so this is not a fixed SEK setting
that can be baked in anywhere. Consequences that reach past this module:

- Any pair may be requested, in either direction. There is no useful pair to
  precompute.
- Switching base currency invalidates every downstream number, not just the
  displayed totals. Volatility, Sharpe, max drawdown and every backtest output
  all change, because a USD instrument valued in SEK also carries the USD/SEK
  movement. Every cache key in the system needs the base currency in it.
- The dashboard polls, so a base currency switch turns into a burst of repeated
  queries. This is exactly what the cache TTL exists for, and it is why a poll
  must never trigger a live fetch per holding.

---

## ECB rates are EUR based, so most pairs are cross rates

This is the single most important fact about the data source. The ECB does not
publish USD to SEK. It publishes one row per day, every currency quoted against
the euro. So the pair we actually care about has to be computed:

```
USD -> SEK  =  (EUR -> SEK) / (EUR -> USD)
```

Consequences worth knowing before writing anything:

- Every non EUR pair costs two lookups, not one.
- Every non EUR pair carries the rounding error of two published figures rather
  than one. The ECB publishes five significant digits, so a cross rate is good
  to roughly four. Do not present portfolio values as exact to the öre and
  expect them to reconcile against a broker statement.
- If either leg is missing for a date, the cross rate is missing, even when the
  other leg is present.
- EUR to anything, and anything to EUR, is a single direct lookup. Same
  currency both sides returns exactly `1.0` without touching the table at all.

## The ECB does not publish every day

Rates land once per day, around 16:00 CET, on TARGET business days only. No
weekends, no TARGET holidays. A lookup for a Saturday has no row.

The rule: **fall back to the most recent published date at or before the
requested one**, and tell the caller which date was actually used. That is what
`rate_date` in the result is for. A portfolio value dated Sunday that was
computed from Friday's rate is correct behaviour, but it has to be visible,
because "why does my Sunday total differ from my Monday total" is a support
question someone will eventually ask.

The fallback is bounded. If nothing is found within a set window (7 calendar
days is a sensible default) the query fails rather than reaching months back.
An unbounded search turns a typo in a date into a plausible looking wrong
number, which is worse than an error.

---

## Lifecycle

Unlike the other two modules, this one has to be set up and torn down.

```c
    int  fx_init(const char* history_csv_path, int cache_ttl_seconds);
    void fx_shutdown(void);
```

`fx_init` loads the ECB historical series into memory once, at application
startup, and is where the file parsing lives. Every query after that is an
in memory lookup. Calling a query function before `fx_init` is an error, not
undefined behaviour.

Sizing: the full ECB history is roughly 7000 business days by about 30
currencies. Around 1.7 MB as doubles. It fits in cache comfortably, and a
binary search by date over a sorted array answers any historical query in a
few hundred nanoseconds.

## Thread safety

The rate table is read only after `fx_init` returns, so historical lookups are
safe to run concurrently with no locking at all.

The live rate cache is not. It is mutable shared state, it will be hit by many
Spring request threads at once, and it needs a mutex around both the read and
the refresh. This is the one place in the whole native codebase where a data
race is possible, so it is worth keeping the locked region small and obvious:
lock, check freshness, copy the value out, unlock. Never hold the lock across
the HTTP call.

---

## Assumptions about the input data

- Currency codes are uppercase ISO 4217, exactly three characters. `"usd"` is
  not accepted. Java normalises before calling.

- `date` is a trade date in CET, which is what the ECB publishes against. It is
  not the user's local date. Around midnight these differ and the answer will
  be off by one publication day.

- The ECB series starts 1999-01-04, and individual currencies start later than
  that. A date before a currency's first row is a hard error, not a fallback.

- ECB reference rates are not tradeable rates. There is a real bid and ask
  spread, and a broker adds a currency surcharge on top. These figures are
  correct for valuing a portfolio and wrong for anything that models an actual
  transaction. If the Backtest Module ever converts currencies inside a
  simulation, it is understating the cost.

- A live fetch is subject to the network and can be slow or fail. Any code path
  that can reach the network needs a timeout, and the caller has to be able to
  tell a fresh rate from a stale one, which is what the status codes below do.

---

## Error handling

Same contract as the other two modules: status as the `int` return value,
results through a caller allocated out parameter.

```c
Entry point:
    int fx_rate(const char* from,
                const char* to,
                int         date,
                FxResult*   out);

    int fx_convert(const char* from,
                   const char* to,
                   int         date,
                   double      amount,
                   double*     out_amount);

    int fx_convert_series(const char*   from,
                          const char*   to,
                          const double* values,     // length entries
                          const int*    dates,      // length entries, YYYYMMDD, ascending
                          int           length,
                          double*       out_values); // length entries, caller allocated
```

Flat arguments rather than a struct pointer here, because there are only four to
six of them. The Backtest Module takes a struct pointer because `BacktestInput`
has nine fields. Neither passes a struct by value, which is the case JNA handles
worst.

`fx_convert_series` is the one that matters for performance. It converts an
entire price history in a single crossing, each element at its own date's rate.
`values` and `out_values` may point at the same array if the caller wants the
conversion done in place.

### Status codes

Three bands, so the caller can branch on the sign alone:

| Value | Name | Meaning |
|---|---|---|
| `0`  | `FX_OK`                  | Exact rate for the requested date. |
| `+1` | `FX_WARN_STALE_DATE`     | No rate published on the requested date, so an earlier one was used. `rate_date` says which. Not an error, this is the normal weekend case. |
| `+2` | `FX_WARN_STALE_CACHE`    | Live fetch failed, an expired cached rate was served instead. The number is usable but old. |
| `-1` | `FX_ERR_NULL`            | `from` or `to` is NULL, or any pointer argument of the call being made is NULL. For `fx_convert_series` that includes `values`, `dates` and `out_values`. |
| `-2` | `FX_ERR_NOT_INITIALIZED` | `fx_init` has not been called, or it failed. |
| `-3` | `FX_ERR_UNKNOWN_CURRENCY`| A code is malformed or not present in the series. |
| `-4` | `FX_ERR_BAD_DATE`        | Not a valid `YYYYMMDD` value, or in the future. `0` is exempt: it is the documented sentinel for "latest available". In `fx_convert_series`, where every element needs a real date, `0` is rejected. |
| `-5` | `FX_ERR_NO_DATA`         | Date is before the series starts, or nothing was found inside the fallback window. |
| `-6` | `FX_ERR_FETCH_FAILED`    | Live fetch failed and there was no cached value to fall back to at all. |

- **Negative means nothing was computed.** `out` is left completely untouched,
  so the caller must not read it.
- **Zero or positive means `out` was written.** A positive code is a caveat
  about the freshness of the number, not a failure. Ignoring the difference is
  how you end up showing a two week old rate as if it were live.
- `fx_convert_series` returns the *worst* status across all elements, so a
  single weekend date in a five year series makes the whole call return
  `FX_WARN_STALE_DATE`. That is almost always true and therefore not very
  informative. It is deliberate: a per element status array would double the
  output size to report something the caller cannot act on element by element
  anyway. A hard error still aborts before anything is written.

A companion lookup turns a code into a human readable message:

```c
    const char* fx_strerror(int code);
```

---

```c
Output:
    typedef struct
    {
        double rate;        // Multiply an amount in `from` by this to get `to`
        int rate_date;      // The date the rate is actually from (YYYYMMDD)
        int source;         // 0 = historical table, 1 = live fetch, 2 = cache
    } FxResult;
```

`rate_date` is the field that makes the fallback rule auditable. When it does
not match the requested date, the caller knows why the number looks the way it
does without having to guess.

```
Compiles as:
    Linux:   libfx.so
    Windows: fx.dll
    MacOS:   libfx.dylib
```

```
File structure
/fx
    /include
        fx.h
    /src
        main.cpp (for testing)
        fx.cpp
    CMakeLists.txt
```

---

## Where this module sits

FX is upstream of the other two. Both Risk and Backtest assume their input is
already in one currency and refuse to deal with conversion, and this is the
module that makes that assumption true. It also replaces the hardcoded
`USD_TO_SEK = 10.45` in v1.

The course documents disagree about how widespread that hardcode is. The root
`README.md` says the FX rate is hardcoded in three places, while
`docs/known-bugs.md` names only `DashboardController.java` and puts "three
places" on a different bug, the hardcoded share prices. Worth grepping for it
rather than trusting either number.

```
holdings + history --> [ FX ] --> series in base currency --> [ Risk ]
                                                          \-> [ Backtest ] --> [ Risk ]
```

Because the user can switch base currency, that arrow gets walked again on every
switch, for every instrument. `fx_convert_series` is what makes that affordable.
It also means FX should be built, or at least stubbed with a fixed rate table,
before the other two can be tested against anything realistic. A stub that
returns 1.0 for every pair is enough to unblock them.

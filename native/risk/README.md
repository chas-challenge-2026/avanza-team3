
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


---

## Assumptions about the input data

These are things the module takes for granted. It cannot verify most of them,
so they are the Java side's responsibility. If any of them is violated,
the numbers come out wrong rather than failing loudly.

- `values` is the closing price or NAV history
  of a single asset, oldest first, newest last.
  
- Currency conversion is the FX module's job.
  A series that silently switches currency will show a fake jump in volatility.

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

## Error handling

C has no exceptions, and every output field is a `double` where every possible
bit pattern is a legitimate value, so there is no sentinel we could return
inside the result. The status therefore comes back as the function's `int`
return value, and the results come back through a caller allocated
out parameter.

```c
Entry point:
    int risk_compute(const double* values,
                     int           length,
                     double        risk_free_rate,
                     int           periods_per_year,
                     RiskResult*   out);
```

### Status codes

Three bands, so the caller can branch on the sign alone:

| Value | Name | Meaning |
|---|---|---|
| `0`  | `RISK_OK`                | All three outputs valid. |
| `+1` | `RISK_WARN_FLAT_SERIES`  | Volatility is exactly zero, so Sharpe is undefined. `volatility` and `max_drawdown` are valid, `sharpe_ratio` is set to NaN. |
| `-1` | `RISK_ERR_NULL`          | `values` or `out` is NULL. |
| `-2` | `RISK_ERR_TOO_SHORT`     | `length < 3`. |
| `-3` | `RISK_ERR_BAD_PRICE`     | Some element is zero, negative, NaN or infinite. |
| `-4` | `RISK_ERR_BAD_PERIODS`   | `periods_per_year < 1`. |
| `-5` | `RISK_ERR_BAD_RATE`      | `risk_free_rate` is NaN, infinite, or `<= -1.0`. |

- **Negative means nothing was computed.** `out` is left completely untouched,
  so the caller must not read it.
- **Zero or positive means `out` was written.** A positive code is a warning
  about one specific field, not a failure.
- Validation happens before any arithmetic, so a bad input never produces a
  half filled result.

A companion lookup turns a code into a human readable message:

```c
    const char* risk_strerror(int code);
```

---


```c
Input:
    typedef struct
    {
        const double* values;     // Array of closing asset value (index [x]-[y], oldest to newest)
        int length;               // Number of days in array, minimum 3 days
        double risk_free_rate;    // E.g 0.02 for 2% yearly interest rate
        int periods_per_year;     // Usually 252 stock market days per year
     } RiskInput;
```

*magic calculations*

```c
Output:
    typedef struct
    {
        double volatility;
        double sharpe_ratio;
        double max_drawdown;
    } RiskResult;
```

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


# Risk Module

---

### Build requirements
- Language: C
- Build tool: CMake

```
cd native/risk
cmake -S . -B build -G Ninja
cmake --build build
ctest --test-dir build -V
```

---

## In a nutshell:
The Risk Module's primary objective is to calculate
three things as efficiently as possible:
- Volatility
- Sharpe Ratio
- Max Drawdown

To do this, we require the following data:
```c
    const double *values;     // Closing asset value per day (index [x]-[y], oldest to newest)
    int length;               // Number of days in array, minimum 3 days
    double risk_free_rate;    // E.g 0.02 for 2% yearly interest rate
    int periods_per_year;     // Number of stock market days per year (usually 252)
    RiskResult *out;          // Struct where the results will be stored
```

RiskResult looks like this, and needs to be matched in Java:
```c
    typedef struct
    {
        double volatility;
        double sharpe_ratio;
        double max_drawdown;
    } RiskResult;
```

The function used by Java looks like this:
```c
    int risk_compute(const double *values, int length, double risk_free_rate, int periods_per_year, RiskResult *out);
```

---

### Status codes
One of the following values will be returned by risk_compute(). 
Here is what each value means:

| Value | Name                       | Meaning                                           |
|-------|----------------------------|---------------------------------------------------|
| `0`   | `RISK_OK`                  | All outputs valid.                                |
| `+1`  | `RISK_ERROR_NULL`          | `values` or `out` is NULL                         |
| `+2`  | `RISK_ERROR_TOO_SHORT`     | `length < 3`                                      |
| `+3`  | `RISK_ERROR_BAD_RATE`      | `risk_free_rate` is NaN, infinite, or `<= -1.0`.  |
| `+4`  | `RISK_ERROR_BAD_PERIODS`   | `periods_per_year < 1`                            |
| `+5`  | `RISK_ERROR_INVALID_VALUE` | `values[i]` is NaN, infinite or `<= 0.0`          |

---

## Specific needs for each variable:

**Volatility**
- `values`
- `periods_per_year`

**Sharpe Ratio**
- `values`
- `risk_free_rate`
- `periods_per_year`

**Max Drawdown**
- `values`

---

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

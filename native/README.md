# Native modules

C and C++ libraries that the Java backend loads through JNA. Each module builds to a shared library (`fx.dll` / `libfx.so`, `risk.dll` / `librisk.so`). Full API and error codes are in each module's own README.

### For more technichal details
| Module | Language     | README                           |
|--------|--------------|----------------------------------|
| Fx     | C++          | [fx/README.md](fx/README.md)     |
| Risk   | C            | [risk/README.md](risk/README.md) |

## FX (`fx/`, C++17)

Converts an amount from one currency to another.

```c
int FxConvert(double amount, const char *from, const char *to, double *outResult);
```

- Fetches the latest rates over HTTPS from `api.frankfurter.dev/v2/rates` with libcurl.
- Caches the rates for 10 minutes. If a refresh fails, the old rates are used for up to 7 days, after that it returns `FX_ERROR_API_UNRESPONSIVE`.
- Currency codes are three letters, case-insensitive.
- Thread-safe: the cache is guarded by a mutex.

## Risk (`risk/`, C17)

Computes three risk figures from a series of closing prices (oldest first, at least 3 values).

```c
int risk_compute(const double *values, int length, double risk_free_rate, int periods_per_year, RiskResult *out);
```

- **Volatility**: how much the returns swing, annualized.
- **Sharpe ratio**: return above the risk-free rate per unit of volatility, annualized. `NaN` if the series is flat.
- **Max drawdown**: largest fall from a previous peak, as a fraction (`0.5` = 50%).

No external dependencies.

## libs

All three are kept in the repo and built with `add_subdirectory`, so the modules build with CMake alone.

| Library    | Version | Used by    | Why |
|------------|---------|------------|-----|
| curl       | 8.22.0  | fx         | The module must fetch rates itself, and the API is HTTPS. On Windows curl uses Schannel (built-in Windows TLS), so OpenSSL is not needed. |
| googletest | 1.18.0  | fx tests   | Test framework for C++. |
| unity      | -       | risk tests | Small test framework for C (three files). |

## Tests

```
cmake -S . -B build -G Ninja
cmake --build build
ctest --test-dir build -V
```

Run from `native/fx` or `native/risk`.

| Suite       | Framework  | Tests | Status |
|-------------|------------|-------|--------|
| `risk_test` | Unity      | 25    | 25/25 pass (2026-11-04) |
| `fx_test`   | GoogleTest | 4     | Not verified, needs network |

`fx_test` calls the live API. `HandlesValidInput`, `HandlesSameCurrencies` and the `xxx` cases in `HandlesInvalidCurrency` fail without network access. 


### Risk Level Module

Language: C
Build: CMake

Input:
    typedef struct
    {
      const double* values;     // Array of closing asset value (index [x]-[y], oldest to newest)
      int length;               // Number of days in array, minimum 2 days
      double risk_free_rate;    // E.g 0.02 for 2% yearly interest rate
      int periods_per_year;     // E.g 252 stock market days per year
     } RiskInput;

    RiskResult calculate_risk
    (
      const double* values,
      int length,
      double risk_free_rate,
      int periods_per_year
    );

*magic calculations*

Output:
    typedef struct
    {
      double volatility;
      double sharpe_ratio;
      double max_drawdown;
    } RiskResult;

    Linux:   librisk.so
    Windows: risk.dll
    MacOS:   librisk.dylib

/risk
    /include
    /src
        main.c
    CMakeLists.txt
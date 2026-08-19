#ifndef RISK_H
#define RISK_H

#define MIN_NUMBER_OF_DAYS = 3

typedef struct
{
    const double* values;   // Array of closing asset value (index [x]-[y], oldest to newest)
    int length;             // Number of days in array, minimum 3 days
    double risk_free_rate;  // E.g 0.02 for 2% yearly interest rate
    int periods_per_year;   // Stock market days per year (usually 252)
} RiskInput;

typedef struct
{
    double volatility;
    double sharpe_ratio;
    double max_drawdown;
} RiskResult;

#endif // RISK_H
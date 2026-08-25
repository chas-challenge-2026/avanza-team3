#ifndef RISK_H
#define RISK_H

#define MIN_NUMBER_OF_ELEMENTS 3

enum
{
    RISK_OK = 0,
    RISK_ERR_NULL,
    RISK_ERR_TOO_SHORT,
    RISK_ERR_BAD_RATE,
    RISK_ERR_BAD_PERIODS,
    RISK_ERR_INVALID_VALUE
};

typedef struct
{
    double volatility;
    double sharpe_ratio;
    double max_drawdown;
} RiskResult;

/** 
 * @brief Calculates volatility, sharpe ratio and max drawdown
 *
 * @param[in]  values Array of closing asset value (index [x]-[y], oldest to newest)
 * @param[in]  length Number of elements in "values"
 * @param[in]  risk_free_rate E.g 0.02 for 2% yearly interest rate
 * @param[in]  periods_per_year Stock market days per year (usually 252)
 * @param[out] out Struct with three doubles: volatility, sharpe ratio and max drawdown
 * 
 * @return Error code
 */
int risk_compute(const double *values, int length, double risk_free_rate, int periods_per_year, RiskResult *out);

#endif // RISK_H
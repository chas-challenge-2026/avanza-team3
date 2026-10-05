#ifndef RISK_H
#define RISK_H

#define MIN_NUMBER_OF_ELEMENTS 3

enum
{
    RISK_OK = 0,
    RISK_ERROR_NULL,
    RISK_ERROR_TOO_SHORT,
    RISK_ERROR_BAD_RATE,
    RISK_ERROR_BAD_PERIODS,
    RISK_ERROR_INVALID_VALUE,
    RISK_ERROR_BAD_WINDOW,
    RISK_ERROR_BAD_LAMBDA,
    RISK_ERROR_SMALL_BUFFER
};

typedef struct
{
    double volatility;
    double sharpe_ratio;
    double max_drawdown;
} RiskResult;

#ifdef __cplusplus
extern "C"
{
#endif

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

/**
 * @brief Annualized EWMA volatility, one value per return
 *
 * out[i] is the volatility after return i, so the last element is the current volatility.
 * The first return seeds the variance.
 *
 * @param[in]  values Closing values, oldest to newest
 * @param[in]  length Number of elements in "values"
 * @param[in]  lambda Decay between 0 and 1 (exclusive). RiskMetrics daily default is 0.94
 * @param[in]  periods_per_year Usually 252
 * @param[out] out Caller allocated array, needs room for length - 1 doubles
 * @param[in]  out_capacity Number of doubles that fit in "out"
 *
 * @return Error code
 */
int risk_ewma_vol(const double *values, int length, double lambda, int periods_per_year, double *out, int out_capacity);

/**
 * @brief Number of results risk_rolling() produces, or 0 if the arguments are invalid
 *
 * @param[in] length Number of elements in "values"
 * @param[in] window Number of returns per window
 */
int risk_rolling_count(int length, int window);

/**
 * @brief Volatility, sharpe ratio and max drawdown over a sliding window
 *
 * A window of "window" returns uses window + 1 values. Result k covers values[k] to values[k + window],
 * so the output has length - window entries and starts at the first full window.
 * Max drawdown is measured inside each window only.
 *
 * @param[in]  values Closing values, oldest to newest
 * @param[in]  length Number of elements in "values"
 * @param[in]  window Returns per window, at least 2 (usually 252)
 * @param[in]  risk_free_rate E.g 0.02 for 2% yearly interest rate
 * @param[in]  periods_per_year Usually 252
 * @param[out] vol_out Caller allocated, room for risk_rolling_count() doubles
 * @param[out] sharpe_out Same size as vol_out
 * @param[out] mdd_out Same size as vol_out
 * @param[in]  out_capacity Number of doubles that fit in each output array
 *
 * @return Error code
 */
int risk_rolling(const double *values, int length, int window, double risk_free_rate, int periods_per_year,
                 double *vol_out, double *sharpe_out, double *mdd_out, int out_capacity);

#ifdef __cplusplus
}
#endif

#endif // RISK_H

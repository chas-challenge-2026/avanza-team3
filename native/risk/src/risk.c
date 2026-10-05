#include <stddef.h>
#include <math.h>
#include "risk.h"

// Below this a series counts as constant. Rounding noise in a perfectly smooth
// series is around 1e-17, so exact comparison with 0.0 is not enough.
#define MIN_STD 1e-12

static int validate(const double *values, int length, double risk_free_rate, int periods_per_year, const void *out)
{
    if (values == NULL || out == NULL)      return RISK_ERROR_NULL;
    if (length < MIN_NUMBER_OF_ELEMENTS)    return RISK_ERROR_TOO_SHORT;
    if (periods_per_year < 1 ||
        periods_per_year >= 366)            return RISK_ERROR_BAD_PERIODS;
    if (!isfinite(risk_free_rate) ||
        risk_free_rate <= -1.0)             return RISK_ERROR_BAD_RATE;
    for (int i = 0; i < length; i++)
    {
        if (!isfinite(values[i]) ||
            values[i] <= 0.0)               return RISK_ERROR_INVALID_VALUE;
    }

    return RISK_OK;
}

static double period_return(const double *values, int i)
{
    return (values[i + 1] - values[i]) / values[i];
}

// Volatility, sharpe and max drawdown for count values starting at values[0].
// Input must already be validated, count >= 3.
static void compute_stats(const double *values, int count, double rf_per_period, double ppy, RiskResult *out)
{
    int return_count = count - 1;

    // Calculate the average return
    double sum = 0.0;
    for (int i = 0; i < return_count; i++)
    {
        sum += period_return(values, i);
    }
    double mean = sum / return_count;

    // Calculate volatility
    double sq = 0.0;
    for (int i = 0; i < return_count; i++)
    {
        double diff = period_return(values, i) - mean;
        sq += diff * diff;
    }
    double sd_daily = sqrt(sq / (return_count - 1));

    out->volatility = sd_daily * sqrt(ppy);

    // Calculate sharpe-ratio
    if (sd_daily < MIN_STD)
    {
        out->sharpe_ratio = NAN;
    }
    else
    {
        out->sharpe_ratio = (mean - rf_per_period) / sd_daily * sqrt(ppy);
    }

    // Calculate max drawdown
    double peak = values[0];
    double worst = 0.0;
    for (int i = 1; i < count; i++)
    {
        if (values[i] > peak)
        {
            peak = values[i];
        }
        else
        {
            double drop = (peak - values[i]) / peak;
            if (drop > worst)
                worst = drop;
        }
    }
    out->max_drawdown = worst;
}

int risk_compute(const double *values, int length, double risk_free_rate, int periods_per_year, RiskResult *out)
{
    int valid_res = validate(values, length, risk_free_rate, periods_per_year, out);
    if (valid_res != RISK_OK)
    {
        return valid_res;
    }

    double ppy = (double)periods_per_year;
    double rf_daily = pow(1.0 + risk_free_rate, 1.0 / ppy) - 1.0;
    compute_stats(values, length, rf_daily, ppy, out);

    return RISK_OK;
}

int risk_ewma_vol(const double *values, int length, double lambda, int periods_per_year, double *out, int out_capacity)
{
    int valid_res = validate(values, length, 0.0, periods_per_year, out);
    if (valid_res != RISK_OK)
    {
        return valid_res;
    }
    if (!(lambda > 0.0 && lambda < 1.0))    return RISK_ERROR_BAD_LAMBDA;

    int return_count = length - 1;
    if (out_capacity < return_count)        return RISK_ERROR_SMALL_BUFFER;

    double annualize = sqrt((double)periods_per_year);
    double variance = 0.0;
    for (int i = 0; i < return_count; i++)
    {
        double r = period_return(values, i);
        if (i == 0)
            variance = r * r;
        else
            variance = lambda * variance + (1.0 - lambda) * r * r;

        out[i] = sqrt(variance) * annualize;
    }

    return RISK_OK;
}

int risk_rolling_count(int length, int window)
{
    if (window < 2 || window > length - 1)
    {
        return 0;
    }
    return length - window;
}

int risk_rolling(const double *values, int length, int window, double risk_free_rate, int periods_per_year,
                 double *vol_out, double *sharpe_out, double *mdd_out, int out_capacity)
{
    int valid_res = validate(values, length, risk_free_rate, periods_per_year, vol_out);
    if (valid_res != RISK_OK)
    {
        return valid_res;
    }
    if (sharpe_out == NULL || mdd_out == NULL)  return RISK_ERROR_NULL;

    int count = risk_rolling_count(length, window);
    if (count == 0)                             return RISK_ERROR_BAD_WINDOW;
    if (out_capacity < count)                   return RISK_ERROR_SMALL_BUFFER;

    double ppy = (double)periods_per_year;
    double rf_daily = pow(1.0 + risk_free_rate, 1.0 / ppy) - 1.0;

    for (int k = 0; k < count; k++)
    {
        RiskResult r;
        compute_stats(values + k, window + 1, rf_daily, ppy, &r);
        vol_out[k] = r.volatility;
        sharpe_out[k] = r.sharpe_ratio;
        mdd_out[k] = r.max_drawdown;
    }

    return RISK_OK;
}

#include <stddef.h>
#include <math.h>
#include "risk.h"

static int validate(const double *values, int length, double risk_free_rate, int periods_per_year, const RiskResult *out)
{
    if (values == NULL || out == NULL)      return RISK_ERR_NULL;
    if (length < MIN_NUMBER_OF_ELEMENTS)    return RISK_ERR_TOO_SHORT;
    if (periods_per_year < 1 ||
        periods_per_year >= 366)            return RISK_ERR_BAD_PERIODS;
    if (!isfinite(risk_free_rate) ||
        risk_free_rate <= -1.0)             return RISK_ERR_BAD_RATE;
    for (int i = 0; i < length; i++)
    {
        if (!isfinite(values[i]) || 
            values[i] <= 0.0)               return RISK_ERR_INVALID_VALUE;
    }

    return RISK_OK;
}

int risk_compute(const double *values, int length, double risk_free_rate, int periods_per_year, RiskResult *out)
{
    int valid_res = validate(values, length, risk_free_rate, periods_per_year, out);
    if (valid_res != RISK_OK)
    {
        return valid_res;
    }

    int return_count = length - 1;
    double ppy = (double)periods_per_year;

    // Calculate the average return
    double sum = 0.0;
    for (int i = 0; i < return_count; i++)
    {
        double ret = (values[i + 1] - values[i]) / values[i];
        sum += ret;
    }
    double mean = sum / return_count;

    // Calculate volatility
    double sq = 0.0;
    for (int i = 0; i < return_count; i++)
    {
        double r = (values[i + 1] - values[i]) / values[i];
        double diff = r - mean;
        sq += diff * diff;
    }
    double variance = sq / (return_count - 1);
    double sd_daily = sqrt(variance);

    out->volatility = sd_daily * sqrt(ppy);

    // Calculate sharpe-ratio
    if (sd_daily == 0.0)
    {
        out->sharpe_ratio = NAN;
    }
    else
    {
        double rf_daily = pow(1.0 + risk_free_rate, 1.0 / ppy) - 1.0;
        out->sharpe_ratio = (mean - rf_daily) / sd_daily * sqrt(ppy);
    }

    // Calculate max drawdown
    double peak = values[0];
    double worst = 0.0;
    for (int i = 1; i < length; i++)
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

    return 0;
}
#include <math.h>
#include "risk.h"
#include "unity.h"

void setUp(void)
{
}

void tearDown(void)
{
}

void test_err_if_ok(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_err_ok_if_length_min(void)
{
    double values[] = { 1, 2, 3 };
    RiskResult r;
    int res = risk_compute(values, 3, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_err_ok_if_periods_min(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 1, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_err_ok_if_periods_max(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 365, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_err_ok_if_rate_just_above_minus_one(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, -0.999, 252, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_err_if_values_null(void)
{
    RiskResult r;
    int res = risk_compute(NULL, 3, 0.2, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_NULL, res);
}

void test_err_if_riskresult_null(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    int res = risk_compute(values, 5, 0.5, 253, NULL);

    TEST_ASSERT_EQUAL(RISK_ERROR_NULL, res);
}

void test_err_if_length_short(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 2, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_TOO_SHORT, res);
}

void test_err_if_length_negative(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, -1, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_TOO_SHORT, res);
}

void test_err_if_periods_short(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.01, 0, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_PERIODS, res);
}

void test_err_if_periods_long(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, 0.01, 366, &r);
    int res2 = risk_compute(values, 5, 0.01, 499, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_PERIODS, res1, "366 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_PERIODS, res2, "499 failed");
}

void test_err_if_periods_negative(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.01, -3, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_PERIODS, res);
}

void test_err_if_rate_bad(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, -2, 232, &r);
    int res2 = risk_compute(values, 5, NAN, 231, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_RATE, res1, "-2 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_RATE, res2, "NaN failed");
}

void test_err_if_rate_minus_one(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, -1.0, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_RATE, res);
}

void test_err_if_rate_infinite(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, INFINITY, 252, &r);
    int res2 = risk_compute(values, 5, -INFINITY, 252, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_RATE, res1, "+inf failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_BAD_RATE, res2, "-inf failed");
}

void test_err_if_invalid_value(void)
{
    double values1[] = { 1, 2, -4, 2, 4 };
    double values2[] = { 1, NAN, 42, 2, 4 };
    RiskResult r;
    int res1 = risk_compute(values1, 5, 1.2, 222, &r);
    int res2 = risk_compute(values2, 5, 1.2, 222, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_INVALID_VALUE, res1, "-4 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERROR_INVALID_VALUE, res2, "NaN failed");
}

void test_err_if_value_zero(void)
{
    double values[] = { 1, 2, 3, 0, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_INVALID_VALUE, res);
}

void test_err_if_value_infinite(void)
{
    double values[] = { 1, 2, 3, 4, INFINITY };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERROR_INVALID_VALUE, res);
}

void test_err_volatility_and_sharpe_known_values(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.0, 1, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, sqrt(0.02), r.volatility);
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.sharpe_ratio);
}

void test_err_flat_series_gives_zero_vol_and_nan_sharpe(void)
{
    double values[] = { 100, 110, 121 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.volatility);
    TEST_ASSERT_TRUE(isnan(r.sharpe_ratio));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.max_drawdown);
}

void test_err_volatility_is_annualized_by_sqrt_periods(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.0, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, sqrt(0.02) * sqrt(252.0), r.volatility);
}

void test_err_sharpe_subtracts_risk_free_rate(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.05, 1, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, -0.05 / sqrt(0.02), r.sharpe_ratio);
}

void test_err_max_drawdown_uses_running_peak(void)
{
    double values[] = { 100, 80, 200, 100 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.5, r.max_drawdown);
}

void test_err_max_drawdown_keeps_worst_not_last(void)
{
    double values[] = { 100, 50, 100, 90 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.5, r.max_drawdown);
}

void test_err_max_drawdown_is_zero_when_only_rising(void)
{
    double values[] = { 10, 20, 30, 40 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.max_drawdown);
}

static const double SERIES[] = { 100.0, 101.5, 99.8, 102.3, 103.1, 101.9, 104.4, 105.0, 103.2, 106.1, 107.5, 105.9 };
#define SERIES_LEN 12

void test_ewma_matches_reference(void)
{
    double expected[] = { 0.2381176179958116, 0.23987388609398014, 0.25214088533832824, 0.2463436241235884,
                          0.24308925573807147, 0.2542591673471982, 0.24752427395906867, 0.24906946580914893,
                          0.26505287169904884, 0.26205036946034255, 0.2605755794678907 };
    double out[SERIES_LEN - 1];
    TEST_ASSERT_EQUAL(RISK_OK, risk_ewma_vol(SERIES, SERIES_LEN, 0.94, 252, out, SERIES_LEN - 1));
    TEST_ASSERT_DOUBLE_ARRAY_WITHIN(1e-12, expected, out, SERIES_LEN - 1);
}

void test_ewma_bad_lambda(void)
{
    double out[SERIES_LEN - 1];
    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_LAMBDA, risk_ewma_vol(SERIES, SERIES_LEN, 0.0, 252, out, SERIES_LEN - 1));
    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_LAMBDA, risk_ewma_vol(SERIES, SERIES_LEN, 1.0, 252, out, SERIES_LEN - 1));
    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_LAMBDA, risk_ewma_vol(SERIES, SERIES_LEN, NAN, 252, out, SERIES_LEN - 1));
}

void test_ewma_small_buffer(void)
{
    double out[SERIES_LEN - 1];
    TEST_ASSERT_EQUAL(RISK_ERROR_SMALL_BUFFER, risk_ewma_vol(SERIES, SERIES_LEN, 0.94, 252, out, SERIES_LEN - 2));
}

void test_ewma_null_and_invalid(void)
{
    double out[SERIES_LEN - 1];
    TEST_ASSERT_EQUAL(RISK_ERROR_NULL, risk_ewma_vol(NULL, SERIES_LEN, 0.94, 252, out, SERIES_LEN - 1));
    TEST_ASSERT_EQUAL(RISK_ERROR_NULL, risk_ewma_vol(SERIES, SERIES_LEN, 0.94, 252, NULL, SERIES_LEN - 1));
    TEST_ASSERT_EQUAL(RISK_ERROR_TOO_SHORT, risk_ewma_vol(SERIES, 2, 0.94, 252, out, 1));
}

void test_rolling_count(void)
{
    TEST_ASSERT_EQUAL(7, risk_rolling_count(12, 5));
    TEST_ASSERT_EQUAL(1, risk_rolling_count(12, 11));
    TEST_ASSERT_EQUAL(0, risk_rolling_count(12, 12));
    TEST_ASSERT_EQUAL(0, risk_rolling_count(12, 1));
}

void test_rolling_matches_reference(void)
{
    double vol_exp[] = { 0.2810440360384873, 0.3112959004908571, 0.2418420620289215, 0.26421320196386905,
                         0.3252059351036929, 0.28617837764202414, 0.3040183545360151 };
    double sh_exp[] = { 3.423320016512027, 4.63420832275627, 10.653246729408087, 1.7027086568680727,
                        4.526431161780868, 9.517386414001752, 2.424275635560298 };
    double mdd_exp[] = { 0.016748768472906433, 0.016748768472906433, 0.011639185257031897, 0.017142857142857116,
                         0.017142857142857116, 0.017142857142857116, 0.017142857142857116 };
    double vol[7], sh[7], mdd[7];
    TEST_ASSERT_EQUAL(RISK_OK, risk_rolling(SERIES, SERIES_LEN, 5, 0.02, 252, vol, sh, mdd, 7));
    TEST_ASSERT_DOUBLE_ARRAY_WITHIN(1e-10, vol_exp, vol, 7);
    TEST_ASSERT_DOUBLE_ARRAY_WITHIN(1e-9, sh_exp, sh, 7);
    TEST_ASSERT_DOUBLE_ARRAY_WITHIN(1e-12, mdd_exp, mdd, 7);
}

void test_rolling_full_window_equals_compute(void)
{
    RiskResult r;
    double vol, sh, mdd;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(SERIES, SERIES_LEN, 0.02, 252, &r));
    TEST_ASSERT_EQUAL(RISK_OK, risk_rolling(SERIES, SERIES_LEN, SERIES_LEN - 1, 0.02, 252, &vol, &sh, &mdd, 1));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, r.volatility, vol);
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, r.sharpe_ratio, sh);
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, r.max_drawdown, mdd);
}

void test_rolling_bad_window_and_buffer(void)
{
    double vol[12], sh[12], mdd[12];
    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_WINDOW, risk_rolling(SERIES, SERIES_LEN, 1, 0.02, 252, vol, sh, mdd, 12));
    TEST_ASSERT_EQUAL(RISK_ERROR_BAD_WINDOW, risk_rolling(SERIES, SERIES_LEN, 12, 0.02, 252, vol, sh, mdd, 12));
    TEST_ASSERT_EQUAL(RISK_ERROR_SMALL_BUFFER, risk_rolling(SERIES, SERIES_LEN, 5, 0.02, 252, vol, sh, mdd, 6));
    TEST_ASSERT_EQUAL(RISK_ERROR_NULL, risk_rolling(SERIES, SERIES_LEN, 5, 0.02, 252, vol, NULL, mdd, 7));
}

void test_sharpe_nan_for_smooth_growth_with_rounding_noise(void)
{
    double values[] = { 100, 101, 102.01, 103.0301, 104.060401 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 5, 0.02, 252, &r));
    TEST_ASSERT_TRUE(isnan(r.sharpe_ratio));
}

void test_sharpe_uses_geometric_rate_at_252(void)
{
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(SERIES, SERIES_LEN, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-9, 4.747136892220402, r.sharpe_ratio);
}

int main(void)
{
    UNITY_BEGIN();

    RUN_TEST(test_err_if_ok);
    RUN_TEST(test_err_ok_if_length_min);
    RUN_TEST(test_err_ok_if_periods_min);
    RUN_TEST(test_err_ok_if_periods_max);
    RUN_TEST(test_err_ok_if_rate_just_above_minus_one);
    RUN_TEST(test_err_if_values_null);
    RUN_TEST(test_err_if_riskresult_null);
    RUN_TEST(test_err_if_length_short);
    RUN_TEST(test_err_if_length_negative);
    RUN_TEST(test_err_if_periods_short);
    RUN_TEST(test_err_if_periods_long);
    RUN_TEST(test_err_if_periods_negative);
    RUN_TEST(test_err_if_rate_bad);
    RUN_TEST(test_err_if_rate_minus_one);
    RUN_TEST(test_err_if_rate_infinite);
    RUN_TEST(test_err_if_invalid_value);
    RUN_TEST(test_err_if_value_zero);
    RUN_TEST(test_err_if_value_infinite);
    RUN_TEST(test_err_volatility_and_sharpe_known_values);
    RUN_TEST(test_err_flat_series_gives_zero_vol_and_nan_sharpe);
    RUN_TEST(test_err_volatility_is_annualized_by_sqrt_periods);
    RUN_TEST(test_err_sharpe_subtracts_risk_free_rate);
    RUN_TEST(test_err_max_drawdown_uses_running_peak);
    RUN_TEST(test_err_max_drawdown_keeps_worst_not_last);
    RUN_TEST(test_err_max_drawdown_is_zero_when_only_rising);

    RUN_TEST(test_ewma_matches_reference);
    RUN_TEST(test_ewma_bad_lambda);
    RUN_TEST(test_ewma_small_buffer);
    RUN_TEST(test_ewma_null_and_invalid);
    RUN_TEST(test_rolling_count);
    RUN_TEST(test_rolling_matches_reference);
    RUN_TEST(test_rolling_full_window_equals_compute);
    RUN_TEST(test_rolling_bad_window_and_buffer);
    RUN_TEST(test_sharpe_nan_for_smooth_growth_with_rounding_noise);
    RUN_TEST(test_sharpe_uses_geometric_rate_at_252);

    return UNITY_END();
}

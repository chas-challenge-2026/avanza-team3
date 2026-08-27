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

void test_ok_if_length_min(void)
{
    double values[] = { 1, 2, 3 };
    RiskResult r;
    int res = risk_compute(values, 3, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_ok_if_periods_min(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 1, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_ok_if_periods_max(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 365, &r);

    TEST_ASSERT_EQUAL(RISK_OK, res);
}

void test_ok_if_rate_just_above_minus_one(void)
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

    TEST_ASSERT_EQUAL(RISK_ERR_NULL, res);
}

void test_err_if_riskresult_null(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    int res = risk_compute(values, 5, 0.5, 253, NULL);

    TEST_ASSERT_EQUAL(RISK_ERR_NULL, res);
}

void test_err_if_length_short(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 2, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_TOO_SHORT, res);
}

void test_err_if_length_negative(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, -1, 0.02, 200, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_TOO_SHORT, res);
}

void test_err_if_periods_short(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.01, 0, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_BAD_PERIODS, res);
}

void test_err_if_periods_long(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, 0.01, 366, &r);
    int res2 = risk_compute(values, 5, 0.01, 499, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_PERIODS, res1, "366 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_PERIODS, res2, "499 failed");
}

void test_err_if_periods_negative(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.01, -3, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_BAD_PERIODS, res);
}

void test_err_if_rate_bad(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, -2, 232, &r);
    int res2 = risk_compute(values, 5, NAN, 231, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_RATE, res1, "-2 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_RATE, res2, "NaN failed");
}

void test_err_if_rate_minus_one(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, -1.0, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_BAD_RATE, res);
}

void test_err_if_rate_infinite(void)
{
    double values[] = { 1, 2, 3, 4, 5 };
    RiskResult r;
    int res1 = risk_compute(values, 5, INFINITY, 252, &r);
    int res2 = risk_compute(values, 5, -INFINITY, 252, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_RATE, res1, "+inf failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_BAD_RATE, res2, "-inf failed");
}

void test_err_if_invalid_value(void)
{
    double values1[] = { 1, 2, -4, 2, 4 };
    double values2[] = { 1, NAN, 42, 2, 4 };
    RiskResult r;
    int res1 = risk_compute(values1, 5, 1.2, 222, &r);
    int res2 = risk_compute(values2, 5, 1.2, 222, &r);

    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_INVALID_VALUE, res1, "-4 failed");
    TEST_ASSERT_EQUAL_MESSAGE(RISK_ERR_INVALID_VALUE, res2, "NaN failed");
}

void test_err_if_value_zero(void)
{
    double values[] = { 1, 2, 3, 0, 5 };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_INVALID_VALUE, res);
}

void test_err_if_value_infinite(void)
{
    double values[] = { 1, 2, 3, 4, INFINITY };
    RiskResult r;
    int res = risk_compute(values, 5, 0.02, 252, &r);

    TEST_ASSERT_EQUAL(RISK_ERR_INVALID_VALUE, res);
}

void test_volatility_and_sharpe_known_values(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.0, 1, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, sqrt(0.02), r.volatility);
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.sharpe_ratio);
}

void test_flat_series_gives_zero_vol_and_nan_sharpe(void)
{
    double values[] = { 100, 110, 121 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.volatility);
    TEST_ASSERT_TRUE(isnan(r.sharpe_ratio));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.max_drawdown);
}

void test_volatility_is_annualized_by_sqrt_periods(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.0, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, sqrt(0.02) * sqrt(252.0), r.volatility);
}

void test_sharpe_subtracts_risk_free_rate(void)
{
    double values[] = { 100, 110, 99 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 3, 0.05, 1, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, -0.05 / sqrt(0.02), r.sharpe_ratio);
}

void test_max_drawdown_uses_running_peak(void)
{
    double values[] = { 100, 80, 200, 100 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.5, r.max_drawdown);
}

void test_max_drawdown_keeps_worst_not_last(void)
{
    double values[] = { 100, 50, 100, 90 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.5, r.max_drawdown);
}

void test_max_drawdown_is_zero_when_only_rising(void)
{
    double values[] = { 10, 20, 30, 40 };
    RiskResult r;
    TEST_ASSERT_EQUAL(RISK_OK, risk_compute(values, 4, 0.02, 252, &r));
    TEST_ASSERT_DOUBLE_WITHIN(1e-12, 0.0, r.max_drawdown);
}

int main(void)
{
    UNITY_BEGIN();

    RUN_TEST(test_err_if_ok);
    RUN_TEST(test_ok_if_length_min);
    RUN_TEST(test_ok_if_periods_min);
    RUN_TEST(test_ok_if_periods_max);
    RUN_TEST(test_ok_if_rate_just_above_minus_one);
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
    RUN_TEST(test_volatility_and_sharpe_known_values);
    RUN_TEST(test_flat_series_gives_zero_vol_and_nan_sharpe);
    RUN_TEST(test_volatility_is_annualized_by_sqrt_periods);
    RUN_TEST(test_sharpe_subtracts_risk_free_rate);
    RUN_TEST(test_max_drawdown_uses_running_peak);
    RUN_TEST(test_max_drawdown_keeps_worst_not_last);
    RUN_TEST(test_max_drawdown_is_zero_when_only_rising);

    return UNITY_END();
}

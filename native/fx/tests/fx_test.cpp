#include "fx.hpp"
#include <gtest/gtest.h>

TEST(FxConvert, HandlesValidInput)
{
    double res;
    EXPECT_EQ(FxConvert(100.0, "eur", "eur", &res), FX_OK);
    EXPECT_EQ(FxConvert(2234.5432, "usd", "sek", &res), FX_OK);
    EXPECT_EQ(FxConvert(3.23232434, "SEK", "usd", &res), FX_OK);
    EXPECT_EQ(FxConvert(75437329213.0, "SEK", "NOK", &res), FX_OK);
}

TEST(FxConvert, HandlesSameCurrencies)
{
    double res;
    const double amount = 100.0;

    FxConvert(amount, "sek", "sek", &res);
    EXPECT_EQ(amount, res);
    FxConvert(amount, "eur", "eur", &res);
    EXPECT_EQ(amount, res);
    FxConvert(amount, "usd", "usd", &res);
    EXPECT_EQ(amount, res);
}

TEST(FxConvert, HandlesInvalidCurrency)
{
    double res;
    EXPECT_EQ(FxConvert(2.0, "xxx", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(3.0, "usd", "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(1.0, "xxx", "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "åäö", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "xxx", "åäö", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "åäö", "åäö", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "---", "---", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(4.2, "u", "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, ".", ";;;", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, " ", "EUR", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "usd", "   ", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "usd", "\\n", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "usd", "sekk", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "usdd", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvert(2.0, "usdd", "eurr", &res), FX_ERROR_UNKNOWN_CURRENCY);
}

TEST(FxConvert, HandlesNullPtr)
{
    double res;
    EXPECT_EQ(FxConvert(1.0, "usd", "eur", nullptr), FX_ERROR_NULL);
    EXPECT_EQ(FxConvert(1.0, "usd", nullptr, &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvert(1.0, nullptr, "eur", &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvert(1.0, nullptr, nullptr, &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvert(1.0, nullptr, nullptr, nullptr), FX_ERROR_NULL);
}
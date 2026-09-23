#include "fx.hpp"
#include <gtest/gtest.h>

TEST(FxConvertPair, HandlesValidInput)
{
    double res;
    EXPECT_EQ(FxConvertPair(100.0, "eur", "gbp", &res), FX_OK);
    EXPECT_EQ(FxConvertPair(2234.5432, "usd", "sek", &res), FX_OK);
    EXPECT_EQ(FxConvertPair(3.23232434, "SEK", "usd", &res), FX_OK);
    EXPECT_EQ(FxConvertPair(75437329213.0, "SEK", "NOK", &res), FX_OK);
}

TEST(FxConvertPair, HandlesInvalidCurrency)
{
    double res;
    EXPECT_EQ(FxConvertPair(2.0, "xxx", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(3.0, "usd", "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(1.0, "xxx", "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "åäö", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "xxx", "åäö", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "åäö", "åäö", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "---", "---", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(4.2, "u",   "xxx", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, ".",   ";;;", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, " ",   "EUR", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "usd", "   ", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "usd", "\\n", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "usd", "sekk", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "usdd", "eur", &res), FX_ERROR_UNKNOWN_CURRENCY);
    EXPECT_EQ(FxConvertPair(2.0, "usdd", "eurr", &res), FX_ERROR_UNKNOWN_CURRENCY);
}

TEST(FxConvertPair, HandlesNullPtr)
{
    double res;
    EXPECT_EQ(FxConvertPair(1.0, "usd", "eur", nullptr), FX_ERROR_NULL);
    EXPECT_EQ(FxConvertPair(1.0, "usd", nullptr, &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvertPair(1.0, nullptr, "eur", &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvertPair(1.0, nullptr, nullptr, &res), FX_ERROR_NULL);
    EXPECT_EQ(FxConvertPair(1.0, nullptr, nullptr, nullptr), FX_ERROR_NULL);
}
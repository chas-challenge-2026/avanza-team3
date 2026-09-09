#include "fx.hpp"

int fxConvert(double amount, const char *from, const char *to, double *outResult)
{
    // SEK 1
    // USD 0.10378
    // "Convert 1000 SEK to USD"

    // *USE API*
    double fromCurrencyValue = 1.0;
    double toCurrencyValue = 0.10378;

    double rate = toCurrencyValue / fromCurrencyValue;
    *outResult = amount * rate;
    return 0;
}
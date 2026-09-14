#pragma once

enum
{
    FX_OK = 0,
    FX_ERROR
};

/** 
 * @brief Converts a given amount of one currency to 
 *        the corresponding amount of another currency
 *        (e.g from USD to SEK)
 *
 * @param[in]  
 * @param[out] 
 * 
 * @return Error code
 */
int fxConvert(double *amount, const char *fromCurrency, const char *toCurrency);
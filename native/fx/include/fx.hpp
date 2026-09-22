#pragma once

#include "currency.hpp"
#include "http.hpp"

enum
{
    FX_OK = 0,
    FX_ERROR_NULL,
    FX_ERROR_REQUEST_FAILED,
    FX_ERROR_UNKNOWN_CURRENCY,
    FX_ERROR_INTERNAL
};

#ifdef __cplusplus
    extern "C"
#endif

/**
 * @brief Converts exchange values. Gets the latest exchange data
 * via HTTP.
 *
 * @param[in]  amount The amount that will be converted
 * @param[in]  from The base currency to convert from
 * @param[in]  to The currency to convert to
 * @param[out] out A double pointer that will store the converted value
 *
 * @return Error code
 */
int FxConvertPair(double amount, const char *from, const char *to, double *out);
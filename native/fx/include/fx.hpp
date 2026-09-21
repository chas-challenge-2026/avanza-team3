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

int FxConvert(double amount, const char *from, const char *to, double *out);

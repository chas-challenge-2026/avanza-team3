#pragma once

enum
{
    FX_OK = 0,
    FX_ERROR_NULL,
    FX_ERROR_REQUEST_FAILED,
    FX_ERROR_UNKNOWN_CURRENCY,
    FX_ERROR_INTERNAL,
    FX_ERROR_API_UNRESPONSIVE,
    FX_ERROR_NO_DATA
};

#ifdef __cplusplus
extern "C"
{
#endif

    /**
     * @brief Converts exchange values. Gets the latest exchange data
     * from api.frankfurter.dev via HTTP.
     *
     * @param[in]  amount The amount that will be converted
     * @param[in]  from The base currency to convert from
     * @param[in]  to The currency to convert to
     * @param[out] outResult A double pointer that will store the converted value
     *
     * @return FX_Error code.
     * List of error codes are found in fx/README.md
     */
    int FxConvert(double amount, const char *from, const char *to, double *outResult);

    /**
     * @brief Looks up the historical FX rates for any currency
     *
     * @param[in]  currency     The currency you want. Will always be compared with EUR.
     * @param[in]  length       Number of days (length of outResult).
     * @param[out] outResult    Result array where every index represents a date.
     *                          If you input 500 days, index [0] is
     *                          499 days ago and index [499] is today.
     *
     * @return FX_Error code.
     * List of error codes are found in fx/README.md
     */
    int FxHistory(const char *currency, int length, double *outResult);

#ifdef __cplusplus
}
#endif
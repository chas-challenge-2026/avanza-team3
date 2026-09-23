#include "fx.hpp"
#include "currency.hpp"
#include "http.hpp"

#include <algorithm>
#include <iostream>
#include <stdexcept>
#include <unordered_map>

namespace
{
constexpr const char *ApiUrl = "https://api.frankfurter.dev/v2/"; // E.g https://api.frankfurter.dev/v2/rate/usd/sek

// The API returns JSON with the fields always in this order:
// {"date":"2026-09-21","base":"EUR","quote":"USD","rate":1.149}
// Reads one such object from pos and leaves pos just past its closing brace
Currency ParseObject(const std::string &str, size_t &pos)
{
    auto skipToValue = [&] {
        pos = str.find(':', pos);
        if (pos == std::string::npos)
            throw std::invalid_argument("malformed response");
        pos++;
    };

    auto readQuoted = [&](size_t len) {
        skipToValue();
        std::string s = str.substr(pos + 1, len);
        pos += 1 + len;
        return s;
    };

    std::string date = readQuoted(10);
    std::string base = readQuoted(3);
    std::string name = readQuoted(3);

    skipToValue();
    size_t close = str.find('}', pos);
    if (close == std::string::npos)
        throw std::invalid_argument("malformed response");
    double rate = std::stod(str.substr(pos, close - pos));

    pos = close + 1;
    return Currency(date, base, name, rate);
}

int Fetch(const std::string &path, std::string &buf)
{
    HttpClient client;
    return client.Get(ApiUrl + path, buf) == CURLE_OK ? FX_OK : FX_ERROR_REQUEST_FAILED;
}
} // namespace

Currency FxParsePair(const std::string &str)
{
    size_t pos = 0;
    return ParseObject(str, pos);
}

std::unordered_map<std::string, Currency> FxParseList(const std::string &str)
{
    std::unordered_map<std::string, Currency> list;

    size_t pos = 0;
    while ((pos = str.find('{', pos)) != std::string::npos)
    {
        Currency currency = ParseObject(str, pos);
        list.emplace(currency.GetName(), currency);
    }

    return list;
}

int FxConvertPair(const double amount, const char *from, const char *to, double *out)
{
    if (!from || !to || !out)
        return FX_ERROR_NULL;

    if (strlen(from) != 3 || strlen(to) != 3)
        return FX_ERROR_UNKNOWN_CURRENCY;

    for (int i = 0; i < 3; i++)
    {
        if (!isalpha(from[i]) || !isalpha(to[i]))
            return FX_ERROR_UNKNOWN_CURRENCY;
    }

    try
    {
        std::string buf;
        int err = Fetch(std::string("rate/") + from + "/" + to, buf);
        if (err != FX_OK)
            return err;

        // The API returns "status":422 if the currency doesn't exist
        if (buf.find(R"("status":422)") != std::string::npos)
            return FX_ERROR_UNKNOWN_CURRENCY;

        *out = amount * FxParsePair(buf).GetRate();
        return FX_OK;
    }
    catch (...)
    {
        return FX_ERROR_INTERNAL;
    }
}
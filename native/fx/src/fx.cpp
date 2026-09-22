#include "fx.hpp"
#include "http.hpp"
#include "currency.hpp"

#include <algorithm>
#include <cctype>
#include <iostream>
#include <stdexcept>
#include <unordered_map>

namespace
{
constexpr const char *ApiUrl = "https://api.frankfurter.dev/v2/";

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

    try
    {
        std::string buf;
        if (int err = Fetch(std::string("rate/") + from + "/" + to, buf); err != FX_OK)
            return err;

        *out = amount * FxParsePair(buf).GetRate();
        return FX_OK;
    }
    catch (...)
    {
        return FX_ERROR_INTERNAL;
    }
}

int main()
{
    double res = 0.0;
    int err = 0;

    err = FxConvertPair(100, "usd", "sek", &res);
    std::cout << "Error code: " << err << std::endl;
    std::cout << res << std::endl;
}
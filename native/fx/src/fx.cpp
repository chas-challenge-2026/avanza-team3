#include "fx.hpp"
#include "currency.hpp"
#include "http.hpp"

#include <algorithm>
#include <cctype>
#include <chrono>
#include <cstring>
#include <iostream>
#include <mutex>
#include <stdexcept>
#include <unordered_map>

namespace
{
constexpr size_t CURRENCY_CODE_LENGTH = 3;
constexpr std::chrono::seconds CACHE_EXPIRATION_S{60};
constexpr const char *API_URL = "https://api.frankfurter.dev/v2/"; // E.g https://api.frankfurter.dev/v2/rates

std::mutex cacheMutex;
std::unordered_map<std::string, Currency> currencyMap{};
std::chrono::time_point<std::chrono::steady_clock> latestFetch;

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

// Parses the latest exchange data from https://api.frankfurter.dev/v2/rates
// into an std::unordered_map
std::unordered_map<std::string, Currency> ParseList(const std::string &str)
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

int Fetch(const std::string &path, std::string &buf)
{
    HttpClient client;
    return client.Get(API_URL + path, buf) == CURLE_OK ? FX_OK : FX_ERROR_REQUEST_FAILED;
}
} // namespace

int FxConvert(const double amount, const char *from, const char *to, double *outResult)
{
    // Error checks
    if (!from || !to || !outResult)
        return FX_ERROR_NULL;

    if (strlen(from) != CURRENCY_CODE_LENGTH || strlen(to) != CURRENCY_CODE_LENGTH)
        return FX_ERROR_UNKNOWN_CURRENCY;

    for (int i = 0; i < 3; i++)
    {
        if (!isalpha(static_cast<unsigned char>(from[i])) || !isalpha(static_cast<unsigned char>(to[i])))
            return FX_ERROR_UNKNOWN_CURRENCY;
    }

    try
    {
        std::lock_guard<std::mutex> lock(cacheMutex);
        // Convert "from" and "to" to uppercase characters
        std::string fromStr(from);
        std::string toStr(to);
        std::transform(fromStr.begin(), fromStr.end(), fromStr.begin(), toupper);
        std::transform(toStr.begin(), toStr.end(), toStr.begin(), toupper);

        // Determine if we need to fetch new data or use the cache
        if (currencyMap.empty() || std::chrono::steady_clock::now() - latestFetch > CACHE_EXPIRATION_S)
        {
            std::cout << "Cache is outdated" << std::endl;
            std::string buf;
            int err = Fetch("rates", buf);
            if (err == FX_OK)
            {
                auto parsedMap = ParseList(buf);
                if (!parsedMap.empty())
                {
                    currencyMap = std::move(parsedMap);
                    latestFetch = std::chrono::steady_clock::now();
                }
            }
            else
                std::cout << "Request failed. Using cache" << std::endl;
        }
        else
            std::cout << "Cache is up to date" << std::endl;

        // If the map is still empty, there has been an issue
        if (currencyMap.empty())
            return FX_ERROR_REQUEST_FAILED;

        // Look for the "from" and "to" currencies in the map
        const auto fromIt = currencyMap.find(fromStr);
        const auto toIt = currencyMap.find(toStr);

        // Check that find() is valid before touching them
        if (toIt == currencyMap.end() || fromIt == currencyMap.end())
            return FX_ERROR_UNKNOWN_CURRENCY;

        // Finally, do the actual conversion
        *outResult = amount * toIt->second.GetRate() / fromIt->second.GetRate();

        return FX_OK;
    }
    catch (...)
    {
        return FX_ERROR_INTERNAL;
    }
}
#include "fx.hpp"
#include "http.hpp"

#include <algorithm>
#include <iostream>
#include <unordered_map>

std::unordered_map<std::string, Currency> FxParseList(const std::string &str)
{
    if (str.empty())
        throw std::invalid_argument("str is empty");

    std::unordered_map<std::string, Currency> list;

    enum Key
    {
        date,
        base,
        name,
        rate
    };

    const char *pos = str.data();
    const char *startPos = pos;
    Key currentKey = date;
    Currency temp;

    while (*pos != ']' && (pos - startPos) < str.size())
    {
        if (*pos == ':')
        {
            pos += 2;
            switch (currentKey)
            {
            case date: {
                std::string s;
                for (int i = 0; i < 10; i++, pos++)
                {
                    s += *pos;
                }
                temp.SetDate(s);
                currentKey = base;
                break;
            }
            case base: {
                std::string s;
                for (int i = 0; i < 3; i++, pos++)
                {
                    s += *pos;
                }
                temp.SetBase(s);
                currentKey = name;
                break;
            }
            case name: {
                std::string s;
                for (int i = 0; i < 3; i++, pos++)
                {
                    s += *pos;
                }
                temp.SetName(s);
                currentKey = rate;
                break;
            }
            case rate: {
                pos--;
                std::string s;
                while (*pos != '}')
                {
                    s += *pos;
                    pos++;
                }
                double r = stod(s);
                temp.SetRate(r);

                list.emplace(temp.GetName(), temp);

                currentKey = date;
                break;
            }
            }
        }

        pos++;
    }

    return list;
}

int FxConvert(double amount, const char *from, const char *to, double *out)
{
    if (!from || !to || !out)
        return FX_ERROR_NULL;

    try
    {
        std::string target = to;
        std::transform(target.begin(), target.end(), target.begin(),
                       [](unsigned char c) { return std::toupper(c); });

        std::string url = "https://api.frankfurter.dev/v2/rates?base=";
        url.append(from);

        HttpClient client;
        std::string buf;
        if (client.Get(url, buf) != CURLE_OK)
            return FX_ERROR_REQUEST_FAILED;

        auto list = FxParseList(buf);

        auto it = list.find(target);
        if (it == list.end())
            return FX_ERROR_UNKNOWN_CURRENCY;

        *out = amount * it->second.GetRate();
        return FX_OK;
    }
    catch (...)
    {
        return FX_ERROR_INTERNAL;
    }
}

int main()
{
    double res;
    int err = FxConvert(100, "usd", "sek", &res);
    std::cout << err << std::endl;
    std::cout << res << std::endl;
}
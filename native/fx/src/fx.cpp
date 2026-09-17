#include "fx.hpp"
#include "http.hpp"
#include <iostream>

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

// API used: https://frankfurter.dev/
// Get list of all currencies: https://api.frankfurter.dev/v2/rates
// Get rate for currency pair: https://api.frankfurter.dev/v2/rate/sek/usd

int main()
{
    HttpClient client;
    std::string buf;
    const int res = client.Get("https://api.frankfurter.dev/v2/rates", buf);
    std::cout << "res: " << res << std::endl;
    std::cout << "buf: " << buf << std::endl;

    auto list = FxParseList(buf);

    for (auto &[fst, snd] : list)
    {
        std::cout << "Key: " << fst << std::endl
                  << "Value: " << snd.GetDate() << std::endl
                  << snd.GetBase() << std::endl
                  << snd.GetName() << std::endl
                  << snd.GetRate() << std::endl;
    }

    return res;
}
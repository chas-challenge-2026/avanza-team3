#include "currency.hpp"
#include <iostream>

Currency::Currency(std::string date, std::string base, std::string name, const double rate)
    : date(std::move(date)), base(std::move(base)), name(std::move(name)), rate(rate)
{
    if (this->date.size() != 10 || this->date.at(4) != '-' || this->date.at(7) != '-')
    {
        throw std::invalid_argument(R"(Invalid parameter "date". "date" must fit the format "YYYY-MM-DD".)");
    }

    if (this->base.size() != 3)
    {
        throw std::invalid_argument(R"(Invalid length of parameter "base". "base" must be 3 characters.)");
    }

    if (this->name.size() != 3)
    {
        throw std::invalid_argument(R"(Invalid length of parameter "name". "name" must be 3 characters.)");
    }
}
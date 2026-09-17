#pragma once

#include <string>

class Currency
{
  public:
    Currency() = default;
    Currency(std::string date, std::string base, std::string name, double rate);

  private:
    std::string date;  // "2026-09-15"
    std::string base;  // "EUR"
    std::string name;  // "AED"
    double rate = 0.0; // 4.2492
};
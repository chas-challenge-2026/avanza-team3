#pragma once

#include <string>

class Currency
{
  public:
    Currency() = default;
    Currency(std::string date, std::string base, std::string name, double rate);

    void SetDate(std::string &date) { this->date = date; }
    void SetBase(std::string &base) { this->base = base; }
    void SetName(std::string &name) { this->name = name; }
    void SetRate(double rate) { this->rate = rate; }

    std::string GetDate() { return date; }
    std::string GetBase() { return base; }
    std::string GetName() { return name; }
    double GetRate() const { return rate; }

  private:
    std::string date;  // "2026-09-15"
    std::string base;  // "EUR"
    std::string name;  // "AED"
    double rate = 0.0; // 4.2492
};
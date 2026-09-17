#pragma once

#include <unordered_map>
#include <string>
#include "currency.hpp"

std::unordered_map<std::string, Currency> FxParseList(const std::string &str);
#pragma once

#include <curl/curl.h>
#include <string>

class HttpClient
{
  public:
    HttpClient();
    ~HttpClient();

    HttpClient(const HttpClient &) = delete;
    HttpClient &operator=(const HttpClient &) = delete;

    CURLcode Get(const std::string &url, std::string &response);

  private:
    CURL *Handle = nullptr;
};
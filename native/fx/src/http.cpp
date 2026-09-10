#include "http.hpp"
#include <iostream>

static size_t WriteCallback(char *data, size_t size, size_t nmemb, void *userdata)
{
    try
    {
        size_t chunkSize = size * nmemb;
        static_cast<std::string *>(userdata)->append(data, chunkSize);

        return chunkSize;
    }
    catch (...)
    {
        return 0;
    }
}

HttpClient::HttpClient()
{
    Handle = curl_easy_init();
    if (!Handle)
        throw std::runtime_error("curl_easy_init failed");

    curl_easy_setopt(Handle, CURLOPT_WRITEFUNCTION, WriteCallback);
    curl_easy_setopt(Handle, CURLOPT_CONNECTTIMEOUT, 5L);
    curl_easy_setopt(Handle, CURLOPT_TIMEOUT, 10L);
}

HttpClient::~HttpClient()
{
    curl_easy_cleanup(Handle);
    Handle = nullptr;
}

/**
 * @brief Sends a blocking GET request
 *
 * @param url The URL to get content from
 * @param response A string to store the response in
 * @return CURLcode error code
 */
CURLcode HttpClient::Get(const std::string &url, std::string &response)
{
    curl_easy_setopt(Handle, CURLOPT_URL, url.c_str());
    curl_easy_setopt(Handle, CURLOPT_WRITEDATA, &response);

    return curl_easy_perform(Handle);
}

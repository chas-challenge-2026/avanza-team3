
# FX Module

---

### Build requirements
- Language: C++17
- Build tool: CMake
- External: libcurl

```
cd native/fx
cmake -S . -B build -G Ninja
cmake --build build
ctest --test-dir build -V
```
---

## In a nutshell:
The FX Module's primary objective is to answer one question as cheaply as
possible:
> What is X amount of currency A worth in currency B?

To do this, we require the following data:
```c++
    double amount       // The amount that will be converted
    const char *from    // The base currency to convert from
    const char *to      // The currency to convert to
    double *out         // A double pointer that will store the converted value
```
So we can reframe our previous question as:
> What is ``amount`` amount of currency ``from`` worth in currency ``to``? 
> The answer is found in ```*out```

For example, if you'd like to know what 100 USD is worth in EUR, 
you'd use the FxConvert function, which looks like this: <br />
```c++
int FxConvert(double amount, const char *from, const char *to, double *out)
```

And using this function, you'd enter: <br />
```c++
int err = FxConvert(100.0, "usd", "eur", &result);
```

---

### Status codes
One of the following values will be returned.
Here is what each value means:

| Value | Name                       | Meaning                                           |
|-------|----------------------------|---------------------------------------------------|
| `0`   | `FX_OK`                    |                                |
| `+1`  | `FX_ERROR_NULL`            |                        |
| `+2`  | `FX_ERROR_REQUEST_FAILED`     |                        |
| `+3`  | `FX_ERROR_UNKNOWN_CURRENCY`      |   |
| `+4`  | `FX_ERROR_INTERNAL`   |                             |

---

```
Compiles as:
    Linux:   libfx.so
    Windows: fx.dll
    MacOS:   libfx.dylib
```

```
File structure
/fx
    /include
        currency.hpp
        fx.hpp
        http.hpp
    /src
        currency.cpp
        fx.cpp
        http.cpp
    CMakeLists.txt
```

---


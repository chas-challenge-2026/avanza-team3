
# FX Module

---

### Build requirements
- Language: C++17
- Build tool: CMake
- External: libcurl for the live fetch

---

## In a nutshell:
The FX Module's primary objective is to answer one question as cheaply as
possible:

> What is X amount of currency A worth in currency B?

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
        fx.h
    /src
        main.cpp (for testing)
        fx.cpp
    CMakeLists.txt
```

---


This folder is the JNA classpath location for Linux x86-64 native libraries.

Docker builds the Linux libraries from `native/risk` and `native/fx` and copies:

- `librisk.so`
- `libfx.so`

into this folder before Maven packages the Spring Boot jar.

Windows DLLs are stored in `../win32-x86-64`.

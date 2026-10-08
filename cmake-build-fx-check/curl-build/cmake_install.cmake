# Install script for directory: C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "C:/Program Files (x86)/fx")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "Debug")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "FALSE")
endif()

# Set path to fallback-tool for dependency-resolution.
if(NOT DEFINED CMAKE_OBJDUMP)
  set(CMAKE_OBJDUMP "C:/Program Files/JetBrains/CLion 2026.2.2/bin/mingw/bin/objdump.exe")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/scripts/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/lib/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/docs/examples/cmake_install.cmake")
endif()

if(CMAKE_INSTALL_COMPONENT STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE FILE PERMISSIONS OWNER_READ OWNER_WRITE OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_READ WORLD_EXECUTE FILES "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/curl-config")
endif()

if(CMAKE_INSTALL_COMPONENT STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/libcurl.pc")
endif()

if(CMAKE_INSTALL_COMPONENT STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE DIRECTORY FILES "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/include/curl" FILES_MATCHING REGEX "/[^/]*\\.h$")
endif()

if(CMAKE_INSTALL_COMPONENT STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL/CURLTargets.cmake")
    file(DIFFERENT _cmake_export_file_changed FILES
         "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL/CURLTargets.cmake"
         "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/CMakeFiles/Export/8e83d16133499b505bf3986f4f209a65/CURLTargets.cmake")
    if(_cmake_export_file_changed)
      file(GLOB _cmake_old_config_files "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL/CURLTargets-*.cmake")
      if(_cmake_old_config_files)
        string(REPLACE ";" ", " _cmake_old_config_files_text "${_cmake_old_config_files}")
        message(STATUS "Old export file \"$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL/CURLTargets.cmake\" will be replaced.  Removing files [${_cmake_old_config_files_text}].")
        unset(_cmake_old_config_files_text)
        file(REMOVE ${_cmake_old_config_files})
      endif()
      unset(_cmake_old_config_files)
    endif()
    unset(_cmake_export_file_changed)
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL" TYPE FILE FILES "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/CMakeFiles/Export/8e83d16133499b505bf3986f4f209a65/CURLTargets.cmake")
  if(CMAKE_INSTALL_CONFIG_NAME MATCHES "^([Dd][Ee][Bb][Uu][Gg])$")
    file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL" TYPE FILE FILES "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/CMakeFiles/Export/8e83d16133499b505bf3986f4f209a65/CURLTargets-debug.cmake")
  endif()
endif()

if(CMAKE_INSTALL_COMPONENT STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/CURL" TYPE FILE FILES
    "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/CURLConfigVersion.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/CURLConfig.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindBrotli.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindCares.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindGSS.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindGnuTLS.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLDAP.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibbacktrace.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibgsasl.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibidn2.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibpsl.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibssh.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibssh2.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindLibuv.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindMbedTLS.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindNGHTTP2.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindNGHTTP3.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindNGTCP2.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindNettle.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindQuiche.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindRustls.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindWolfSSL.cmake"
    "C:/Users/alexa/CLionProjects/avanza-team3/native/libs/curl-8.22.0/CMake/FindZstd.cmake"
    )
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
if(CMAKE_INSTALL_LOCAL_ONLY)
  file(WRITE "C:/Users/alexa/CLionProjects/avanza-team3/cmake-build-fx-check/curl-build/install_local_manifest.txt"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
endif()

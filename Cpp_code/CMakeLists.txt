cmake_minimum_required(VERSION 3.15)
project(CheckersSFML VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)
FetchContent_Declare(
        SFML
        GIT_REPOSITORY https://github.com/SFML/SFML.git
        GIT_TAG 2.6.1
)
set(SFML_BUILD_AUDIO OFF CACHE BOOL "" FORCE)
set(SFML_BUILD_NETWORK OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(SFML)

add_executable(checkers
        main.cpp
        checkers.cpp
        Chess.cpp
)

target_link_libraries(checkers PRIVATE sfml-graphics)

if(WIN32)
    add_custom_command(TARGET checkers POST_BUILD
            COMMAND "${CMAKE_COMMAND}" -E copy_if_different
            "$<TARGET_FILE:sfml-graphics>"
            "$<TARGET_FILE:sfml-window>"
            "$<TARGET_FILE:sfml-system>"
            "$<TARGET_FILE_DIR:checkers>"
            VERBATIM
    )
endif()
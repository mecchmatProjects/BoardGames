cmake_minimum_required(VERSION 3.15)
project(BoardGamesSFML VERSION 1.0 LANGUAGES CXX)

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
        Checkers.cpp
)
target_link_libraries(checkers PRIVATE sfml-graphics)

add_executable(chess
        chess_main.cpp
        Chess.cpp
)
target_link_libraries(chess PRIVATE sfml-graphics)

if(WIN32)
    foreach(game checkers chess)
        add_custom_command(TARGET ${game} POST_BUILD
                COMMAND "${CMAKE_COMMAND}" -E copy_if_different
                "$<TARGET_FILE:sfml-graphics>"
                "$<TARGET_FILE:sfml-window>"
                "$<TARGET_FILE:sfml-system>"
                "$<TARGET_FILE_DIR:${game}>"
                VERBATIM
        )
    endforeach()
endif()

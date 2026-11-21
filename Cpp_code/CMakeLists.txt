cmake_minimum_required(VERSION 3.16)
project(Checkers LANGUAGES CXX)

add_executable(checkers main.cpp Checkers.cpp)
target_compile_features(checkers PRIVATE cxx_std_17)

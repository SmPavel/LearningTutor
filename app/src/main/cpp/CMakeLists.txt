cmake_minimum_required(VERSION 3.18.1)
project("learningtutor")

# Указываем, что исходники находятся в подпапке jni
add_library(
        learningtutor-lib
        SHARED
        jni/llama_jni.cpp
)

# Ищем log библиотеку
find_library(
        log-lib
        log
)

# Связываем библиотеки
target_link_libraries(
        learningtutor-lib
        ${log-lib}
)

# Включаем C++17 стандарт
set_target_properties(
        learningtutor-lib
        PROPERTIES
        CXX_STANDARD 17
        CXX_STANDARD_REQUIRED ON
)
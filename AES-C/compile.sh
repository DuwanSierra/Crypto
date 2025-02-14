#!/bin/bash
# Script de compilación para Linux con pybind11

# 1. Verificar si pybind11 está instalado, si no, instalarlo.
if ! python3 -m pip show pybind11 >/dev/null 2>&1; then
    echo "pybind11 no está instalado. Instalando..."
    python3 -m pip install pybind11
else
    echo "pybind11 ya está instalado."
fi

# 2. Compilar el módulo usando c++ (o g++).
#    -O3: optimización de nivel 3.
#    -Wall: todas las advertencias.
#    -shared: genera una biblioteca compartida.
#    -std=c++14: usa el estándar C++14.
#    -fPIC: código independiente de posición (requerido para bibliotecas compartidas).
#    $(python3 -m pybind11 --includes): obtiene las rutas de inclusión de pybind11 y Python.
#    $(python3-config --extension-suffix): obtiene el sufijo adecuado para el módulo (ej. .so).
echo "Compilando aes.cpp..."
c++ -O3 -Wall -shared -std=c++14 -fPIC $(python3 -m pybind11 --includes) aes.cpp -o ../ProyectoFinal/internal_aes$(python3-config --extension-suffix)

if [ $? -eq 0 ]; then
    echo "Compilación exitosa. El módulo se generó en: ../ProyectoFinal/internal_aes$(python3-config --extension-suffix)"
else
    echo "Error en la compilación."
fi

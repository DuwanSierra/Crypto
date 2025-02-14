@echo off
REM ============================================================
REM Script para compilar aes.cpp con pybind11 en Windows
REM ============================================================

REM 1. Verificar si pybind11 está instalado; si no, se instala.
pip show pybind11 >nul 2>&1
if errorlevel 1 (
    echo pybind11 no está instalado. Instalando...
    pip install pybind11
) else (
    echo pybind11 ya está instalado.
)

REM 2. Obtener las opciones de inclusión de pybind11
for /f "tokens=*" %%i in ('python -m pybind11 --includes') do set "PYBIND_INCLUDES=%%i"
echo Rutas de inclusión: %PYBIND_INCLUDES%

REM 3. Compilar el módulo con cl (MSVC)
REM    /O2 -> optimización, /LD -> crea una DLL, /EHsc -> excepciones en C++,
REM    /std:c++14 -> usa el estándar C++14.
REM    Se asume que aes.cpp está en el directorio actual y que se desea crear el módulo
REM    en la carpeta ../ProyectoFinal con el nombre internal_aes.pyd.
cl /O2 /LD /EHsc /std:c++14 %PYBIND_INCLUDES% aes.cpp /link /OUT:..\ProyectoFinal\internal_aes.pyd

if errorlevel 1 (
    echo Error en la compilacion.
) else (
    echo Compilacion exitosa. El modulo se genero en: ..\ProyectoFinal\internal_aes.pyd
)

pause

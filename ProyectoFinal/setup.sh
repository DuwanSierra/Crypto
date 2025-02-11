#!/bin/bash
# setup_env.sh: Crea un entorno virtual y instala los paquetes de requirements.txt

# Verifica que exista el archivo requirements.txt
if [ ! -f "requirements.txt" ]; then
    echo "No se encontró el archivo requirements.txt"
    exit 1
fi

# Crea el entorno virtual en el directorio 'venv'
echo "Creando entorno virtual..."
python3 -m venv venv

# Activa el entorno virtual
echo "Activando el entorno virtual..."
source venv/bin/activate

# Actualiza pip a la última versión (opcional)
echo "Actualizando pip..."
pip install --upgrade pip

# Instala los paquetes listados en requirements.txt
echo "Instalando paquetes desde requirements.txt..."
pip install -r requirements.txt

echo "¡Entorno virtual configurado correctamente!"
echo "Para activar el entorno virtual en el futuro, ejecuta: source venv/bin/activate"

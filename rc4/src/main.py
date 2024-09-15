import re

# Secuencia binaria de KeyStream proporcionada
key_stream_binary = "00100 11000 00110 11011 01001 00010 11110 10110 11011 01001 11011 10011 01000 01110 00001 11000 00101 10010 11011 10010 10011 01110 00000 01110 11110 01111 10000 00010 00011 01011 01100 01100 11011"
key_stream_binary = key_stream_binary.replace(" ", "")  # Eliminar espacios

# Función para contar las corridas en la secuencia
def contar_corridas(secuencia):
    # Encuentra corridas de 0s o 1s consecutivos
    corridas_0 = re.findall(r'0+', secuencia)
    corridas_1 = re.findall(r'1+', secuencia)
    
    # Combina todas las corridas y ordena por longitud
    corridas = corridas_0 + corridas_1
    longitudes_corridas = [len(c) for c in corridas]
    
    # Cuenta cuántas corridas hay de cada longitud
    distribucion_corridas = {}
    for longitud in longitudes_corridas:
        if longitud in distribucion_corridas:
            distribucion_corridas[longitud] += 1
        else:
            distribucion_corridas[longitud] = 1
    
    return distribucion_corridas

# Contar corridas en la secuencia de KeyStream
distribucion_corridas_key_stream = contar_corridas(key_stream_binary)

# Imprimir el total de corridas
total_corridas = sum(distribucion_corridas_key_stream.values())
print(f"Total de corridas en la secuencia de KeyStream: {total_corridas} corridas")

# Imprimir resultados
print("Distribución de corridas en la secuencia de KeyStream:")
for longitud, cantidad in distribucion_corridas_key_stream.items():
    print(f"Corridas de longitud {longitud}: {cantidad} corridas")


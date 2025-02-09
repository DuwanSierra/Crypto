# Implementa la multiplicación de dos numeros utilizando el polinomio ireductible 
def matrizx2(a, b, polinomio=0x17B):
    result = 0
    for i in range(8): #itera 8 veces porque el GF opera en 8 bits
        if (b & 1) == 1: #Si el bit menos significativo de b es 1
            result ^=a # realiza una XOR entre a y result
        carry = a & 0x80 #comprueba que el bit más significativo de a es 1 indica desbordamiento
        a<<=1 #Desplaza a un bit a la izquierda
        if carry: #Si hay desbordamiento
            a ^=polinomio #reduce a modulo el polinomio irreductible
            a &= 0xFF #asegura que a siga siendo un byte
        b>>=1 # Desplaza un bit a la derecha de b 
    return result;

#Genera las constantes de rondo (RCON) utilizadas en la expansion de claves
def calcular_rcon(polinomio=0x17B):
    rcon = [0] * 256
    rcon[1] = 0x01
    for i in range(2, 256):
        rcon[i] = matrizx2(rcon[i-1], 0x02, polinomio) # calcula cada constante de ronda 
    return [f"0x{x:02X}" for x in rcon]

#Multiplica dos numeros en un campo finito, utilizando el polinomio
def gmul(a, b, poly=0x17B):  # Usando el polinomio 0x17B
    p = 0
    for _ in range(8):
        if b & 1:
            p ^= a
        carry = a & 0x80
        a <<= 1
        if carry:
            a ^= poly
        b >>= 1
    return p & 0xFF

#Calcula el inverso multiplicativo
def inv(x):
    for i in range(256):
        if gmul(x, i) == 1:
            return i
    return 0

#La transformacion usa una matriz especifica, para alterar los bytes
def affine_transform(x):
    # Affine transformation matrix
    matrix = [
        [1, 0, 0, 0, 1, 1, 1, 1],
        [1, 1, 0, 0, 0, 1, 1, 1],
        [1, 1, 1, 0, 0, 0, 1, 1],
        [1, 1, 1, 1, 0, 0, 0, 1],
        [1, 1, 1, 1, 1, 0, 0, 0],
        [0, 1, 1, 1, 1, 1, 0, 0],
        [0, 0, 1, 1, 1, 1, 1, 0],
        [0, 0, 0, 1, 1, 1, 1, 1]
    ]
    c = 0x63  # Constant for the affine transformation
    result = 0
    for i in range(8):
        bit = 0
        for j in range(8):
            bit ^= (x >> j & 1) * matrix[i][j]
        result |= (bit << i)
    return result ^ c

def generate_sbox():
    sbox = []
    for i in range(256):
        inv_i = inv(i)
        sbox.append(affine_transform(inv_i))
    return sbox

def generate_inv_sbox(sbox):
    inv_sbox = [0] * 256
    for i in range(256):
        inv_sbox[sbox[i]] = i
    return inv_sbox


mul2_new = [matrizx2(i,2, 0x17B) for i in range(256)]
mul2_hex = [f"0x{x:02X}" for x in mul2_new]

mul3_new = [matrizx2(i, 3, 0x17B) for i in range(256)]
mul3_hex = [f"0x{x:02X}" for x in mul3_new]

mul9_new = [matrizx2(i, 9, 0x17B) for i in range(256)]
mul9_hex = [f"0x{x:02X}" for x in mul9_new]

mul11_new = [matrizx2(i, 11, 0x17B) for i in range(256)]
mul11_hex = [f"0x{x:02X}" for x in mul11_new]

mul13_new = [matrizx2(i, 13, 0x17B) for i in range(256)]
mul13_hex = [f"0x{x:02X}" for x in mul13_new]

mul14_new = [matrizx2(i, 14, 0x17B) for i in range(256)]
mul14_hex = [f"0x{x:02X}" for x in mul14_new]

rcon_hex=calcular_rcon(0xE7)

print("\n mul2_hex")
print (mul2_hex)
print("\n mul3_hex")
print (mul3_hex)
print("\n mul9_hex")
print (mul9_hex)
print("\n mul2_hex")
print (mul2_hex)
print("\n mul3_hex")
print (mul3_hex)
print("\n mul9_hex")
print (mul9_hex)
print("\n mul11_hex")
print (mul11_hex)
print("\n mul13_hex")
print (mul13_hex)
print("\n mul14_hex")
print (mul14_hex)


sbox = generate_sbox()
inv_sbox = generate_inv_sbox(sbox)
# Print the S-box
print("S-box:")
for i in range(0, 256, 16):
 print(', '.join(f'0x{b:02x}' for b in sbox[i:i+16]))
# Print the Inverse S-box
print("\nInverse S-box:")
for i in range(0, 256, 16):
 print(', '.join(f'0x{b:02x}' for b in inv_sbox[i:i+16]))

print("RCON")
print(rcon_hex)




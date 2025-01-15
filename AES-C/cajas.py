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


def inv(x):
    for i in range(256):
        if gmul(x, i) == 1:
            return i
    return 0


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

sbox = generate_sbox()
inv_sbox = generate_inv_sbox(sbox)
# Print the S-box
print("S-box22:")
for i in range(0, 256, 16):
    print(', '.join(f'0x{b:02x}' for b in sbox[i:i+16]))
# Print the Inverse S-box
print("\nInverse S-box:")
for i in range(0, 256, 16):
    print(', '.join(f'0x{b:02x}' for b in inv_sbox[i:i+16]))


def gf_multiply(a, b, poly_red=0x17B):
    """
    Función para multiplicar dos números en GF(2^8) con reducción de polinomio.
    """
    result = 0
    for _ in range(8):
        if b & 1:
            result ^= a
        high_bit_set = a & 0x80
        a <<= 1
        if high_bit_set:
            a ^= poly_red  # Aplicar la reducción si hay desbordamiento
        a &= 0xFF  # Mantener 'a' en 8 bits
        b >>= 1
    return result


def calculate_rcon(poly_red=0x17B):
    """
    Calcula la matriz Rcon para AES utilizando el polinomio de reducción proporcionado.
    """
    rcon = [0] * 256
    rcon[1] = 0x01  # x^0
    for i in range(2, 256):
        rcon[i] = gf_multiply(rcon[i - 1], 0x02, poly_red)
    # Convertir cada elemento a formato hexadecimal
    return [f"0x{x:02X}" for x in rcon]


# Ejemplo de uso con el polinomio 0x17B
rcon_hex = calculate_rcon(0x17B)

# Mostrar la matriz Rcon en formato hexadecimal
print("RCON:")
for i in range(0, 256, 16):
    print(", ".join(rcon_hex[i:i + 16]))


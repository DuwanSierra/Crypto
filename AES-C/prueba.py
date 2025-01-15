def gmul(a, b, poly=0xE7):
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
    c = 0x63 # Constant for the affine transformation
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
print("S-box:")
for i in range(0, 256, 16):
    print(', '.join(f'0x{b:02x}' for b in sbox[i:i+16]))
# Print the Inverse S-box
print("\nInverse S-box:")
for i in range(0, 256, 16):
    print(', '.join(f'0x{b:02x}' for b in inv_sbox[i:i+16]))
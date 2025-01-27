def is_prime(n):
    """Verifica si un número es primo."""
    if n < 2:
        return False
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            return False
    return True

def find_primitive_root(q):
    """Encuentra una raíz primitiva de un número primo q."""
    if not is_prime(q):
        raise ValueError(f"{q} no es un número primo.")

    # Calculamos los factores de phi = q - 1
    phi = q - 1
    factors = []
    n = phi
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            factors.append(i)
            while n % i == 0:
                n //= i
    if n > 1:
        factors.append(n)

    # Verificamos candidatos para raíz primitiva
    for g in range(2, q):
        is_primitive = True
        for factor in factors:
            if pow(g, phi // factor, q) == 1:
                is_primitive = False
                break
        if is_primitive:
            return g
    return -1  # Si no se encuentra una raíz primitiva

def word_to_ascii(word):
    """Convierte una palabra a su valor concatenado en ASCII."""
    return int("".join(str(ord(char)) for char in word))

def verify_word(word, q):
    """Verifica si la representación ASCII de la palabra es menor que q."""
    ascii_value = word_to_ascii(word)
    return ascii_value, ascii_value < q

# Parámetros
q = 4294967311  # Número primo proporcionado
word = "REAL"  # Palabra de ejemplo

# Cálculo
try:
    print(f"Número primo: {q}")
    primitive_root = find_primitive_root(q)
    print(f"Raíz primitiva de {q}: {primitive_root}")

    ascii_value, is_valid = verify_word(word, q)
    print(f"Valor ASCII de '{word}': {ascii_value}")
    print(f"¿El valor generado ({ascii_value}) es menor que {q}? {'Sí' if is_valid else 'No'}")
except Exception as e:
    print(f"Error: {e}")

import random

def is_prime(n):
    """Verifica si un número es primo."""
    if n < 2:
        return False
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            return False
    return True

def find_primitive_roots(q):
    """Encuentra todas las raíces primitivas de q."""
    phi = q - 1
    factors = []

    # Factorización de phi
    n = phi
    for i in range(2, int(n**0.5) + 1):
        if n % i == 0:
            factors.append(i)
            while n % i == 0:
                n //= i
    if n > 1:
        factors.append(n)

    primitive_roots = []
    for g in range(2, q):
        is_primitive = True
        for factor in factors:
            if pow(g, phi // factor, q) == 1:
                is_primitive = False
                break
        if is_primitive:
            primitive_roots.append(g)
    return primitive_roots

def elgamal_key_generation(q, alpha):
    """Genera claves pública y privada para ElGamal."""
    private_key = random.randint(2, q - 1)  # Clave privada
    public_key = pow(alpha, private_key, q)  # Clave pública
    return private_key, public_key

def elgamal_shared_key(other_public_key, private_key, q):
    """Calcula la clave compartida."""
    return pow(other_public_key, private_key, q)

def activity_1(q, alpha):
    """Actividad 1: Intercambio de claves usando ElGamal."""
    print("\n--- Actividad 1: Intercambio de claves ---")
    
    # Generar claves para Ana
    private_key_ana, public_key_ana = elgamal_key_generation(q, alpha)
    print(f"Ana - Clave privada: {private_key_ana}, Clave pública: {public_key_ana}")

    # Generar claves para Bob
    private_key_bob, public_key_bob = elgamal_key_generation(q, alpha)
    print(f"Bob - Clave privada: {private_key_bob}, Clave pública: {public_key_bob}")

    # Calcular clave compartida
    shared_key_ana = elgamal_shared_key(public_key_bob, private_key_ana, q)
    shared_key_bob = elgamal_shared_key(public_key_ana, private_key_bob, q)

    print(f"Clave compartida (calculada por Ana): {shared_key_ana}")
    print(f"Clave compartida (calculada por Bob): {shared_key_bob}")

def activity_2(q, alpha):
    """Actividad 2: Simulación de un ataque Man-in-the-Middle (MITM)."""
    print("\n--- Actividad 2: Ataque Man-in-the-Middle ---")

    # Generar claves para Ana
    private_key_ana, public_key_ana = elgamal_key_generation(q, alpha)
    print(f"Ana - Clave privada: {private_key_ana}, Clave pública: {public_key_ana}")

    # Generar claves para Bob
    private_key_bob, public_key_bob = elgamal_key_generation(q, alpha)
    print(f"Bob - Clave privada: {private_key_bob}, Clave pública: {public_key_bob}")

    # Atacante genera su propia clave
    private_key_attacker, public_key_attacker = elgamal_key_generation(q, alpha)
    print(f"Atacante - Clave privada: {private_key_attacker}, Clave pública: {public_key_attacker}")

    # Ana y el atacante calculan una clave compartida
    shared_key_ana_attacker = elgamal_shared_key(public_key_attacker, private_key_ana, q)
    shared_key_attacker_ana = elgamal_shared_key(public_key_ana, private_key_attacker, q)
    print(f"Clave compartida (Ana-Attacker): {shared_key_ana_attacker}")

    # Bob y el atacante calculan una clave compartida
    shared_key_bob_attacker = elgamal_shared_key(public_key_attacker, private_key_bob, q)
    shared_key_attacker_bob = elgamal_shared_key(public_key_bob, private_key_attacker, q)
    print(f"Clave compartida (Bob-Attacker): {shared_key_bob_attacker}")

    print("Atacante ahora puede leer y falsear mensajes entre Ana y Bob.")

# Parámetros
q = 65537  # Número primo grande
roots = find_primitive_roots(q)  # Encontrar raíces primitivas
alpha = roots[0]  # Usar la primera raíz primitiva

# Mostrar primeras 10 raíces primitivas
print(f"Primeras 10 raíces primitivas de {q}: {roots[:10]}")

# Ejecutar actividades
activity_1(q, alpha)
activity_2(q, alpha)

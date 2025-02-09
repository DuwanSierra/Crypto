import hashlib
import time
import json
import rsa
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import os

'''
Implementa un sistema de contratos inteligentes usando blockchain
simplificada, con funcionaes de cifrado AES, firmas digitales RSA y prueba de trabajo PoW.
'''
class SmartContract:


    def __init__(self):
        self.chain = [] #Lista que almacenará los bloques.  
        self.aes_key = os.urandom(16)  # Genración clave AES de 128 bits
        self.public_key, self.private_key = rsa.newkeys(512) #Par de claves RSA (publica y privada)
        self.create_genesis_block() #Crea el primer bloque de la blockchain

    '''
    Bloque Inicial
    Se usa una función de mineria para encontrar un "nonce"
    Se genera un hash único para el bloque
    '''
    def create_genesis_block(self):
        block = {
            "index": 0,
            "previous_hash": "0" * 128, #Hash anterior vacio (Porque es el primero)
            "timestamp": time.time(),
            "hash_code": self.hash_code("Genesis Block"), #Hash del contenido
            "verification_list": [],
            "observations": [],
        }
        block["nonce"], block["proof_of_work"] = self.mine_nonce(block) #Resuelve PoW
        block["hash"] = self.hash_block(block) #Calcula hash del bloque
        self.chain.append(block) #Lo agrega a la blockchain

    '''
    Calcula hash SHA-256
    Convierte cualquier dato en un hash SHA-256
    '''
    def hash_code(self, code):
        return hashlib.sha256(code.encode()).hexdigest()

    '''
    Calcula hash del bloque
    Se usa SHA-512 para mayor seguridad. 
    '''
    def hash_block(self, block):
        block_str = json.dumps(block, sort_keys=True).encode() #Convierte a Json ordenado
        return hashlib.sha512(block_str).hexdigest() #Hash SHA-512 del bloque

    '''
    Implementa prueba de trabajo PoW
    Encuentra un nonce tal que el hash MD5 empiece con 00
    Prueba de trabajo para validar los bloques
    '''
    def mine_nonce(self, block):
        nonce = 0
        while True:
            block_str = f"{nonce}{block['hash_code']}{block['timestamp']}"
            hash_attempt = hashlib.md5(block_str.encode()).hexdigest()
            if hash_attempt[:2] == "00": #Condición de dificultad: el hash debe empezar con "00"
                return nonce, hash_attempt
            nonce += 1

    '''
    Cifra una transacción con AES
    Usa AES en modo CBC
    Rellena los datos para sean multiplos del bloque de 16 bytes
    Concatena IV + datos cifrados para poder descifrarlos despues.
    '''
    def encrypt_transaction(self, data):
        cipher = AES.new(self.aes_key, AES.MODE_CBC)
        encrypted_data = cipher.encrypt(pad(data.encode(), AES.block_size))
        return cipher.iv + encrypted_data

    '''
    Descifra datos AES
    Extrae el IV y usa la clave AES para descifrar.
    '''
    def decrypt_transaction(self, encrypted_data):
        iv = encrypted_data[:AES.block_size] #Extrae IV
        cipher = AES.new(self.aes_key, AES.MODE_CBC, iv)
        return unpad(cipher.decrypt(encrypted_data[AES.block_size:]), AES.block_size).decode()


    '''
    Firma digital con RSA
    Calcula el hash MD5 del mensaje
    Firma con la clave privada RSA
    Devuelve la firma en formato hexadecimal
    '''
    def sign_observation(self, observation):
        hash_obs = hashlib.md5(observation.encode()).hexdigest() #Hash de la observación
        signature = rsa.sign(hash_obs.encode(), self.private_key, "MD5") #Firma con RSA 
        return signature.hex()

    '''
    Verifica la firma digital usando la clave publica RSA.
    Devulve true si la firma es valida, false si no.
    '''
    def verify_signature(self, observation, signature):
        hash_obs = hashlib.md5(observation.encode()).hexdigest()
        try:
            rsa.verify(hash_obs.encode(), bytes.fromhex(signature), self.public_key)
            return True
        except rsa.VerificationError:
            return False


    '''
    Agrega un nuevo bloque
    Cada bloque se enlaza con el anterior usando previouss_hash
    Cada observación es firmada digitalmente.
    Se realiza PoW para validar el bloque antes de añadirlo.
    '''
    def add_block(self, observations, verification_list):
        last_block = self.chain[-1]
        block = {
            "index": len(self.chain),
            "previous_hash": last_block["hash"], #Enlaza con el bloque anterior
            "timestamp": time.time(),
            "hash_code": self.hash_code(str(verification_list)),
            "verification_list": verification_list,
            "observations": [(obs, self.sign_observation(obs)) for obs in observations],#Firma cada observación
        }
        block["nonce"], block["proof_of_work"] = self.mine_nonce(block) #Resuelve PoW
        block["hash"] = self.hash_block(block)#Genera hash del bloque
        self.chain.append(block)

    '''
    Muestra la blockchain en formato Json
    '''
    def display_chain(self):
        for block in self.chain:
            print(json.dumps(block, indent=4))

# Pruebas
contract = SmartContract() #Crea blockchain con bloque génesis 
contract.add_block([
    "Requerimientos aprobados",
    "Primer prototipo aprobado"
], ["Requerimientos", "Prototipo 1"]) #Agrega un nuevo bloque
contract.display_chain() #Muestra la blockchain

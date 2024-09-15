const DICTIONARY = 'ABCDEFGHIJKLMNÑOPQRSTUVWXYZ12345';
const DICTIONARY_LENGTH = DICTIONARY.length; // 32 caracteres

// Genera un Uint8Array con los índices de los caracteres en el diccionario.
const CONST_ARR = new Uint8Array(DICTIONARY_LENGTH).map((_, index) => index);

// Convierte un número en su representación binaria de 5 bits.
function to5BitBinary(num: number): string {
  return num.toString(2).padStart(5, '0');
}

function validateKey(key: string) {
  if (key.length < 4 || key.length > 16) {
    throw new Error('La clave debe tener entre 4 y 16 caracteres.');
  }

  for (const char of key) {
    if (!DICTIONARY.includes(char)) {
      throw new Error(`La clave contiene un carácter inválido: ${char}`);
    }
  }
}

// Convierte un texto a un array de índices numéricos dentro del diccionario.
function convert(text: string) {
  const codes: Uint8Array = new Uint8Array(text.length);

  for (let i = 0; i < text.length; i++) {
    const index = DICTIONARY.indexOf(text[i]);
    if (index === -1) {
      throw new Error(`El carácter ${text[i]} no está en el diccionario`);
    }
    codes[i] = index;
  }

  return codes;
}

// Key setup para inicializar el estado de RC4
function keySetup(_key: string) {
  validateKey(_key);
  const K = new Uint8Array(CONST_ARR); // Crea una copia de CONST_ARR
  let j = 0;
  const key = convert(_key);

  for (let i = 0; i < DICTIONARY_LENGTH; i++) {
    j = (j + K[i] + key[i % key.length]) % DICTIONARY_LENGTH;
    [K[i], K[j]] = [K[j], K[i]];
  }

  return K;
}

// Generador de flujo de bytes con Uint8Array
function* byteStreamGenerator(K: Uint8Array): Generator<number> {
  let i = 0;
  let j = 0;

  while (true) {
    i = (i + 1) % DICTIONARY_LENGTH;
    j = (j + K[i]) % DICTIONARY_LENGTH;
    [K[i], K[j]] = [K[j], K[i]];
    yield (K[(K[i] + K[j]) % DICTIONARY_LENGTH]);
  }
}

class RC4Class {
  privateKey: Uint8Array;

  constructor(key: string) {
    validateKey(key);
    this.privateKey = keySetup(key);
  }

  // Método para cifrar el mensaje con registro en informe
  encrypt(input: string) {
    let outputText = '';
    const byteStream = byteStreamGenerator(new Uint8Array(this.privateKey)); // copia de la clave privada
    const binaryMessage = [];
    const binaryKeyStream: string[] = [];
    const binaryXOR = [];

    for (let i = 0; i < input.length; i++) {
      const charIndex = DICTIONARY.indexOf(input[i]);
      if (charIndex === -1) throw new Error(`Carácter inválido en el mensaje: ${input[i]}`);
      
      const keyStreamByte = byteStream.next().value!;
      const encryptedChar = (charIndex ^ keyStreamByte) % DICTIONARY_LENGTH;

      // Códigos binarios en 5 bits
      binaryMessage.push(to5BitBinary(charIndex));
      binaryKeyStream.push(to5BitBinary(keyStreamByte));
      binaryXOR.push(to5BitBinary(encryptedChar));

      outputText += DICTIONARY[encryptedChar];
    }

    const countZeros = binaryKeyStream.join('').split('0').length - 1;
    const countOnes = binaryKeyStream.join('').split('1').length - 1;

    // Registro en el informe
    console.log("Mensaje en binario (5 bits por carácter):", binaryMessage.join(' '));
    console.log("KeyStream en binario (5 bits por carácter):", binaryKeyStream.join(' '));
    console.log("Número de ceros en el KeyStream:", countZeros);
    console.log("Número de unos en el KeyStream:", countOnes);
    console.log("XOR en binario (mensaje ^ KeyStream):", binaryXOR.join(' '));
    console.log("Mensaje cifrado en el diccionario:", outputText);

    return outputText;
  }

  // Método para descifrar el mensaje
  decrypt(input: string) {
    let outputText = '';
    const byteStream = byteStreamGenerator(new Uint8Array(this.privateKey)); // copia de la clave privada

    for (let i = 0; i < input.length; i++) {
      const charIndex = DICTIONARY.indexOf(input[i]);
      if (charIndex === -1) throw new Error(`Carácter inválido en el mensaje cifrado: ${input[i]}`);
      const decryptedChar = (charIndex ^ byteStream.next().value!) % DICTIONARY_LENGTH;
      outputText += DICTIONARY[decryptedChar];
    }

    return outputText;
  }
}

// Ejemplo de uso
const key = "CLAVE123";
const message = "MENSAJEDEPRUEBARC4PARACRIPTOLOGIA";
const rc4 = new RC4Class(key);

const encrypted = rc4.encrypt(message);

const decrypted = rc4.decrypt(encrypted);

const key2 = "PEDRO";
const rc42 = new RC4Class(key2);

const encrypted2 = rc42.encrypt(message);

const decrypted2 = rc42.decrypt(encrypted2);
const decrypted3 = rc4.decrypt(encrypted2);
console.log("Texto descifrado clave propia:", decrypted2);
console.log("Texto descifrado clave original:", decrypted);
console.log("Texto descifrado clave original pero cambiando el decrypted al 2:", decrypted3);

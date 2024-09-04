class Salsa20 {
  private secretKey: Uint8Array;
  private encryptedMessage: Uint8Array | null;
  private roundsMatrices: Int32Array[] = []; // Array para almacenar las matrices de cada iteración

  constructor() {
    this.secretKey = new Uint8Array(64);
    this.encryptedMessage = null;
  }

  public encryptedMessageAsString(): string {
    if (this.encryptedMessage === null) {
      throw new Error("Encrypted message is null");
    }
    return new TextDecoder("ISO-8859-1").decode(this.encryptedMessage);
  }

  public getEncryptedMessage(): Uint8Array | null {
    return this.encryptedMessage;
  }

  public salsa20EncryptionFunction(
    key: Uint8Array,
    nonce: Uint8Array,
    originalMessage: Uint8Array
  ): void {
    if (
      nonce.length === 8 &&
      (key.length === 16 || key.length === 32) &&
      originalMessage.length > 0
    ) {
      this.encryptedMessage = new Uint8Array(originalMessage.length);
      let flag = false;
      let i = 0;
      const nonceSecondPart = new Uint8Array(8);

      while (true) {
        if (i >= originalMessage.length) break;

        const fullNonce = new Uint8Array(16);
        fullNonce.set(nonce);
        fullNonce.set(nonceSecondPart, 8);

        this.salsa20ExpansionFunction(key, fullNonce);

        for (let j = 0; j < 64; j++) {
          if (i + j >= originalMessage.length) {
            flag = true;
            break;
          }
          this.encryptedMessage[i + j] =
            this.secretKey[j] ^ originalMessage[i + j];
        }

        if (flag) break;

        this.incrementNonce(nonceSecondPart);
        i += 64;
      }
    } else {
      console.error(
        "Invalid input parameters: nonce length must be 8, key length must be 16 or 32, and message must not be empty."
      );
      this.encryptedMessage = null;
    }
  }

  private salsa20ExpansionFunction(key: Uint8Array, nonce: Uint8Array): void {
    const sigma = new Uint8Array([
      101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107,
    ]);
    const kLength = key.length;

    this.secretKey.set(sigma.subarray(0, 4), 0);
    this.secretKey.set(key.subarray(0, 16), 4);
    this.secretKey.set(sigma.subarray(4, 8), 20);
    this.secretKey.set(nonce, 24);
    this.secretKey.set(sigma.subarray(8, 12), 40);
    this.secretKey.set(key.subarray(kLength - 16, kLength), 44);
    this.secretKey.set(sigma.subarray(12, 16), 60);

    this.salsa20HashFunction(this.secretKey);
  }

  private salsa20HashFunction(x: Uint8Array): void {
    const y = new Int32Array(16);
    const z = new Int32Array(16);

    for (let i = 0; i < 16; i++) {
      y[i] = this.littleEndian(x.subarray(4 * i, 4 * i + 4));
      z[i] = y[i];
    }

    this.saveMatrix(z, "Initial state (z)");

    for (let i = 0; i < 10; i++) {
      this.doubleRound(z);
      this.saveMatrix(z, `State after Double Round ${i + 1}`);
    }

    for (let i = 0; i < 16; i++) {
      z[i] = (z[i] + y[i]) | 0;
    }

    for (let i = 0; i < 16; i++) {
      x.set(this.inverseLittleEndian(z[i]), 4 * i);
    }

    this.saveMatrix(new Int32Array(x.buffer), "Final hashed key");
  }

  private doubleRound(x: Int32Array): void {
    this.columnRound(x);
    this.rowRound(x);
  }

  private columnRound(x: Int32Array): void {
    this.quarterRound([x[0], x[4], x[8], x[12]]);
    this.quarterRound([x[1], x[5], x[9], x[13]]);
    this.quarterRound([x[2], x[6], x[10], x[14]]);
    this.quarterRound([x[3], x[7], x[11], x[15]]);
  }

  private rowRound(x: Int32Array): void {
    this.quarterRound([x[0], x[1], x[2], x[3]]);
    this.quarterRound([x[5], x[6], x[7], x[4]]);
    this.quarterRound([x[10], x[11], x[8], x[9]]);
    this.quarterRound([x[15], x[12], x[13], x[14]]);
  }

  private quarterRound(y: number[]): void {
    y[1] ^= this.rotateLeft((y[0] + y[3]) | 0, 7);
    y[2] ^= this.rotateLeft((y[1] + y[0]) | 0, 9);
    y[3] ^= this.rotateLeft((y[2] + y[1]) | 0, 13);
    y[0] ^= this.rotateLeft((y[3] + y[2]) | 0, 18);
  }

  private rotateLeft(value: number, shift: number): number {
    return (value << shift) | (value >>> (32 - shift));
  }

  private littleEndian(b: Uint8Array): number {
    return b[0] | (b[1] << 8) | (b[2] << 16) | (b[3] << 24);
  }

  private inverseLittleEndian(y: number): Uint8Array {
    return new Uint8Array([
      y & 0xff,
      (y >> 8) & 0xff,
      (y >> 16) & 0xff,
      (y >> 24) & 0xff,
    ]);
  }

  private incrementNonce(nonce: Uint8Array): void {
    for (let i = 0; i < nonce.length; i++) {
      nonce[i]++;
      if (nonce[i] !== 0) break;
    }
  }

  private saveMatrix(state: Int32Array, label: string): void {
    console.log(label);
    this.printStateAsHexMatrix(state);
    this.roundsMatrices.push(state.slice()); // Corregido: Usamos .slice() para copiar la matriz
  }

  private printStateAsHexMatrix(state: Int32Array): void {
    for (let i = 0; i < 4; i++) {
      console.log(
        state[4 * i].toString(16).padStart(8, "0"),
        state[4 * i + 1].toString(16).padStart(8, "0"),
        state[4 * i + 2].toString(16).padStart(8, "0"),
        state[4 * i + 3].toString(16).padStart(8, "0")
      );
    }
  }

  public getMatrices(): Int32Array[] {
    return this.roundsMatrices;
  }
}

// Función para comparar las matrices generadas
const compareMatrices = (matrices: Int32Array[]) => {
  for (let round = 0; round < matrices.length - 1; round++) {
    console.log(`Comparando Ronda ${round + 1} con Ronda ${round + 2}:`);
    for (let i = 0; i < 16; i++) {
      if (matrices[round][i] !== matrices[round + 1][i]) {
        console.log(
          `Diferencia en posición [${Math.floor(i / 4)}, ${i % 4}]: R${
            round + 1
          } = ${matrices[round][i].toString(16)}, R${round + 2} = ${matrices[
            round + 1
          ][i].toString(16)}`
        );
      }
    }
  }
};

// Función para ejecutar el test con múltiples claves y nonces
const executeTestMultipleKeysNonces = (
  message: string,
  tuplas: {
    key: string;
    nonce: string;
  }[]
) => {
  for (let i = 0; i < tuplas.length; i++) {
    const { key, nonce } = tuplas[i];
    const salsa20 = new Salsa20();
    console.log(`[ITERACIÓN ${i + 1}] - Clave: ${key} - Nonce: ${nonce}`);

    // Ajustar claves y nonces
    const keyGenerated = adjustStringToSize(key, 16); // 16 bytes
    const nonceGenerated = adjustStringToSize(nonce, 8); // 8 bytes

    // Mensaje de ejemplo
    const originalMessage = stringToUint8Array(message);

    // Cifrar el mensaje con cada combinación de clave y nonce
    salsa20.salsa20EncryptionFunction(
      keyGenerated,
      nonceGenerated,
      originalMessage
    );

    // Mostrar mensaje cifrado
    console.log("Mensaje cifrado:", salsa20.encryptedMessageAsString());

  }
};

// Funciones auxiliares
function adjustStringToSize(str: string, size: number): Uint8Array {
  const arr = new Uint8Array(size);
  const strBytes = new TextEncoder().encode(str);

  // Copiar tantos bytes como sea posible
  for (let i = 0; i < size; i++) {
    arr[i] = strBytes[i % strBytes.length];
  }

  return arr;
}

function stringToUint8Array(str: string): Uint8Array {
  const encoder = new TextEncoder();
  return encoder.encode(str);
}

// Prueba con múltiples claves y nonces
const message = "Este es un mensaje de prueba para los algoritmos SALSA20 y CHACHA21";
const tuplas = [
  {
    key: "0123456789abcdef0123456789abcdef",
    nonce: "00000000",
  },
  {
    key: "0123456789abcdef0123456789abcdef",
    nonce: "12345678",
  },
  {
    key: "1123456789abcdef0123456789abcdef",
    nonce: "00000000",
  },
  {
    key: "1123456789abcdef0123456789abcdef",
    nonce: "12345678",
  },
];

executeTestMultipleKeysNonces(message, tuplas);

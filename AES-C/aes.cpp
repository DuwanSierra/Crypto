#include <iostream>
#include <cstring>
#include <fstream>
#include <sstream>
#include <cstdlib>
#include <iomanip>
#include "structures.h"
#include <vector>
#include <bitset>

using namespace std;

// Funci�n para convertir texto a hexadecimal
string textToHex(const string& text) {
    stringstream ss;
    for (size_t i = 0; i < text.length(); ++i) {
        ss << hex << setw(2) << setfill('0') << (int)(unsigned char)text[i];
    }
    return ss.str();
}

// Funci�n para convertir hexadecimal a texto
string hexToText(const string& hex) {
    string text;
    for (size_t i = 0; i < hex.length(); i += 2) {
        string byteString = hex.substr(i, 2);
        char byte = (char)(int)strtol(byteString.c_str(), NULL, 16);
        text += byte;
    }
    return text;
}


// Realiza la operaciòn XOR entre el estado y una clave de ronda
void AddRoundKey(unsigned char* state, unsigned char* roundKey) {
    for (int i = 0; i < 16; i++) {
        state[i] ^= roundKey[i];
    }
}

// Aplica la sustitución de bytes utilizando la caja S de AES
void SubBytes(unsigned char* state) {
    for (int i = 0; i < 16; i++) {
        state[i] = s[state[i]];
    }
}


// Realiza la operación de desplamiento de filas
void ShiftRows(unsigned char* state) {
    unsigned char tmp[16];

    tmp[0] = state[0]; // No se mueve
    tmp[1] = state[5]; // Se desplaza a la izquierda
    tmp[2] = state[10]; // se deplaza dos posiciones a la izquierda
    tmp[3] = state[15]; //se desplaza tres posiciones a la izquierda 

    tmp[4] = state[4];
    tmp[5] = state[9];
    tmp[6] = state[14];
    tmp[7] = state[3];

    tmp[8] = state[8];
    tmp[9] = state[13];
    tmp[10] = state[2];
    tmp[11] = state[7];

    tmp[12] = state[12];
    tmp[13] = state[1];
    tmp[14] = state[6];
    tmp[15] = state[11];

// copiar el resultado de vuelta al estado
    for (int i = 0; i < 16; i++) {
        state[i] = tmp[i];
    }
}

//Aplica una mezcla de columnas que combina los bytes de cada columna con ciertas constantes
void MixColumns(unsigned char* state) {
    unsigned char tmp[16];

    // Mezcla de la primera comunla bytes 0,1,2,3
    tmp[0] = (unsigned char)mul2[state[0]] ^ mul3[state[1]] ^ state[2] ^ state[3];
    tmp[1] = (unsigned char)state[0] ^ mul2[state[1]] ^ mul3[state[2]] ^ state[3];
    tmp[2] = (unsigned char)state[0] ^ state[1] ^ mul2[state[2]] ^ mul3[state[3]];
    tmp[3] = (unsigned char)mul3[state[0]] ^ state[1] ^ state[2] ^ mul2[state[3]];

// Mezcla de la segunda columna bytes 4,5,6,7
    tmp[4] = (unsigned char)mul2[state[4]] ^ mul3[state[5]] ^ state[6] ^ state[7];
    tmp[5] = (unsigned char)state[4] ^ mul2[state[5]] ^ mul3[state[6]] ^ state[7];
    tmp[6] = (unsigned char)state[4] ^ state[5] ^ mul2[state[6]] ^ mul3[state[7]];
    tmp[7] = (unsigned char)mul3[state[4]] ^ state[5] ^ state[6] ^ mul2[state[7]];

//Mezcla de la tercera columna 8,9,10,11
    tmp[8] = (unsigned char)mul2[state[8]] ^ mul3[state[9]] ^ state[10] ^ state[11];
    tmp[9] = (unsigned char)state[8] ^ mul2[state[9]] ^ mul3[state[10]] ^ state[11];
    tmp[10] = (unsigned char)state[8] ^ state[9] ^ mul2[state[10]] ^ mul3[state[11]];
    tmp[11] = (unsigned char)mul3[state[8]] ^ state[9] ^ state[10] ^ mul2[state[11]];

// Mezcla de la cuarta columna 12, 13, 14, 15
    tmp[12] = (unsigned char)mul2[state[12]] ^ mul3[state[13]] ^ state[14] ^ state[15];
    tmp[13] = (unsigned char)state[12] ^ mul2[state[13]] ^ mul3[state[14]] ^ state[15];
    tmp[14] = (unsigned char)state[12] ^ state[13] ^ mul2[state[14]] ^ mul3[state[15]];
    tmp[15] = (unsigned char)mul3[state[12]] ^ state[13] ^ state[14] ^ mul2[state[15]];

//Copiar el resultado de vuelta al estado
    for (int i = 0; i < 16; i++) {
        state[i] = tmp[i];
    }
}

//Genera una ronda
//Aplicando la sustitucion
//El desplazamiento de filas 
// y por ultimo la operación XOR entre el estado y la clave

void Round(unsigned char* state, unsigned char* key) {
    SubBytes(state);
    ShiftRows(state);
    MixColumns(state);
    AddRoundKey(state, key);
}

/*
La ronda final se realiza sin realizar el mixColumns, es decir,
sin mezclar las columnas con las constantes
*/
void FinalRound(unsigned char* state, unsigned char* key) {
    SubBytes(state);
    ShiftRows(state);
    AddRoundKey(state, key);
}

/*
Implemente el codigo de cifrado completo
La clave se expande a una serie de claves de ronda utilizando la funcion KeyExpansion
El mensaje se divide en 16Bytes y para cada bloque se aplica el proceso de cifrado llamando al metodo round
Una vez finalizado en la ultima ronda se llama al metodo FinalRound (Sin mixColumns)
*/
void AESEncrypt(unsigned char* message, unsigned char* expandedKey, unsigned char* encryptedMessage) {
    unsigned char state[16];

    for (int i = 0; i < 16; i++) {
        state[i] = message[i];
    }

    int numberOfRounds = 9;

    AddRoundKey(state, expandedKey);

    for (int i = 0; i < numberOfRounds; i++) {
        Round(state, expandedKey + (16 * (i + 1)));
    }

    FinalRound(state, expandedKey + 160);

    for (int i = 0; i < 16; i++) {
        encryptedMessage[i] = state[i];
    }
}

/*
Aplica una operación XOR con la clave de la ronda.
*/
void SubRoundKey(unsigned char* state, unsigned char* roundKey) {
    for (int i = 0; i < 16; i++) {
        state[i] ^= roundKey[i];
    }
}

/*
Utiliza las tablas definidas para multiplicar los bytes por constantes inversamente a como se hizo en el cifrado.
*/
void InverseMixColumns(unsigned char* state) {
    unsigned char tmp[16];

// Inversa de la mezcla de la primera columna 0,1,2,3
    tmp[0] = (unsigned char)mul14[state[0]] ^ mul11[state[1]] ^ mul13[state[2]] ^ mul9[state[3]];
    tmp[1] = (unsigned char)mul9[state[0]] ^ mul14[state[1]] ^ mul11[state[2]] ^ mul13[state[3]];
    tmp[2] = (unsigned char)mul13[state[0]] ^ mul9[state[1]] ^ mul14[state[2]] ^ mul11[state[3]];
    tmp[3] = (unsigned char)mul11[state[0]] ^ mul13[state[1]] ^ mul9[state[2]] ^ mul14[state[3]];

//Inversa de la mezcla de la segunda columna bytes 4,5,6,7
    tmp[4] = (unsigned char)mul14[state[4]] ^ mul11[state[5]] ^ mul13[state[6]] ^ mul9[state[7]];
    tmp[5] = (unsigned char)mul9[state[4]] ^ mul14[state[5]] ^ mul11[state[6]] ^ mul13[state[7]];
    tmp[6] = (unsigned char)mul13[state[4]] ^ mul9[state[5]] ^ mul14[state[6]] ^ mul11[state[7]];
    tmp[7] = (unsigned char)mul11[state[4]] ^ mul13[state[5]] ^ mul9[state[6]] ^ mul14[state[7]];

//Inversa de la columna tercera
    tmp[8] = (unsigned char)mul14[state[8]] ^ mul11[state[9]] ^ mul13[state[10]] ^ mul9[state[11]];
    tmp[9] = (unsigned char)mul9[state[8]] ^ mul14[state[9]] ^ mul11[state[10]] ^ mul13[state[11]];
    tmp[10] = (unsigned char)mul13[state[8]] ^ mul9[state[9]] ^ mul14[state[10]] ^ mul11[state[11]];
    tmp[11] = (unsigned char)mul11[state[8]] ^ mul13[state[9]] ^ mul9[state[10]] ^ mul14[state[11]];

//Inversa de la columna cuarta
    tmp[12] = (unsigned char)mul14[state[12]] ^ mul11[state[13]] ^ mul13[state[14]] ^ mul9[state[15]];
    tmp[13] = (unsigned char)mul9[state[12]] ^ mul14[state[13]] ^ mul11[state[14]] ^ mul13[state[15]];
    tmp[14] = (unsigned char)mul13[state[12]] ^ mul9[state[13]] ^ mul14[state[14]] ^ mul11[state[15]];
    tmp[15] = (unsigned char)mul11[state[12]] ^ mul13[state[13]] ^ mul9[state[14]] ^ mul14[state[15]];

    for (int i = 0; i < 16; i++) {
        state[i] = tmp[i];
    }
}

/*
Realiza el desplazamiento inverso de filas
*/
void ShiftRowsDecrypt(unsigned char* state) {
    unsigned char tmp[16];

    tmp[0] = state[0]; // No se mueve
    tmp[1] = state[13]; //Se desplaza una posición a la derecha
    tmp[2] = state[10]; // se desplaza dos posiciones a la derecha
    tmp[3] = state[7]; // se desplaza tres posiciones a la derecha

    tmp[4] = state[4];
    tmp[5] = state[1];
    tmp[6] = state[14];
    tmp[7] = state[11];

    tmp[8] = state[8];
    tmp[9] = state[5];
    tmp[10] = state[2];
    tmp[11] = state[15];

    tmp[12] = state[12];
    tmp[13] = state[9];
    tmp[14] = state[6];
    tmp[15] = state[3];

    for (int i = 0; i < 16; i++) {
        state[i] = tmp[i];
    }
}

/*
Aplica la caja s inversa, en lugar de la caja s0
*/
void SubBytesDecrypt(unsigned char* state) {
    for (int i = 0; i < 16; i++) {
        state[i] = inv_s[state[i]];
    }
}

/*
A diferencia del cifrado se llama de segundas al mix columns
luego al shiftrows y por ultimo al Subbytes 
*/
void RoundDecrypt(unsigned char* state, unsigned char* key) {
    SubRoundKey(state, key);
    InverseMixColumns(state);
    ShiftRowsDecrypt(state);
    SubBytesDecrypt(state);
}

/*
La primera ronda no llama a mix columns inversa
*/
void InitialRound(unsigned char* state, unsigned char* key) {
    SubRoundKey(state, key);
    ShiftRowsDecrypt(state);
    SubBytesDecrypt(state);
}

/*
Realiza todo el proceso de descifrado.
El mensaje se divide en 16 bytes y se aplica el proceso de descifrado en cada bloque.
*/
void AESDecrypt(unsigned char* encryptedMessage, unsigned char* expandedKey, unsigned char* decryptedMessage) {
    unsigned char state[16];

    for (int i = 0; i < 16; i++) {
        state[i] = encryptedMessage[i];
    }

    InitialRound(state, expandedKey + 160);

    int numberOfRounds = 9;

    for (int i = 8; i >= 0; i--) {
        RoundDecrypt(state, expandedKey + (16 * (i + 1)));
    }

    SubRoundKey(state, expandedKey);

    for (int i = 0; i < 16; i++) {
        decryptedMessage[i] = state[i];
    }
}

char menu() {
    char choice;
    cout << "Seleccione una opcion:" << endl;
    cout << "1. Encriptar mensaje" << endl;
    cout << "2. Desencriptar mensaje" << endl;
    cout << "3. Cerrar" << endl;
    cout << "Ingrese la opcion: ";
    cin >> choice;
    return choice;
}

int process(char choice) {
    unsigned char key[16];
    int i = 0;
    unsigned int c;

    unsigned char expandedKey[176];

    if (choice == '1' || choice == '2') {
        cout << "Ingrese la clave en hexadecimal separada por espacios (16 valores): ";
        cin.ignore();
        string keystr;
        getline(cin, keystr);

        istringstream hex_chars_stream(keystr);

        while (hex_chars_stream >> hex >> c && i < 16) {
            key[i] = c;
            i++;
        }

        if (i != 16) {
            cout << "La clave debe contener exactamente 16 valores hexadecimales." << endl;
            return 1;
        }

        KeyExpansion(key, expandedKey);
    } else {
        return 2;
    }

    if (choice == '1') {
        char msgChoice;
        cout << "�Desea ingresar el mensaje en texto (t) o en hexadecimal (h)? ";
        cin >> msgChoice;
        cin.ignore();

        string message;
        if (msgChoice == 't' || msgChoice == 'T') {
            cout << "Ingrese el mensaje a encriptar: ";
            getline(cin, message);
        } else if (msgChoice == 'h' || msgChoice == 'H') {
            cout << "Ingrese el mensaje en hexadecimal: ";
            getline(cin, message);
            message = hexToText(message);
            cout << "Mensaje Hex a texto "+ message;
        } else {
            cout << "Opci�n no v�lida." << endl;
            return 1;
        }

        vector<unsigned char> messageVec(message.begin(), message.end());
        int originalLen = messageVec.size();
        int padding = 16 - (originalLen % 16);
        int paddedMessageLen = originalLen + padding;

        for (int i = 0; i < padding; i++) {
            messageVec.push_back(padding);
        }

        unsigned char* paddedMessage = messageVec.data();
        unsigned char* encryptedMessage = new unsigned char[paddedMessageLen];

        for (int i = 0; i < paddedMessageLen; i += 16) {
            AESEncrypt(paddedMessage + i, expandedKey, encryptedMessage + i);
        }

        cout << "\nMensaje encriptado en caracteres: " << endl;
        for (int i = 0; i < paddedMessageLen; i++) {
            cout << static_cast<char>(encryptedMessage[i]);
        }
        cout << endl;

        cout << "\nMensaje encriptado en binario: " << endl;
        for (int i = 0; i < paddedMessageLen; i++) {
            cout << bitset<8>(encryptedMessage[i]) << " ";
        }
        cout << endl;

        cout << "\nMensaje encriptado en hexa: " << endl;
        for (int i = 0; i < paddedMessageLen; i++) {
            cout << hex << (int)encryptedMessage[i] << " ";
        }
        cout << endl;

        delete[] encryptedMessage;
        return 1;
    } else if (choice == '2') {
        cout << "Ingrese el mensaje en hexadecimal separado por espacios: ";
        string msgstr;
        getline(cin, msgstr);

        istringstream hex_chars_stream(msgstr);
        vector<unsigned char> encryptedMessageVec;
        while (hex_chars_stream >> hex >> c) {
            encryptedMessageVec.push_back(static_cast<unsigned char>(c));
        }

        unsigned char* encryptedMessage = encryptedMessageVec.data();
        int encryptedMessageLen = encryptedMessageVec.size();
        unsigned char* decryptedMessage = new unsigned char[encryptedMessageLen];

        for (int block = 0; block < encryptedMessageLen / 16; block++) {
            AESDecrypt(encryptedMessage + (block * 16), expandedKey, decryptedMessage + (block * 16));
        }

        int padding = decryptedMessage[encryptedMessageLen - 1];
        int messageLength = encryptedMessageLen - padding;

        cout << "Mensaje desencriptado:" << endl;
        for (int i = 0; i < messageLength; i++) {
            cout << decryptedMessage[i];
        }
        cout << endl;

        delete[] decryptedMessage;
        return 1;
    }
    return 2;
}

int main() {
    cout << "                       AES-128                        " << endl;;
    cout << "    		            CODIGOS                        " << endl;
    cout << "    	20231678001 - 20232678020 - 20232678022        " << endl;
    cout << "======================================================\n" << endl;

    int result = 1;
    while (result == 1) {
        char choice = menu();
        result = process(choice);
        cout << "\n\n\n";
    }

    exit(0);
    return 0;
}


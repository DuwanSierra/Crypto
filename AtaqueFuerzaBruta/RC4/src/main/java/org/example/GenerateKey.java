package org.example;

import java.util.ArrayList;
import java.util.List;

public class GenerateKey {
    private String CHARACTERS;
    private int lengthKey = 4;

    private List<String> combinaciones;

    public GenerateKey(String CHARACTERS, int length) {
        this.CHARACTERS = CHARACTERS;
        this.lengthKey = length;
        this.combinaciones = new ArrayList<>();
    }

    public void generateCombinations() {
        char[] combination = new char[this.lengthKey];
        generateCombinationsRecursive(combination, 0, this.lengthKey);
    }

    public List<String> getCombinaciones() {
        return combinaciones;
    }

    public void setCombinaciones(List<String> combinaciones) {
        this.combinaciones = combinaciones;
    }

    public void generateCombinationsRecursive(char[] combination, int pos, int length) {
        if (pos == length) {
            this.combinaciones.add(new String(combination));
            return;
        }

        // Caso recursivo: Para cada posición, probamos todas las letras del conjunto de caracteres
        for (int i = 0; i < this.CHARACTERS.length(); i++) {
            combination[pos] = this.CHARACTERS.charAt(i);
            generateCombinationsRecursive(combination, pos + 1, length); // Recursión para la siguiente posición
        }
    }



}

package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Polynomial {

    private static final Map<Integer, Integer> irreduciblePolynomials = new HashMap<>();

    static {
        irreduciblePolynomials.put(0, 0x11B);  // x^8 + x^4 + x^3 + x + 1
        irreduciblePolynomials.put(1, 0x12D);  // x^8 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(2, 0x14D);  // x^8 + x^5 + x^3 + x^2 + 1
        irreduciblePolynomials.put(3, 0x16B);  // x^8 + x^6 + x^3 + x^2 + 1
        irreduciblePolynomials.put(4, 0x12B);  // x^8 + x^5 + x^4 + x^3 + 1
        irreduciblePolynomials.put(5, 0x135);  // x^8 + x^5 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(6, 0x17D);  // x^8 + x^6 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(7, 0x13D);  // x^8 + x^6 + x^5 + x^2 + 1
        irreduciblePolynomials.put(8, 0x15D);  // x^8 + x^6 + x^5 + x^3 + x^2 + 1
        irreduciblePolynomials.put(9, 0x1A7);  // x^8 + x^6 + x^5 + x^4 + x^2 + 1
        irreduciblePolynomials.put(10, 0x1B5); // x^8 + x^6 + x^5 + x^4 + x^3 + 1
        irreduciblePolynomials.put(11, 0x1C3); // x^8 + x^6 + x^5 + x^4 + x^3 + x + 1
        irreduciblePolynomials.put(12, 0x1E3); // x^8 + x^6 + x^5 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(13, 0x17B); // x^8 + x^7 + x^3 + x^2 + 1
        irreduciblePolynomials.put(14, 0x15B); // x^8 + x^7 + x^5 + x^4 + 1
        irreduciblePolynomials.put(15, 0x181); // x^8 + x^7 + x^6 + x + 1
        irreduciblePolynomials.put(16, 0x19B); // x^8 + x^7 + x^6 + x^3 + x^2 + 1
        irreduciblePolynomials.put(17, 0x16F); // x^8 + x^7 + x^6 + x^5 + x^3 + 1
        irreduciblePolynomials.put(18, 0x1CB); // x^8 + x^7 + x^6 + x^5 + x^3 + x + 1
        irreduciblePolynomials.put(19, 0x1D7); // x^8 + x^7 + x^6 + x^5 + x^4 + 1
        irreduciblePolynomials.put(20, 0x193); // x^8 + x^7 + x^6 + x^5 + x^4 + x + 1
        irreduciblePolynomials.put(21, 0x1B9); // x^8 + x^7 + x^6 + x^5 + x^4 + x^3 + 1
        irreduciblePolynomials.put(22, 0x17A); // x^8 + x^7 + x^6 + x^5 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(23, 0x12F); // x^8 + x^7 + x^6 + x^4 + x^3 + 1
        irreduciblePolynomials.put(24, 0x197); // x^8 + x^7 + x^6 + x^4 + x^3 + x + 1
        irreduciblePolynomials.put(25, 0x1B1); // x^8 + x^7 + x^6 + x^5 + x^3 + x^2 + 1
        irreduciblePolynomials.put(26, 0x1AD); // x^8 + x^7 + x^5 + x^4 + x^3 + x^2 + 1
        irreduciblePolynomials.put(27, 0x1C9); // x^8 + x^7 + x^5 + x^4 + x^3 + x + 1
        irreduciblePolynomials.put(28, 0x1D3); // x^8 + x^7 + x^5 + x^4 + x^3 + x^2 + x + 1
        irreduciblePolynomials.put(29, 0x1E9); // x^8 + x^7 + x^6 + x^5 + x^3 + x + 1
    }

    public static int calculateAESModulo(List<String> codes) {
        int sumLastTwoDigits = 0;
        int largestLastDigit = 0;

        for (String code : codes) {
            // Extract the last two digits
            int lastTwoDigits = Integer.parseInt(code.substring(code.length() - 2));
            sumLastTwoDigits += lastTwoDigits;

            // Get the last digit and update the largest if necessary
            int lastDigit = Integer.parseInt(code.substring(code.length() - 1));
            if (lastDigit > largestLastDigit) {
                largestLastDigit = lastDigit;
            }
        }

        // Calculate modulo 30
        int moduloResult = sumLastTwoDigits % 30;

        // Apply additional rule if the result is 0
        if (moduloResult == 0) {
            moduloResult += largestLastDigit;
        }

        return moduloResult;
    }

    public static Integer getIrreduciblePolynomial(int moduloResult) {
        return irreduciblePolynomials.getOrDefault(moduloResult, 0);
    }
    
}

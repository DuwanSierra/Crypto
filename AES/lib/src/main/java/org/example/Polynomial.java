package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Polynomial {

    private static final Map<Integer, String> irreduciblePolynomials = new HashMap<>();

    static {
        irreduciblePolynomials.put(0, "x^8 + x^4 + x^3 + x + 1");
        irreduciblePolynomials.put(1, "x^8 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(2, "x^8 + x^5 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(3, "x^8 + x^6 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(4, "x^8 + x^5 + x^4 + x^3 + 1");
        irreduciblePolynomials.put(5, "x^8 + x^5 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(6, "x^8 + x^6 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(7, "x^8 + x^6 + x^5 + x^2 + 1");
        irreduciblePolynomials.put(8, "x^8 + x^6 + x^5 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(9, "x^8 + x^6 + x^5 + x^4 + x^2 + 1");
        irreduciblePolynomials.put(10, "x^8 + x^6 + x^5 + x^4 + x^3 + 1");
        irreduciblePolynomials.put(11, "x^8 + x^6 + x^5 + x^4 + x^3 + x + 1");
        irreduciblePolynomials.put(12, "x^8 + x^6 + x^5 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(13, "x^8 + x^7 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(14, "x^8 + x^7 + x^5 + x^4 + 1");
        irreduciblePolynomials.put(15, "x^8 + x^7 + x^6 + x + 1");
        irreduciblePolynomials.put(16, "x^8 + x^7 + x^6 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(17, "x^8 + x^7 + x^6 + x^5 + x^3 + 1");
        irreduciblePolynomials.put(18, "x^8 + x^7 + x^6 + x^5 + x^3 + x + 1");
        irreduciblePolynomials.put(19, "x^8 + x^7 + x^6 + x^5 + x^4 + 1");
        irreduciblePolynomials.put(20, "x^8 + x^7 + x^6 + x^5 + x^4 + x + 1");
        irreduciblePolynomials.put(21, "x^8 + x^7 + x^6 + x^5 + x^4 + x^3 + 1");
        irreduciblePolynomials.put(22, "x^8 + x^7 + x^6 + x^5 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(23, "x^8 + x^7 + x^6 + x^4 + x^3 + 1");
        irreduciblePolynomials.put(24, "x^8 + x^7 + x^6 + x^4 + x^3 + x + 1");
        irreduciblePolynomials.put(25, "x^8 + x^7 + x^6 + x^5 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(26, "x^8 + x^7 + x^5 + x^4 + x^3 + x^2 + 1");
        irreduciblePolynomials.put(27, "x^8 + x^7 + x^5 + x^4 + x^3 + x + 1");
        irreduciblePolynomials.put(28, "x^8 + x^7 + x^5 + x^4 + x^3 + x^2 + x + 1");
        irreduciblePolynomials.put(29, "x^8 + x^7 + x^6 + x^5 + x^3 + x + 1");
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

    public static String getIrreduciblePolynomial(int moduloResult) {
        return irreduciblePolynomials.getOrDefault(moduloResult, "Polynomial not found");
    }

}

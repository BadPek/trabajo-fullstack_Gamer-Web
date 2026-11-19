package com.gamerzone.auth.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class RutValidator {

    public String normalize(String rut) {
        String compact = compact(rut);
        if (compact.length() < 2) {
            return compact;
        }
        return compact.substring(0, compact.length() - 1) + "-" + compact.charAt(compact.length() - 1);
    }

    public boolean isValid(String rut) {
        String normalized = compact(rut);
        if (!normalized.matches("\\d{7,8}[0-9K]")) {
            return false;
        }
        String body = normalized.substring(0, normalized.length() - 1);
        int factor = 2;
        int sum = 0;
        for (int index = body.length() - 1; index >= 0; index--) {
            sum += Character.digit(body.charAt(index), 10) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int remainder = 11 - sum % 11;
        char expected = remainder == 11 ? '0' : remainder == 10 ? 'K' : (char) ('0' + remainder);
        return normalized.charAt(normalized.length() - 1) == expected;
    }

    private static String compact(String rut) {
        return rut.replace(".", "").replace("-", "").replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }
}

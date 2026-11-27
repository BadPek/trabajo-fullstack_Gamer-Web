package cl.gamerzone.catalogo.dto.product;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueCatalogValuesValidator implements ConstraintValidator<UniqueCatalogValues, List<String>> {

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }

        Set<String> normalizedValues = new HashSet<>();
        for (String value : values) {
            if (value != null && !normalizedValues.add(value.trim().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }
}

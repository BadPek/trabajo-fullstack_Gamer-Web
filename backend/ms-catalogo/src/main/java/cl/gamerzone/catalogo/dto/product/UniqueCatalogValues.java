package cl.gamerzone.catalogo.dto.product;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCatalogValuesValidator.class)
public @interface UniqueCatalogValues {

    String message() default "No se permiten valores repetidos.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package cl.gamerzone.catalogo.dto.product;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MediaPathValidator.class)
public @interface MediaPath {

    String message() default "Debe ser una ruta relativa válida o una URL HTTPS.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

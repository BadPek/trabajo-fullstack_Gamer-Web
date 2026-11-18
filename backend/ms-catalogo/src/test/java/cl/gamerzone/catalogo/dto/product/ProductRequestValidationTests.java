package cl.gamerzone.catalogo.dto.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class ProductRequestValidationTests {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void acceptsCreateAndUpdateRequestsWithPositivePriceAndValidNestedData() {
        assertTrue(validator.validate(validCreateRequest()).isEmpty());
        assertTrue(validator.validate(validUpdateRequest()).isEmpty());
    }

    @Test
    void rejectsZeroPriceForCreateAndUpdateRequests() {
        ProductCreateRequest create = validCreateRequestWithPrice(0);
        ProductUpdateRequest update = new ProductUpdateRequest(
                create.titulo(),
                create.precio(),
                create.categoria(),
                create.imagen(),
                create.descripcionCorta(),
                create.descripcion(),
                create.video(),
                create.galeria(),
                create.detalles(),
                create.requisitos());

        assertEquals(1, validator.validate(create).size());
        assertEquals(1, validator.validate(update).size());
    }

    @Test
    void createAndUpdateRequestsApplyTheSameConstraints() {
        String over2048 = "src/" + "a".repeat(2045);
        ProductCreateRequest create = new ProductCreateRequest(
                "t".repeat(151),
                -1,
                "c".repeat(101),
                over2048,
                "s".repeat(501),
                "d".repeat(10001),
                "http://cdn.example.com/video.mp4",
                List.of("invalid-path"),
                null,
                null);
        ProductUpdateRequest update = new ProductUpdateRequest(
                create.titulo(),
                create.precio(),
                create.categoria(),
                create.imagen(),
                create.descripcionCorta(),
                create.descripcion(),
                create.video(),
                create.galeria(),
                null,
                null);

        assertEquals(violationsByConstraint(validator.validate(create)),
                violationsByConstraint(validator.validate(update)));
        assertFalse(validator.validate(create).isEmpty());
    }

    @Test
    void validatesEveryProductDetailsFieldAndItsLength() {
        ProductDetails invalid = new ProductDetails(
                "",
                "e".repeat(151),
                null,
                "m".repeat(501),
                " ");

        Set<ConstraintViolation<ProductDetails>> violations = validator.validate(invalid);
        assertEquals(5, violations.size());
        assertTrue(violations.stream().allMatch(violation -> violation.getMessage().contains("obligator")
                || violation.getMessage().contains("superar")));
        assertTrue(validator.validate(validDetails()).isEmpty());
    }

    @Test
    void validatesAllRequiredAndMaximumLengthsForSystemRequirements() {
        SystemRequirements blank = new SystemRequirements("", "", "", "", "", "");
        SystemRequirements tooLong = new SystemRequirements(
                "x".repeat(301), "x".repeat(301), "x".repeat(301),
                "x".repeat(301), "x".repeat(301), "x".repeat(301));

        assertEquals(6, validator.validate(blank).size());
        assertEquals(6, validator.validate(tooLong).size());
        assertTrue(validator.validate(validSystemRequirements()).isEmpty());
    }

    private Map<String, String> violationsByConstraint(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().collect(Collectors.toMap(
                violation -> violation.getPropertyPath() + ":"
                        + violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName(),
                ConstraintViolation::getMessage,
                (first, second) -> first));
    }

    private ProductCreateRequest validCreateRequest() {
        return validCreateRequestWithPrice(1);
    }

    private ProductCreateRequest validCreateRequestWithPrice(int price) {
        return new ProductCreateRequest(
                "Juego",
                price,
                "Acción",
                "src/images/cover.png",
                "Descripción breve",
                "Descripción",
                null,
                List.of("src/images/gallery.png"),
                validDetails(),
                new ProductRequirements(validSystemRequirements(), validSystemRequirements()));
    }

    private ProductUpdateRequest validUpdateRequest() {
        ProductCreateRequest request = validCreateRequest();
        return new ProductUpdateRequest(
                request.titulo(),
                request.precio(),
                request.categoria(),
                request.imagen(),
                request.descripcionCorta(),
                request.descripcion(),
                request.video(),
                request.galeria(),
                request.detalles(),
                request.requisitos());
    }

    private ProductDetails validDetails() {
        return new ProductDetails("Desarrollador", "Editor", LocalDate.of(2025, 1, 1), "Un jugador", "Español");
    }

    private SystemRequirements validSystemRequirements() {
        return new SystemRequirements("Windows", "CPU", "8 GB", "GPU", "DirectX 12", "20 GB");
    }
}

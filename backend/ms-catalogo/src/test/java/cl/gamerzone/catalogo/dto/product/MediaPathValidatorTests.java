package cl.gamerzone.catalogo.dto.product;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MediaPathValidatorTests {

    private final MediaPathValidator validator = new MediaPathValidator();

    @Test
    void acceptsOptionalValuesAndSafeReferences() {
        assertTrue(isValid(null));
        assertTrue(isValid(""));
        assertTrue(isValid("   "));
        assertTrue(isValid("src/images/game-cover.png"));
        assertTrue(isValid("src/images/game+cover.png"));
        assertTrue(isValid("https://cdn.example.com/images/game.png"));
        assertTrue(isValid("https://cdn.example.com/image%20one.png"));
        assertTrue(isValid("https://cdn.example.com/game+cover.png"));
        assertTrue(isValid("https://cdn.example.com/game%2Bcover.png"));
    }

    @Test
    void rejectsPathsOutsideSrcAndAbsolutePaths() {
        assertFalse(isValid("images/game.png"));
        assertFalse(isValid("../images/game.png"));
        assertFalse(isValid("/src/images/game.png"));
        assertFalse(isValid("C:/src/images/game.png"));
        assertFalse(isValid("src/"));
        assertFalse(isValid("src/images/"));
        assertFalse(isValid("src/./images/game.png"));
        assertFalse(isValid("src/folder/../game.png"));
    }

    @Test
    void rejectsEncodedAndNestedTraversal() {
        assertFalse(isValid("src/%2e%2e/secrets.txt"));
        assertFalse(isValid("src/%2E%2E/secrets.txt"));
        assertFalse(isValid("src/%252e%252e/secrets.txt"));
        assertFalse(isValid("src/%25252E%25252e/secrets.txt"));
        assertFalse(isValid("src/images%2f%2e%2e/secrets.txt"));
    }

    @Test
    void rejectsUnsafeSchemesCredentialsAndMalformedReferences() {
        assertFalse(isValid("http://cdn.example.com/image.png"));
        assertFalse(isValid("file:///src/image.png"));
        assertFalse(isValid("javascript:alert(1)"));
        assertFalse(isValid("https://user:password@cdn.example.com/image.png"));
        assertFalse(isValid("https://cdn.example.com:99999/image.png"));
        assertFalse(isValid("https:///image.png"));
        assertFalse(isValid("https://bad..host/image.png"));
        assertFalse(isValid("src/images\\game.png"));
        assertFalse(isValid("src/images%00.png"));
        assertFalse(isValid("src/images%ZZ.png"));
        assertFalse(isValid("src/images.png?download=true"));
    }

    private boolean isValid(String value) {
        return validator.isValid(value, null);
    }
}

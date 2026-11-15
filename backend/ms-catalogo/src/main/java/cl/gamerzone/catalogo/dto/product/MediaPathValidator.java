package cl.gamerzone.catalogo.dto.product;

import java.io.ByteArrayOutputStream;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MediaPathValidator implements ConstraintValidator<MediaPath, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (containsControlCharacter(value) || value.contains("\\")) {
            return false;
        }

        try {
            if (!hasNoEncodedControls(value)) {
                return false;
            }
            URI uri = new URI(value);
            if (uri.isAbsolute()) {
                return "https".equalsIgnoreCase(uri.getScheme())
                        && hasValidHttpsAuthority(uri)
                        && hasSafePath(uri.getRawPath(), false);
            }

            String path = uri.getRawPath();
            return uri.getRawAuthority() == null
                    && uri.getRawQuery() == null
                    && uri.getRawFragment() == null
                    && path != null
                    && path.startsWith("src/")
                    && hasSafePath(path, true);
        } catch (URISyntaxException | CharacterCodingException | IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean hasNoEncodedControls(String value) throws CharacterCodingException {
        String decoded = value;
        for (int i = 0; i <= value.length(); i++) {
            if (containsControlCharacter(decoded)) {
                return false;
            }
            String next = decodePercentEscapes(decoded);
            if (next.equals(decoded)) {
                return true;
            }
            decoded = next;
        }
        return false;
    }

    private boolean hasValidHttpsAuthority(URI uri) {
        String authority = uri.getRawAuthority();
        String host = uri.getHost();
        int port = uri.getPort();
        return authority != null
                && host != null
                && !host.isBlank()
                && uri.getRawUserInfo() == null
                && authority.indexOf('@') < 0
                && (port == -1 || port <= 65535)
                && isValidHost(host);
    }

    private boolean isValidHost(String host) {
        if (host.startsWith("[") && host.endsWith("]")) {
            return host.indexOf(':') > 0;
        }

        try {
            String asciiHost = IDN.toASCII(host, IDN.USE_STD3_ASCII_RULES);
            return asciiHost.length() <= 253
                    && Arrays.stream(asciiHost.split("\\.", -1))
                            .allMatch(label -> !label.isEmpty()
                                    && label.length() <= 63
                                    && !label.startsWith("-")
                                    && !label.endsWith("-"));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean hasSafePath(String rawPath, boolean relativePath) {
        if (rawPath == null) {
            return !relativePath;
        }

        String decodedPath = rawPath;
        try {
            for (int i = 0; i <= rawPath.length(); i++) {
                if (containsControlCharacter(decodedPath) || decodedPath.contains("\\")) {
                    return false;
                }
                if (hasDotSegment(decodedPath)) {
                    return false;
                }

                String nextPath = decodePercentEscapes(decodedPath);
                if (nextPath.equals(decodedPath)) {
                    if (!relativePath) {
                        return true;
                    }
                    return decodedPath.startsWith("src/")
                            && !decodedPath.endsWith("/")
                            && decodedPath.length() > "src/".length();
                }
                decodedPath = nextPath;
            }
        } catch (CharacterCodingException exception) {
            return false;
        }
        return false;
    }

    private boolean hasDotSegment(String path) {
        return Arrays.stream(path.split("/", -1))
                .anyMatch(segment -> segment.equals(".") || segment.equals(".."));
    }

    private String decodePercentEscapes(String value) throws CharacterCodingException {
        StringBuilder decoded = new StringBuilder(value.length());
        for (int i = 0; i < value.length();) {
            if (value.charAt(i) != '%') {
                decoded.append(value.charAt(i++));
                continue;
            }

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while (i < value.length() && value.charAt(i) == '%') {
                if (i + 2 >= value.length()) {
                    throw new IllegalArgumentException("Invalid percent escape");
                }
                int high = Character.digit(value.charAt(i + 1), 16);
                int low = Character.digit(value.charAt(i + 2), 16);
                if (high < 0 || low < 0) {
                    throw new IllegalArgumentException("Invalid percent escape");
                }
                bytes.write((high << 4) + low);
                i += 3;
            }

            decoded.append(StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes.toByteArray())));
        }
        return decoded.toString();
    }

    private boolean containsControlCharacter(String value) {
        return value.chars().anyMatch(Character::isISOControl);
    }
}

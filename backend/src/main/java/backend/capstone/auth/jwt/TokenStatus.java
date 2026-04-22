package backend.capstone.auth.jwt;

public enum TokenStatus {
    VALID,
    EXPIRED,
    INVALID_SIGNATURE,
    UNSUPPORTED,
    MALFORMED,
    INVALID_TOKEN,
    MISSING_TOKEN,
    INVALID_TOKEN_TYPE
}

package roomescape.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenType
) {

    private static final String BEARER = "Bearer";

    public static LoginResponse bearer(String accessToken) {
        return new LoginResponse(accessToken, BEARER);
    }
}

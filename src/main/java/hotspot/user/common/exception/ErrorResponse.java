package hotspot.user.common.exception;

public record ErrorResponse(int statusCode, String code, String message) {}

package hotspot.user.common.exception;

public record ErrorResponse(int status, String code, String message) {}

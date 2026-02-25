package hotspot.user.s3.controller.response;

public record S3PathResponse(
        String uploadUrl,
        String tempKey
) {
}

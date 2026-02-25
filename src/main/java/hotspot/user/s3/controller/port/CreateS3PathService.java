package hotspot.user.s3.controller.port;

import hotspot.user.s3.controller.response.S3PathResponse;

public interface CreateS3PathService {

    S3PathResponse createS3Path(String contentType);
}

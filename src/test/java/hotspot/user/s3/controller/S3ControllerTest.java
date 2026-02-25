package hotspot.user.s3.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.S3ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.s3.controller.port.CreateS3PathService;
import hotspot.user.s3.controller.response.S3PathResponse;

@WebMvcTest(controllers = S3Controller.class)
@AutoConfigureMockMvc(addFilters = false)
class S3ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CreateS3PathService createS3PathService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Presigned URL 생성 성공")
    void shouldReturnPresignedUrlSuccessfully() throws Exception {

        S3PathResponse response =
                new S3PathResponse(
                        "https://mock-url",
                        "temp/2026-02-25/test.png"
                );

        when(createS3PathService.createS3Path("image/png"))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/image/presigned-url")
                                .param("contentType", "image/png")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.uploadUrl").value("https://mock-url"))
                .andExpect(jsonPath("$.data.tempKey").value("temp/2026-02-25/test.png"));
    }

    @Test
    @DisplayName("PNG가 아닌 경우 예외 반환")
    void shouldFailWhenContentTypeIsNotPng() throws Exception {

        when(createS3PathService.createS3Path("image/jpeg"))
                .thenThrow(new ApplicationException(S3ErrorCode.EXTENSION_NOT_PNG));

        mockMvc.perform(
                        post("/api/v1/image/presigned-url")
                                .param("contentType", "image/jpeg")
                )
                .andExpect(status().is4xxClientError());
    }
}
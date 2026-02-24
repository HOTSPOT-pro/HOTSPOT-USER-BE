package hotspot.user.notification.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.notification.controller.port.FindNotificationAllowService;
import hotspot.user.notification.controller.port.UpdateNotificationAllowService;
import hotspot.user.notification.controller.request.UpdateNotificationAllowRequest;
import hotspot.user.notification.controller.response.NotificationAllowListResponse;
import hotspot.user.notification.controller.response.NotificationAllowResponse;
import hotspot.user.notification.domain.NotificationCategory;

@WebMvcTest(controllers = NotificationAllowController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationAllowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindNotificationAllowService findNotificationAllowService;

    @MockBean
    private UpdateNotificationAllowService updateNotificationAllowService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("get notification allows success")
    void getNotificationAllowsSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        given(findNotificationAllowService.findNotificationAllows(1L))
                .willReturn(NotificationAllowListResponse.builder()
                        .notificationAllows(List.of(
                                NotificationAllowResponse.builder()
                                        .notificationCategory(NotificationCategory.DATA)
                                        .notificationAllow(true)
                                        .build(),
                                NotificationAllowResponse.builder()
                                        .notificationCategory(NotificationCategory.POLICY)
                                        .notificationAllow(false)
                                        .build()
                        ))
                        .build());

        mockMvc.perform(get("/api/v1/notifications/allow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationAllows[0].notificationCategory").value("DATA"))
                .andExpect(jsonPath("$.data.notificationAllows[0].notificationAllow").value(true));
    }

    @Test
    @DisplayName("update notification allow success")
    void updateNotificationAllowSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        given(updateNotificationAllowService.updateNotificationAllow(eq(1L), isA(UpdateNotificationAllowRequest.class)))
                .willReturn(NotificationAllowResponse.builder()
                        .notificationCategory(NotificationCategory.APP_SERVICE)
                        .notificationAllow(true)
                        .build());

        mockMvc.perform(patch("/api/v1/notifications/allow")
                        .contentType("application/json")
                        .content("""
                                {
                                  "notificationCategory": "APP_SERVICE",
                                  "notificationAllow": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationCategory").value("APP_SERVICE"))
                .andExpect(jsonPath("$.data.notificationAllow").value(true));
    }
}

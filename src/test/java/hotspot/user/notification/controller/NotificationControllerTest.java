package hotspot.user.notification.controller;

import static hotspot.user.util.TestSecurityUtil.setAuthentication;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.security.jwt.JwtFilter;
import hotspot.user.common.security.jwt.JwtProvider;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.notification.controller.port.NotificationService;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.NotificationResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("알림 목록 조회 성공: 인증 사용자 id 기준으로 조회한다")
    void getNotificationsSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        NotificationListResponse response = new NotificationListResponse(
                List.of(new NotificationResponse(
                        10L,
                        "evt-1",
                        "ALERT",
                        "데이터가 80% 소진되었습니다.",
                        false,
                        LocalDateTime.of(2026, 2, 23, 12, 0)
                )),
                0,
                20,
                1,
                1,
                false
        );

        given(notificationService.findNotifications(eq(1L), isA(Pageable.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications[0].id").value(10L))
                .andExpect(jsonPath("$.data.notifications[0].eventId").value("evt-1"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        then(notificationService).should().findNotifications(eq(1L), isA(Pageable.class));
    }

    @Test
    @DisplayName("안 읽은 알림 개수 조회 성공")
    void getUnreadCountSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        given(notificationService.findUnreadCount(1L))
                .willReturn(new UnreadNotificationCountResponse(3));

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("미읽음 개수 조회 실패: 알림/회선이 없으면 404를 반환한다")
    void getUnreadCountFailWhenNotificationNotFound() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        willThrow(new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
                .given(notificationService).findUnreadCount(1L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTI_001"));
    }

    @Test
    @DisplayName("알림 전체 읽음 처리 성공")
    void markAllReadSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        then(notificationService).should().markAllRead(1L);
    }

    @Test
    @DisplayName("알림 단건 읽음 처리 성공")
    void markReadSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        mockMvc.perform(patch("/api/v1/notifications/10/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        then(notificationService).should().markRead(1L, 10L);
    }

    @Test
    @DisplayName("알림 단건 읽음 처리 실패: 알림이 없으면 404를 반환한다")
    void markReadFailWhenNotificationNotFound() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        willThrow(new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
                .given(notificationService).markRead(1L, 10L);

        mockMvc.perform(patch("/api/v1/notifications/10/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTI_001"));
    }
}

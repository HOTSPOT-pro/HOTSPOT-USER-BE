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
import hotspot.user.notification.controller.port.FindNotificationService;
import hotspot.user.notification.controller.port.ReadNotificationService;
import hotspot.user.notification.controller.response.NotificationListResponse;
import hotspot.user.notification.controller.response.NotificationResponse;
import hotspot.user.notification.controller.response.UnreadNotificationCountResponse;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FindNotificationService findNotificationService;

    @MockBean
    private ReadNotificationService readNotificationService;

    @MockBean
    private JwtFilter jwtFilter;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("get notifications success")
    void getNotificationsSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(List.of(NotificationResponse.builder()
                        .id(10L)
                        .eventId("evt-1")
                        .notificationType("ALERT")
                        .title("Data alert")
                        .content("80% used")
                        .isRead(false)
                        .createdTime(LocalDateTime.of(2026, 2, 23, 12, 0))
                        .build()))
                .page(0)
                .size(20)
                .hasNext(false)
                .build();

        given(findNotificationService.findNotifications(eq(1L), isA(Pageable.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notifications[0].id").value(10L))
                .andExpect(jsonPath("$.data.notifications[0].eventId").value("evt-1"))
                .andExpect(jsonPath("$.data.notifications[0].title").value("Data alert"))
                .andExpect(jsonPath("$.data.hasNext").value(false));

        then(findNotificationService).should().findNotifications(eq(1L), isA(Pageable.class));
    }

    @Test
    @DisplayName("get unread count success")
    void getUnreadCountSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        given(findNotificationService.findUnreadCount(1L))
                .willReturn(UnreadNotificationCountResponse.builder()
                        .unreadCount(3)
                        .build());

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    @DisplayName("get unread count fail when not found")
    void getUnreadCountFailWhenNotificationNotFound() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        willThrow(new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
                .given(findNotificationService).findUnreadCount(1L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTI_001"));
    }

    @Test
    @DisplayName("mark all read success")
    void markAllReadSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        mockMvc.perform(patch("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        then(readNotificationService).should().markAllRead(1L);
    }

    @Test
    @DisplayName("mark single read success")
    void markReadSuccess() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);

        mockMvc.perform(patch("/api/v1/notifications/10/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());

        then(readNotificationService).should().markRead(1L, 10L);
    }

    @Test
    @DisplayName("mark single read fail when not found")
    void markReadFailWhenNotificationNotFound() throws Exception {
        setAuthentication(1L, 100L, FamilyRole.OWNER);
        willThrow(new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND))
                .given(readNotificationService).markRead(1L, 10L);

        mockMvc.perform(patch("/api/v1/notifications/10/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTI_001"));
    }
}

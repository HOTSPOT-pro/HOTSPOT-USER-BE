package hotspot.user.policy.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import hotspot.user.common.BaseEntity;
import hotspot.user.policy.domain.AppBlockedService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자가 설정한 앱 차단 서비스 엔티티
 */

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "app_blocked_service")
@SQLDelete(sql = "UPDATE app_blocked_service SET is_deleted = true WHERE app_blocked_service_id = ?")
@Where(clause = "is_deleted = false")
public class AppBlockedServiceEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appBlockedServiceId;

    @Column(length = 20, nullable = false)
    private String blockedServiceName;

    @Column(length = 20, nullable = false)
    private String blockedServiceCode;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    public AppBlockedServiceEntity domainToEntity(AppBlockedService appBlockedService) {
        return AppBlockedServiceEntity.builder()
                .appBlockedServiceId(appBlockedService.getId())
                .blockedServiceName(appBlockedService.getName())
                .blockedServiceCode(appBlockedService.getServiceCode())
                .build();
    }

    public AppBlockedService entityToDomain() {
        return AppBlockedService.builder()
                .id(this.appBlockedServiceId)
                .name(this.blockedServiceName)
                .serviceCode(this.blockedServiceCode)
                .build();
    }
}

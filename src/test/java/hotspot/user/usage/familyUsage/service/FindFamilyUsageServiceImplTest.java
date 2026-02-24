package hotspot.user.usage.familyUsage.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.family.service.port.FamilySubscriptionRepository;
import hotspot.user.usage.familyUsage.controller.response.FamilyUsageResponse;
import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.service.port.FamilyUsageRepository;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

@ExtendWith(MockitoExtension.class)
class FindFamilyUsageServiceImplTest {

    @Mock
    FamilyUsageRepository familyUsageRepository;

    @Mock
    FamilySubscriptionRepository familySubscriptionRepository;

    @Mock
    Clock clock;

    @InjectMocks
    FindFamilyUsageServiceImpl service;

    @Test
    @DisplayName("서비스 정상 동작")
    void shouldReturnFamilyUsageSuccessfully() {

        Long familyId = 1L;

        Instant fixedInstant = Instant.parse("2026-02-24T08:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        List<FamilySubList> subList =
                List.of(new FamilySubList(1L, "홍길동"));

        when(familySubscriptionRepository.findByFamilyId(familyId))
                .thenReturn(List.of(/* mock subscription */));

        when(familyUsageRepository.findFamilyUsage(any(), any()))
                .thenReturn(
                        new FamilyUsage(
                                20971520,
                                5242880,
                                Map.of()
                        )
                );

        FamilyUsageResponse response =
                service.findFamilyUsage(familyId);

        assertNotNull(response);
        assertNotNull(response.currentTime());
    }
}

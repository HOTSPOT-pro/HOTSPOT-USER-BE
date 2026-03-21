package hotspot.user.usage.familyUsage.infrastructure.respository;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import hotspot.user.usage.familyUsage.domain.FamilyUsage;
import hotspot.user.usage.familyUsage.service.schema.FamilySubList;

@ExtendWith(MockitoExtension.class)
class FamilyUsageRepositoryImplTest {

    @Mock
    private FamilyUsageRedisRepository redisRepository;

    @InjectMocks
    private FamilyUsageRepositoryImpl repository;

    @Test
    @DisplayName("subId 목록을 추출해서 redisRepository에 전달한다")
    void shouldExtractSubIdsAndCallRedisRepository() {

        // given
        Long familyId = 1L;

        FamilySubList sub1 = mock(FamilySubList.class);
        FamilySubList sub2 = mock(FamilySubList.class);

        when(sub1.subId()).thenReturn(10L);
        when(sub2.subId()).thenReturn(20L);

        List<FamilySubList> subList = List.of(sub1, sub2);

        FamilyUsage expected = mock(FamilyUsage.class);

        when(redisRepository.findFamilyAndSubData(familyId, List.of(10L, 20L)))
                .thenReturn(expected);

        // when
        FamilyUsage result = repository.findFamilyUsage(familyId, subList);

        // then
        assertEquals(expected, result);

        verify(redisRepository, times(1))
                .findFamilyAndSubData(familyId, List.of(10L, 20L));
    }
}

package hotspot.user.presentData.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PresentDataRepositoryImplTest {

    @Mock
    private PresentDataJpaRepository presentDataJpaRepository;

    @InjectMocks
    private PresentDataRepositoryImpl repository;

    @Test
    @DisplayName("giftId → giverName 매핑 정상 변환")
    void shouldReturnGiftGiverNamesSuccessfully() {

        List<Long> giftIds = List.of(1L, 2L);

        PresentDataJpaRepository.GiftGiverRow row1 =
                new PresentDataJpaRepository.GiftGiverRow() {
                    public Long getGiftId() { return 1L; }
                    public String getGiverName() { return "김태연"; }
                };

        PresentDataJpaRepository.GiftGiverRow row2 =
                new PresentDataJpaRepository.GiftGiverRow() {
                    public Long getGiftId() { return 2L; }
                    public String getGiverName() { return "홍길동"; }
                };

        when(presentDataJpaRepository.findGiftGivers(giftIds))
                .thenReturn(List.of(row1, row2));

        Map<Long, String> result =
                repository.findGiftGiverNames(giftIds);

        assertEquals(2, result.size());
        assertEquals("김태연", result.get(1L));
        assertEquals("홍길동", result.get(2L));
    }

    @Test
    @DisplayName("giftIds가 비어있으면 빈 Map 반환")
    void shouldReturnEmptyMapWhenGiftIdsEmpty() {

        Map<Long, String> result =
                repository.findGiftGiverNames(List.of());

        assertEquals(0, result.size());
    }
}

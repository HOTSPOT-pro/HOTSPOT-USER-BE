package hotspot.user.family.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.family.domain.Family;
import hotspot.user.family.domain.FamilyDetailInfo;
import hotspot.user.family.infrastructure.entity.FamilyDetailInfoDto;
import hotspot.user.family.infrastructure.entity.FamilyEntity;
import hotspot.user.family.service.port.FamilyRepository;
import hotspot.user.member.domain.MemberDetailInfo;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FamilyRepositoryImpl implements FamilyRepository {

    private final FamilyJpaRepository familyJpaRepository;
    private final PhoneDecryptor phoneDecryptor;

    @Override
    public Optional<Family> findById(Long id) {
        return familyJpaRepository.findById(id)
                .map(FamilyEntity::entityToDomain);
    }

    @Override
    public Family save(Family family) {
        FamilyEntity entity = FamilyEntity.domainToEntity(family);
        FamilyEntity savedEntity = familyJpaRepository.save(entity);
        return savedEntity.entityToDomain();
    }

    @Override
    public Optional<FamilyDetailInfo> findInfoById(Long id) {
        List<FamilyDetailInfoDto> results = familyJpaRepository.findFamilyDetailQueryResult(id);

        // 1. 해당 ID의 가족 자체가 없는 경우
        if (results.isEmpty()) {
            return Optional.empty();
        }

        // 2. 첫 번째 행에서 가족 공통 정보 추출
        FamilyEntity familyEntity = results.get(0).familyEntity();

        // 3. 멤버 매핑
        List<MemberDetailInfo> members = results.stream()
                // LEFT JOIN 방어: 멤버가 없는(0명인) 가족일 경우 이 필터에서 걸러짐
                .filter(dto -> dto.memberEntity() != null)
                .map(dto -> MemberDetailInfo.builder()
                        .member(dto.memberEntity().entityToDomain())
                        .email(dto.email()) // LEFT JOIN이라 null일 수 있음 (소셜 연동 안한 유저)
                        .phone(phoneDecryptor.decrypt(dto.phone())) // 복호화 적용
                        .subId(dto.subId())
                        .role(dto.role())
                        .familyId(familyEntity.getFamilyId())
                        .build())
                .toList();

        // 4. 최종 가족 도메인 객체 반환
        return Optional.of(FamilyDetailInfo.builder()
                .familyId(familyEntity.getFamilyId())
                .familyNum(familyEntity.getFamilyNum())
                .memberDetailInfoList(members)
                .build());
    }
}

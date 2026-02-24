package hotspot.user.subscription.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import hotspot.user.common.crpyto.PhoneDecryptor;
import hotspot.user.common.crpyto.PhoneHashIndexer;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.infrastructure.entity.SubscriptionEntity;
import hotspot.user.subscription.service.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final PhoneDecryptor phoneDecryptor;
    private final PhoneHashIndexer phoneHashIndexer;

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findByMemberId(Long memberId) {
        return subscriptionJpaRepository.findByMemberId(memberId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findByPhoneNumber(String phoneNumber) {
        String phoneHash = phoneHashIndexer.toHash(phoneNumber);
        return subscriptionJpaRepository.findByPhoneHash(phoneHash)
                .map(this::toDomain);
    }

    @Override
    public Subscription save(Subscription subscription) {
        // 기존 데이터를 먼저 조회하여 암호화된 정보(phoneEnc, phoneHash)를 유지함
        SubscriptionEntity existingEntity = subscriptionJpaRepository.findById(subscription.getId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        SubscriptionEntity entityToSave = SubscriptionEntity.builder()
                .subId(subscription.getId())
                .member(subscription.getMember() != null ? hotspot.user.member.infrastructure.entity.MemberEntity.domainToEntity(subscription.getMember()) : null)
                .plan(subscription.getPlan() != null ? hotspot.user.plan.infrastructure.entity.PlanEntity.domainToEntity(subscription.getPlan()) : null)
                .phoneEnc(existingEntity.getPhoneEnc())
                .phoneHash(existingEntity.getPhoneHash())
                .isLocked(subscription.getIsLocked())
                .isDeleted(existingEntity.getIsDeleted())
                .build();

        return toDomain(subscriptionJpaRepository.save(entityToSave));
    }

    private Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        // DB의 암호문을 복호화하여 평문으로 도메인에 전달
        String plainPhone = phoneDecryptor.decrypt(entity.getPhoneEnc());
        return entity.entityToDomain(plainPhone);
    }
}

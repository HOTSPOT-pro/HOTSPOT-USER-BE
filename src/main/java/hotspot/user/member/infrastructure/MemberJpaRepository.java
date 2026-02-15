package hotspot.user.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hotspot.user.member.infrastructure.entity.MemberEntity;

@Repository
public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
}

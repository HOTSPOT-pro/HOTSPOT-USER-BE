package hotspot.user.familyReport.infrastructure;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotspot.user.familyReport.infrastructure.entity.FamilyReportEntity;

public interface FamilyReportJpaRepository extends JpaRepository<FamilyReportEntity, Long> {

    @EntityGraph(attributePaths = {"family"})
    Optional<FamilyReportEntity> findByFamilyFamilyId(Long familyId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE FamilyReportEntity fr
            SET fr.receiveDay = :receiveDay
            WHERE fr.family.familyId = :familyId
              AND fr.isActive = true
            """)
    int updateReceiveDay(@Param("familyId") Long familyId, @Param("receiveDay") DayOfWeek receiveDay);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE FamilyReportEntity fr
            SET fr.isActive = :newIsActive
            WHERE fr.family.familyId = :familyId
              AND fr.isActive = :currentIsActive
            """)
    int updateActive(
            @Param("familyId") Long familyId,
            @Param("currentIsActive") boolean currentIsActive,
            @Param("newIsActive") boolean newIsActive
    );
}

package hotspot.user.common.ex.infrastructure.entity;

import hotspot.user.common.ex.domain.Ex;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "EX")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exId;

    private String exName;

    private String exDescription;


    public static ExEntity domainToEntity(Ex ex) {
        return ExEntity.builder()
                .exId(ex.getExId())
                .exName(ex.getExName())
                .exDescription(ex.getExDescription())
                .build();
    }

    public Ex entityToDomain() {
        return Ex.builder()
                .exId(exId)
                .exName(exName)
                .exDescription(exDescription)
                .build();
    }
}

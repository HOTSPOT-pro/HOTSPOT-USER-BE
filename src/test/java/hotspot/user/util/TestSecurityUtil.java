package hotspot.user.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.member.domain.FamilyRole;
import hotspot.user.member.domain.Status;

public class TestSecurityUtil {

    public static void setAuthentication(Long id, Long familyId, FamilyRole role) {

        PrincipalDetails principal = PrincipalDetails.builder()
                .id(id)
                .email("test@test.com")
                .familyId(familyId)
                .role(role)
                .status(Status.APPROVED)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}

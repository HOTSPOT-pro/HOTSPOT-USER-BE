package hotspot.user.usage.giftUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.common.security.PrincipalDetails;
import hotspot.user.usage.giftUsage.controller.port.FindGiftUsageService;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import hotspot.user.usage.giftUsage.controller.swagger.GiftUsageApi;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/giftUsage")
public class GiftUsageController implements GiftUsageApi {

    private final FindGiftUsageService findGiftUsageService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<GiftUsageListResponse>> findGiftUsages(
            @AuthenticationPrincipal PrincipalDetails details) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        findGiftUsageService.findGiftUsages(details.getId())
                )
        );
    }
}

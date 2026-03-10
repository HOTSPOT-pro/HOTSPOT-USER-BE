package hotspot.user.usage.giftUsage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hotspot.user.common.ApiResponse;
import hotspot.user.usage.giftUsage.controller.port.FindGiftUsageService;
import hotspot.user.usage.giftUsage.controller.response.GiftUsageListResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/giftUsage")
public class GiftUsageController {

    private final FindGiftUsageService findGiftUsageService;

    @GetMapping("/gifts")
    public ResponseEntity<ApiResponse<GiftUsageListResponse>> findGiftUsages(
            @RequestParam Long memberId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        findGiftUsageService.findGiftUsages(memberId)
                )
        );
    }
}

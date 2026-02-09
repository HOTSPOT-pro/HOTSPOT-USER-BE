package hotspot.user.common.ex.controller;

import hotspot.user.common.ex.controller.port.FindExService;
import hotspot.user.common.ex.controller.port.SaveExService;
import hotspot.user.common.ex.controller.port.UpdateExService;
import hotspot.user.common.ex.controller.request.CreateExRequest;
import hotspot.user.common.ex.controller.request.UpdateExRequest;
import hotspot.user.common.ex.controller.response.ExResponse;
import hotspot.user.common.ex.domain.Ex;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ex")
public class ExController {

    private final FindExService findExService;
    private final SaveExService saveExService;
    private final UpdateExService updateExService;

    @GetMapping("/{exId}")
    public ResponseEntity<ExResponse> find(@PathVariable Long exId) {
        return ResponseEntity.ok(findExService.findEx(exId));
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody CreateExRequest request) {
        saveExService.save(request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{exId}")
    public ResponseEntity<Void> update(@PathVariable Long exId, @RequestBody UpdateExRequest request) {
        updateExService.update(exId, request);
        return ResponseEntity.noContent().build();
    }
}

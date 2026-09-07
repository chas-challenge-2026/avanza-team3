package se.comerit.avanza.targetallocation.controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.targetallocation.dto.TargetAllocationResponse;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

@RestController
@RequestMapping("/api/target-allocations")
public class TargetAllocationController {

    private final TargetAllocationService targetAllocationService;

    public TargetAllocationController(TargetAllocationService targetAllocationService) {
        this.targetAllocationService = targetAllocationService;
    }

    @GetMapping
    public ResponseEntity<Page<TargetAllocationResponse>> getTargetAllocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        if (page < 0) {
            page = 0;
        }

        if (size < 1) {
            size = 20;
        }

        size = Math.min(size, 100);
        return ResponseEntity.ok(
                targetAllocationService.getTargetAllocationsByUserId(
                        userId,
                        page,
                        size));
    }
}

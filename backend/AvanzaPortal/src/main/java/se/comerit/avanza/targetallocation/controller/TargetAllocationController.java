package se.comerit.avanza.targetallocation.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

@RestController
@RequestMapping("/api/targetallocations")
public class TargetAllocationController {

    private final TargetAllocationService targetAllocationService;

    public TargetAllocationController(TargetAllocationService targetAllocationService) {
        this.targetAllocationService = targetAllocationService;
    }
}

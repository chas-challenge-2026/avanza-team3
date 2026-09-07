package se.comerit.avanza.targetallocation.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.targetallocation.model.TargetAllocation;
import se.comerit.avanza.targetallocation.repository.TargetAllocationRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TargetAllocationService {

    private final TargetAllocationRepository targetAllocationRepository;

    public TargetAllocationService(TargetAllocationRepository targetAllocationRepository) {
        this.targetAllocationRepository = targetAllocationRepository;
    }

    public List<TargetAllocation> getTargetAllocationsByUserId(Integer userId) {
        return targetAllocationRepository.findByUserIdOrderByAccountTypeAsc(userId);
    }

    public TargetAllocation getTargetAllocationByIdForUser(Integer targetAllocationId, Integer userId) {
        return targetAllocationRepository.findByIdAndUserId(targetAllocationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Target allocation not found"
                ));
    }

    public List<Map<String, Object>> getTargetMapsByUserId(Integer userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (TargetAllocation target : getTargetAllocationsByUserId(userId)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", target.getId());
            row.put("user_id", target.getUserId());
            row.put("account_type", target.getAccountType());
            row.put("target_pct", target.getTargetPct());
            result.add(row);
        }

        return result;
    }
}

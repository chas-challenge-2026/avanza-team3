package se.comerit.avanza.targetallocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.targetallocation.dto.TargetAllocationResponse;
import se.comerit.avanza.targetallocation.model.TargetAllocation;
import se.comerit.avanza.targetallocation.repository.TargetAllocationRepository;
import se.comerit.avanza.targetallocation.service.TargetAllocationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TargetAllocationServiceTest {

    @Mock
    private TargetAllocationRepository targetAllocationRepository;

    @InjectMocks
    private TargetAllocationService targetAllocationService;

    @Test
    void getTargetAllocationsByUserIdShouldMapEntitiesToResponsesAndUseRequestedPagination() {
        TargetAllocation target = targetAllocation(21, 7, "ISK", new BigDecimal("60.00"));

        when(targetAllocationRepository.findByUserIdOrderByAccountTypeAsc(eq(7), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(target), invocation.getArgument(1), 1));

        Page<TargetAllocationResponse> result =
                targetAllocationService.getTargetAllocationsByUserId(7, 1, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(targetAllocationRepository)
                .findByUserIdOrderByAccountTypeAsc(eq(7), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(1, result.getContent().size());
        TargetAllocationResponse response = result.getContent().getFirst();
        assertEquals(21, response.id());
        assertEquals("ISK", response.accountType());
        assertEquals(new BigDecimal("60.00"), response.targetPercentage());
    }

    @Test
    void getTargetAllocationByIdForUserShouldReturnAllocationOwnedByUser() {
        TargetAllocation target = targetAllocation(21, 7, "ISK", new BigDecimal("60.00"));
        when(targetAllocationRepository.findByIdAndUserId(21, 7)).thenReturn(Optional.of(target));

        TargetAllocation result = targetAllocationService.getTargetAllocationByIdForUser(21, 7);

        assertSame(target, result);
        verify(targetAllocationRepository).findByIdAndUserId(21, 7);
    }

    @Test
    void getTargetAllocationByIdForUserShouldRejectAllocationNotOwnedByUser() {
        when(targetAllocationRepository.findByIdAndUserId(21, 7)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> targetAllocationService.getTargetAllocationByIdForUser(21, 7)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Target allocation not found", exception.getReason());
    }

    @Test
    void getTargetMapsByUserIdShouldExposeFieldsUsedByBackend() {
        TargetAllocation target = targetAllocation(21, 7, "ISK", new BigDecimal("60.00"));
        when(targetAllocationRepository.findByUserIdOrderByAccountTypeAsc(7))
                .thenReturn(List.of(target));

        List<Map<String, Object>> result = targetAllocationService.getTargetMapsByUserId(7);

        assertEquals(1, result.size());
        Map<String, Object> row = result.getFirst();
        assertEquals(21, row.get("id"));
        assertEquals(7, row.get("user_id"));
        assertEquals("ISK", row.get("account_type"));
        assertEquals(new BigDecimal("60.00"), row.get("target_pct"));
    }

    private TargetAllocation targetAllocation(
            Integer id,
            Integer userId,
            String accountType,
            BigDecimal targetPct) {

        TargetAllocation targetAllocation = new TargetAllocation(userId, accountType, targetPct);
        ReflectionTestUtils.setField(targetAllocation, "id", id);
        return targetAllocation;
    }
}

package com.politicalops.elections;

import com.politicalops.common.TenantContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Endpoints from the HLD "Representative APIs" table that belong to the elections module. */
@RestController
@RequestMapping("/api/v1")
public class ElectionController {

    private final ElectionImportService importService;
    private final ElectionRepository electionRepository;
    private final BoothResultRepository boothResultRepository;

    public ElectionController(ElectionImportService importService,
                               ElectionRepository electionRepository,
                               BoothResultRepository boothResultRepository) {
        this.importService = importService;
        this.electionRepository = electionRepository;
        this.boothResultRepository = boothResultRepository;
    }

    public record RegisterImportRequest(@NotNull UUID electionId, @NotBlank String storageKey,
                                         @NotBlank String checksum, @NotBlank String parserVersion) {}

    @PostMapping("/election-imports")
    public ResponseEntity<ElectionSourceFile> registerImport(@RequestBody RegisterImportRequest req) {
        // DATA_STEWARD only (method security in SecurityConfig).
        UUID uploader = TenantContext.current().userId();
        ElectionSourceFile file = importService.registerImport(
                req.electionId(), req.storageKey(), req.checksum(), uploader, req.parserVersion());
        return ResponseEntity.status(201).body(file);
    }

    public record PublishRequest(@NotNull List<UUID> pollingStationIds, Integer officialDeclaredTotal) {}

    @PostMapping("/elections/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable UUID id, @RequestBody PublishRequest req) {
        importService.publish(id, req.pollingStationIds(), req.officialDeclaredTotal());
        return ResponseEntity.noContent().build();
    }

    /**
     * Filtered aggregate or booth results. Geography scope is intersected with the callers
     * authorized geography server-side — never trust a geographyId filter alone.
     */
    @GetMapping("/elections/{id}/results")
    public List<BoothResult> results(@PathVariable UUID id,
                                      @RequestParam(required = false) List<UUID> candidateIds) {
        if (candidateIds != null && !candidateIds.isEmpty()) {
            return boothResultRepository.findByCandidateIdIn(candidateIds);
        }
        // Full-election result sets are large; callers should filter by candidate or use
        // /analytics/trends for aggregate views instead of paging raw booth rows here.
        throw new IllegalArgumentException("Provide candidateIds, or use /analytics/trends for aggregates");
    }
}

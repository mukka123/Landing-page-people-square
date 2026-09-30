package com.politicalops.elections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Orchestrates "Election import" (HLD workflow table): register source -> OCR/parse (out of
 * process, via the AI orchestrator for document extraction) -> validate totals -> map booths
 * -> steward review -> publish. This service owns registration + the publish gate; the actual
 * OCR/parsing step is a background worker (see ai/ and the AIExecution audit trail) that calls
 * back into recordParsedResults once parsing completes.
 */
@Service
public class ElectionImportService {

    private final ElectionSourceFileRepository sourceFileRepository;
    private final ElectionRepository electionRepository;
    private final ReconciliationService reconciliationService;

    public ElectionImportService(ElectionSourceFileRepository sourceFileRepository,
                                  ElectionRepository electionRepository,
                                  ReconciliationService reconciliationService) {
        this.sourceFileRepository = sourceFileRepository;
        this.electionRepository = electionRepository;
        this.reconciliationService = reconciliationService;
    }

    @Transactional
    public ElectionSourceFile registerImport(UUID electionId, String storageKey, String checksum,
                                              UUID uploadedBy, String parserVersion) {
        ElectionSourceFile file = new ElectionSourceFile(electionId, storageKey, checksum, uploadedBy, parserVersion);
        // Landing-zone write is immutable per HLD data zones — we only ever add new source
        // file rows, never mutate storageKey/checksum after creation.
        return sourceFileRepository.save(file);
    }

    /** Called by the parsing worker once OCR/table extraction produces booth-level rows. */
    @Transactional
    public void markParsed(UUID sourceFileId) {
        ElectionSourceFile file = sourceFileRepository.findById(sourceFileId)
                .orElseThrow(() -> new com.politicalops.common.NotFoundException("Source file not found"));
        file.markStatus(ElectionSourceFile.Status.PARSED);
        sourceFileRepository.save(file);
    }

    /**
     * Publish gate: every station must reconcile before an election moves to PUBLISHED and
     * becomes visible to analytics. This is the one place "steward review" becomes binding.
     */
    @Transactional
    public void publish(UUID electionId, java.util.List<UUID> pollingStationIds, Integer officialDeclaredTotal) {
        reconciliationService.assertReadyForPublication(pollingStationIds, officialDeclaredTotal);
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new com.politicalops.common.NotFoundException("Election not found"));
        election.publish();
        electionRepository.save(election);
    }
}

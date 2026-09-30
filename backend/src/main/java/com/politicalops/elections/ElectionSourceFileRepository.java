package com.politicalops.elections;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ElectionSourceFileRepository extends JpaRepository<ElectionSourceFile, UUID> {}

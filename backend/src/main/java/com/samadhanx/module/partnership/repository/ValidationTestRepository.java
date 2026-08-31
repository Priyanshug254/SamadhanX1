package com.samadhanx.module.partnership.repository;

import com.samadhanx.module.partnership.entity.ValidationTest;
import com.samadhanx.module.partnership.entity.enums.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ValidationTestRepository extends JpaRepository<ValidationTest, UUID> {
    List<ValidationTest> findByProposalId(UUID proposalId);
    boolean existsByProposalIdAndTestResult(UUID proposalId, TestResult testResult);
    long countByTestResult(TestResult testResult);
}

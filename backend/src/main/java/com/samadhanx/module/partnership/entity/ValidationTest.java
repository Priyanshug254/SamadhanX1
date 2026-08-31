package com.samadhanx.module.partnership.entity;

import com.samadhanx.module.partnership.entity.enums.TestResult;
import com.samadhanx.module.partnership.entity.enums.TestType;
import com.samadhanx.module.solution.entity.Proposal;
import com.samadhanx.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "validation_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 50)
    private TestType testType;

    @Column(name = "test_environment", nullable = false)
    private String testEnvironment;

    @Builder.Default
    @Column(name = "test_date", nullable = false)
    private Instant testDate = Instant.now();

    @Column(name = "evaluator_name", nullable = false, length = 150)
    private String evaluatorName;

    @Column(name = "parameters_tested", nullable = false, columnDefinition = "TEXT")
    private String parametersTested;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_result", nullable = false, length = 30)
    private TestResult testResult;

    @Column(name = "issues_identified", columnDefinition = "TEXT")
    private String issuesIdentified;

    @Column(name = "corrective_actions", columnDefinition = "TEXT")
    private String correctiveActions;

    @Column(name = "evidence_document_url", length = 500)
    private String evidenceDocumentUrl;

    @Column(name = "validation_remarks", columnDefinition = "TEXT")
    private String validationRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationTest that = (ValidationTest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

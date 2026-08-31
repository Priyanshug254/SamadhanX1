package com.samadhanx.module.challenge.entity;

import com.samadhanx.module.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Challenge Endorsement entity capturing citizen upvotes and confirmation of community impact.
 */
@Entity
@Table(
        name = "challenge_endorsements",
        uniqueConstraints = @UniqueConstraint(name = "uq_challenge_user_endorsement", columnNames = {"challenge_id", "user_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeEndorsement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "comment", length = 500)
    private String comment;

    @Builder.Default
    @Column(name = "is_affected", nullable = false)
    private boolean affected = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeEndorsement that = (ChallengeEndorsement) o;
        return Objects.equals(challenge != null ? challenge.getId() : null, that.challenge != null ? that.challenge.getId() : null) &&
                Objects.equals(user != null ? user.getId() : null, that.user != null ? that.user.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challenge != null ? challenge.getId() : null, user != null ? user.getId() : null);
    }
}

package com.samadhanx.module.organization.entity;

import com.samadhanx.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Domain / Sector entity representing societal challenge focus areas
 * (e.g. Water & Sanitation, Agri-Tech, Clean Energy, Healthcare, etc.)
 */
@Entity
@Table(name = "domains")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Domain extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Domain domain = (Domain) o;
        return Objects.equals(code, domain.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}

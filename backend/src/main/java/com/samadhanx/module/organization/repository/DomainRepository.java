package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
    Optional<Domain> findByCode(String code);
    boolean existsByCode(String code);
    List<Domain> findAllByActiveTrue();
}

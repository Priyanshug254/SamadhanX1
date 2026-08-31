package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.UniversityProfile;
import com.samadhanx.module.organization.entity.enums.InstitutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UniversityProfileRepository extends JpaRepository<UniversityProfile, UUID> {
    Optional<UniversityProfile> findByAisheCodeIgnoreCase(String aisheCode);
    boolean existsByAisheCodeIgnoreCase(String aisheCode);
    List<UniversityProfile> findByInstitutionType(InstitutionType institutionType);
    List<UniversityProfile> findByHasIncubationCentreTrue();
}

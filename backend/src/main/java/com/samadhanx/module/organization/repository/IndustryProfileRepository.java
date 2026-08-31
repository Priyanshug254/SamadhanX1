package com.samadhanx.module.organization.repository;

import com.samadhanx.module.organization.entity.IndustryProfile;
import com.samadhanx.module.organization.entity.enums.CompanyStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IndustryProfileRepository extends JpaRepository<IndustryProfile, UUID> {
    List<IndustryProfile> findByCompanyStage(CompanyStage companyStage);
    List<IndustryProfile> findByDpiitRecognizedTrue();
}

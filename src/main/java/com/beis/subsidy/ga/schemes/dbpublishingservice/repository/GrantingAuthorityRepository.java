package com.beis.subsidy.ga.schemes.dbpublishingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.GrantingAuthority;

public interface GrantingAuthorityRepository
		extends JpaRepository<GrantingAuthority, Long>, JpaSpecificationExecutor<GrantingAuthority> {

	GrantingAuthority findBygaId(Long gaId);
	
	GrantingAuthority findBygrantingAuthorityName(String grantingAuthorityName);

	GrantingAuthority findByGrantingAuthorityName(String Name);
}

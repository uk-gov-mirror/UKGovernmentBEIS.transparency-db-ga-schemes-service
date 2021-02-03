package com.beis.subsidy.ga.schemes.dbpublishingservice.service;


import com.beis.subsidy.ga.schemes.dbpublishingservice.exception.SearchResultNotFoundException;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.LegalBasis;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeDetailsRequest;
import com.beis.subsidy.ga.schemes.dbpublishingservice.request.SchemeSearchInput;
import com.beis.subsidy.ga.schemes.dbpublishingservice.response.SearchSubsidyResultsResponse;
import com.beis.subsidy.ga.schemes.dbpublishingservice.service.impl.SubsidySchemeService;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SchemeSpecificationUtils;
import com.beis.subsidy.ga.schemes.dbpublishingservice.util.SearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class SubsidySchemeServiceImpl implements SubsidySchemeService {
    @Autowired
    private SubsidyMeasureRepository subsidyMeasureRepository;

    @Autowired
    private GrantingAuthorityRepository gaRepository;

    @Override
    public SearchSubsidyResultsResponse findMatchingSubsidySchemeDetails(SchemeSearchInput searchInput) {
        Specification<SubsidyMeasure> schemeSpecifications = getSpecificationSchemeDetails(searchInput);

        List<Sort.Order> orders = getOrderByCondition(searchInput.getSortBy());

        Pageable pagingSortSchemes = PageRequest.of(searchInput.getPageNumber() - 1, searchInput.getTotalRecordsPerPage(), Sort.by(orders));

        Page<SubsidyMeasure> pageAwards = subsidyMeasureRepository.findAll(schemeSpecifications, pagingSortSchemes);

        List<SubsidyMeasure> schemeResults = pageAwards.getContent();

        SearchSubsidyResultsResponse searchResults = null;

        if (!schemeResults.isEmpty()) {
            searchResults = new SearchSubsidyResultsResponse(schemeResults, pageAwards.getTotalElements(),
                    pageAwards.getNumber() + 1, pageAwards.getTotalPages(), schemeCounts(schemeResults));
        } else {
            searchResults = new SearchSubsidyResultsResponse(schemeCounts(schemeResults));
        }
        return searchResults;
    }

    @Override
    public ResponseEntity<Object> addSubsidySchemeDetails(SchemeDetailsRequest scheme) {
        SubsidyMeasure schemeToSave = new SubsidyMeasure();
        LegalBasis legalBasis = new LegalBasis();
        if(!StringUtils.isEmpty(scheme.getSubsidyMeasureTitle())){
            schemeToSave.setSubsidyMeasureTitle(scheme.getSubsidyMeasureTitle());
        }
       /* if(!StringUtils.isEmpty(scheme.getApprovedBy())){
            schemeToSave.setApprovedBy(scheme.getApprovedBy());
        }*/
        if(!StringUtils.isEmpty(scheme.getBudget())){
            schemeToSave.setBudget(scheme.getBudget());
        }
       /* if(!StringUtils.isEmpty(scheme.getCreatedBy())){
            schemeToSave.setCreatedBy(scheme.getCreatedBy());
        }*/
        if(scheme.getDuration() != null){
            schemeToSave.setDuration(scheme.getDuration());
        }
        if(scheme.getStartDate() != null){
            schemeToSave.setStartDate(scheme.getStartDate());
        }
        if(scheme.getEndDate() != null){
            schemeToSave.setEndDate(scheme.getEndDate());
        }
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLink())){
            schemeToSave.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        }
        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeToSave.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeToSave.setStatus(scheme.getStatus());
        }
        if(! StringUtils.isEmpty(scheme.getGaName())){
            Long gaId = gaRepository.findByGrantingAuthorityName(scheme.getGaName()).getGaId();
            schemeToSave.setGaId(gaId);
        }
        if(scheme.getPublishedMeasureDate() != null){
            schemeToSave.setPublishedMeasureDate(scheme.getPublishedMeasureDate());
        }
        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        schemeToSave.setLastModifiedTimestamp(LocalDate.now());

        legalBasis.setLastModifiedTimestamp(new Date());
        legalBasis.setCreatedTimestamp(new Date());
        schemeToSave.setLegalBases(legalBasis);
        legalBasis.setSubsidyMeasure(schemeToSave);

        SubsidyMeasure savedScheme = subsidyMeasureRepository.save(schemeToSave);
        log.info("Scheme saved successfully with Id : "+savedScheme.getScNumber());
        return ResponseEntity.status(200).build();
    }

    @Override
    public ResponseEntity<Object> updateSubsidySchemeDetails(SchemeDetailsRequest scheme) {
        SubsidyMeasure schemeById = subsidyMeasureRepository.findById(scheme.getScNumber()).get();
        LegalBasis legalBasis = schemeById.getLegalBases();
        if (Objects.isNull(schemeById)) {
            throw new SearchResultNotFoundException("Scheme details not found::" + scheme.getScNumber());
        }
        if(!StringUtils.isEmpty(scheme.getSubsidyMeasureTitle())){
            schemeById.setSubsidyMeasureTitle(scheme.getSubsidyMeasureTitle());
        }
        if(!StringUtils.isEmpty(scheme.getApprovedBy())){
            schemeById.setApprovedBy(scheme.getApprovedBy());
        }
        if(!StringUtils.isEmpty(scheme.getBudget())){
            schemeById.setBudget(scheme.getBudget());
        }
        if(!StringUtils.isEmpty(scheme.getCreatedBy())){
            schemeById.setCreatedBy(scheme.getCreatedBy());
        }
        if(scheme.getDuration() != null){
            schemeById.setDuration(scheme.getDuration());
        }
        if(scheme.getStartDate() != null){
            schemeById.setStartDate(scheme.getStartDate());
        }
        if(scheme.getEndDate() != null){
            schemeById.setEndDate(scheme.getEndDate());
        }
        if(!StringUtils.isEmpty(scheme.getGaSubsidyWebLink())){
            schemeById.setGaSubsidyWebLink(scheme.getGaSubsidyWebLink());
        }
        if(scheme.isAdhoc() || !scheme.isAdhoc()){
            schemeById.setAdhoc(scheme.isAdhoc());
        }
        if(!StringUtils.isEmpty(scheme.getStatus())){
            schemeById.setStatus(scheme.getStatus());
        }
        if(!StringUtils.isEmpty(scheme.getLegalBasisText())){
            legalBasis.setLegalBasisText(scheme.getLegalBasisText());
        }
        schemeById.setLastModifiedTimestamp(LocalDate.now());

        legalBasis.setLastModifiedTimestamp(new Date());
        legalBasis.setCreatedTimestamp(new Date());
        //legalBasis.setStatus("Draft");
        schemeById.setLegalBases(legalBasis);
        legalBasis.setSubsidyMeasure(schemeById);

        subsidyMeasureRepository.save(schemeById);
        return ResponseEntity.status(200).build();
    }


    private Map<String, Long> schemeCounts(List<SubsidyMeasure> schemeList) {
        long allScheme = schemeList.size();
        long activeScheme = 0;
        long inactiveScheme = 0;

        if(schemeList != null && schemeList.size() > 0){
            for(SubsidyMeasure sm : schemeList){
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_ACTIVE)){
                    activeScheme++;
                }
                if(sm.getStatus().equalsIgnoreCase(AccessManagementConstant.SM_INACTIVE)){
                    inactiveScheme++;
                }
            }
        }
        Map<String, Long> smUserActivityCount = new HashMap<>();
        smUserActivityCount.put("allScheme",allScheme);
        smUserActivityCount.put("activeScheme",activeScheme);
        smUserActivityCount.put("inactiveScheme",inactiveScheme);
        return smUserActivityCount;
    }

    public Specification<SubsidyMeasure>  getSpecificationSchemeDetails(SchemeSearchInput searchInput) {
        String searchName = getSearchName(searchInput);
        Specification<SubsidyMeasure> schemeSpecifications = Specification
                .where(
                        SearchUtils.checkNullOrEmptyString(searchName)
                                ? null : SchemeSpecificationUtils.subsidySchemeName(searchName.trim())
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null : SchemeSpecificationUtils.subsidyNumber(searchName.trim()))
                                .or(SearchUtils.checkNullOrEmptyString(searchName)
                                        ? null :SchemeSpecificationUtils.grantingAuthorityName(searchName.trim()))
                )
                .and(SearchUtils.checkNullOrEmptyString(searchInput.getStatus())
                        ? null : SchemeSpecificationUtils.schemeByStatus(searchInput.getStatus().trim()));
        return schemeSpecifications;
    }
    private String getSearchName(SchemeSearchInput searchInput){
        String searchName = "";
        if(searchInput.getSubsidySchemeName() != null && !searchInput.getSubsidySchemeName().isEmpty()){
            searchName = searchInput.getSubsidySchemeName();
        }
        if(searchInput.getScNumber() != null && !searchInput.getScNumber().isEmpty()){
            searchName = searchInput.getScNumber();
        }
        if(searchInput.getGaName() != null && !searchInput.getGaName().isEmpty()){
            searchName = searchInput.getGaName();
        }
        return searchName;
    }
    private List<Sort.Order> getOrderByCondition(String[] sortBy) {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        if (sortBy != null && sortBy.length > 0 && sortBy[0].contains(",")) {
            for (String sortOrder : sortBy) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(getSortDirection("desc"), "lastModifiedTimestamp"));
        }
        return orders;
    }

    private Sort.Direction getSortDirection(String direction) {
        Sort.Direction sortDir = Sort.Direction.ASC;
        if (direction.equals("desc")) {
            sortDir = Sort.Direction.DESC;
        }
        return sortDir;
    }
}
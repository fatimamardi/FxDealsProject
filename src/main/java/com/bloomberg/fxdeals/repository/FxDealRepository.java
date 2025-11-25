package com.bloomberg.fxdeals.repository;

import com.bloomberg.fxdeals.model.FxDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FX Deal operations
 */
@Repository
public interface FxDealRepository extends JpaRepository<FxDeal, Long> {

    /**
     * Find a deal by its unique identifier
     * 
     * @param dealUniqueId the unique deal identifier
     * @return Optional containing the deal if found
     */
    Optional<FxDeal> findByDealUniqueId(String dealUniqueId);

    /**
     * Check if a deal with the given unique ID exists
     * 
     * @param dealUniqueId the unique deal identifier
     * @return true if deal exists, false otherwise
     */
    boolean existsByDealUniqueId(String dealUniqueId);

    /**
     * Find all deals by deal unique IDs
     * 
     * @param dealUniqueIds list of unique deal identifiers
     * @return list of existing deals
     */
    @Query("SELECT d FROM FxDeal d WHERE d.dealUniqueId IN :dealUniqueIds")
    List<FxDeal> findByDealUniqueIds(@Param("dealUniqueIds") List<String> dealUniqueIds);
}


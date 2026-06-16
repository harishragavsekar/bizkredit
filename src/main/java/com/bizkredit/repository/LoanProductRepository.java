package com.bizkredit.repository;

import com.bizkredit.entity.LoanProduct;
import com.bizkredit.enums.LoanProductStatus;
import com.bizkredit.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    boolean existsByProductCode(String productCode);

    Optional<LoanProduct> findByProductCode(String productCode);

    List<LoanProduct> findByStatus(LoanProductStatus status);

    List<LoanProduct> findByProductTypeAndStatus(ProductType productType, LoanProductStatus status);
}

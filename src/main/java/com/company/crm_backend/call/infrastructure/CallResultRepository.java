package com.company.crm_backend.call.infrastructure;

import com.company.crm_backend.call.domain.CallResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallResultRepository extends JpaRepository<CallResult, Long> { }
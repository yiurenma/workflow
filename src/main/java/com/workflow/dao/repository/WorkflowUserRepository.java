package com.workflow.dao.repository;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

@Repository
@RequestMapping(value = "/workflow/")
@RepositoryRestResource(path = "workflow-user")
@Hidden
@Tag(name = "DB Repository", description = "[Tracking] Workflow user repository")
public interface WorkflowUserRepository extends
        QuerydslPredicateExecutor<WorkflowUser>,
        JpaRepository<WorkflowUser, Long>,
        JpaSpecificationExecutor<WorkflowUser> {
}

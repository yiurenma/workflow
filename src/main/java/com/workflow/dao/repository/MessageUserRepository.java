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
@RepositoryRestResource(path = "user")
@Hidden
@Tag(name = "DB Repository", description = "[Tracking] Message user repository")
public interface MessageUserRepository extends
        QuerydslPredicateExecutor<MessageUser>,
        JpaRepository<MessageUser, Long>,
        JpaSpecificationExecutor<MessageUser> {
}

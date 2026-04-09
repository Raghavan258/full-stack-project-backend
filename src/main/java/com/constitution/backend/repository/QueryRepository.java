package com.constitution.backend.repository;

import com.constitution.backend.entity.Query;
import com.constitution.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    List<Query> findByCitizen(User citizen);
    List<Query> findByAssignedTo(User assignedTo);
    List<Query> findByStatus(Query.QueryStatus status);
    List<Query> findByAssignedToOrStatus(User assignedTo, Query.QueryStatus status);
    long countByStatus(Query.QueryStatus status);
}

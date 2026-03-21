package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentJpaRepository extends JpaRepository<Content, UUID> {

    List<Content> findByStatus(String status);

    List<Content> findByType(String type);

    List<Content> findByStatusAndType(String status, String type);

    Page<Content> findAll(Pageable pageable);

    List<Content> findByTitleContainingIgnoreCase(String title);

    boolean existsByTitle(String title);
}

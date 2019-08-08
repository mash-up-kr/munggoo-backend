package com.mashup.munggoo.highlight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {
    List<Highlight> findByFileId(Long fileId);
    void deleteAllByFileId(Long fileId);
}

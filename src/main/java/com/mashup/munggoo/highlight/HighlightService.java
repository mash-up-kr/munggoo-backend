package com.mashup.munggoo.highlight;

import com.mashup.munggoo.exception.BadRequestException;
import com.mashup.munggoo.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HighlightService {
    private final HighlightRepository highlightRepository;

    public HighlightsDto save(Long fileId, ReqHighlightsDto reqHighlightsDto) {
        if (reqHighlightsDto.getHighlights().isEmpty()) {
            throw new BadRequestException("Request Body Is Empty.");
        }
        List<Highlight> highlights = highlightRepository.findByFileId(fileId);
        if (!highlights.isEmpty()) {
            highlightRepository.deleteAllByFileId(fileId);
        }
        return new HighlightsDto(highlightRepository.saveAll(reqHighlightsDto.getHighlights().stream()
                .map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto))
                .collect(Collectors.toList())));
    }

    public ResHighlightsDto getHighlights(Long fileId) {
        List<Highlight> highlights = highlightRepository.findByFileId(fileId);
        if (highlights.isEmpty()) {
            throw new NotFoundException("Highlight Does Not Exist.");
        }
        return new ResHighlightsDto(highlights.stream().map(ResHighlightDto::new).collect(Collectors.toList()));
    }
}

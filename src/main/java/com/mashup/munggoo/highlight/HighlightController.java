package com.mashup.munggoo.highlight;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/devices/{device-id}/files/{file-id}/highlights")
public class HighlightController {
    private final HighlightService highlightService;

    @PostMapping
    public ResponseEntity<HighlightsDto> saveHighlights(@PathVariable(value = "file-id") Long fileId, @RequestBody ReqHighlightsDto reqHighlightsDto) {
        return ResponseEntity.status(HttpStatus.OK).body(highlightService.save(fileId, reqHighlightsDto));
    }

    @GetMapping
    public ResponseEntity<ResHighlightsDto> getHighlights(@PathVariable(value = "file-id") Long fileId) {
        return ResponseEntity.status(HttpStatus.OK).body(highlightService.getHighlights(fileId));
    }
}

package com.mashup.munggoo.highlight;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/devices/{device-id}/files/{file-id}/highlights")
public class HighlightController {
    private final HighlightService highlightService;

    @PostMapping
    public ResponseEntity<List<Highlight>> saveHighlights(@PathVariable(value = "file-id") Long fileId, @RequestBody List<ReqHighlightDto> reqHighlightDtos) {
        return ResponseEntity.status(HttpStatus.OK).body(highlightService.save(fileId, reqHighlightDtos));
    }

    @GetMapping
    public ResponseEntity<List<ResHighlightDto>> getHighlights(@PathVariable(value = "file-id") Long fileId) {
        return ResponseEntity.status(HttpStatus.OK).body(highlightService.getHighlights(fileId));
    }
}

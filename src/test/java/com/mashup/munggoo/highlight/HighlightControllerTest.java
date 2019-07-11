package com.mashup.munggoo.highlight;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = HighlightController.class)
public class HighlightControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HighlightService highlightService;

    private Long fileId;

    private List<ReqHighlightDto> reqHighlightDtos;

    private List<Highlight> highlights;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        fileId = 1L;
        objectMapper = new ObjectMapper();
    }

    @Test
    public void saveHighlights() throws Exception {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        when(highlightService.save(any(), any())).thenReturn(highlights);
        mockMvc.perform(post("/v1/devices/{device-id}/files/{file-id}/highlights", 1L, fileId)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(reqHighlightDtos)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[0].id").hasJsonPath())
                .andExpect(jsonPath("$.[0].fileId").value(fileId))
                .andExpect(jsonPath("$.[0].startIndex").value(reqHighlightDtos.get(0).getStartIndex()))
                .andExpect(jsonPath("$.[0].endIndex").value(reqHighlightDtos.get(0).getEndIndex()))
                .andExpect(jsonPath("$.[0].content").value(reqHighlightDtos.get(0).getContent()))
                .andExpect(jsonPath("$.[0].type").value(HighlightType.WORD.toString()))
                .andExpect(jsonPath("$.[0].isImportant").value(Boolean.TRUE))
                .andExpect(jsonPath("$.[1].id").hasJsonPath())
                .andExpect(jsonPath("$.[1].fileId").value(fileId))
                .andExpect(jsonPath("$.[1].startIndex").value(reqHighlightDtos.get(1).getStartIndex()))
                .andExpect(jsonPath("$.[1].endIndex").value(reqHighlightDtos.get(1).getEndIndex()))
                .andExpect(jsonPath("$.[1].content").value(reqHighlightDtos.get(1).getContent()))
                .andExpect(jsonPath("$.[1].type").value(HighlightType.SENTENCE.toString()))
                .andExpect(jsonPath("$.[1].isImportant").value(Boolean.FALSE))
                .andDo(print());
    }
}

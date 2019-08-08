package com.mashup.munggoo.highlight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashup.munggoo.exception.BadRequestException;
import com.mashup.munggoo.exception.NotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private ReqHighlightsDto reqHighlightsDto;

    private HighlightsDto highlightsDto;

    private ResHighlightsDto resHighlightsDto;

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
        reqHighlightsDto = new ReqHighlightsDto(reqHighlightDtos);
        highlightsDto = new HighlightsDto(reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList()));
        when(highlightService.save(any(), any())).thenReturn(highlightsDto);
        mockMvc.perform(post("/v1/devices/{device-id}/files/{file-id}/highlights", 1L, fileId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(objectMapper.writeValueAsString(reqHighlightsDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.highlights").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[0].id").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[0].fileId").value(fileId))
                .andExpect(jsonPath("$.highlights.[0].startIndex").value(reqHighlightDtos.get(0).getStartIndex()))
                .andExpect(jsonPath("$.highlights.[0].endIndex").value(reqHighlightDtos.get(0).getEndIndex()))
                .andExpect(jsonPath("$.highlights.[0].content").value(reqHighlightDtos.get(0).getContent()))
                .andExpect(jsonPath("$.highlights.[0].type").value(HighlightType.WORD.toString()))
                .andExpect(jsonPath("$.highlights.[0].isImportant").value(Boolean.TRUE))
                .andExpect(jsonPath("$.highlights.[1].id").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[1].fileId").value(fileId))
                .andExpect(jsonPath("$.highlights.[1].startIndex").value(reqHighlightDtos.get(1).getStartIndex()))
                .andExpect(jsonPath("$.highlights.[1].endIndex").value(reqHighlightDtos.get(1).getEndIndex()))
                .andExpect(jsonPath("$.highlights.[1].content").value(reqHighlightDtos.get(1).getContent()))
                .andExpect(jsonPath("$.highlights.[1].type").value(HighlightType.SENTENCE.toString()))
                .andExpect(jsonPath("$.highlights.[1].isImportant").value(Boolean.FALSE))
                .andDo(print());
    }

    @Test
    public void saveEmptyHighlight() throws Exception {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightsDto = new ReqHighlightsDto(reqHighlightDtos);
        when(highlightService.save(any(), any())).thenThrow(new BadRequestException("Request Body Is Empty."));
        mockMvc.perform(post("/v1/devices/{device-id}/files/{file-id}/highlights", 1L, fileId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(objectMapper.writeValueAsString(reqHighlightsDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.msg").value("Request Body Is Empty."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }

    @Test
    public void getHighlights() throws Exception {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlightsDto = new HighlightsDto(reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList()));
        resHighlightsDto = new ResHighlightsDto(highlightsDto.getHighlights().stream().map(ResHighlightDto::new).collect(Collectors.toList()));
        when(highlightService.getHighlights(fileId)).thenReturn(resHighlightsDto);
        mockMvc.perform(get("/v1/devices/{device-id}/files/{file-id}/highlights", 1L, fileId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.highlights").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[0].id").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[0].startIndex").value(reqHighlightDtos.get(0).getStartIndex()))
                .andExpect(jsonPath("$.highlights.[0].endIndex").value(reqHighlightDtos.get(0).getEndIndex()))
                .andExpect(jsonPath("$.highlights.[0].content").value(reqHighlightDtos.get(0).getContent()))
                .andExpect(jsonPath("$.highlights.[0].isImportant").value(Boolean.TRUE))
                .andExpect(jsonPath("$.highlights.[1].id").hasJsonPath())
                .andExpect(jsonPath("$.highlights.[1].startIndex").value(reqHighlightDtos.get(1).getStartIndex()))
                .andExpect(jsonPath("$.highlights.[1].endIndex").value(reqHighlightDtos.get(1).getEndIndex()))
                .andExpect(jsonPath("$.highlights.[1].content").value(reqHighlightDtos.get(1).getContent()))
                .andExpect(jsonPath("$.highlights.[1].isImportant").value(Boolean.FALSE))
                .andDo(print());
    }

    @Test
    public void getEmptyHighlight() throws Exception {
        when(highlightService.getHighlights(any())).thenThrow(new NotFoundException("Highlight Does Not Exist."));
        mockMvc.perform(get("/v1/devices/{device-id}/files/{file-id}/highlights", 1L, fileId)
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.msg").value("Highlight Does Not Exist."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }
}

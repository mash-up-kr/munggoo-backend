package com.mashup.munggoo.highlight;

import com.mashup.munggoo.exception.BadRequestException;
import com.mashup.munggoo.exception.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HighlightServiceTest {
    @Autowired
    private HighlightService highlightService;

    @MockBean
    private HighlightRepository highlightRepository;


    private Long fileId;

    private List<ReqHighlightDto> reqHighlightDtos;

    private List<Highlight> highlights;

    private ResHighlightsDto resHighlightsDto;

    @Before
    public void setUp() {
        fileId = 1L;
    }

    @Test
    public void saveHighlights() {
        // 비어있는 상태에서 저장
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        given(highlightRepository.findByFileId(any())).willReturn(new ArrayList<>());
        given(highlightRepository.saveAll(anyCollection())).willReturn(highlights);
        HighlightsDto savedHighlights = highlightService.save(fileId, new ReqHighlightsDto(reqHighlightDtos));
        assertThat(savedHighlights.getHighlights().size()).isEqualTo(reqHighlightDtos.size());
        assertThat(savedHighlights.getHighlights().get(0).getStartIndex()).isEqualTo(10L);
        assertThat(savedHighlights.getHighlights().get(1).getStartIndex()).isEqualTo(30L);

        // 이미 하이라이트 있는 상태에서 저장
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        reqHighlightDtos.add(new ReqHighlightDto(40L, 50L, "냠냠", 0));
        List<Highlight> newHighlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        given(highlightRepository.saveAll(anyCollection())).willReturn(newHighlights);
        given(highlightRepository.findByFileId(any())).willReturn(highlights);
        savedHighlights = highlightService.save(fileId, new ReqHighlightsDto(reqHighlightDtos));
        assertThat(savedHighlights.getHighlights().size()).isEqualTo(reqHighlightDtos.size());
        assertThat(savedHighlights.getHighlights().get(0).getStartIndex()).isEqualTo(10L);
        assertThat(savedHighlights.getHighlights().get(1).getStartIndex()).isEqualTo(30L);
    }

    @Test(expected = BadRequestException.class)
    public void saveEmptyHighlight() {
        reqHighlightDtos = new ArrayList<>();
        highlightService.save(fileId, new ReqHighlightsDto(reqHighlightDtos));
    }

    @Test
    public void getHighlights() {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        given(highlightRepository.findByFileId(any())).willReturn(highlights);
        resHighlightsDto = highlightService.getHighlights(fileId);
        assertThat(resHighlightsDto.getHighlights().size()).isEqualTo(reqHighlightDtos.size());
        assertThat(resHighlightsDto.getHighlights().get(0).getStartIndex()).isEqualTo(reqHighlightDtos.get(0).getStartIndex());
        assertThat(resHighlightsDto.getHighlights().get(0).getContent()).isEqualTo(reqHighlightDtos.get(0).getContent());
        assertThat(resHighlightsDto.getHighlights().get(0).getIsImportant()).isEqualTo(Boolean.TRUE);
        assertThat(resHighlightsDto.getHighlights().get(1).getStartIndex()).isEqualTo(reqHighlightDtos.get(1).getStartIndex());
        assertThat(resHighlightsDto.getHighlights().get(1).getContent()).isEqualTo(reqHighlightDtos.get(1).getContent());
        assertThat(resHighlightsDto.getHighlights().get(1).getIsImportant()).isEqualTo(Boolean.FALSE);
    }

    @Test(expected = NotFoundException.class)
    public void getEmptyHighlight() {
        highlights = new ArrayList<>();
        given(highlightRepository.findByFileId(any())).willReturn(highlights);
        highlightService.getHighlights(fileId);
    }
}

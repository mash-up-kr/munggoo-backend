package com.mashup.munggoo.highlight;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class HighlightRepositoryTest {
    @Autowired
    private HighlightRepository highlightRepository;

    private Long fileId;

    private List<ReqHighlightDto> reqHighlightDtos;

    private List<Highlight> highlights;

    @Before
    public void setUp() {
        fileId = 1L;
    }

    @Test
    public void findHighlightsByFileId() {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        highlightRepository.saveAll(highlights);
        List<Highlight> savedHighlights = highlightRepository.findByFileId(fileId);
        assertThat(savedHighlights.size()).isEqualTo(reqHighlightDtos.size());
        assertThat(savedHighlights.get(0).getFileId()).isEqualTo(fileId);
        assertThat(savedHighlights.get(0).getType()).isEqualTo(HighlightType.WORD);
        assertThat(savedHighlights.get(0).getIsImportant()).isEqualTo(Boolean.TRUE);
        assertThat(savedHighlights.get(1).getFileId()).isEqualTo(fileId);
        assertThat(savedHighlights.get(1).getType()).isEqualTo(HighlightType.SENTENCE);
        assertThat(savedHighlights.get(1).getIsImportant()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void deleteAllByFileId() {
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights = reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId, reqHighlightDto)).collect(Collectors.toList());
        reqHighlightDtos = new ArrayList<>();
        reqHighlightDtos.add(new ReqHighlightDto(10L, 20L, "안녕", 1));
        reqHighlightDtos.add(new ReqHighlightDto(30L, 40L, "안녕하세요 반갑습니다.", 0));
        highlights.addAll(reqHighlightDtos.stream().map(reqHighlightDto -> Highlight.from(fileId + 1, reqHighlightDto)).collect(Collectors.toList()));
        highlightRepository.saveAll(highlights);
        highlightRepository.deleteAllByFileId(fileId);
        List<Highlight> savedHighlights1 = highlightRepository.findByFileId(fileId);
        List<Highlight> savedHighlights2 = highlightRepository.findByFileId(fileId + 1);
        assertThat(savedHighlights1.size()).isEqualTo(0);
        assertThat(savedHighlights2.size()).isEqualTo(2);
    }
}

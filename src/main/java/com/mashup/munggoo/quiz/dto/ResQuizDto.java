package com.mashup.munggoo.quiz.dto;

import com.mashup.munggoo.quiz.domain.Quiz;
import lombok.Getter;

@Getter
public class ResQuizDto {
    private Long startIndex;
    private Long endIndex;

    public ResQuizDto(Quiz quiz){
        startIndex = quiz.getStartIndex();
        endIndex = quiz.getEndIndex();
    }
}

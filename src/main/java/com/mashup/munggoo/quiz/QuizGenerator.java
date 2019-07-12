package com.mashup.munggoo.quiz;

import com.mashup.munggoo.highlight.HighlightType;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class QuizGenerator {
    private static final Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

    public static List<Quiz> generateQuizSet(List<HighlightForQuizDto> highlights){
        List<Quiz> selectedQuiz = new ArrayList<>();
        List<QuizDto> candidateQuiz = new ArrayList<>();
        List<QuizDto> preliminaryQuiz = new ArrayList<>();

        Random r = new Random();

        for (HighlightForQuizDto highlight : highlights){
            if(highlight.getContent().trim().length() == 0) continue;

            if(highlight.getType() == HighlightType.WORD){
                QuizDto temp = new QuizDto(highlight.getFileId(),
                        highlight.getStartIndex(),
                        highlight.getEndIndex(),
                        highlight.getContent());
                if(highlight.getIsImportant())
                    selectedQuiz.add(Quiz.from(temp));
                else
                    candidateQuiz.add(temp);
                continue;
            }

            KomoranResult analyzeResultList = komoran.analyze(highlight.getContent());
            List<Token> tokenList = analyzeResultList.getTokenList();
            GeneratedQuizDto generatedQuizDto = selectWords(tokenList);
            List<QuizDto> quizList = new ArrayList<>();
            for(Token word : generatedQuizDto.selected){
                quizList.add(tokenToQuizDto(word, highlight));
            }
            if(highlight.getIsImportant()) {
                if(quizList.size() > 0) {
                    int selectOne = r.nextInt(quizList.size());
                    selectedQuiz.add(Quiz.from(quizList.get(selectOne)));
                    quizList.remove(selectOne);
                }
                else if(generatedQuizDto.preliminary.size() > 0) {
                    int selectOne = r.nextInt(generatedQuizDto.preliminary.size());
                    selectedQuiz.add(tokenToQuiz(generatedQuizDto.preliminary.get(selectOne), highlight));
                    generatedQuizDto.preliminary.remove(selectOne);
                }

            }
            candidateQuiz.addAll(quizList);
            if(selectedQuiz.size()+candidateQuiz.size() < QuizConfig.quizNum) {
                for(Token word : generatedQuizDto.preliminary){
                    preliminaryQuiz.add(tokenToQuizDto(word, highlight));
                }
            }
        }

        if(QuizConfig.quizNum > selectedQuiz.size()){
            int cnt = (QuizConfig.quizNum - selectedQuiz.size() > candidateQuiz.size() ?
                    candidateQuiz.size() : QuizConfig.quizNum - selectedQuiz.size());
            for(int i=0 ; i<cnt; i++){
                int newIdx = r.nextInt(candidateQuiz.size());
                selectedQuiz.add(Quiz.from(candidateQuiz.get(newIdx)));
                candidateQuiz.remove(newIdx);
            }
            if(selectedQuiz.size() < QuizConfig.quizNum){
                System.out.println("add preliminary quiz");
                cnt = (QuizConfig.quizNum - selectedQuiz.size() > preliminaryQuiz.size() ?
                        preliminaryQuiz.size() : QuizConfig.quizNum - selectedQuiz.size());
                for(int i=0 ; i<cnt; i++){
                    int newIdx = r.nextInt(preliminaryQuiz.size());
                    selectedQuiz.add(Quiz.from(preliminaryQuiz.get(newIdx)));
                    preliminaryQuiz.remove(newIdx);
                }
            }
        }
        else if(QuizConfig.quizNum < selectedQuiz.size()){
            int cnt = selectedQuiz.size() - QuizConfig.quizNum;
            for(int i=0 ; i<cnt; i++){
                selectedQuiz.remove(r.nextInt(selectedQuiz.size()));
            }

        }
        return selectedQuiz;
    }

    private static GeneratedQuizDto selectWords(List<Token> tokenList){
        GeneratedQuizDto result = new GeneratedQuizDto();
        Stack<Token> tokenStack = new Stack<>();

        for(Token token : tokenList){
            if(tokenStack.empty()){
                tokenStack.add(token);
                continue;
            }

            Token peek = tokenStack.peek();
            switch(token.getPos()){
                case "NNG":
                case "NNP":
                    switch(peek.getPos()){
                        case "XPN":
                        case "XSN":
                        case "NNG":
                        case "NNP":
                        case "NNB":
                        case "NN":
                            Token temp = tokenConcat(tokenStack.pop(), token, "NN");
                            tokenStack.push(temp);
                            continue;
                    }
                    break;
                case "ETM":
                    if(peek.getPos().startsWith("XS") || peek.getPos().startsWith("V")){
                        Token temp = tokenConcat(tokenStack.pop(), token, peek.getPos());
                        tokenStack.push(temp);
                        continue;
                    }
                    break;
                case "XSN":
                case "XSV":
                case "XSA":
                    if(peek.getPos().startsWith("XR") || peek.getPos().startsWith("NN")){
                        Token temp = tokenConcat(tokenStack.pop(), token,   token.getPos());
                        tokenStack.push(temp);
                        continue;
                    }
                    break;
                case "SL":
                    if(QuizConfig.engPassWords.contains(peek.getMorph().toLowerCase()) ||
                            QuizConfig.engPassWords.contains(token.getMorph().toLowerCase()))
                        break;
                    if(peek.getPos().equals("SL")){
                        Token temp = tokenConcat(tokenStack.pop(), token, "SL");
                        tokenStack.push(temp);
                        continue;
                    }
                    else if(peek.getMorph().equals("(") || peek.getPos().equals("SSL")){
                        Token temp = tokenConcat(tokenStack.pop(), token, "SSL");
                        tokenStack.push(temp);
                        continue;
                    }
                    break;
                case "SS":
                    if(token.getMorph().equals(")") && peek.getPos().equals("SSL")){
                        Token temp = tokenConcat(tokenStack.pop(), token, "SS");
                        if (tokenStack.peek().getPos().equals("NN") || tokenStack.peek().getPos().equals("NNP"))
                            temp = tokenConcat(tokenStack.pop(), temp, "NNSS");
                        tokenStack.push(temp);
                        continue;
                    }
                    if(token.getMorph().equals("\'") && peek.getPos().equals("SL")){
                        Token temp = tokenConcat(tokenStack.pop(), token, "SL");
                        tokenStack.push(temp);
                        continue;
                    }
                    break;
                case "SF":
                    if(token.getMorph().equals(".") && peek.getPos().equals("SSL")){
                        Token temp = tokenConcat(tokenStack.pop(), token, "SSL");
                        tokenStack.push(temp);
                        continue;
                    }
                    break;
            }
            tokenStack.add(token);
        }
        for (Token token : tokenStack){
            switch (token.getPos()){
                case "NN":
                case "NNP":
                case "NNSS":
//                    System.out.printf("`%s(%s)`", token.getMorph(), token.getPos());
                    result.selected.add(token);
                    break;
                case "SL":
                    if(!QuizConfig.engPassWords.contains(token.getMorph().toLowerCase()))
                        result.selected.add(token);
                    break;
                case "NNG":
                case "SN":
                    result.preliminary.add(token);
                    break;
            }
        }
        return result;
    }

    private static Token tokenConcat(Token first, Token second, String pos){
        first.setPos(pos);
        first.setMorph(first.getMorph()+" "+second.getMorph());
        first.setEndIndex(second.getEndIndex());
        return first;
    }

    private static QuizDto tokenToQuizDto(Token token, HighlightForQuizDto highlight){
        String content = highlight.getContent().substring(token.getBeginIndex(), token.getEndIndex());
        return new QuizDto(highlight.getFileId(),
                highlight.getStartIndex() + token.getBeginIndex(),
                highlight.getStartIndex() + token.getEndIndex(),
                content);
    }

    private static Quiz tokenToQuiz(Token token, HighlightForQuizDto highlight){
        String content = highlight.getContent().substring(token.getBeginIndex(), token.getEndIndex());
        return Quiz.from(new QuizDto(highlight.getFileId(),
                highlight.getStartIndex() + token.getBeginIndex(),
                highlight.getStartIndex() + token.getEndIndex(),
                content));
    }
}
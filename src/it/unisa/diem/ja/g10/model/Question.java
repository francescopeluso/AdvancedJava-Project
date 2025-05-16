package it.unisa.diem.ja.g10.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author david
 */
public class Question{
    private String question;
    private List<String> answers;
    private String correct;
    private String myAnswer;

    public Question(String question, String correct, String myAnswer) {
        this.question = question;
        this.correct = correct;
        this.myAnswer = myAnswer;
        answers = new ArrayList<>();
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public String getCorrect() {
        return correct;
    }

    public String getMyAnswer() {
        return myAnswer;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public void setMyAnswer(String myAnswer) {
        this.myAnswer = myAnswer;
    }
    
    
    
}

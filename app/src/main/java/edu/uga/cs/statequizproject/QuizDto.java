package edu.uga.cs.statequizproject;

import java.util.ArrayList;
import java.util.List;

public class QuizDto {
    private long quizId;
    private String quizDate;
    private int answeredCount;
    private int correctCount;
    private final List<QuizQuestion> questions = new ArrayList<>();

    public QuizDto(long quizId) {
        this.quizId = quizId;
    }

    public long getQuizId() { return quizId; }
    public void setQuizId(long quizId) { this.quizId = quizId; }

    public String getQuizDate() { return quizDate; }
    public void setQuizDate(String quizDate) { this.quizDate = quizDate; }

    public int getAnsweredCount() { return answeredCount; }
    public void setAnswserCount(int answeredCount){
        this.answeredCount = answeredCount;
    }
    public int getCorrectCount() { return correctCount; }
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    public int getTotalCount() { return questions.size(); }
    public List<QuizQuestion> getQuestions() { return questions; }

    public void addQuestion(QuizQuestion q) { questions.add(q); }
    public void recomputeCounters() {
        int ans = 0, corr = 0;
        for (QuizQuestion q : questions) {
            if (q.getUserAnswer() != null) {
                ans++;
                if (q.getUserAnswer().trim().equalsIgnoreCase(q.getCapital())) {
                    corr++;
                }
            }
        }
        this.answeredCount = ans;
        this.correctCount = corr;
    }
}

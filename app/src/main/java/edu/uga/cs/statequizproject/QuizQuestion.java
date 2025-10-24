package edu.uga.cs.statequizproject;

public class QuizQuestion extends StateQuestion {
    private long quizId;
    private String userAnswer;

    public QuizQuestion(long questionRowId,
                        String state,
                        String capital,
                        String secondCity,
                        String thirdCity,
                        long quizId,
                        String userAnswer) {
        super(questionRowId, state, capital, secondCity, thirdCity);
        this.quizId = quizId;
        this.userAnswer = userAnswer;
    }

    // Getters / setters
    public long getQuizId() { return quizId; }
    public void setQuizId(long quizId) { this.quizId = quizId; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}

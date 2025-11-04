package edu.uga.cs.statequizproject;

/*
* This QQ is a stateQuestion but with added attributes of
* userAnswer and quizId
* */
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

    // getters and setters
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
}

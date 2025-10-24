package edu.uga.cs.statequizproject;

public class Quiz {
    private long id;
    private String quizDate;
    private int correctCount;
    private int answeredCount;

    public Quiz(long id, String quizDate, int correctCount, int answeredCount) {
        this.id = id;
        this.quizDate = quizDate;
        this.correctCount = correctCount;
        this.answeredCount = answeredCount;
    }

    // getters
    public long getId(){
        return id;
    }
    public String getQuizDate() {
        return quizDate;
    }
    public int getCorrectCount() {
        return correctCount;
    }
    public int getAnsweredCount() {
        return answeredCount;
    }
    //setters and incs
    public void setQuizDate(String date){
        this.quizDate = date;
    }
    public void incrementAnsweredCount(){
        this.answeredCount++;
    }
    public void incrementCorrectCount(){
        this.correctCount++;
    }

}


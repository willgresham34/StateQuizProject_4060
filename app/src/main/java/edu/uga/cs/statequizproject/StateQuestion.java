package edu.uga.cs.statequizproject;

public class StateQuestion {

    private long id;
    private String state;
    private String capital;
    private String secondCity;
    private String thirdCity;

    public StateQuestion(long id, String state,  String capital, String secondCity, String thirdCity) {
        this.id = id;
        this.state = state;
        this.capital = capital;
        this.secondCity = secondCity;
        this.thirdCity = thirdCity;
    }

    public long getId() {
        return  id;
    }

    public String getState() {
        return state;
    }
    public String getCapital() {
        return capital;
    }
    public String getSecondCity() {
        return secondCity;
    }
    public String getThirdCity() {
        return thirdCity;
    }
}

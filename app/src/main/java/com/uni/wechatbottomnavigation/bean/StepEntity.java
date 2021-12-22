package com.uni.wechatbottomnavigation.bean;

public class StepEntity {
    private String curDate;
    private String steps;

    public StepEntity(String curDate, String steps) {
        this.curDate = curDate;
        this.steps = steps;
    }

    public StepEntity() {
        this.curDate=null;
        this.steps=null;
    }


    @Override
    public String toString() {
        return "StepEntity{" +
                "curData='" + curDate + '\'' +
                ", steps='" + steps + '\'' +
                '}';
    }

    public void setCurDate(String curDate) {
        this.curDate = curDate;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCurDate() {
        return this.curDate;
    }

    public String getSteps() {
        return this.steps;
    }
}

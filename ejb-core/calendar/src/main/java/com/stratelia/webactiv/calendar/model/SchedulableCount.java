package com.stratelia.webactiv.calendar.model;

public class SchedulableCount implements java.io.Serializable {

  private int count;
  private String day;
  
  public SchedulableCount(int count, String day) {
    this.count = count;
    this.day = day;
  }
  
  public int getCount() {
    return count;
  }
  
  public String getDay() {
    return day;
  }

}

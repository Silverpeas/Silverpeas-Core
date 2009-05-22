package com.stratelia.webactiv.calendar.model;

public class JournalHeader extends Schedulable implements Cloneable
{
  public JournalHeader(String name, String organizerId) {
    super(name, organizerId);
  }
  
  public JournalHeader(String id, String name, String organizerId) {
    super(id, name, organizerId);
  }

  public String getEndDay() {
    String endDate = super.getEndDay();
    if (endDate == null) return getStartDay();
    return endDate;
  }
 
  public Schedulable getCopy() {
    try {
      return (JournalHeader) this.clone();
    }
    catch (Exception e) {
      return null;
    }
  }
}
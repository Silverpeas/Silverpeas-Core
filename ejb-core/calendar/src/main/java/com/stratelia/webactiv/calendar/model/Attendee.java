package com.stratelia.webactiv.calendar.model;

public class Attendee implements java.io.Serializable {

  private String userId = null;
  private ParticipationStatus participationStatus = null;

  public Attendee() {
  }

  public Attendee(String userId) {
    setUserId(userId);
  }

  public Attendee(String userId, String participationStatus) {
    setUserId(userId);
    getParticipationStatus().setString(participationStatus);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public ParticipationStatus getParticipationStatus() {
    if (participationStatus == null)
      participationStatus = new ParticipationStatus();
    return participationStatus;
  }

  public void setParticipationStatus(ParticipationStatus participate) {
    participationStatus = participate;
  }

}
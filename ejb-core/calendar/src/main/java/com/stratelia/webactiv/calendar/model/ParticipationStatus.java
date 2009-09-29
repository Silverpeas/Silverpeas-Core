package com.stratelia.webactiv.calendar.model;

import java.io.Serializable;

public class ParticipationStatus implements Serializable {

  final public static String TENTATIVE = "tentative";
  final public static String ACCEPTED = "accepted";
  final public static String DECLINED = "declined";

  static public String[] getJournalParticipationStatus() {
    String[] all = { TENTATIVE, ACCEPTED, DECLINED };
    return all;
  }

  private String participation = TENTATIVE;

  public ParticipationStatus() {
  }

  public ParticipationStatus(String participation) {
    setString(participation);
  }

  public void setString(String participation) {
    if (participation == null)
      return;
    if (participation.equals(TENTATIVE))
      this.participation = TENTATIVE;
    if (participation.equals(ACCEPTED))
      this.participation = ACCEPTED;
    if (participation.equals(DECLINED))
      this.participation = DECLINED;
  }

  public String getString() {
    return participation;
  }

  public boolean isTentative() {
    return (participation.equals(TENTATIVE)); // has the object is Serializable,
    // this has to be an equals()
    // method
  }

  public boolean isAccepted() {
    return (participation.equals(ACCEPTED)); // has the object is Serializable,
    // this has to be an equals()
    // method
  }

  public boolean isDeclined() {
    return (participation.equals(DECLINED)); // has the object is Serializable,
    // this has to be an equals()
    // method
  }

}
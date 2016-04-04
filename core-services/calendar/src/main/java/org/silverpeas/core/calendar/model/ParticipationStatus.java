/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar.model;

import java.io.Serializable;

public class ParticipationStatus implements Serializable {

  private static final long serialVersionUID = -6706542842606711494L;
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
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

package org.silverpeas.core.contribution.publication.model;

import java.io.Serializable;
import java.util.Date;

public class ValidationStep implements Serializable {

  private static final long serialVersionUID = -5783286372913745898L;
  private int id = -1;
  private PublicationPK pubPK = null;
  private String userId = null;
  private Date validationDate = null;
  private String decision = null;

  private String userFullName = null;

  public ValidationStep() {
  }

  public ValidationStep(PublicationPK pubPK, String userId, String decision) {
    this.pubPK = pubPK;
    this.userId = userId;
    this.decision = decision;
  }

  public PublicationPK getPubPK() {
    return pubPK;
  }

  public void setPubPK(PublicationPK pubPK) {
    this.pubPK = pubPK;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getValidationDate() {
    return validationDate;
  }

  public void setValidationDate(Date validationDate) {
    this.validationDate = validationDate;
  }

  public String getUserFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
    this.decision = decision;
  }

  public int getId() {
    return id;
  }

  public void setId(int stepId) {
    this.id = stepId;
  }

}

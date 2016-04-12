/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.validation;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.ValidableContribution;
import org.silverpeas.core.contribution.model.ContributionValidation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

/**
 * It represents the state of a contribution validation as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ContributionValidationEntity {

  private final ContributionValidation validation;

  public static ContributionValidationEntity fromValidableContribution(
      final ValidableContribution validableContribution) {
    return fromContributionValidation(validableContribution.getValidation());
  }

  private static ContributionValidationEntity fromContributionValidation(
      final ContributionValidation validation) {
    return new ContributionValidationEntity(validation);
  }

  @XmlElement
  public ContributionStatus getStatus() {
    return validation.getStatus();
  }

  protected void setStatus(ContributionStatus status) {
    validation.setStatus(status);
  }

  @XmlTransient
  public boolean isInDraft() {
    return getStatus().isInDraft();
  }

  @XmlTransient
  public boolean isRefused() {
    return getStatus().isRefused();
  }

  @XmlTransient
  public boolean isPendingValidation() {
    return getStatus().isPendingValidation();
  }

  @XmlTransient
  public boolean isValidated() {
    return getStatus().isValidated();
  }

  @XmlElement
  public Date getDate() {
    return validation.getDate();
  }

  protected void setDate(Date validationDate) {
    validation.setDate(validationDate);
  }

  @XmlElement
  public String getComment() {
    return validation.getComment();
  }

  protected void setComment(String validationComment) {
    validation.setComment(validationComment);
  }

  @XmlElement
  public String getValidatorName() {
    String validatorName = "";
    if (validation.getValidator() != null) {
      validatorName = validation.getValidator().getDisplayedName();
    }
    return validatorName;
  }

  protected void setValidatorName(String validatorName) {

  }

  @XmlElement
  public String getValidatorId() {
    if (validation.getValidator() == null) {
      return null;
    }
    return validation.getValidator().getId();
  }

  protected void setValidatorId(String validatorId) {
    validation.setValidator(UserDetail.getById(validatorId));
  }

  public ContributionValidation toContributionValidation() {
    return this.validation;
  }

  protected ContributionValidationEntity() {
    this.validation = new ContributionValidation();
  }

  protected ContributionValidationEntity(final ContributionValidation validation) {
    this.validation = validation;
  }

  @Override
  public String toString() {
    return "ContributionValidationEntity{status=" + getStatus() + ", date=" + getDate() +
        ", comment=" + getComment() + ", validatorId=" + getValidatorId() + '}';
  }
}

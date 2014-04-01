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
package org.silverpeas.validation.web;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.contribution.model.ContributionValidation;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * It represents the state of a contribution validation as transmitted within the body of
 * an HTTP response or an HTTP request.
 * @author mmoquillon
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ContributionValidationEntity {

  private final ContributionValidation validation;

  public static final ContributionValidationEntity fromContributionValidation(
      final ContributionValidation validation) {
    if (validation == ContributionValidation.NONE_VALIDATION) {
      return null;
    }
    return new ContributionValidationEntity(validation);
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
  public String getValidatorId() {
    return validation.getValidator().getId();
  }

  protected void setValidator(String validatorId) {
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

}

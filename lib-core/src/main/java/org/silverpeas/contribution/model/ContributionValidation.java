/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.contribution.model;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.Date;

/**
 * This embeddable entity permits to get a common management of contribution validation
 * persistence.
 * @author Yohann Chastagnier
 */
@Embeddable
public class ContributionValidation implements Serializable {

  private static final long serialVersionUID = 6313266204966304781L;

  /**
   * It represents none contribution validation.
   */
  public static final ContributionValidation NONE_VALIDATION = null;

  @Column(name = "validationDate")
  @Temporal(value = TemporalType.TIMESTAMP)
  private Date validationDate;

  @Column(name = "validationComment")
  @Size(min = 1)
  @NotNull
  private String validationComment;

  @Column(name = "validationBy", length = 40)
  @NotNull
  private String validationBy;

  @Transient
  private UserDetail validator;

  /**
   * Gets the date of the validation of the contribution.
   * @return the date of the validation of the contribution.
   */
  public Date getDate() {
    return validationDate;
  }

  /**
   * Sets the date of the effective validation of the contribution.
   * @param validationDate the date of the validation.
   */
  public void setDate(final Date validationDate) {
    this.validationDate = validationDate;
  }

  /**
   * Gets the comment written by the validator of the contribution.
   * @return the comment written by a validator.
   */
  public String getComment() {
    return validationComment;
  }

  /**
   * Sets the comment written by the validator of the contribution.
   * @param validationComment the comment written by the validator.
   */
  public void setComment(final String validationComment) {
    this.validationComment = validationComment;
  }

  /**
   * Gets the validator of the contribution.
   * @return a user detail object that represents the validator of the contribution.
   */
  public UserDetail getValidator() {
    if (StringUtil.isDefined(validationBy)) {
      if (validator == null || !validationBy.equals(validator.getId())) {
        validator = UserDetail.getById(validationBy);
      }
    } else {
      validator = null;
    }
    return validator;
  }

  /**
   * Sets the validator of the contribution.
   * @param validator the validator of the contribution.
   */
  public void setValidator(final UserDetail validator) {
    this.validator = validator;
    validationBy = ((validator != null) ? validator.getId() : null);
  }

  @Override
  public String toString() {
    return "ContributionValidation{" + "validationDate=" + validationDate + ", validationComment="
        + validationComment + ", validationBy=" + validationBy + '}';
  }

  /**
   * Constructs an empty contribution validation.
   */
  public ContributionValidation() {

  }

  /**
   * Constructs a contribution validation that was done by the specified validator at the given
   * date.
   * @param validator the validator that emitted this validation.
   * @param validationDate the date at which this validation was done.
   */
  public ContributionValidation(final UserDetail validator, final Date validationDate) {
    this.validationBy = validator.getId();
    this.validator = validator;
    this.validationDate = validationDate;
  }

  /**
   * Constructs a contribution validation that was done by the specified validator at the given
   * date and with the specified comment.
   * @param validator the validator that emitted this validation.
   * @param validationDate the date at which this validation was done.
   * @param comment the comment about validation done by the validator.
   */
  public ContributionValidation(final UserDetail validator, final Date validationDate,
      String comment) {
    this(validator, validationDate);
    this.validationComment = comment;
  }
}

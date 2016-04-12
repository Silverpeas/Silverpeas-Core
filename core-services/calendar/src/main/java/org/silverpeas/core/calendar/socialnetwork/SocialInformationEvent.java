/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.calendar.socialnetwork;

import java.util.Date;

import org.silverpeas.core.socialnetwork.model.AbstractSocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.calendar.model.Schedulable;
import org.silverpeas.core.util.DateUtil;

/**
 * @author Bensalem Nabil
 */
public class SocialInformationEvent extends AbstractSocialInformation {

  private String classification = "public";
  private Schedulable schedulable = null;

  /**
   * Constructor with on Param
   *
   * @param schedulable
   */
  public SocialInformationEvent(Schedulable schedulable) {
    this.schedulable = schedulable;
    this.classification = schedulable.getClassification().getString();
    if (schedulable.getEndDate().after(new Date())) {
      setType(SocialInformationType.EVENT.toString());
    } else {
      setType(SocialInformationType.LASTEVENT.toString());
    }
    setUpdated(true);
  }

  /**
   * * Constructor with Tow Params
   *
   * @param schedulable
   * @param isMyEvent
   */
  public SocialInformationEvent(Schedulable schedulable, boolean isMyEvent) {
    this.schedulable = schedulable;
    this.classification = schedulable.getClassification().getString();
    if (schedulable.getEndDate().after(new Date())) {
      setType(SocialInformationType.EVENT.toString());
    } else {
      setType(SocialInformationType.LASTEVENT.toString());
    }
    setUpdated(isMyEvent);
  }

  /**
   * return the icon of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getIcon() {
    if ("private".equals(classification)) {
      return SocialInformationType.EVENT + "_private.gif";
    }
    return SocialInformationType.EVENT + "_public.gif";
  }

  /**
   * return the Title of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getTitle() {
    return schedulable.getName();
  }

  /**
   * return the Description of this SocialInformation
   *
   * @return String
   */
  @Override
  public String getDescription() {
    return schedulable.getDescription();
  }

  /**
   * return the Author of this SocialInfo
   *
   * @return String
   */
  @Override
  public String getAuthor() {
    return schedulable.getDelegatorId();
  }

  /**
   * return the Url of this SocialInfo
   *
   * @return String
   */
  @Override
  public String getUrl() {
    if (isUpdated()) {
      return URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "SelectDay?Day=" + DateUtil
          .getInputDate(getDate(), "FR");
    }
    return URLUtil.getURL(URLUtil.CMP_AGENDA, null, null) + "ViewOtherAgenda?Id="
        + getAuthor();
  }

  /**
   * return the Date of this SocialInfo
   *
   * @return
   */
  @Override
  public Date getDate() {
    return schedulable.getStartDate();
  }

  /**
   * Indicates whether some other SocialInformation date is befor or after the date of this one.
   *
   * @return int
   */
  @Override
  public int compareTo(SocialInformation socialInfo) {
    if (SocialInformationType.LASTEVENT.toString().equals(getType())) {
      // event in the past
      return getDate().compareTo(socialInfo.getDate()) * -1;
    }
    return getDate().compareTo(socialInfo.getDate());// future event
  }
}
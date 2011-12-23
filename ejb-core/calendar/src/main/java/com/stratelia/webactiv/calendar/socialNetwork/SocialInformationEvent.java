/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.calendar.socialnetwork;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;

import static com.silverpeas.socialnetwork.model.SocialInformationType.*;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.calendar.model.Schedulable;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;

/**
 *
 * @author Bensalem Nabil
 */
public class SocialInformationEvent implements SocialInformation {

  private String classification = "public";
  private  SocialInformationType type = EVENT;
  private Schedulable schedulable = null;
  private boolean isMyEvent=true;
/**
 * Constructor with on Param
 * @param schedulable
 */
  public SocialInformationEvent(Schedulable schedulable) {
    this.schedulable = schedulable;
    this.classification = schedulable.getClassification().getString();
    if(schedulable.getEndDate().after(new Date()))
    type=EVENT;
    else
    type=LASTEVENT;

  }
  /**
   * * Constructor with Tow Params
   *
   * @param schedulable
   * @param isMyEvent
   */

  public SocialInformationEvent(Schedulable schedulable,boolean isMyEvent ) {
    this.isMyEvent=isMyEvent;
    this.schedulable = schedulable;
    this.classification = schedulable.getClassification().getString();
    if(schedulable.getEndDate().after(new Date()))
    type=EVENT;
    else
    type=LASTEVENT;
  }
/**
   * return the type of this SocialInformation
   * @return String
   */
  @Override
  public String getType() {
    return type.toString();
  }
/**
   * return the icon of this SocialInformation
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
   * @return String
   */
  @Override
  public String getTitle() {
    return schedulable.getName();
  }
/**
   * return the Description of this SocialInformation
   * @return String
   */
  @Override
  public String getDescription() {
    return schedulable.getDescription();
  }
/**
   * return the Author of this SocialInfo
   * @return String
   */
  @Override
  public String getAuthor() {
    return schedulable.getDelegatorId();
  }
 /**
   * return the Url of this SocialInfo
   * @return String
   */
  @Override
  public String getUrl() {
    if(isMyEvent)
    return URLManager.getURL(URLManager.CMP_AGENDA) + "SelectDay?Day=" + DateUtil.getInputDate(
        getDate(), "FR");
    return URLManager.getURL(URLManager.CMP_AGENDA) +"ViewOtherAgenda?Id="+getAuthor();
  }
/**
   * return the Date of this SocialInfo
   * @return
   */
  @Override
  public Date getDate() {
    return schedulable.getStartDate();
  }
/**
   * return if this socialInfo was updtated or not
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return isMyEvent;
  }
/**
   *Indicates whether some other SocialInformation date is befor or after the date of this one.
   *@param   obj   the reference object with which to compare.
   * @return int
   */
  @Override
  public int compareTo(SocialInformation si) {
    if(SocialInformationType.LASTEVENT==type)//event in the passe
       return getDate().compareTo(si.getDate())*-1;
    return getDate().compareTo(si.getDate());//futer Event
  }
}

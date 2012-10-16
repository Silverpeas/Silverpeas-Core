/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.socialnetwork.relationShip;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;

import java.util.Date;

/**
 * @author Bensalem Nabil
 */
public class SocialInformationRelationShip implements SocialInformation {

  private String title;
  private String author;
  private String url;
  private Date date;

  /**
   * @param relationShip
   */
  public SocialInformationRelationShip(RelationShip relationShip) {
    author = relationShip.getUser1Id() + "";// myFriend
    title = relationShip.getUser2Id() + "";// Friend of my Friend
    date = relationShip.getAcceptanceDate();
    this.url = "/Rprofil/jsp/Main?userId=" + relationShip.getUser2Id();

  }

  /**
   * return the Title of this SocialInformation
   * @return String
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * return the Description of this SocialInformation
   * @return String
   */
  @Override
  public String getDescription() {
    return "";
  }

  /**
   * return the Author of this SocialInfo
   * @return String
   */
  @Override
  public String getAuthor() {
    return author;
  }

  /**
   * return the Url of this SocialInfo
   * @return String
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * return the Date of this SocialInfo
   * @return
   */
  @Override
  public Date getDate() {
    return date;
  }

  /**
   * return the Type of this SocialInfo
   * @return
   */
  @Override
  public String getType() {
    return SocialInformationType.RELATIONSHIP.toString();
  }

  /**
   * return icon name of this SocialInfo
   * @return String
   */
  @Override
  public String getIcon() {
    return "Photo_profil.jpg";
  }

  /**
   * return if this socialInfo was updtated or not
   * @return boolean
   */
  @Override
  public boolean isUpdeted() {
    return false;
  }

  /**
   *Indicates whether some other SocialInformation date is befor the date of this one.
   *@param obj the reference object with which to compare.
   * @return int
   */
  @Override
  public int compareTo(SocialInformation o) {
    return getDate().compareTo(o.getDate()) * -1;
  }
}

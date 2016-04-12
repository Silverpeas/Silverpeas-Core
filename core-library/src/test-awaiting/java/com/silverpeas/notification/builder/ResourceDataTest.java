/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.usernotification.builder;

import java.util.Date;

import com.silverpeas.SilverpeasContent;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class ResourceDataTest implements SilverpeasContent {

  private static final long serialVersionUID = 7473567565248504755L;

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getId()
   */
  @Override
  public String getId() {
    return "aIdFromResource";
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getComponentInstanceId()
   */
  @Override
  public String getComponentInstanceId() {
    return "aComponentInstanceIdFromResource";
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getSilverpeasContentId()
   */
  @Override
  public String getSilverpeasContentId() {
    return "aContentIdFromResource";
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getCreator()
   */
  @Override
  public UserDetail getCreator() {
    final UserDetail userDetail = new UserDetail();
    userDetail.setId("100");
    return userDetail;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getCreationDate()
   */
  @Override
  public Date getCreationDate() {
    return java.sql.Date.valueOf("2012-01-01fsb");
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getTitle()
   */
  @Override
  public String getTitle() {
    return "aTitleFromResource";
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getDescription()
   */
  @Override
  public String getDescription() {
    return "";
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.core.contribution.model.SilverpeasContent#getContributionType()
   */
  @Override
  public String getContributionType() {
    return "aContributionTypeFromResource";
  }

  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    return true;
  }
}

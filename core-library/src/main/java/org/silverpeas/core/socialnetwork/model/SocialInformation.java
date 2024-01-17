/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.socialnetwork.model;

import org.silverpeas.core.ResourceReference;

import java.util.Date;

/**
 * @author azzedine
 */
public interface SocialInformation extends Comparable<SocialInformation> {

  /**
   * @return the {@link ResourceReference} the {@link SocialInformation} is aiming.
   */
  ResourceReference getResourceReference();

  /**
   * return the Title of this SocialInformation
   * @return String
   */
  String getTitle();

  /**
   * return the Description of this SocialInformation
   * @return String
   */
  String getDescription();

  /**
   * return the Author of this SocialInfo
   * @return String
   */
  String getAuthor();

  /**
   * return the Url of this SocialInfo
   * @return String
   */
  String getUrl();

  /**
   * return the Date of this SocialInfo
   * @return Date
   */
  Date getDate();

  /**
   * return the Type of this SocialInfo
   * @return String
   */
  String getType();

  /**
   * return icon name of this SocialInfo
   * @return String
   */
  String getIcon();

  /**
   * return if this socialInfo was updated or not
   * @return boolean
   */
  boolean isUpdated();

  void setUpdated(boolean updated);

}

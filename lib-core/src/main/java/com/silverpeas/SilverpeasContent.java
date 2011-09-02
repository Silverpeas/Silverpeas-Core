/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas;

import com.stratelia.webactiv.beans.admin.UserDetail;
import java.io.Serializable;

/**
 * A content managed in the Silverpeas collaborative portal.
 * This interface defines the common properties all kinds of content has to provide.
 */
public interface SilverpeasContent extends Serializable {
  
  /**
   * Gets the unique identifier of this content in the Silverpeas collaborative portal.
   * @return the unique identifier of this content.
   */
  String getId();
  
  /**
   * Gets the unique identifier of the Silverpeas component instance that handles this content.
   * @return the unique identifier of the component instance.
   */
  String getComponentInstanceId();
  
  /**
   * Gets the URL at which this content can be retrieved.
   * @return the URL of this content.
   */
  String getURL();
  
  /**
   * Gets the creator of this content.
   * @return the detail about the user that created this content.
   */
  UserDetail getCreator();
  
  /**
   * Gets the title of this content if any.
   * @return the content title. Can be empty if no title was set or no title is defined for the
   * content.
   */
  String getTitle();
}

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

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import java.io.Serializable;
import java.util.Date;

/**
 * A content managed in the Silverpeas collaborative portal. A content in Silverpeas is resource
 * with a content (that can be empty); for example, a publication in Silverpeas is a content. This
 * interface defines the common properties the different type of content in Silverpeas has to
 * support.
 * @deprecated please use instead {@code org.silverpeas.core.contribution.model.Contribution} interface.
 */
@Deprecated
public interface SilverpeasContent extends Serializable {

  /**
   * Gets the identifier of this content in the Silverpeas component providing it. This identifier
   * is only unique among all of the contents managed by the same component (whatever its different
   * instances). As each type of contents in Silverpeas is provided by a single Silverpeas
   * component, the identifier of a content is then specific to the component it belongs to. It is a
   * way for an instance of a such component to identify uniquely the different contents it manages.
   * So, each component can have their own policy to identify their content, whatever the way they
   * are identified in Silverpeas.
   * @return the identifier of this content.
   */
  String getId();

  /**
   * Gets the unique identifier of the Silverpeas component instance that manages this content.
   * @return the unique identifier of the component instance in the Silverpeas collaborative portal.
   */
  String getComponentInstanceId();

  /**
   * Gets the unique identifier of this content among all the contents managed in the Silverpeas
   * collaborative portal. It is the alone unique identifier of a content in the whole Silverpeas
   * portal and it is refered as the Silverpeas content identifier or the silver content identifier.
   * For each content put into the Silverpeas collaborative portal, an entry is uniquely created in
   * the whole system so that is can be refered by transversal services and by component instances
   * others the one that manages it. For compatibility reason, the Silverpeas content identifier of
   * contents that are no yet taken into account in the whole system isn't defined, so an empty
   * string is then returned.
   * @return the unique identifier of this content in the whole Silverpeas collaborative portal. Can
   * be empty if no such identifier is defined for the type of this content.
   */
  String getSilverpeasContentId();

  /**
   * Gets the author that has created this content.
   * @return the detail about the user that created this content.
   */
  UserDetail getCreator();

  /**
   * Gets the date at which this content was created.
   * @return the date at which this content was created.
   */
  Date getCreationDate();

  /**
   * Gets the title of this content if any.
   * @return the resource title. Can be empty if no title was set or no title is defined for a such
   * content.
   */
  String getTitle();

  /**
   * Gets the description of this content if any.
   * @return the resource description. Can be empty if no description was set or no description is defined for a such
   * content.
   */
  String getDescription();

  /**
   * Gets the type of this content.
   * @return the resource type. This can be Post, Message, Publication, Survey...
   */
  String getContributionType();

  /**
   * Is the specified user can access this content?
   *
   * A user can access a content if it has enough rights to access the application instance in
   * which is managed this content. In the case where the application instance distributes its
   * contents among one or more nodes and these nodes have access rights, then the user must have
   * also the rights to access the node to which belongs the content.
   * @param user a user in Silverpeas.
   * @return true if the user can access this content, false otherwise.
   */
  boolean canBeAccessedBy(UserDetail user);
}

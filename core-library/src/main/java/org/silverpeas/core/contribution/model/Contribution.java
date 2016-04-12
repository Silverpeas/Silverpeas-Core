/*
 * Copyright (C) 2000 - 2014 Silverpeas
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

package org.silverpeas.core.contribution.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.IdentifiableResource;

import java.util.Date;

/**
 * A contribution in Silverpeas. A contribution is an identifiable resource that is pushed by a
 * user onto Silverpeas and that is manageable by the users within Silverpeas.
 * @author mmoquillon
 */
public interface Contribution extends IdentifiableResource {

  /**
   * Gets the unique identifier of this contribution.
   * @return the unique identifier of the contribution.
   */
  ContributionIdentifier getId();

  /**
   * Gets the content of this contribution. A contribution can support several type of contents.
   * @param <T> the concrete or generic type of the content.
   * @return the content of this contribution or null if this contribution has not yet a content or
   * it doesn't support any content.
   */
  <T extends ContributionContent> T getContent();

  /**
   * Does this contribution have a content?
   * @return true of this contribution has a content. False if this contribution has not yet a
   * content or doesn't support any content.
   */
  default boolean hasContent() {
    return getContent() != null;
  }

  /**
   * Gets the user that has created this content.
   * @return the detail about the user that has created this content.
   */
  UserDetail getCreator();

  /**
   * Gets the date at which this content was created.
   * @return the date at which this content was created.
   */
  Date getCreationDate();

  /**
   * Gets the title of this contribution if any. By default returns an empty String.
   * @return the contribution's title in the specified language.
   * Can be empty if no title was set or no title is defined for a such contribution.
   */
  default String getTitle() {
    return "";
  }

  /**
   * Gets a description about this contribution if any. By default returns an empty String.
   * @return the description on this contribution. Can be empty if no description was set or no
   * description is defined for a such contribution.
   */
  default String getDescription() {
    return "";
  }

  /**
   * Is the specified user can access this contribution?
   * <p>
   * A user can access a contribution if it has enough rights to access the application instance
   * in which is managed this contribution. In the case the application instance distributes its
   * contribution along of a categorization tree and the nodes of this tree support access rights,
   * then the user must have also the rights to access the node to which belongs the content.
   * </p>
   * @param user a user in Silverpeas.
   * @return true if the user can access this content, false otherwise.
   */
  boolean canBeAccessedBy(UserDetail user);

  /**
   * Gets the type of this contribution. The type is a label that identifies uniquely a kind of
   * contribution handled by a Silverpeas application.
   * By default, this method returns the simple name of the class implementing this interface.
   * @return the label of the type of this contribution.
   */
  default String getContributionType() {
    return getClass().getSimpleName();
  }
}

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
package org.silverpeas.core.contribution.model;

import org.silverpeas.core.Instance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import java.io.Serializable;
import java.util.Date;

/**
 * A contribution in Silverpeas. A contribution is an identifiable resource that is pushed by a
 * user onto Silverpeas and that is manageable by the users within Silverpeas. A contribution has
 * always a content that can be of any type (simple text, WYSIWYG, form, image, ...).
 * @author mmoquillon
 */
public interface Contribution extends Serializable, Securable, Instance<Contribution> {

  /**
   * Gets the unique identifier of this contribution.
   * @return the unique identifier of the contribution.
   */
  ContributionIdentifier getContributionId();

  /**
   * Gets the user that has created this contribution.
   * @return the user that has created this contribution.
   */
  User getCreator();

  /**
   * Gets the date at which this content was created.
   * @return the date at which this content was created.
   */
  Date getCreationDate();

  /**
   * Gets the last user that has modified this contribution.<br>
   * When some old entities can not provide a modifier, then the creator is returned.
   * @return the detail about the user that has modified this contribution.
   */
  User getLastModifier();

  /**
   * Gets the date at which this content was modified.
   * <p>
   * Some beans can not handle both creation and modification dates. In a such case, both methods
   * ({@link #getCreationDate()} and this method) returns the same date.
   * </p>
   * @return the date at which this content was created.
   */
  Date getLastModificationDate();

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
   * Gets the type of this contribution. The type is a label that identifies uniquely a kind of
   * contribution handled by a Silverpeas application.
   * By default, this method returns the simple name of the class implementing this interface.
   * @return the label of the type of this contribution.
   */
  default String getContributionType() {
    return getClass().getSimpleName();
  }

  /**
   * Is this contribution indexable? By default true.
   * @return a boolean indicating if this contribution can be taken in charge by the Indexation
   * Engine. By default, any contribution in Silverpeas are indexable unless specified otherwise.
   */
  default boolean isIndexable() {
    return true;
  }

  /**
   * Is the specified user can access this contribution?
   * <p>
   * By default {@link Securable#canBeAccessedBy(User)} is implemented so that a user can access
   * a contribution if it has enough rights to access the application instance in which is
   * managed this contribution.<br>
   * Indeed, this behavior is mostly the common one.<br>
   * But In the case the application instance distributes its contribution along of a
   * categorization tree and the nodes of this tree support access rights, then the user must
   * have also the rights to access the node to which belongs the content.<br>
   * Of course it could exist other access rules...
   * </p>
   * @param user a user in Silverpeas.
   * @return true if the user can access this content, false otherwise.
   */
  @Override
  default boolean canBeAccessedBy(final User user) {
    return ComponentAccessControl.get()
        .isUserAuthorized(user.getId(), getContributionId().getComponentInstanceId());
  }

  /**
   * Gets a model to this contribution. A model is a business abstract representation of the
   * concrete type of this contribution. If not overridden, it returns by default a
   * {@link DefaultContributionModel} instance that access the business properties of the
   * contribution by reflection.
   * @return a {@link ContributionModel} object. By default, if this method isn't overridden, a
   * {@link DefaultContributionModel} instance is returned.
   */
  @SuppressWarnings("unchecked")
  default ContributionModel getModel() {
    return new DefaultContributionModel(this);
  }
}

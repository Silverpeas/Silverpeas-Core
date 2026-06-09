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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.Identifiable;
import org.silverpeas.core.Nameable;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * An instance of a component. It can be either an instance of a multi-user applications or an
 * instance of a personal application.
 *
 * @author Yohann Chastagnier
 */
public interface SilverpeasComponentInstance extends Identifiable, Nameable, Serializable {

  /**
   * Gets a silverpeas component instance from the specified identifier.
   *
   * @param componentInstanceId a component instance identifier as string.
   * @return an optional silverpeas component instance of {@link SilverpeasComponentInstance}.
   */
  static Optional<SilverpeasComponentInstance> getById(String componentInstanceId) {
    return SilverpeasComponentInstanceProvider.get().getById(componentInstanceId);
  }

  /**
   * Gets the identity of the component instance referred by the specified unique global identifier.
   * The identity of a component instance is serialized in its unique identifier. The identity can
   * be either the one of a shared component instance or the one of a personal component instance.
   * If the specified identifier doesn't refer a valid component instance identifier, then an
   * {@link IllegalArgumentException} is thrown; This is why it is recommended to check the
   * serialized form of the identifier with the
   * {@link SilverpeasComponentInstance#isIdentityValid(String)} method.
   *
   * @param componentInstanceId the unique global identifier of a component instance. It must be
   * non-null and well-formed.
   * @return a non-null Identity.
   * @throws IllegalArgumentException if the argument isn't a correct component instance
   * identifier.
   * @throws NullPointerException if the argument is null.
   */
  @NonNull
  static Identity getIdentity(@NonNull final String componentInstanceId) {
    return new ComponentInstanceIdentityFactory().create(componentInstanceId);
  }

  /**
   * Is the specified component instance identity is valid?
   * @param componentInstanceId the serialized form of a component instance identity.
   * @return true if the specified identifier refers a valid identifier of a Silvepreas component
   * instance.
   */
  static boolean isIdentityValid(@NonNull final String componentInstanceId) {
    return StringUtil.isDefined(componentInstanceId) &&
        new ComponentInstanceIdentityFactory().isValid(componentInstanceId);
  }

  /**
   * Gets the identifier of the space which the component instance belong to.
   *
   * @return an identifier as string.
   */
  String getSpaceId();

  /**
   * Gets the component name of the component instance.
   *
   * @return the name of the component instance.
   */
  String getName();

  /**
   * Gets the name of the component (from a functional point of view).
   *
   * @return the name of the component.
   */
  String getLabel();

  /**
   * Gets the translated name of the component according to given language (from a functional point
   * of view).<br> If no translation exists for given language, then the one of
   * {@link DisplayI18NHelper#getDefaultLanguage()} is returned.
   *
   * @return the translated name of the component.
   */
  String getLabel(String language);

  /**
   * Gets the description of the component (from a functional point of view).
   *
   * @return the description of the component.
   */
  String getDescription();

  /**
   * Gets the translated description of the component according to given language (from a functional
   * point of view).<br> If no translation exists for given language, then the one of
   * {@link DisplayI18NHelper#getDefaultLanguage()} is returned.
   *
   * @return the translated description of the component.
   */
  String getDescription(String language);

  /**
   * Indicates if the component instance is a personal one.<br> A personal component instance is
   * linked to a user.
   *
   * @return true if it is a personal one, false otherwise.
   */
  default boolean isPersonal() {
    return false;
  }

  /**
   * Indicates if the component instance is public.
   *
   * @return true if public, false otherwise.
   */
  default boolean isPublic() {
    return false;
  }

  /**
   * Indicates if the component instance is hidden.
   *
   * @return true if hidden, false otherwise.
   */
  default boolean isHidden() {
    return false;
  }

  /**
   * Indicates if the component instance is a workflow one.
   *
   * @return true if it is a workflow, false otherwise.
   */
  default boolean isWorkflow() {
    return false;
  }

  /**
   * Indicates if the component instance is a topic tracker.
   *
   * @return true if it is a topic tracker, false otherwise.
   */
  default boolean isTopicTracker() {
    return false;
  }

  /**
   * Indicates if the component instance is into a REMOVED state.
   *
   * @return true if into removed state, false otherwise.
   */
  default boolean isRemoved() {
    return false;
  }

  /**
   * Gets the position index of the component against the others into a space.
   *
   * @return a position as int.
   */
  int getOrderPosition();

  /**
   * Gets the silverpeas role the given user has on the component instance.<br> BE CAREFUL, the
   * manager role is never returned as it corresponds to a space role.
   *
   * @param user the user for which the roles are retrieved.
   * @return a list of {@link SilverpeasRole}, empty of no roles.
   */
  Collection<SilverpeasRole> getSilverpeasRolesFor(User user);

  /**
   * Gets the highest silverpeas role the given user has on the component instance.<br> BE CAREFUL,
   * the manager role is never returned as it corresponds to a space role.
   *
   * @param user the user for which the roles are retrieved.
   * @return a role if any, null otherwise.
   */
  default SilverpeasRole getHighestSilverpeasRolesFor(User user) {
    return SilverpeasRole.getHighestFrom(getSilverpeasRolesFor(user));
  }

  /**
   * Gets the value of component instance parameter.
   *
   * @param parameterName the name of the parameter.
   * @return the value of the parameter, empty string when the parameter does not exist.
   */
  default String getParameterValue(String parameterName) {
    return "";
  }

  /**
   * Identity of a component instance. The identity of a component instance is serialized in its
   * unique identifier. The identity of a component instance is made up of the name of the component
   * and of its unique local identifier.
   */
  abstract class Identity {

    private String name;
    private int localId;

    protected Identity(String componentInstanceId) {
      Objects.requireNonNull(componentInstanceId, "The component instance identifier is null!");
      decode(componentInstanceId);
    }

    protected Identity(String name, int localId) {
      // local id -1 is a particular and predefined id for undefined component instance
      // this is a convention in the implementation code of the component instances management!
      if ((StringUtil.isNotDefined(name) || localId < 1) && localId != -1) {
        throw new IllegalArgumentException("The component name '" + name +
            "' or local instance identifier " + localId + " is invalid!" );
      }
      this.name = name;
      this.localId = localId;
    }

    protected abstract void decode(@NonNull String componentInstanceId)
        throws IllegalArgumentException;

    protected final void setInstanceLocalId(int localId) {
      this.localId = localId;
    }

    protected final void setComponentName(String name) {
      this.name = name;
    }

    public int getInstanceLocalId() {
      return localId;
    }

    public String getComponentName() {
      return name;
    }
  }
}

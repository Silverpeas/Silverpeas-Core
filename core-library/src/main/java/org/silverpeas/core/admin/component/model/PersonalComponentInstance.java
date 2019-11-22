/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.space.PersonalSpaceManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * @author Yohann Chastagnier
 */
public class PersonalComponentInstance implements SilverpeasPersonalComponentInstance {
  private static final long serialVersionUID = -2622307102886492318L;

  private static final String INSTANCE_SUFFIX = "_PCI";
  private static final Pattern INSTANCE_IDENTIFIER_PATTERN =
      Pattern.compile("^([a-zA-Z]+)([0-9]+)" + INSTANCE_SUFFIX + "$");
  private static final int USER_ID_INDEX = 2;
  private static final int COMPONENT_NAME_INDEX = 1;

  private final User user;
  private final transient PersonalComponent personalComponent;

  /**
   * Hidden constructor.
   */
  private PersonalComponentInstance(final User user, final PersonalComponent personalComponent) {
    this.user = user;
    this.personalComponent = personalComponent;
  }

  /**
   * Gets the name of the personal component from which the specified instance was spawn. By
   * convention, the identifiers of the component instances are made up of the name of the
   * component followed by a number (the user identifier) plus additional stuff. This method is a
   * way to get directly the component name from an instance identifier.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return the name of the personal component or null if the specified identifier doesn't match
   * the rule of a personal component instance identifier.
   */
  public static String getComponentName(final String componentInstanceId) {
    String componentName = null;
    Matcher matcher = INSTANCE_IDENTIFIER_PATTERN.matcher(componentInstanceId);
    if (matcher.matches()) {
      componentName = matcher.group(COMPONENT_NAME_INDEX);
    }
    return componentName;
  }

  /**
   * Gets the personal component instance from an instance identifier.
   * @param personalComponentInstanceId identifier of a personal component instance.
   * @return optionally an instance of {@link PersonalComponentInstance}.
   */
  public static Optional<PersonalComponentInstance> from(String personalComponentInstanceId) {
    if (isNotDefined(personalComponentInstanceId)) {
      String message = undefined("personal component instance");
      SilverLogger.getLogger(PersonalComponentInstance.class).error(message);
      throw new IllegalArgumentException(message);
    }
    PersonalComponentInstance instance = null;
    Matcher matcher = INSTANCE_IDENTIFIER_PATTERN.matcher(personalComponentInstanceId);
    if (matcher.find()) {
      Optional<PersonalComponent> personalComponent =
          PersonalComponent.getByName(matcher.group(COMPONENT_NAME_INDEX));
      User user = User.getById(matcher.group(USER_ID_INDEX));
      if (personalComponent.isPresent() && user != null) {
        instance = from(user, personalComponent.get());
      }
    }
    return Optional.ofNullable(instance);
  }

  /**
   * Gets the personal component instance from a user and the definition of a personal component.
   * @param user a getUser().
   * @param personalComponent the definition of a personal component.
   * @return an instance of {@link PersonalComponentInstance}.
   */
  public static PersonalComponentInstance from(User user, PersonalComponent personalComponent) {
    if (user == null || personalComponent == null) {
      if (user == null) {
        SilverLogger.getLogger(PersonalComponentInstance.class).error("user is not define");
      } else {
        SilverLogger.getLogger(PersonalComponentInstance.class)
            .error("personal component is not defined");
      }
      throw new IllegalArgumentException("user or/and personal component are not defined");
    }
    return new PersonalComponentInstance(user, personalComponent);
  }

  @Override
  public User getUser() {
    return user;
  }

  @Override
  public String getId() {
    return getName() + getUser().getId() + INSTANCE_SUFFIX;
  }

  @Override
  public String getSpaceId() {
    // TODO as for component instances, SpaceInstance interface must be coded...
    // For now, existing personal space mechanism is used (registering a space tagged as personal)
    return PersonalSpaceManager.get().getOrCreatePersonalSpace(getUser()).getId();
  }

  @Override
  public String getName() {
    return personalComponent.getName();
  }

  @Override
  public String getLabel() {
    return personalComponent.getLabel(null);
  }

  @Override
  public String getLabel(final String language) {
    return personalComponent.getLabel(language);
  }

  @Override
  public String getDescription() {
    return personalComponent.getDescription(null);
  }

  @Override
  public String getDescription(final String language) {
    return personalComponent.getDescription(language);
  }

  @Override
  public int getOrderPosition() {
    return 0;
  }
}

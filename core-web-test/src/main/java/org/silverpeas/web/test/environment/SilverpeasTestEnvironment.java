/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.test.environment;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.UserReference;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;

/**
 * This class allows to load easily a Silverpeas environment dedicated to the tests:
 * <ul>
 * <li>Spaces</li>
 * <li>Components</li>
 * <li>Users</li>
 * <li>etc.</li>
 * </ul>
 */
@Technical
@Bean
@Singleton
public class SilverpeasTestEnvironment {

  public static final String DEFAULT_DOMAIN = "0";

  /**
   * Gets the Silverpeas environment.
   * @return the instance of the Silverpeas environment.
   */
  public static SilverpeasTestEnvironment get() {
    return ServiceProvider.getService(SilverpeasTestEnvironment.class);
  }

  /**
   * Creates a default user to use in the test case.
   * @return the detail about the user in use in the current test case.
   */
  public User createDefaultUser() {
    UserDetail user = new UserDetail();
    user.setLogin("toto");
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setDomainId(DEFAULT_DOMAIN);
    user.setState(UserState.VALID);
    user.setAccessLevel(UserAccessLevel.USER);
    addUser(user);
    return user;
  }

  /**
   * Adds a user.
   */
  public void addUser(UserDetail userDetail) {
    try {
      Administration.get().addUser(userDetail);
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Gets the token of a user that must be passed into the request header "Authorization" as a
   * value of a bearer authorization scheme.
   * @param userDetail the user details for which the token is needed.
   * @return the API token of the given user.
   */
  public String getTokenOf(final User userDetail) {
    try {
      return PersistentResourceToken.getOrCreateToken(UserReference.fromUser(userDetail))
          .getValue();
    } catch (TokenException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Gets the dummy component created into database at start of the test.
   * @return a {@link ComponentInst} instance that represents the dummy component.
   */
  public ComponentInst getDummyPublicComponent() {
    try {
      return Administration.get().getComponentInst("dummyComponent0");
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Updates the database with the given component instance.
   * @param componentInst the {@link ComponentInst} instance
   */
  public void updateComponent(ComponentInst componentInst) {
    try {
      Administration.get().updateComponentInst(componentInst);
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Removes all the user profiles set for the specified component instance. So, nobody will be
   * authorized to access and to modify the contributions managed by the component instance.
   * @param componentInst the component instance.
   */
  public void removeAllProfiles(ComponentInst componentInst) {
    try {
      Administration admin = Administration.get();
      var profiles = componentInst.getAllProfilesInst();
      for (var profile: profiles) {
        admin.deleteProfileInst(profile.getId(), "0");
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  /**
   * Removes all the user profiles set for the specified space instance. So, nobody will be
   * authorized to access the space and the component instances in it.
   * @param spaceInst the space instance.
   */
  public void removeAllProfiles(SpaceInst spaceInst) {
    try {
      Administration admin = Administration.get();
      var profiles = spaceInst.getAllSpaceProfilesInst();
      for (var profile: profiles) {
        admin.deleteSpaceProfileInst(profile.getId(), "0");
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}

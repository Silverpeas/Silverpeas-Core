/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.authentication.web;

import com.silverpeas.web.TestResources;
import com.stratelia.webactiv.beans.admin.UserFull;
import javax.inject.Named;
import org.silverpeas.core.admin.user.constant.UserState;

import static com.silverpeas.web.TestResources.TEST_RESOURCES_NAME;

/**
 * The resources required by tests on the REST services.
 */
@Named(TEST_RESOURCES_NAME)
public class WebTestResources extends TestResources {

  public static final String SPRING_CONTEXT = "spring-authentication-webservice.xml";

  public static final String WEB_PACKAGES = "com.silverpeas.web";

  public static final String COMPONENT_ID = "kmelia36";

  public static final String WEB_RESOURCE_ID = "42";

  public static final String WEB_RESOURCE_PATH = "resources/" + COMPONENT_ID + "/" + WEB_RESOURCE_ID;

  private static final UserFull user = new UserFull();

  static {
    user.setFirstName("Toto");
    user.setLastName("Chez-les-papoos");
    user.setId(USER_ID_IN_TEST);
    user.setDomainId(DEFAULT_DOMAIN);
    user.setLogin("toto");
    user.setPassword("motherfucker");
    user.setState(UserState.VALID);
  }

  public UserFull getAUser() {
    return user;
  }
}

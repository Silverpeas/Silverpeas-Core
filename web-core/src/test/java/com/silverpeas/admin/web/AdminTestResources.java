/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.admin.web;

import javax.inject.Named;

import com.silverpeas.web.TestResources;
import com.silverpeas.web.mock.OrganizationControllerMock;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Resources required by all the unit tests on the comment web resource.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class AdminTestResources extends TestResources {

  public static final String JAVA_PACKAGE = "com.silverpeas.admin.web";
  public static final String SPRING_CONTEXT = "spring-admin-webservice.xml";

  public static UserDetail saveUser(final UserDetail user) {
    final OrganizationControllerFactory factory = OrganizationControllerFactory.getFactory();
    final OrganizationControllerMock mock =
        (OrganizationControllerMock) factory.getOrganizationController();
    mock.addUserDetail(user);
    return user;
  }

  public static SpaceBuilder getSpaceBuilder(final int id) {
    return new SpaceBuilder().withId(id);
  }

  public static ComponentBuilder getComponentBuilder(final int id) {
    return new ComponentBuilder().withId(id);
  }

  public UserDetail save(final UserDetail user) {
    getOrganizationControllerMock().addUserDetail(user);
    return user;
  }

  public void save(final ComponentInstLight... components) {
    getOrganizationControllerMock().addComponentInstLight(components);
  }

  public void save(final SpaceInstLight... spaces) {
    getOrganizationControllerMock().addSpaceInstLight(spaces);
  }
}

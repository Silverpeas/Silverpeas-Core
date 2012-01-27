/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.profile.web;

import com.silverpeas.rest.TestResources;

/**
 * The resources to use in the test on the UserProfileResource REST service.
 * Theses objects manage in a single place all the resources required to perform correctly unit
 * tests on the UserProfileResource published operations.
 */
public class UserProfileTestResources extends TestResources {
  
  public static final String JAVA_PACKAGE = "com.silverpeas.profile.web";
  public static final String SPRING_CONTEXT = "spring-profile-webservice.xml";
  public static final String USER_PROFILE_PATH = "/profile/users";
}

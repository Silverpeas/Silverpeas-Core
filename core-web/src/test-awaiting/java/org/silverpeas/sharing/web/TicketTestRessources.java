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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.sharing.web;

import javax.inject.Named;

import com.silverpeas.web.TestResources;

@Named(TestResources.TEST_RESOURCES_NAME)
public class TicketTestRessources extends TestResources {

  public static final String JAVA_PACKAGE = "org.silverpeas.sharing.web";
  public static final String SPRING_CONTEXT = "spring-sharing-webservice.xml";

  public static final String A_URI= "mytickets/";
  public static final String UNEXISTING_URI = "mytickets/kmelia99";

  public static final String INSTANCE_ID = "kmelia12";

}

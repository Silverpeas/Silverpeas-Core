/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.admin.components;

import java.sql.Connection;

/**
 * Interface for the instanciator component class. An instanciator creates and deletes components on
 * a space for a user.
 * @author Joaquim Vieira
 */
public interface ComponentsInstanciatorIntf {
  /**
   * The name of the component descriptor parameter holding the process file name
   */
  public static final String PROCESS_XML_FILE_NAME = "XMLFileName";

  /**
   * Create a new instance of the component for a requested user and space.
   * @param connection - Connection to the database used to save the create information.
   * @param spaceId - Identity of the space where the component will be instancied.
   * @param componentId - Identity of the component to instanciate.
   * @param userId - Identity of the user who want the component
   * @throws InstanciationException
   * @roseuid 3B82286B0236
   */
  public void create(Connection connection, String spaceId, String componentId,
      String userId) throws InstanciationException;

  /**
   * Delete the component instance created for the user on the requested space.
   * @param connection - Connection to the database where the create information will be destroyed.
   * @param spaceId - Identity of the space where the instanced component will be deleted.
   * @param componentId - Identity of the instanced component
   * @param userId - Identity of the user who have instantiate the component.
   * @throws InstanciationException
   * @roseuid 3B8228740117
   */
  public void delete(Connection connection, String spaceId, String componentId,
      String userId) throws InstanciationException;
}

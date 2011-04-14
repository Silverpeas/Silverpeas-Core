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

package com.stratelia.webactiv.persistence;

import java.sql.*;
import java.util.*;
import com.stratelia.webactiv.util.*;

/**
 * SilverpeasBeanDAO is the interface to use for instanciable component persistence. To get a
 * SilverpeasBeanDAO instance, use the SilverpeasBeanDAO factory. A SilverpeasBeanDAO instance is
 * linked with a SilverpeasBean specialisation. The code below show you how to build a
 * SilverpeasBeanDAO, enable to create, update... objects for TrainingDetail class. <CODE>
 * // get a dao instance, associated with TrainingDetail bean class
 * SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO("com.stratelia.webactiv.training.model.TrainingDetail");
 * </CODE> Once a dao instance is build, it enables you to create, update, remove and list objects
 * from TrainingDetail. TrainingDetail has to be a SilverpeasBean specialisation. (A SilverpeasBean
 * contains a PK). The persistance mechanism is based on your bean properties. In your bean, you
 * need to have getXXX and setXXX methods for each "column" you want to be persistant. For the
 * moment, SilverpeasBeanDAO is able to work with int, String and Date. This list can grow in the
 * near futur.
 */
public interface SilverpeasBeanDAO {

  public static final int CONNECTION_TYPE_EJBDATASOURCE_SILVERPEAS = 0;
  public static final int CONNECTION_TYPE_DATASOURCE_SILVERPEAS = 1;
  public static final int CONNECTION_TYPE_DATASOURCE = 2;
  public static final int CONNECTION_TYPE_JDBC_CLASSIC = 3;

  /**
   * update the row in db with the new bean properties.
   * @param bean the SilverpeasBean to update, with its complete primaryKey.
   */
  public void update(SilverpeasBeanIntf bean) throws PersistenceException;

  public void update(Connection con, SilverpeasBeanIntf bean)
      throws PersistenceException;

  /**
   * create the bean and a row in DB.
   * @param bean the SilverpeasBeanIntf to update, with a primaryKey initialized with only spaceId
   * and componentId.
   * @return The complete primary key, with the id.
   */
  public WAPrimaryKey add(SilverpeasBeanIntf bean) throws PersistenceException;

  public WAPrimaryKey add(Connection con, SilverpeasBeanIntf bean)
      throws PersistenceException;

  /**
   * remove the row in db represented by the primary key.
   * @param bean the SilverpeasBeanIntf to update, with a primaryKey initialized with only spaceId
   * and componentId.
   */
  public void remove(WAPrimaryKey pk) throws PersistenceException;

  public void remove(Connection con, WAPrimaryKey pk)
      throws PersistenceException;

  /**
   * remove all row in db represented by the where clause.
   * @param the where clause.
   */
  public void removeWhere(WAPrimaryKey pk, String p_WhereClause)
      throws PersistenceException;

  public void removeWhere(Connection con, WAPrimaryKey pk, String p_WhereClause)
      throws PersistenceException;

  /**
   * get a bean list, representing a specific row selection.
   * @param pk the beans primary key initialized with at least spaceId and componentId.
   * @param whereClause The where clause to put in select request. If null, all SilverpeasBeanIntfs
   * will be selected (all rows in the table).
   * @return The list of SilverpeasBeanIntfs corresponding to the where clause
   */
  public Collection findByWhereClause(WAPrimaryKey pk, String whereClause)
      throws PersistenceException;

  public Collection findByWhereClause(Connection con, WAPrimaryKey pk,
      String whereClause) throws PersistenceException;

  /**
   * get a bean representing a row in db from its pk
   * @param pk the complete beans primary key
   * @return The SilverpeasBeanIntfs corresponding to the pk, null if not found
   */
  public SilverpeasBeanIntf findByPrimaryKey(WAPrimaryKey pk)
      throws PersistenceException;

  public SilverpeasBeanIntf findByPrimaryKey(Connection con, WAPrimaryKey pk)
      throws PersistenceException;

}

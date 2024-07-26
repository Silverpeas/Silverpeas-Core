/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;

import java.sql.Connection;
import java.util.Collection;

/**
 * SilverpeasBeanDAO is the interface to use for persistent beans. It is a generic DAO for such
 * beans. To get a SilverpeasBeanDAO instance, use the SilverpeasBeanDAO factory. A
 * SilverpeasBeanDAO instance is linked with a SilverpeasBean specialisation. The code below show
 * you how to build a SilverpeasBeanDAO and then how to save, update or delete a beans.
 * <pre>
 *   {@code
 *   TrainingBean bean = ...
 *   SilverpeasBeanDAO dao = SilverpeasBeanDAOFactory.getDAO(TrainingBean.class.getName());
 *   // persists the bean
 *   dao.add(bean);
 *
 *   // update the state of the bean in database
 *   dao.update(bean)
 *
 *   // remove the bean from the database
 *   dao.remove(bean)
 *   }
 * </pre>
 * <p>
 * This persistence mechanism is deprecated in favor of JPA. Nevertheless, for instance, in order
 * to ensure compatibility with old codes, it is yet maintained.
 * </p>
 *
 * @param <T> the SilverpeasEntityBean type
 * @deprecated Replaced it with the new persistence layer built upon JPA
 */
@Deprecated
public interface SilverpeasBeanDAO<T extends SilverpeasEntityBean> {

  /**
   * update the row in db with the new bean properties.
   *
   * @param bean the SilverpeasBean to update, with its complete primaryKey.
   * @throws PersistenceException if the update fails.
   */
  void update(T bean) throws PersistenceException;

  /**
   * update the row in db with the new bean properties.
   *
   * @param con connection to the database to use for the update.
   * @param bean the SilverpeasBean to update, with its complete primaryKey.
   * @throws PersistenceException if the update fails.
   */
  void update(Connection con, T bean) throws PersistenceException;

  /**
   * create the bean and a row in DB.
   *
   * @param bean the SilverpeasEntityBean to update, with a primaryKey initialized with only spaceId
   * and componentId.
   * @return The complete primary key, with the id.
   * @throws PersistenceException if the creation fails.
   */
  WAPrimaryKey add(T bean) throws PersistenceException;

  /**
   * create the bean and a row in DB.
   *
   * @param con connection to the database to use for the adding.
   * @param bean the SilverpeasEntityBean to update, with a primaryKey initialized with only spaceId
   * and componentId.
   * @return The complete primary key, with the id.
   * @throws PersistenceException if the creation fails.
   */
  WAPrimaryKey add(Connection con, T bean) throws PersistenceException;

  /**
   * remove the row in db represented by the primary key.
   *
   * @param pk the SilverpeasEntityBean to update, with a primaryKey initialized with only spaceId and
   * componentId.
   * @throws PersistenceException if the deletion fails.
   */
  void remove(WAPrimaryKey pk) throws PersistenceException;

  /**
   * remove the row in db represented by the primary key.
   *
   * @param con connection to the database to use for the removing.
   * @param pk the SilverpeasEntityBean to update, with a primaryKey initialized with only spaceId and
   * componentId.
   * @throws PersistenceException if the deletion fails.
   */
  void remove(Connection con, WAPrimaryKey pk) throws PersistenceException;

  /**
   * remove all row in db represented by the where clause.
   *
   * @param criteria the criteria the beans to remove have to satisfy.
   * @throws PersistenceException if the deletion fails.
   */
  void removeBy(BeanCriteria criteria) throws PersistenceException;

  /**
   * remove all row in db represented by the where clause.
   *
   * @param con connection to the database to use for the removing.
   * @param criteria the criteria the beans to remove have to satisfy.
   * @throws PersistenceException if the deletion fails.
   */
  void removeBy(Connection con, BeanCriteria criteria)
      throws PersistenceException;

  /**
   * get a bean list, representing a specific row selection.
   *
   * @param criteria the criteria the beans to get have to satisfy.
   * @return a list of {@link SilverpeasEntityBean} objects matching the criteria.
   * @throws PersistenceException if the finding fails.
   */
  Collection<T> findBy(BeanCriteria criteria)
      throws PersistenceException;

  /**
   * get a bean list, representing a specific row selection.
   *
   * @param con connection to the database to use for the getting.
   * @param criteria the criteria the beans to get have to satisfy.
   * @return The list of SilverpeasBeanIntfs corresponding to the where clause
   * @throws PersistenceException if the finding fails.
   */
  Collection<T> findBy(Connection con, BeanCriteria criteria)
      throws PersistenceException;

  /**
   * get a bean representing a row in db from its pk
   *
   * @param pk the complete beans primary key
   * @return The SilverpeasBeanIntfs corresponding to the pk, null if not found
   * @throws PersistenceException if the finding fails.
   */
  T findByPrimaryKey(WAPrimaryKey pk) throws PersistenceException;

  /**
   * get a bean representing a row in db from its pk
   *
   * @param con connection to the database to use for the getting.
   * @param pk the complete beans primary key
   * @return The SilverpeasBeanIntfs corresponding to the pk, null if not found
   * @throws PersistenceException if the finding fails.
   */
  T findByPrimaryKey(Connection con, WAPrimaryKey pk) throws PersistenceException;
}

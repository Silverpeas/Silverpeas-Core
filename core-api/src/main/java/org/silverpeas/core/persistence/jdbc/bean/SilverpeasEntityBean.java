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
package org.silverpeas.core.persistence.jdbc.bean;

import org.silverpeas.core.WAPrimaryKey;

/**
 * A persistent entity bean in Silverpeas taken in charge by a {@link SilverpeasBeanDAO} DAO.
 * <p>
 * The persistence mechanism based upon {@link SilverpeasBeanDAO} is deprecated in favor of JPA.
 * </p>
 *
 * @deprecated
 */
@Deprecated
public interface SilverpeasEntityBean {

  /**
   * Gets the primary key of this bean in the database.
   *
   * @return the primary key of the bean.
   */
  WAPrimaryKey getPK();

  /**
   * Sets the primary key of this bean. The primary key is set by the {@link SilverpeasBeanDAO} once
   * the bean saved the first time into the database.
   *
   * @param value the primary key.
   */
  void setPK(WAPrimaryKey value);

}

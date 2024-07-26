/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception.  You should have received a copy of the text describi
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * The second version of the persistence layer implementation of Silverpeas. The previous one was
 * built on the BMP (Bean Managed Persistence) way of J2EE (the first persistence mechanism in J2EE)
 * in which the beans were accessed through DAOs in which the knowledge of how accessing the beans
 * in the database(s) were coded. This second version is an attempt to generalize and to abstract
 * this persistence approach; Instead of having a dedicated DAO to each type of entity beans in
 * Silverpeas, a generic DAO, {@link org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO} is
 * defined with all the usual methods to perform CRUD (Creation/Request/Update/Delete) operations.
 * In order to cover all the possible requests, methods accepting filtering criteria as parameter
 * are defined. A factory,
 * {@link org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory} is provided to
 * construct and returns the DAO for a given type of entity beans. To take advantage of this API,
 * only an implementation of the entity bean, by extending the abstract class
 * {@link org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean}, is required.
 *
 * @author mmoquillon
 */
package org.silverpeas.core.persistence.jdbc.bean;
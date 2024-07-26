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
 * While waiting to replace the old persistence implementations in Silverpeas by the new approach
 * built atop of JPA, the old persistence layer is kept and updated in some circumstances to fix
 * possible vulnerabilities and to enhance it by generalizing the way to request the database. This
 * is done because it is a lot cheaper and sure to enhance the old deprecated persistence
 * implementation than to replace it. The
 * {@link org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery} is an attempt to improve the way
 * to perform JDBC SQL queries by abstracting it into a single object. This object provides a DSL
 * to create and execute in a flexible way SQL statements. This is to be used by the old
 * persistence implementations built with the DAO approach.
 *
 * @author mmoquillon
 */
package org.silverpeas.core.persistence.jdbc.sql;
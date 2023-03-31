/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

/**
 * The JCR implementation is provided by the Apache Jackrabbit Oak project. The classes defined here
 * are for putting a bridge between the Silverpeas world and the Oak one so that Oak can access the
 * resources it requires from Silverpeas and Silverpeas can initialize and use transparently the JCR
 * backed by Oak. The {@link javax.jcr.Repository} is provided to Silverpeas by the
 * {@link org.silverpeas.core.jcr.impl.oak.OakRepositoryFactory} factory that has the knowledge to create
 * a repository backed by Oak and that is initialized with some peculiar Silverpeas objects to allow
 * Oak to access the context of Silverpeas (mainly for authentication and for authorization).
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.impl.oak;
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
 * Provides the classes required by the implementation of the JCR when a user in Silverpeas is
 * accessing the repository in order to delegate both the authentication and the authorization to
 * the Silverpeas security mechanism. The expectation of the JCR API is the users and the groups of
 * users should be managed within the JCR repository so that the authentication and the
 * authorization can be performed automatically by the JCR itself when a user accesses the content
 * of the repository. But the users and the groups of users are managed by Silverpeas itself and out
 * of the JCR which is used only to store some data, and they don't need to be synchronized with the
 * repository to both avoid double security checkups and to keep the accesses strongly controlled by
 * Silverpeas.
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.security;
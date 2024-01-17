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
 * The implementation of the JCR is provided by Apache Jackrabbit Oak. It expects, like any other
 * JCR implementations, the users and the groups of users are managed within the JCR. But those are
 * managed directly by Silverpeas itself. In the case they are managed externally, Oak expects a
 * synchronization of them with the repository, and it is what we don't want here to avoid user
 * management duplication, even for satisfying any specific authentication and authorization
 * process. Nevertheless, custom authentication and authorization mechanisms can be plugged into
 * Oak, but they have to be based upon JAAS and the security system of Silverpeas isn't built upon
 * JAAS. So this package provides the components required by Oak to authenticate and to authorize a
 * user but in bypassing all of this security mechanism in Oak.
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.impl.oak.security;
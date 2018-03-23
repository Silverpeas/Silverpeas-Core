/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

/**
 * Provides the mechanism to manage the delegations of responsibilities between users. A
 * responsibility in the system is defined as the set of actions that are mapped to a given role
 * in a given application instance. So a delegation is hence always done for a given component
 * instance. In that case, the user that gives whole of his responsibilities for a given component
 * instance is named the delegator whereas the user that receives those responsibilities is named
 * the delegate. When the delegate choose to use the delegation that was attributed to him, he
 * uses either the higher responsibility of the delegator or a responsibility previously selected
 * by him; this behavior depends on the application within which the delegated responsibility is
 * applied.
 * The delegation is useful when a user wish to temporarily delegate its responsibilities to another
 * user before taking vacations or when he's not available during a lapse of time.
 * @author mmoquillon
 */
package org.silverpeas.core.delegation;
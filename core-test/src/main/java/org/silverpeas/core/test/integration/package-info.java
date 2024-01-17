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

/**
 * It provides all the classes to facilitate the writing of integration tests and only integration tests. the goal
 * of the integration tests is to test the behaviour of a component within its own running environment; so with all of
 * its dependencies. For doing, the tested component and all of the dependencies are deployed into a running
 * JEE server. In opposite to the unit tests, the dependencies aren't mocked. Nevertheless, some transitive dependencies
 * can be stubbed (and not mocked) in order to keep the integration test simple.
 *
 * @author mmoquillon
 */
package org.silverpeas.core.test.integration;
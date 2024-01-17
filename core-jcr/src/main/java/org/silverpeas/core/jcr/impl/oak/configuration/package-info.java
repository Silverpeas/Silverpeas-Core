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
 * Provides the classes defining the different configuration parameters to create and initialize a
 * JCR in Oak. These classes are serializable and are instantiated when loading the JCR
 * configuration file. They are an alternative way to the OSGi configuration adopted by Oak. Indeed,
 * Oak provides only two ways to create a repository: either programmatically by using builders or
 * by OSGi with a dedicated configuration file. Nevertheless, a non-OSGi application can use the Oak
 * OSGi configuration but this requires to load a lot of dependencies on OSGi and on its
 * implementation which aren't needed by the application itself. So, this package is a way to
 * provide a classic and a conventional way to configure a repository through a configuration file
 * out of an OSGi environment by using transparently the programmatic way with the builders.
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.impl.oak.configuration;
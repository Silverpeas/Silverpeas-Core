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
 * <p>
 * Provides the factories of {@link org.apache.jackrabbit.oak.spi.state.NodeStore} on top of which
 * the JCR is built. The node storage is the backend to use for the JCR in the Apache
 * Jackrabbit Oak implementation. Oak provides three backends to applications, and hence three types
 * of node storage:
 * </p>
 * <ul>
 *   <li>the <em>segment storage</em> in which data are stored into the filesystem in the form of
 *   tar files.</li>
 *   <li>the <em>document storage</em> in which data are stored into a document-based database.
 *   This storage supports itself three subtypes of backend:
 *   <ul>
 *     <li>MongoDB,</li>
 *     <li>a relational SQL database (not supported by Silverpeas),</li>
 *     <li>the memory (to be used for testing purpose and not supported by Silverpeas).</li>
 *   </ul></li>
 *   <li>the <em>memory storage</em> in which data are stored in memory; this is only for testing
 *   purpose.</li>
 * </ul>
 * <p>
 *   It is possible in Oak to use a composite storage (for instance, a segment storage alongside
 *   a document storage) but this isn't yet addressed by Silverpeas.
 * </p>
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.impl.oak.factories;
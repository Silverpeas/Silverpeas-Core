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
 * <p>
 * The JCR (Java Content Repository) provides a tree-based approach to store data with support of
 * metadata, versioning, and WebDAV access. This package provides the classes that wrap the specific
 * implementation of the JCR for Silverpeas use. All the mechanisms used to put a bridge between the
 * JCR backend and Silverpeas are defined in the subpackages.
 * </p>
 * <p>
 * The {@link org.silverpeas.core.jcr.RepositoryProvider} instance aims to provide, with the IoC
 * mechanism, the {@link javax.jcr.Repository} object through which Silverpeas access the JCR. The
 * implementation of the {@link javax.jcr.Repository} is provided by the underlying JCR backend. For
 * doing, the {@link org.silverpeas.core.jcr.RepositoryProvider} instance delegates the getting of such a
 * repository to a {@link javax.jcr.RepositoryFactory} object gotten through the Java Service
 * Provider interface (as specified in the JCR API). This factory has to be defined by the bridge
 * between Silverpeas and a given JCR backend and it must satisfy some settings defined in
 * {@link org.silverpeas.core.jcr.impl.RepositorySettings} object.
 * </p>
 * @author mmoquillon
 */
package org.silverpeas.core.jcr;
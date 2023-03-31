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
 * Implementations of the JCR to use in Silverpeas. Only one implementation should provide a JCR
 * backend to be used by Silverpeas. The access to the JCR is performed through the
 * {@link javax.jcr.Repository} object for which each backend has to provide an implementation. An
 * instance of this implementation can be transparently retrieved through of a
 * {@link javax.jcr.RepositoryFactory}. Such a factory, according to the JCR specification, has to
 * be found through the Java Service Provider API and hence the backend has to satisfy this
 * mechanism. In Silverpeas, the implementation of this factory should be provided by this library
 * itself and not by the underlying backend, even whether it provides one. Indeed, it should be by
 * the {@link javax.jcr.RepositoryFactory} implementation the wrapping of the JCR backend and hence
 * the bridge between this backend and Silverpeas has to be provided. So, as the
 * {@link javax.jcr.Repository} is obtained from a call to the
 * {@link javax.jcr.RepositoryFactory#getRepository(java.util.Map)} method, it is by the passed
 * parameters the factory to use could be selected. Indeed, only the
 * {@link javax.jcr.RepositoryFactory} instances defined in this package knows how to interpret the
 * parameters. In the case several implementations supporting the parameters are available, only the
 * first one will be selected. This is done by the {@link org.silverpeas.core.jcr.RepositoryProvider}
 * class. So, for an implementation of the JCR to be available in Silverpeas, the provider expects
 * this implementation understands the following parameters:
 * <ul>
 *   <li><code>jcr.home</code>: the absolute path of the JCR home directory.</li>
 *   <li><code>jcr.config</code>: the absolute path of the JCR configuration file.</li>
 * </ul>
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.impl;
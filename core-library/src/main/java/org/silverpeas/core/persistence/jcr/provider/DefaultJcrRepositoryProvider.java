/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jcr.provider;

import org.silverpeas.core.persistence.jcr.JcrRepositoryProvider;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.jcr.Repository;

/**
 * The default implementation of the {@code org.silverpeas.core.persistence.jcr.JcrRepositoryProvider} interface
 * used in Silverpeas.
 * <p>
 * This implementation currently fetch by JNDI the JCR repository that in fact managed by a JCA
 * implementation of the JCR API. By default, the repository must be available by injection under
 * the name <i>jcr/repository</i>.
 * </p>
 * @author mmoquillon
 */
@Singleton
public class DefaultJcrRepositoryProvider implements JcrRepositoryProvider {

  @Resource(name = "jcr/repository")
  private Repository repository;

  @Override
  @Produces
  public Repository getRepository() {
    return repository;
  }

}

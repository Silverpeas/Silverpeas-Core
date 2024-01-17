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
 */

package org.silverpeas.core.jcr;

import javax.jcr.RepositoryFactory;

/**
 * A factory of JCR repositories in the context of execution of Silverpeas. All JCR backends must
 * provide an implementation of this interface in order for Silverpeas to look up and find a factory
 * of repositories usable for Silverpeas; that is to say a factory provided by a bridge between
 * Silverpeas and a given implementation of the JCR. This interface is by which the
 * {@link RepositoryProvider} filters the factories.
 * @author mmoquillon
 */
public interface SilverpeasRepositoryFactory extends RepositoryFactory {

}

/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

package org.silverpeas.core.cache;

import org.silverpeas.core.cache.service.VolatileResourceCacheService;
import org.silverpeas.core.contribution.model.Contribution;

/**
 * <p>
 * Some treatments needs sometimes to attach resources to a {@link Contribution} by using its
 * identifier (WYSIWYG and images on a contribution creation for example). But the {@link
 * Contribution} is not always yet registered into the repository when the attachments are
 * realized.
 * It is at this moment that {@link VolatileResourceCacheService} is used.<br>
 * When a such service is used, some resources have to be cleared in case where creation of a
 * {@link Contribution} that has been aborted before its validation.
 * </p>
 * <p>
 * So all the services which potentially are used by volatile services should implements this
 * interface.<br>
 * At the end of the user session, {@link #cleanVolatileResources(String, String)} of all
 * implementations is called in order to clean the volatile context.
 * </p>
 * @author silveryocha
 */
public interface VolatileResourceCleaner {

  /**
   * Cleans the resources referenced with the given resource identifier and linked to the component
   * instance represented by the given identifier.
   * @param volatileResourceId a resource identifier.
   * @param componentInstanceIdentifier a component instance identifier.
   */
  void cleanVolatileResources(String volatileResourceId, String componentInstanceIdentifier);
}

/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;

/**
 * This class permits to handles complex cases into document update into context of a request.
 * @author silveryocha
 */
class SimpleDocumentServiceContext {
  private static final String UNLOCK_DOC_UPDATE_KEY_PATTERN = "SimpleDocument@noNotifyUpdateOnunlock@%s";
  
  private SimpleDocumentServiceContext() {
    // hidden constructor.
  }

  /**
   * Marks that technical update notification MUST be avoid by unlock operation into the context
   * of the current request.
   * @param document the concerned {@link SimpleDocument} instance.
   */
  static void unlockMustNotNotifyUpdateIntoRequestContext(final SimpleDocument document) {
    final String cacheKey = String.format(UNLOCK_DOC_UPDATE_KEY_PATTERN, document.getId());
    getRequestCacheService().getCache().put(cacheKey, Boolean.TRUE);
  }

  /**
   * Indicates if the technical update notification can be sent by unlock operation into the
   * context of the current request.
   * @param document the concerned {@link SimpleDocument} instance.
   * @return true if it can, false otherwise.
   */
  static boolean canUnlockNotifyUpdateFromRequestContext(final SimpleDocument document) {
    final String cacheKey = String.format(UNLOCK_DOC_UPDATE_KEY_PATTERN, document.getId());
    return Boolean.TRUE != getRequestCacheService().getCache().remove(cacheKey, Boolean.class);
  }
}

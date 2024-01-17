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

package org.silverpeas.core.io.media.image.thumbnail.control;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;

import static org.silverpeas.core.util.StringUtil.isInteger;

/**
 * Deleter of the thumbnail associated with the contribution that has been deleted.
 * @author mmoquillon
 */
@Service
public class ThumbnailDeleter implements ContributionDeletion {

  @Override
  public void delete(final Contribution contribution) {
    try {
      if (contribution instanceof WithThumbnail) {
        final String instanceId = contribution.getIdentifier().getComponentInstanceId();
        final String localId = contribution.getIdentifier().getLocalId();
        if (isInteger(localId)) {
          final int contribId = Integer.parseInt(localId);
          ThumbnailDetail thumbToDelete = new ThumbnailDetail(instanceId, contribId, ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
          ThumbnailController.deleteThumbnail(thumbToDelete);
        }
      }
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e);
    }
  }
}
  
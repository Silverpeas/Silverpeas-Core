/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.contribution.publication;

import org.silverpeas.core.contribution.contentcontainer.content.AbstractSilverpeasContentManager.ContributionWrapper;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailReference;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileServerUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public abstract class PublicationGlobalSilverpeasContentProcessor extends
    AbstractGlobalSilverContentProcessor {

  @Inject
  private ThumbnailService thumbnailService;

  @Override
  public Stream<GlobalSilverContent> asGlobalSilverContent(List<SilverContentInterface> silverContents) {
    final Map<String, PublicationDetail> indexedPublications = silverContents.stream()
        .map(c -> (PublicationDetail) ((ContributionWrapper) c).getWrappedInstance())
        .collect(toMap(PublicationDetail::getId, p -> p));
    final Set<ThumbnailReference> thumbnailReferences = indexedPublications.values().stream()
        .map(p -> new ThumbnailReference(p.getId(), p.getComponentInstanceId(), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE))
        .collect(toSet());
    final Map<String, ThumbnailDetail> thumbnailsByPubId = thumbnailService.getByReference(thumbnailReferences).stream()
        .collect(toMap(t -> t.getReference().getId(), t -> t));
    return super.asGlobalSilverContent(silverContents)
        .peek(g ->  {
          final String pubId = g.getId();
          g.setType("Publication");
          final ThumbnailDetail thumbnail = thumbnailsByPubId.get(pubId);
          if (thumbnail != null) {
            final PublicationDetail pub = indexedPublications.get(pubId);
            pub.setThumbnail(thumbnail);
            if (StringUtil.isDefined(pub.getImage())) {
              final String imageURL = FileServerUtils.getUrl(pub.getPK().
                  getComponentName(), "vignette", pub.getImage(), pub.getImageMimeType(), "images");
              g.setThumbnailURL(imageURL);
            }
          }
        });
  }
}
/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.contribution.publication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.service.ThumbnailService;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the thumbnail URL of the publications found by a search on the PdC: the
 * thumbnail of a publication is either an image uploaded in the publication itself, or an image
 * picked up in a gallery.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PublicationGlobalSilverpeasContentProcessorTest {

  private static final String INSTANCE_ID = "quickinfo208";
  private static final String PUBLICATION_ID = "42";
  private static final String GALLERY_IMAGE_URL =
      "/silverpeas/GalleryInWysiwyg/dummy?ImageId=21d008b7-2a04-4020-b1d4-622abb086c4d" +
          "&ComponentId=gallery27&UseOriginal=false";

  @TestManagedMock
  ThumbnailService thumbnailService;

  @TestedBean
  QuickInfoContentProcessor processor;

  @BeforeEach
  void setUpTheLanguages(@TestManagedMock I18n i18n) {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
  }

  @Test
  void theThumbnailUploadedInThePublicationIsServedByTheFileServer() {
    publicationHasThumbnail("1790759697400.png");

    final GlobalSilverContent content = processThePublication();

    assertThat(content.getThumbnailURL(), startsWith("/silverpeas/FileServer/"));
    assertThat(content.getThumbnailURL(), endsWith(
        "?ComponentId=" + INSTANCE_ID + "&SourceFile=1790759697400.png&MimeType=image/png" +
            "&Directory=images"));
  }

  @Test
  void theThumbnailPickedUpInAGalleryIsServedByTheGallery() {
    publicationHasThumbnail(GALLERY_IMAGE_URL);

    final GlobalSilverContent content = processThePublication();

    assertThat(content.getThumbnailURL(), is(GALLERY_IMAGE_URL));
  }

  @Test
  void aPublicationWithoutThumbnailHasNoThumbnailURL() {
    when(thumbnailService.getByReference(anySet())).thenReturn(List.of());

    final GlobalSilverContent content = processThePublication();

    assertThat(content.getThumbnailURL(), is(emptyString()));
  }

  private void publicationHasThumbnail(final String originalFileName) {
    final ThumbnailDetail thumbnail = new ThumbnailDetail(INSTANCE_ID,
        Integer.parseInt(PUBLICATION_ID), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
    thumbnail.setOriginalFileName(originalFileName);
    thumbnail.setMimeType("image/png");
    when(thumbnailService.getByReference(anySet())).thenReturn(List.of(thumbnail));
  }

  private GlobalSilverContent processThePublication() {
    final PublicationDetail publication = PublicationDetail.builder()
        .setPk(new PublicationPK(PUBLICATION_ID, INSTANCE_ID))
        .build();
    final ManagedContribution contribution = mock(ManagedContribution.class);
    when(contribution.getWrappedContribution()).thenReturn(publication);
    when(contribution.getId()).thenReturn(PUBLICATION_ID);
    when(contribution.getComponentInstanceId()).thenReturn(INSTANCE_ID);
    when(contribution.getCreator()).thenReturn(mock(User.class));
    when(contribution.getLanguages()).thenReturn(List.of());
    return processor.asGlobalSilverContent(List.of(contribution)).findFirst().orElseThrow();
  }

  static class QuickInfoContentProcessor extends PublicationGlobalSilverpeasContentProcessor {

    @Override
    public String relatedToComponent() {
      return "quickinfo";
    }
  }
}

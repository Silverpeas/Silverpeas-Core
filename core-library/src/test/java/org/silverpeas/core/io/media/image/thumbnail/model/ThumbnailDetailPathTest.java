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
package org.silverpeas.core.io.media.image.thumbnail.model;

import jakarta.activation.FileDataSource;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.content.LinkUrlDataSource;
import org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.core.contribution.content.LinkUrlDataSourceScanner.extractUrlParameters;
import static org.silverpeas.core.util.StringDataExtractor.RegexpPatternDirective.regexp;
import static org.silverpeas.core.util.StringDataExtractor.from;

/**
 * Unit tests on the path in the file system of a thumbnail: the thumbnail is either an image
 * uploaded in the contribution itself, or an image picked up in the images store of Silverpeas,
 * that is to say in a media library whose images can be used by other applications.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ThumbnailDetailPathTest {

  private static final String INSTANCE_ID = "quickinfo208";
  private static final Path GALLERY_IMAGE = Path.of("/tmp/gallery27/image21/preview.jpg");

  @TestManagedBean
  GalleryScanner4Test galleryScanner;

  @Test
  void theThumbnailUploadedInTheContributionIsInTheImagesDirectoryOfTheApplication() {
    final ThumbnailDetail thumbnail = thumbnail("1790759697400.png");

    final Path path = thumbnail.getPath().orElseThrow();

    assertThat(path.toString(), endsWith(INSTANCE_ID + "/images/1790759697400.png"));
  }

  @Test
  void theThumbnailPickedUpInTheImagesStoreIsTheImageInTheMediaLibrary() {
    final ThumbnailDetail thumbnail = thumbnail(
        "/silverpeas/GalleryInWysiwyg/dummy?ImageId=21&ComponentId=gallery27&UseOriginal=false");

    final Path path = thumbnail.getPath().orElseThrow();

    assertThat(path, is(GALLERY_IMAGE));
  }

  @Test
  void theThumbnailPickedUpInTheImagesStoreButNoMoreFoundHasNoPath() {
    final ThumbnailDetail thumbnail = thumbnail(
        "/silverpeas/GalleryInWysiwyg/dummy?ImageId=666&ComponentId=gallery27&UseOriginal=false");

    assertThat(thumbnail.getPath().isEmpty(), is(true));
  }

  @Test
  void theThumbnailFromAnUnknownSourceHasNoPath() {
    final ThumbnailDetail thumbnail = thumbnail("/silverpeas/Unknown/dummy?ImageId=21");

    assertThat(thumbnail.getPath().isEmpty(), is(true));
  }

  private static ThumbnailDetail thumbnail(final String originalFileName) {
    final ThumbnailDetail thumbnail = new ThumbnailDetail(INSTANCE_ID, 42,
        ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
    thumbnail.setOriginalFileName(originalFileName);
    thumbnail.setMimeType("image/png");
    return thumbnail;
  }

  /**
   * Scanner of the images of a media library: like the one of the gallery application, it
   * fails when the image doesn't exist anymore.
   */
  static class GalleryScanner4Test implements LinkUrlDataSourceScanner {

    private static final Pattern GALLERY_CONTENT_LINK_PATTERN =
        Pattern.compile("(?i)=\"([^\"]*/GalleryInWysiwyg/[^\"]+)");

    @Override
    public List<LinkUrlDataSource> scanHtml(final String htmlContent) {
      return from(htmlContent)
          .withDirectives(singletonList(regexp(GALLERY_CONTENT_LINK_PATTERN, 1)))
          .extract()
          .stream()
          .map(l -> {
            if (!"21".equals(extractUrlParameters(l).get("ImageId"))) {
              throw new IllegalStateException("No such image");
            }
            final File image = GALLERY_IMAGE.toFile();
            return new LinkUrlDataSource(l, () -> new FileDataSource(image));
          })
          .toList();
    }
  }
}

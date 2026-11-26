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
package org.silverpeas.core.io.file;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.io.media.image.ImageToolDirective;
import org.silverpeas.core.io.media.image.option.AbstractImageToolOption;
import org.silverpeas.core.io.media.image.option.DimensionOption;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.silverpeas.core.io.file.ImageResizingProcessor.IMAGE_CACHE_PATH;

/**
 * Unit tests on the resizing of an image asked with a size directive in its path.
 *
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ImageResizingProcessorTest {

  private static final String IMAGE_NAME = "image-test.jpg";

  @TestManagedMock
  private ImageTool imageTool;

  @TestedBean
  private ImageResizingProcessor processor;

  private File imageDirectory;

  @BeforeEach
  void setUp() {
    File originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    imageDirectory = originalImage.getParentFile();
  }

  @AfterEach
  void tearDown() {
    FileUtils.deleteQuietly(new File(IMAGE_CACHE_PATH));
  }

  @Test
  void resizeWithWidthAndHeight() {
    DimensionOption dimension = resizeWithDirective("250x150");
    assertThat(dimension.getWidth(), is(250));
    assertThat(dimension.getHeight(), is(150));
  }

  @Test
  void resizeWithWidthOnly() {
    DimensionOption dimension = resizeWithDirective("800x");
    assertThat(dimension.getWidth(), is(800));
    assertThat(dimension.getHeight(), nullValue());
  }

  @Test
  void resizeWithHeightOnly() {
    DimensionOption dimension = resizeWithDirective("x150");
    assertThat(dimension.getWidth(), nullValue());
    assertThat(dimension.getHeight(), is(150));
  }

  @Test
  void aZeroHeightMeansNoHeightConstraint() {
    DimensionOption dimension = resizeWithDirective("800x0");
    assertThat(dimension.getWidth(), is(800));
    assertThat(dimension.getHeight(), nullValue());
  }

  @Test
  void aZeroWidthMeansNoWidthConstraint() {
    DimensionOption dimension = resizeWithDirective("0x150");
    assertThat(dimension.getWidth(), nullValue());
    assertThat(dimension.getHeight(), is(150));
  }

  @Test
  void noResizingWithoutAnyPositiveDimension() {
    String path = new File(imageDirectory, "0x0/" + IMAGE_NAME).getPath();
    String processedPath = processor.processBefore(path, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(processedPath, is(path));
    verify(imageTool, never()).convert(any(File.class), any(File.class), any(Set.class),
        any(ImageToolDirective.class));
  }

  @Test
  void noResizingForAnImageWithoutParentDirectory() {
    String processedPath = processor.processBefore(IMAGE_NAME, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(processedPath, is(IMAGE_NAME));
  }

  @SuppressWarnings("unchecked")
  private DimensionOption resizeWithDirective(final String size) {
    String path = new File(imageDirectory, size + "/" + IMAGE_NAME).getPath();
    String processedPath = processor.processBefore(path, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(processedPath.startsWith(IMAGE_CACHE_PATH), is(true));

    ArgumentCaptor<Set<AbstractImageToolOption>> options = ArgumentCaptor.forClass(Set.class);
    verify(imageTool).convert(eq(new File(imageDirectory, IMAGE_NAME)), any(File.class),
        options.capture(), eq(ImageToolDirective.GEOMETRY_SHRINK));
    return options.getValue().stream()
        .filter(DimensionOption.class::isInstance)
        .map(DimensionOption.class::cast)
        .findFirst()
        .orElseThrow();
  }
}

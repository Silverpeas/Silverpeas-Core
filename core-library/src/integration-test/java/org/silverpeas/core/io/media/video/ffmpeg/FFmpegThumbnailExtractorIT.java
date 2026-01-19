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
package org.silverpeas.core.io.media.video.ffmpeg;

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Arquillian.class)
public class FFmpegThumbnailExtractorIT {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  private VideoThumbnailExtractor videoThumbnailExtractor;

  @Inject
  private FFmpegToolManager ffmpegToolManager;

  private File mp4File;
  private File flvFile;
  private File movFile;

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onWarForTestClass(FFmpegThumbnailExtractorIT.class)
        .addIndexingEngine()
        .addMavenDependencies("org.apache.commons:commons-exec")
        .addPackages(true, "org.silverpeas.core.io.media")
        .addAsResource("video.mp4")
        .addAsResource("video.flv")
        .addAsResource("video.mov")
        .build();
  }

  @Before
  public void initialize() throws Exception {
    ffmpegToolManager.init();
  }

  private String getIntegrationTestResourcePath() {
    return mavenTargetDirectoryRule.getResourceTestDirFile().getAbsolutePath();
  }


  @Before
  public void setUp() throws Exception {
    String BASE_PATH = getIntegrationTestResourcePath();
    mp4File = getDocumentNamed(BASE_PATH + "/video.mp4");
    flvFile = getDocumentNamed(BASE_PATH + "/video.flv");
    movFile = getDocumentNamed(BASE_PATH + "/video.mov");

    videoThumbnailExtractor = VideoThumbnailExtractor.get();
    cleanThumbnails();
  }

  @After
  public void tearDown() throws Exception {
    cleanThumbnails();
  }

  private void cleanThumbnails() throws IOException {
    for (int i = 0; i < 5; i++) {
      Files.deleteIfExists(mp4File.getParentFile().toPath().resolve("img" + i + ".jpg"));
    }
  }

  @Test
  public void testGenerateThumbnailsFromMp4() {
    generateThumbnailsFromFile(mp4File);
  }

  @Test
  public void testGenerateThumbnailsFromFlv() {
    generateThumbnailsFromFile(flvFile);
  }

  @Test
  public void testGenerateThumbnailsFromMov() {
    generateThumbnailsFromFile(movFile);
  }

  private void generateThumbnailsFromFile(File file) {
    assertThat(videoThumbnailExtractor, notNullValue());
    if (videoThumbnailExtractor.isActivated()) {
      videoThumbnailExtractor.generateThumbnailsFrom(file);
      assertThat(new File(file.getParentFile(), "img0.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img1.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img2.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img3.jpg").exists(), equalTo(true));
      assertThat(new File(file.getParentFile(), "img4.jpg").exists(), equalTo(true));
    }
  }

  private static File getDocumentNamed(final String name) {
    return new File(name);
  }

}

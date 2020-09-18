/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.io.media.video.ffmpeg;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opends.server.admin.DurationUnit;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.exec.CollectingLogOutputStream;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecutionException;
import org.silverpeas.core.util.time.Duration;
import org.silverpeas.core.util.time.DurationConversionBoardKey;

import javax.inject.Inject;
import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class FFmpegThumbnailExtractorIT {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  private VideoThumbnailExtractor videoThumbnailExtractor;

  @Inject
  private FFmpegToolManager ffmpegToolManager;

  private String BASE_PATH;

  private File mp4File;
  private File flvFile;
  private File movFile;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(FFmpegThumbnailExtractorIT.class)
        .addSilverpeasExceptionBases()
        .addCommonBasicUtilities()
        .addMavenDependencies("org.apache.commons:commons-exec").testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.io.media");
          warBuilder.addClasses(ExternalExecution.class, MetadataExtractor.class, MetaData.class,
              ExternalExecutionException.class, CollectingLogOutputStream.class, UnitUtil.class,
              TimeUnit.class, Duration.class, DurationConversionBoardKey.class, DurationUnit.class);
          warBuilder.addAsResource("video.mp4");
          warBuilder.addAsResource("video.flv");
          warBuilder.addAsResource("video.mov");
        }).build();
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
    BASE_PATH = getIntegrationTestResourcePath();
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

  private void cleanThumbnails() {
    for (int i = 0; i < 5; i++) {
      new File(mp4File.getParentFile(), "img" + i + ".jpg").delete();
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

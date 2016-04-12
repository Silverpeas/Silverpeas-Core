package org.silverpeas.core.io.media.video.ffmpeg;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.util.exec.CollectingLogOutputStream;
import org.silverpeas.core.util.exec.ExternalExecution;
import org.silverpeas.core.util.exec.ExternalExecutionException;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractor;
import org.silverpeas.core.io.media.video.VideoThumbnailExtractorProvider;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.time.TimeConversionBoardKey;
import org.silverpeas.core.util.time.TimeData;
import org.silverpeas.core.util.time.TimeUnit;

import javax.inject.Inject;
import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class FFmpegThumbnailExtractorTest {

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
    return WarBuilder4LibCore.onWarForTestClass(FFmpegThumbnailExtractorTest.class)
        .addSilverpeasExceptionBases()
        .addCommonBasicUtilities()
        .addMavenDependencies("org.apache.tika:tika-core", "org.apache.tika:tika-parsers",
            "org.apache.commons:commons-exec").testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.io.media");
          warBuilder.addClasses(ExternalExecution.class, MetadataExtractor.class, MetaData.class,
              ExternalExecutionException.class, CollectingLogOutputStream.class, UnitUtil.class,
              TimeUnit.class, TimeConversionBoardKey.class, TimeData.class);
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

    videoThumbnailExtractor = VideoThumbnailExtractorProvider.getVideoThumbnailExtractor();
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

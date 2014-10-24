package org.silverpeas.media.video.ffmpeg;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.exec.CollectingLogOutputStream;
import org.silverpeas.exec.ExternalExecution;
import org.silverpeas.exec.ExternalExecutionException;
import org.silverpeas.media.video.VideoThumbnailExtractor;
import org.silverpeas.media.video.VideoThumbnailExtractorProvider;
import org.silverpeas.test.WarBuilder4LibCore;
import org.silverpeas.util.MetaData;
import org.silverpeas.util.MetadataExtractor;
import org.silverpeas.util.UnitUtil;
import org.silverpeas.util.time.TimeConversionBoardKey;
import org.silverpeas.util.time.TimeData;
import org.silverpeas.util.time.TimeUnit;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class FFmpegThumbnailExtractorTest {

  private VideoThumbnailExtractor videoThumbnailExtractor;

  // Inject the following class in order to call @PostContructor init method
  @Inject
  private FFmpegToolManager ffmpegToolManager;

  private static final String BASE_PATH = getIntegrationTestResourcePath();

  private static final File mp4File = getDocumentNamed(BASE_PATH + "/video.mp4");
  private static final File flvFile = getDocumentNamed(BASE_PATH + "/video.flv");
  private static final File movFile = getDocumentNamed(BASE_PATH + "/video.mov");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWar().addSilverpeasExceptionBases().addCommonBasicUtilities()
        .addMavenDependencies("org.apache.tika:tika-core", "org.apache.tika:tika-parsers",
            "org.apache.commons:commons-exec").testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.media");
          warBuilder.addClasses(ExternalExecution.class, MetadataExtractor.class, MetaData.class,
              ExternalExecutionException.class, CollectingLogOutputStream.class, UnitUtil.class,
              TimeUnit.class, TimeConversionBoardKey.class, TimeData.class);
          warBuilder.addAsResource("maven.properties");
          warBuilder.addAsResource("video.mp4");
          warBuilder.addAsResource("video.flv");
          warBuilder.addAsResource("video.mov");
        }).build();
  }


  private static String getIntegrationTestResourcePath() {
    Properties props = new Properties();
    try {
      props.load(FFmpegThumbnailExtractorTest.class.getClassLoader()
          .getResourceAsStream("maven.properties"));
    } catch (IOException ex) {
      Logger.getLogger(FFmpegThumbnailExtractorTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    File targetDir = new File(props.getProperty("int-test.dir", "/home/user/"));
    try {
      return targetDir.getCanonicalPath();
    } catch (IOException e) {
      fail();
    }
    return "";
  }


  @Before
  public void setUp() throws Exception {
    videoThumbnailExtractor = VideoThumbnailExtractorProvider.getVideoThumbnailExtractor();
    cleanThumbnails();
  }

  @After
  public void tearDown() throws Exception {
    cleanThumbnails();
  }

  private static void cleanThumbnails() {
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

package org.silverpeas.media.video.ffmpeg;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class FFmpegUtilTest {

  private static final String OS_KEY = "os.name";

  private static String realSystem;

  @BeforeClass
  public static void computeRealSystem() {
    realSystem = System.getProperty(OS_KEY);
  }

  @AfterClass
  public static void restoreRealSystem() {
    System.setProperty(OS_KEY, realSystem);
  }

  @Test
  @Ignore
  public void testIsActivated() {
  }

  @Test
  public void testBuildFFmpegThumbnailExtractorCommandLine() {
    System.setProperty(OS_KEY, "Linux");
    File inputFile = new File("/silverpeas/video/", "movie.mp4");
    File outputFile = new File("/silverpeas/viewer/", "thumb.jpg");
    CommandLine result =
        FFmpegUtil.buildFFmpegThumbnailExtractorCommandLine(inputFile, outputFile, 30);
    Assert.assertThat(result, Matchers.is(Matchers.notNullValue()));
    Assert.assertThat(FilenameUtils.separatorsToUnix(String.join(" ", result.toStrings())), Matchers
        .is("ffmpeg -ss 30 -i /silverpeas/video/movie.mp4 -vframes 1 -vf scale=600:-1 " +
            "/silverpeas/viewer/thumb.jpg"));
  }

}

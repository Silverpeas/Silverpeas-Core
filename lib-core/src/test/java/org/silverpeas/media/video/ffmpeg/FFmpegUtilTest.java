package org.silverpeas.media.video.ffmpeg;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
    assertThat(result, is(notNullValue()));
    assertThat(FilenameUtils.separatorsToUnix(result.toString()),
        is("ffmpeg -ss 30 -i /silverpeas/video/movie.mp4 -vframes 1 /silverpeas/viewer/thumb.jpg"));
  }

}

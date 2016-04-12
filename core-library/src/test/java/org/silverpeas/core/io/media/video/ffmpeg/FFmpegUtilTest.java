package org.silverpeas.core.io.media.video.ffmpeg;

import org.apache.commons.exec.CommandLine;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.lang.SystemWrapper;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FFmpegUtilTest {

  private static final String OS_KEY = "os.name";

  private static String realSystem;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void computeRealSystem() {
    realSystem = SystemWrapper.get().getProperty(OS_KEY);
  }

  @After
  public void restoreRealSystem() {
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
    assertThat(result, is(Matchers.notNullValue()));
    assertThat(String.join(" ", result.toStrings()),
        is("ffmpeg -ss 30 -i " + inputFile.getAbsolutePath() + " -vframes 1 -vf scale=600:-1 " +
            outputFile.getAbsolutePath()));
  }
}

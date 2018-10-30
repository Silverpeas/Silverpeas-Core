/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

import org.apache.commons.exec.CommandLine;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.test.extention.SilverTestEnv;
import org.silverpeas.core.util.lang.SystemWrapper;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SilverTestEnv.class)
public class FFmpegUtilTest {

  private static final String OS_KEY = "os.name";

  private static String realSystem;

  @BeforeEach
  public void computeRealSystem() {
    realSystem = SystemWrapper.get().getProperty(OS_KEY);
  }

  @AfterEach
  public void restoreRealSystem() {
    System.setProperty(OS_KEY, realSystem);
  }

  @Test
  @Disabled
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

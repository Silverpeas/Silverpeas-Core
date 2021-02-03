/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.viewer.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.UnitTest;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author ehugonnet
 */
@UnitTest
public class SwfUtilTest {

  private static final String OS_KEY = "os.name";

  private static String realSystem;

  public SwfUtilTest() {
  }

  @BeforeAll
  public static void computeRealSystem() {
    realSystem = System.getProperty(OS_KEY);
  }

  @AfterAll
  public static void restoreRealSystem() {
    System.setProperty(OS_KEY, realSystem);
  }

  /**
   * Test of buildPdfToSwfCommandLine method, of class SwfUtil.
   */
  @Test
  public void testLinuxBuildPdfToSwfCommandLineWithoutEndingCommand() {
    System.setProperty(OS_KEY, "Linux");
    String endingCommand = "";
    File inputFile = new File("/silverpeas/viewer/", "file ' - '' .pdf");
    File outputFile = new File("/silverpeas/viewer/", "file ' - '' .swf");
    CommandLine result = SwfUtil.buildPdfToSwfCommandLine(endingCommand, inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(separatorsToUnix(String.join(" ", result.toStrings())),
        is("pdf2swf " + "/silverpeas/viewer/file ' - '' .pdf " +
            "-o /silverpeas/viewer/file ' - '' .swf -f -T 9 -t -s storeallcharacters"));
  }

  /**
   * Test of buildPdfToSwfCommandLine method, of class SwfUtil.
   */
  @Test
  public void testBuildPdfToSwfCommandLineWithoutEndingCommand() {
    System.setProperty(OS_KEY, "Windows XP");
    String endingCommand = "";
    File inputFile = new File("/silverpeas/viewer/", "file ' - '' .pdf");
    File outputFile = new File("/silverpeas/viewer/", "file ' - '' .swf");
    CommandLine result = SwfUtil.buildPdfToSwfCommandLine(endingCommand, inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(separatorsToUnix(String.join(" ", result.toStrings())),
        is("pdf2swf /silverpeas/viewer/file ' - '' .pdf" +
            " -o /silverpeas/viewer/file ' - '' .swf -f -T 9 -t -s storeallcharacters"));
  }

  private static String separatorsToUnix(String filePath) {
    return FilenameUtils.separatorsToUnix(filePath).replaceAll("[a-zA-Z]:", "");
  }

}

/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.viewer.util;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.silverpeas.util.FileUtil.convertFilePath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class SwfUtilTest {

  private static final String OS_KEY = "os.name";

  private static String realSystem;

  public SwfUtilTest() {
  }

  @BeforeClass
  public static void computeRealSystem() {
    realSystem = System.getProperty(OS_KEY);
  }

  @AfterClass
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
    String inputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    String outputFile = convertFilePath(new File("/silverpeas/viewer/",
        "file ' - '' .swf"));
    CommandLine result = SwfUtil.buildPdfToSwfCommandLine(endingCommand, inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("pdf2swf /silverpeas/viewer/file\\ \\'\\ -\\ \\'\\'\\ .pdf "
        + "-o /silverpeas/viewer/file\\ \\'\\ -\\ \\'\\'\\ .swf -f -T 9 -t -s storeallcharacters"));
  }

  /**
   * Test of buildPdfDocumentInfoCommandLine method, of class SwfUtil.
   */
  @Test
  public void testLinuxBuildPdfDocumentInfoCommandLine() {
    System.setProperty(OS_KEY, "Linux");
    String file = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    CommandLine result = SwfUtil.buildPdfDocumentInfoCommandLine(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("pdf2swf -qq /silverpeas/viewer/file\\ \\'\\ -\\ \\'\\'\\ .pdf"
        + " --info"));
  }

  /**
   * Test of buildSwfToImageCommandLine method, of class SwfUtil.
   */
  @Test
  public void testLinuxBuildSwfToImageCommandLine() {
    System.setProperty(OS_KEY, "Linux");
    String inputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    String outputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .swf"));
    CommandLine result = SwfUtil.buildSwfToImageCommandLine(inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("swfrender /silverpeas/viewer/file\\ \\'\\ -\\ \\'\\'\\ .pdf"
        + " -o /silverpeas/viewer/file\\ \\'\\ -\\ \\'\\'\\ .swf"));
  }

  /**
   * Test of buildPdfToSwfCommandLine method, of class SwfUtil.
   */
  @Test
  public void testBuildPdfToSwfCommandLineWithoutEndingCommand() {
    System.setProperty(OS_KEY, "Windows XP");
    String endingCommand = "";
    String inputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    String outputFile = convertFilePath(new File("/silverpeas/viewer/",
        "file ' - '' .swf"));
    CommandLine result = SwfUtil.buildPdfToSwfCommandLine(endingCommand, inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("pdf2swf \"/silverpeas/viewer/file ' - '' .pdf\" "
        + "-o \"/silverpeas/viewer/file ' - '' .swf\" -f -T 9 -t -s storeallcharacters"));
  }

  /**
   * Test of buildPdfDocumentInfoCommandLine method, of class SwfUtil.
   */
  @Test
  public void testBuildPdfDocumentInfoCommandLine() {
    System.setProperty(OS_KEY, "Windows XP");
    String file = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    CommandLine result = SwfUtil.buildPdfDocumentInfoCommandLine(file);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("pdf2swf -qq \"/silverpeas/viewer/file ' - '' .pdf\""
        + " --info"));
  }

  /**
   * Test of buildSwfToImageCommandLine method, of class SwfUtil.
   */
  @Test
  public void testBuildSwfToImageCommandLine() {
    System.setProperty(OS_KEY, "Windows XP");
    String inputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .pdf"));
    String outputFile = convertFilePath(new File("/silverpeas/viewer/", "file ' - '' .swf"));
    CommandLine result = SwfUtil.buildSwfToImageCommandLine(inputFile, outputFile);
    assertThat(result, is(notNullValue()));
    assertThat(result.toString(), is("swfrender \"/silverpeas/viewer/file ' - '' .pdf\""
        + " -o \"/silverpeas/viewer/file ' - '' .swf\""));
  }

}

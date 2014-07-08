/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import com.silverpeas.util.exception.RelativeFileAccessException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class FileUtilTest {

  public FileUtilTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetFilename() {
    assertThat(FileUtil.getFilename(null), is(""));
    assertThat(FileUtil.getFilename(""), is(""));
    assertThat(FileUtil.getFilename("     "), is(""));
    assertThat(FileUtil.getFilename(" /a\\b/c/file "), is("file "));
    assertThat(FileUtil.getFilename(" /a\\b/c/file.txt.bat.jpg"), is("file.txt.bat.jpg"));
    assertThat(FileUtil.getFilename(" /a\\b/c\\file.txt_zkw"), is("file.txt_zkw"));
    assertThat(FileUtil.getFilename(" /a\\b/c\\file.txt"), is("file.txt"));
    assertThat(FileUtil.getFilename(" file.txt"), is(" file.txt"));
  }

  /**
   * Test of getMimeType method, of class FileUtil.
   */
  @Test
  public void testGetMimeType() {
    String fileName = "";
    String expResult = FileUtil.DEFAULT_MIME_TYPE;
    String result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
    fileName = "toto.DOc";
    expResult = FileUtil.WORD_MIME_TYPE;
    result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
  }

  /**
   * Test of getAttachmentContext method, of class FileUtil.
   */
  @Test
  public void testGetAttachmentContext() {
    String context = "";
    String[] expResult = new String[]{FileUtil.BASE_CONTEXT};
    String[] result = FileUtil.getAttachmentContext(context);
    assertNotNull(result);
    assertArrayEquals(expResult, result);
    context = "test,context,complex";
    expResult = new String[]{FileUtil.BASE_CONTEXT, "test", "context", "complex"};
    result = FileUtil.getAttachmentContext(context);
    assertNotNull(result);
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of loadBundle method, of class FileUtil.
   */
  @Test
  public void testLoadBundle() {
    String name = "com/stratelia/webactiv/multilang/generalMultilang";
    ResourceBundle result = FileUtil.loadBundle(name, Locale.FRENCH);
    assertNotNull(result);
  }

  @Test
  public void testIsArchive() {
    assertTrue(FileUtil.isArchive("toto.zip"));
    assertTrue(FileUtil.isArchive("toto.tar.gz"));
    assertTrue(FileUtil.isArchive("toto.jar"));
    assertFalse(FileUtil.isArchive("toto.war"));
    assertFalse(FileUtil.isArchive("toto.ear"));
    assertFalse(FileUtil.isArchive("toto.txt"));
    assertTrue(FileUtil.isArchive("toto.tgz"));
  }

  @Test
  public void testConvertFilePath() {
    String result = FileUtil.convertFilePath(new File("/", "file\r \\' - '' .pdf"));
    assertThat(result, is("/file\\ \\ \\\\\\'\\ -\\ \\'\\'\\ .pdf"));

    result = FileUtil.convertFilePath(new File("/", "test {linux}[4 ever].pdf"));
    assertThat(result, is("/test\\ \\{linux\\}\\[4\\ ever\\].pdf"));
  }

  @Test
  public void testCheckPathNotRelative() throws RelativeFileAccessException {
    FileUtil.checkPathNotRelative(null);
    FileUtil.checkPathNotRelative("klkl");
    FileUtil.checkPathNotRelative("klkl.lk");
    FileUtil.checkPathNotRelative("klkl/dsdsd/SdsdsD/dlsls.ld");
    FileUtil.checkPathNotRelative("klkl/dsdsd/Sdsd..sD/dlsls.ld");
    FileUtil.checkPathNotRelative("klkl/dsdsd/Sdsd./dlsls.ld");
    FileUtil.checkPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.checkPathNotRelative(".klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.checkPathNotRelative("..klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.checkPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld.");
    FileUtil.checkPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld..");
  }

  @Test(expected = RelativeFileAccessException.class)
  public void testCheckPathNotRelativeError1() throws RelativeFileAccessException {
    FileUtil.checkPathNotRelative("../");
  }

  @Test(expected = RelativeFileAccessException.class)
  public void testCheckPathNotRelativeError2() throws RelativeFileAccessException {
    FileUtil.checkPathNotRelative("..\\");
  }

  @Test(expected = RelativeFileAccessException.class)
  public void testCheckPathNotRelativeError3() throws RelativeFileAccessException {
    FileUtil.checkPathNotRelative("/..");
  }

  @Test(expected = RelativeFileAccessException.class)
  public void testCheckPathNotRelativeError4() throws RelativeFileAccessException {
    FileUtil.checkPathNotRelative("\\..");
  }

  @Test
  public void testDeleteEmptyDir() throws IOException {
    File root = File.createTempFile("prefix", "suffix");
    FileUtils.deleteQuietly(root);
    assertThat(root.exists(), is(false));

    File newFile = new File(root, "aFile.txt");
    FileUtils.touch(newFile);
    assertThat(root.exists(), is(true));
    assertThat(root.isDirectory(), is(true));
    assertThat(newFile.exists(), is(true));
    assertThat(newFile.isFile(), is(true));

    assertThat(FileUtil.deleteEmptyDir(root), is(false));
    assertThat(root.exists(), is(true));
    assertThat(root.isDirectory(), is(true));
    assertThat(newFile.exists(), is(true));
    assertThat(newFile.isFile(), is(true));

    assertThat(newFile.delete(), is(true));
    assertThat(newFile.exists(), is(false));

    assertThat(FileUtil.deleteEmptyDir(root), is(true));
    assertThat(root.exists(), is(false));
  }
}

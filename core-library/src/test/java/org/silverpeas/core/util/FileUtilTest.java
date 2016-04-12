/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.util;

import org.junit.Rule;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class FileUtilTest {

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  private File rootFolder;

  @Before
  public void setUp() throws IOException {
    rootFolder = File.createTempFile("root", "Folder");
    if (rootFolder.exists()) {
      FileUtils.deleteQuietly(rootFolder);
      rootFolder =
          new File(FileUtils.getTempDirectory(), "rootFolder_" + UUID.randomUUID().toString());
    }
    rootFolder.mkdirs();
    FileUtils.touch(new File(rootFolder, "atRoot.txt"));
    File subFolderA = new File(rootFolder, "SubFolderA");
    subFolderA.mkdir();
    FileUtils.touch(new File(subFolderA, "atSubFolderA_1.txt"));
    FileUtils.touch(new File(subFolderA, "atSubFolderA_2.txt"));
    File subFolderB = new File(rootFolder, "SubFolderB");
    subFolderB.mkdir();
    FileUtils.touch(new File(subFolderB, "atSubFolderB_1.txt"));
    FileUtils.touch(new File(subFolderB, "sameName.txt"));
    File subFolderBSubFolderA = new File(subFolderB, "SubFolderBSubFolderA");
    subFolderBSubFolderA.mkdir();
    FileUtils.touch(new File(subFolderBSubFolderA, "atSubFolderBSubFolderA_1.txt"));
    FileUtils.touch(new File(subFolderBSubFolderA, "atSubFolderBSubFolderA_2.txt"));
    FileUtils.touch(new File(subFolderBSubFolderA, "sameName.txt"));

    String[] expectedFiles = new String[]{"/atRoot.txt", "/SubFolderA/atSubFolderA_1.txt",
        "/SubFolderA/atSubFolderA_2.txt", "/SubFolderB/atSubFolderB_1.txt",
        "/SubFolderB/sameName.txt", "/SubFolderB/SubFolderBSubFolderA/atSubFolderBSubFolderA_1.txt",
        "/SubFolderB/SubFolderBSubFolderA/atSubFolderBSubFolderA_2.txt",
        "/SubFolderB/SubFolderBSubFolderA/sameName.txt"};

    List<String> actualFiles = new ArrayList<String>();
    int substringIndex = rootFolder.getPath().length();
    for (File file : FileUtils.listFiles(rootFolder, FileFilterUtils.trueFileFilter(),
        FileFilterUtils.trueFileFilter())) {
      actualFiles.add(FilenameUtils.separatorsToUnix(file.getPath().substring(substringIndex)));
    }
    assertThat(actualFiles, containsInAnyOrder(expectedFiles));
  }

  @After
  public void tearDown() {
    FileUtils.deleteQuietly(rootFolder);
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

  @Ignore("This test is not multi-platform compliant")
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

  @Test
  public void testMoveAllFilesAtRootFolder() throws IOException {
    File[] foldersAtRoot = FileUtil.moveAllFilesAtRootFolder(rootFolder);
    assertThat(foldersAtRoot, arrayWithSize(2));
    for (File folder : foldersAtRoot) {
      assertThat(folder.exists(), is(false));
    }

    String[] expectedFiles =
        new String[]{"/atRoot.txt", "/atSubFolderA_1.txt", "/atSubFolderA_2.txt",
            "/atSubFolderB_1.txt", "/atSubFolderBSubFolderA_1.txt", "/atSubFolderBSubFolderA_2.txt",
            "/sameName.txt"};

    List<String> actualFiles = new ArrayList<String>();
    int substringIndex = rootFolder.getPath().length();
    for (File file : FileUtils.listFiles(rootFolder, FileFilterUtils.trueFileFilter(),
        FileFilterUtils.trueFileFilter())) {
      actualFiles.add(FilenameUtils.separatorsToUnix(file.getPath().substring(substringIndex)));
    }
    assertThat(actualFiles, containsInAnyOrder(expectedFiles));
  }

  @Test
  public void testMoveAllFilesAtRootFolderThatDoesNotExist() throws IOException {
    File[] foldersAtRoot = FileUtil.moveAllFilesAtRootFolder(new File("juudejdefgegzflbzefjze"));
    assertThat(foldersAtRoot, arrayWithSize(0));
  }

  @Test
  public void testMoveAllFilesAtRootFolderWhichInstanceIsNull() throws IOException {
    File[] foldersAtRoot = FileUtil.moveAllFilesAtRootFolder(null);
    assertThat(foldersAtRoot, arrayWithSize(0));
  }

  @Test
  public void testValidateFileNameOk() throws Exception {
    FileUtil.validateFilename("myFileName", ".");
  }

  @Test(expected = IllegalStateException.class)
  public void testValidateFileNameKo() throws Exception {
    FileUtil.validateFilename(".." + File.separator, ".");
  }

}

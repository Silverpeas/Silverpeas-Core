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
package org.silverpeas.core.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ehugonnet
 */
@EnableSilverTestEnv
class FileUtilTest {

  private File rootFolder;

  @BeforeEach
  public void cleanUPCaches() {
    CacheServiceProvider.getRequestCacheService().clearAllCaches();
    CacheServiceProvider.getThreadCacheService().clearAllCaches();
  }

  @BeforeEach
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

  @AfterEach
  public void tearDown() {
    FileUtils.deleteQuietly(rootFolder);
  }

  @Test
  void testGetFilename() {
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
  void testGetMimeType() {
    String fileName = "";
    String expResult = MimeTypes.DEFAULT_MIME_TYPE;
    String result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
    fileName = "toto.DOc";
    expResult = MimeTypes.WORD_MIME_TYPE;
    result = FileUtil.getMimeType(fileName);
    assertEquals(expResult, result);
  }

  /**
   * Test of getAttachmentContext method, of class FileUtil.
   */
  @Test
  void testGetAttachmentContext() {
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
  void testIsArchive() {
    assertTrue(FileUtil.isArchive("toto.zip"));
    assertTrue(FileUtil.isArchive("toto.tar.gz"));
    // with Apache Chemistry, all the Java archives (jar, war, sar, ear, ...)
    // are now taken into account as a Java archive and no more as a simple zip archive.
    assertTrue(FileUtil.isArchive("toto.jar"));
    assertTrue(FileUtil.isArchive("toto.war"));
    assertTrue(FileUtil.isArchive("toto.ear"));
    assertFalse(FileUtil.isArchive("toto.txt"));
    assertTrue(FileUtil.isArchive("toto.tgz"));
    assertFalse(FileUtil.isArchive("toto.odt"));
  }

  @Test
  void testCheckPathNotRelative() throws RelativeFileAccessException {
    FileUtil.assertPathNotRelative(null);
    FileUtil.assertPathNotRelative("klkl");
    FileUtil.assertPathNotRelative("klkl.lk");
    FileUtil.assertPathNotRelative("klkl/dsdsd/SdsdsD/dlsls.ld");
    FileUtil.assertPathNotRelative("klkl/dsdsd/Sdsd..sD/dlsls.ld");
    FileUtil.assertPathNotRelative("klkl/dsdsd/Sdsd./dlsls.ld");
    FileUtil.assertPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.assertPathNotRelative(".klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.assertPathNotRelative("..klkl/dsdsd/.Sdsd/dlsls.ld");
    FileUtil.assertPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld.");
    FileUtil.assertPathNotRelative("klkl/dsdsd/.Sdsd/dlsls.ld..");
  }

  @Test
  void testCheckPathNotRelativeError1() throws RelativeFileAccessException {
    assertThrows(RelativeFileAccessException.class, () -> FileUtil.assertPathNotRelative("../"));
  }

  @Test
  void testCheckPathNotRelativeError2() throws RelativeFileAccessException {
    assertThrows(RelativeFileAccessException.class, () -> FileUtil.assertPathNotRelative("..\\"));
  }

  @Test
  void testCheckPathNotRelativeError3() throws RelativeFileAccessException {
    assertThrows(RelativeFileAccessException.class, () -> FileUtil.assertPathNotRelative("/.."));
  }

  @Test
  void testCheckPathNotRelativeError4() throws RelativeFileAccessException {
    assertThrows(RelativeFileAccessException.class, () -> FileUtil.assertPathNotRelative("\\.."));
  }

  @Test
  void testDeleteEmptyDir() throws IOException {
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
  void testMoveAllFilesAtRootFolder() throws IOException {
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
  void testMoveAllFilesAtRootFolderThatDoesNotExist() throws IOException {
    File[] foldersAtRoot = FileUtil.moveAllFilesAtRootFolder(new File("juudejdefgegzflbzefjze"));
    assertThat(foldersAtRoot, arrayWithSize(0));
  }

  @Test
  void testMoveAllFilesAtRootFolderWhichInstanceIsNull() throws IOException {
    File[] foldersAtRoot = FileUtil.moveAllFilesAtRootFolder(null);
    assertThat(foldersAtRoot, arrayWithSize(0));
  }

  @Test
  void testValidateFileNameOk() throws Exception {
    FileUtil.validateFilename("myFileName", ".");
  }

  @Test
  void testValidateFileNameKo() throws Exception {
    assertThrows(IllegalStateException.class,
        () -> FileUtil.validateFilename(".." + File.separator, "."));
  }

}

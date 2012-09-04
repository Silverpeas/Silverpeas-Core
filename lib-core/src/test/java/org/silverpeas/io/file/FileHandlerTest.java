/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package org.silverpeas.io.file;

import static com.stratelia.webactiv.util.GeneralPropertiesManager.getString;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.sizeOf;
import static org.apache.commons.io.FileUtils.touch;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.io.excepion.IOFileHandlerException;
import org.silverpeas.io.session.IOSession;

/**
 * @author Yohann Chastagnier
 */
public class FileHandlerTest {

  private static final IOBasePath BASE_PATH_TEST = IOBasePath.UPLOAD_PATH;
  private static final IOSession currentSession = createSessionTest();
  private static final String componentInstanceId = "componentInstanceId";
  private static final File sessionRootPath = new File(getString("tempPath"));
  private static final File realRootPath = new File(BASE_PATH_TEST.getPath());
  private static final File otherFile = new File(
      new File(BASE_PATH_TEST.getPath()).getParentFile(), "other");
  private static final File sessionHandledPath = getFile(sessionRootPath, currentSession.getId(),
      BASE_PATH_TEST.getIoNodeName());
  private static final File realComponentPath = getFile(realRootPath, componentInstanceId);
  private static final File sessionComponentPath = getFile(sessionRootPath, currentSession.getId(),
      BASE_PATH_TEST.getIoNodeName(), componentInstanceId);

  private FileHandler fileHandler;

  @Before
  public void beforeTest() throws Exception {
    fileHandler = new FileHandler(currentSession);
    realComponentPath.mkdirs();
    sessionComponentPath.mkdirs();
    touch(otherFile);
  }

  @After
  public void afterTest() throws Exception {
    deleteQuietly(sessionRootPath);
    deleteQuietly(realRootPath);
    deleteQuietly(otherFile);
  }

  /*
   * getFile
   */

  @Test(expected = NullPointerException.class)
  public void test_getFile_onFailure_1() {
    fileHandler.getFile(BASE_PATH_TEST, (File) null);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_getFile_onFailure_2() {
    fileHandler.getFile(BASE_PATH_TEST, otherFile);
  }

  @Test
  public void test_getFile() throws Exception {
    File test = fileHandler.getFile(BASE_PATH_TEST, sessionComponentPath);
    File expected = sessionComponentPath;
    assertFileNames(test, expected);

    test = fileHandler.getFile(BASE_PATH_TEST, realComponentPath, "file");
    expected = getFile(sessionComponentPath, "file");
    assertFileNames(test, expected);

    touch(getFile(realComponentPath, "file"));
    test = fileHandler.getFile(BASE_PATH_TEST, realComponentPath, "file");
    expected = getFile(realComponentPath, "file");
    assertFileNames(test, expected);

    test = fileHandler.getFile(otherFile);
    expected = otherFile;
    assertFileNames(test, expected);

    test = fileHandler.getFile(otherFile, "file");
    expected = getFile(otherFile, "file");
    assertFileNames(test, expected);
  }

  /*
   * openOutputStream
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_openOutputStream_onFailure_1() throws Exception {
    touch(otherFile);
    closeQuietly(fileHandler.openOutputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = FileNotFoundException.class)
  public void test_openOutputStream_onFailure_2() throws Exception {
    final File file = getFile(realComponentPath, "file");
    final OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file);
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(file), is("toto"));
  }

  @Test
  public void test_openOutputStream_1() throws Exception {
    File file = getFile(realComponentPath, "file");
    OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file);
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("toto"));

    // Not append
    test = fileHandler.openOutputStream(BASE_PATH_TEST, file);
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("toto"));

    // Append
    file = getFile(realComponentPath, "file");
    test = fileHandler.openOutputStream(BASE_PATH_TEST, file, true);
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("totototo"));

    // Append
    file = getFile(realComponentPath, "file2");
    writeStringToFile(file, "file2");
    test = fileHandler.openOutputStream(BASE_PATH_TEST, file, true);
    try {
      write("line 2", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file2")), is("file2line 2"));
  }

  /*
   * openInputStream
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_openInputStream_onFailure_1() throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_openInputStream_onFailure_2() throws Exception {
    touch(otherFile);
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = FileNotFoundException.class)
  public void test_openInputStream_onFailure_3() throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(sessionComponentPath, "file")));
  }

  @Test(expected = FileNotFoundException.class)
  public void test_openInputStream_onFailure_4() throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(realComponentPath, "file")));
  }

  @Test
  public void test_openInputStream() throws Exception {

    // File exists in real path only
    File file = getFile(realComponentPath, "file");
    touch(file);
    InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file);
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(file);
    }

    // File exists in session path only
    file = getFile(sessionComponentPath, "file");
    touch(file);
    test = fileHandler.openInputStream(BASE_PATH_TEST, file);
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(file);
    }

    // File exists in session path only
    file = getFile(sessionComponentPath, "file");
    final File file2 = getFile(realComponentPath, "file");
    touch(file);
    touch(file2);
    test = fileHandler.openInputStream(BASE_PATH_TEST, file);
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(file);
      deleteQuietly(file2);
    }
  }

  /*
   * touch
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_touch_onFailure_1() throws Exception {
    fileHandler.touch(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_touch_onFailure_2() throws Exception {
    fileHandler.touch(null, otherFile);
  }

  @Test
  public void test_touch() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    assertThat(sessionFile.exists(), is(false));
    assertThat(realFile.exists(), is(false));
    fileHandler.touch(BASE_PATH_TEST, realFile);
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(false));
    deleteQuietly(sessionFile);
    writeStringToFile(realFile, "content");
    fileHandler.touch(BASE_PATH_TEST, realFile);
    fileHandler.touch(BASE_PATH_TEST, realFile);
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(true));
    assertThat(readFileToString(sessionFile), is("content"));
    assertThat(readFileToString(realFile), is("content"));
  }

  /*
   * listFiles
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_listFiles_onFailure_1() throws Exception {
    buildCommonPathStructure();
    fileHandler.listFiles(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_listFiles_onFailure_2() throws Exception {
    buildCommonPathStructure();
    fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b/file_ab_1"));
  }

  @Test
  public void test_listFiles_1() throws Exception {
    buildCommonPathStructure();
    Collection<File> files = fileHandler.listFiles(BASE_PATH_TEST, realRootPath);
    assertThat(files.size(), is(13));
    assertThat(listFiles(sessionHandledPath, null, true).size(), is(7));
    assertThat(listFiles(realRootPath, null, true).size(), is(14));

    files = fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b"));
    assertThat(files.size(), is(3));
    assertThat(listFiles(getFile(sessionComponentPath, "a/b"), null, true).size(), is(3));
    assertThat(listFiles(getFile(realComponentPath, "a/b"), null, true).size(), is(4));

    files = fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b/doesNotExist"));
    assertThat(files.size(), is(0));

    // Non recursive
    files = fileHandler.listFiles(BASE_PATH_TEST, realRootPath, false);
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, null, false).size(), is(2));
    assertThat(listFiles(realRootPath, null, false).size(), is(2));

    files = fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b"), false);
    assertThat(files.size(), is(2));
    assertThat(listFiles(getFile(sessionComponentPath, "a/b"), null, false).size(), is(2));
    assertThat(listFiles(getFile(realComponentPath, "a/b"), null, false).size(), is(2));

    // Extension
    files = fileHandler.listFiles(BASE_PATH_TEST, realRootPath, "test");
    assertThat(files.size(), is(1));
    assertThat(listFiles(sessionHandledPath, new String[] { "test" }, true).size(), is(0));
    assertThat(listFiles(realRootPath, new String[] { "test" }, true).size(), is(1));

    files = fileHandler.listFiles(BASE_PATH_TEST, realRootPath, "test", "xml", "txt");
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, new String[] { "test", "xml", "txt" }, true).size(),
        is(1));
    assertThat(listFiles(realRootPath, new String[] { "test", "xml", "txt" }, true).size(), is(3));
  }

  @Test
  public void test_listFiles_2() throws Exception {
    buildCommonPathStructure();
    Collection<File> files =
        fileHandler.listFiles(BASE_PATH_TEST, realRootPath, TrueFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE);
    assertThat(files.size(), is(13));
    assertThat(listFiles(sessionHandledPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .size(), is(7));
    assertThat(listFiles(realRootPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size(),
        is(14));

    // Non recursive
    files =
        fileHandler.listFiles(BASE_PATH_TEST, realRootPath, TrueFileFilter.INSTANCE,
            FalseFileFilter.INSTANCE);
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE)
        .size(), is(2));
    assertThat(listFiles(realRootPath, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE).size(),
        is(2));

    // Extension
    files =
        fileHandler.listFiles(BASE_PATH_TEST, realRootPath, new SuffixFileFilter(new String[] {
            ".test", ".xml", ".txt" }), TrueFileFilter.INSTANCE);
    assertThat(files.size(), is(3));
    assertThat(
        listFiles(sessionHandledPath, new SuffixFileFilter(new String[] { "test", ".xml", "txt" }),
            TrueFileFilter.INSTANCE).size(), is(1));
    assertThat(
        listFiles(realRootPath, new SuffixFileFilter(new String[] { "test", "xml", "txt" }),
            TrueFileFilter.INSTANCE).size(), is(3));
  }

  /*
   * listFiles
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_contentEquals_onFailure_1() throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(BASE_PATH_TEST, otherFile, getFile(realRootPath, "root_file_2"));
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_contentEquals_onFailure_2() throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"), otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_contentEquals_onFailure_3() throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST, otherFile);
  }

  @Test
  public void test_contentEquals_1() throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(false));
    writeStringToFile(getFile(sessionHandledPath, "root_file_3"), "root_file_2");
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(false));
    deleteQuietly(getFile(sessionHandledPath, "root_file_2"));
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(true));
  }

  @Test
  public void test_contentEquals_2() throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST,
            getFile(realRootPath, "root_file_3")), is(false));
    writeStringToFile(getFile(sessionHandledPath, "root_file_3"), "root_file_2");
    assertThat(
        fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST,
            getFile(realRootPath, "root_file_3")), is(true));
  }

  @Test
  public void test_contentEquals_3() throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(otherFile, BASE_PATH_TEST, getFile(realRootPath, "root_file_3")),
        is(false));
    writeStringToFile(otherFile, "root_file_3_session");
    assertThat(
        fileHandler.contentEquals(otherFile, BASE_PATH_TEST, getFile(realRootPath, "root_file_3")),
        is(true));
  }

  /*
   * copyFile
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_copyFile_onFailure_1() throws Exception {
    fileHandler.copyFile(getFile(sessionHandledPath, "file"), BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_copyFile_onFailure_2() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionHandledPath, "file"), otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_copyFile_onFailure_3() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, otherFile, getFile(sessionHandledPath, "file"));
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_copyFile_onFailure_4() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, sessionComponentPath, otherFile);
  }

  @Test(expected = IOException.class)
  public void test_copyFile_onFailure_5() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, sessionHandledPath, getFile(sessionHandledPath, "file"));
  }

  @Test(expected = IOException.class)
  public void test_copyFile_onFailure_6() throws Exception {
    fileHandler.copyFile(otherFile, BASE_PATH_TEST, sessionHandledPath);
  }

  @Test(expected = IOException.class)
  public void test_copyFile_onFailure_7() throws Exception {
    fileHandler.copyFile(otherFile.getParentFile(), BASE_PATH_TEST, sessionHandledPath);
  }

  @Test
  public void test_copyFile() throws Exception {
    File test = getFile(realComponentPath, otherFile.getName());
    File expected = getFile(sessionComponentPath, otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(otherFile, BASE_PATH_TEST, test);
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "a", otherFile.getName());
    expected = getFile(sessionComponentPath, "a", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionComponentPath, otherFile.getName()), test);
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "b", otherFile.getName());
    expected = getFile(sessionComponentPath, "b", otherFile.getName());
    assertThat(expected.exists(), is(false));
    final OutputStream os = fileHandler.openOutputStream(BASE_PATH_TEST, test);
    try {
      fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionComponentPath, otherFile.getName()), os);
    } finally {
      closeQuietly(os);
    }
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "c", otherFile.getName());
    expected = getFile(sessionComponentPath, "c", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyURLToFile(
        FileUtils.toURLs(new File[] { getFile(sessionComponentPath, otherFile.getName()) })[0],
        BASE_PATH_TEST, test);
    assertThat(expected.exists(), is(true));
  }

  /*
   * delete
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_delete_onFailure_1() {
    fileHandler.delete(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_delete_onFailure_2() throws Exception {
    fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile.getParentFile());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_delete_onFailure_3() throws Exception {
    fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile);
  }

  @Test
  public void test_delete_1() throws Exception {
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));

    touch(getFile(realComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void test_delete_2() throws Exception {
    touch(getFile(sessionComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void test_delete_3() throws Exception {
    touch(getFile(realComponentPath, "file"));
    touch(getFile(sessionComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void test_delete_4() throws Exception {
    buildCommonPathStructure();

    final File session = getFile(sessionComponentPath);
    final File real = getFile(realComponentPath);

    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(5));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(3));

    fileHandler.delete(BASE_PATH_TEST, real);

    assertThat(session.exists(), is(false));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(4));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    for (final File fileMarkedToDelete : listFiles(real, null, true)) {
      assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, fileMarkedToDelete), is(true));
    }
  }

  @Test
  public void test_delete_5() throws Exception {
    buildCommonPathStructure();

    final File session = getFile(sessionComponentPath);
    final File real = getFile(realComponentPath);

    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(5));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(3));

    fileHandler.cleanDirectory(BASE_PATH_TEST, real);

    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(0));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(7));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(false));
    for (final File fileMarkedToDelete : listFiles(real, null, true)) {
      assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, fileMarkedToDelete), is(true));
    }
  }

  /*
   * waitFor
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_waitFor_onFailure_1() {
    fileHandler.waitFor(BASE_PATH_TEST, otherFile, 10);
  }

  @Test
  public void test_waitFor_1() throws Exception {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    final Thread create = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(450);
          touch(getFile(sessionComponentPath, "file"));
        } catch (final Exception e) {
        }
      }
    });
    final RunnableTest<Boolean> waitForRun = new RunnableTest<Boolean>() {
      @Override
      public void run() {
        result = false;
        result = fileHandler.waitFor(BASE_PATH_TEST, getFile(realComponentPath, "file"), 1);
      }
    };
    final Thread waitFor = new Thread(waitForRun);

    try {
      create.start();
      waitFor.start();
      Thread.sleep(200);
      assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
      assertThat(waitForRun.getResult(), is(false));
      Thread.sleep(400);
      assertThat(getFile(sessionComponentPath, "file").exists(), is(true));
      assertThat(waitForRun.getResult(), is(true));
      Thread.sleep(500);
    } finally {
      create.interrupt();
      waitFor.interrupt();
    }
    assertThat(waitForRun.getResult(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));
  }

  @Test
  public void test_waitFor_2() throws Exception {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    final Thread create = new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          Thread.sleep(1200);
          touch(getFile(sessionComponentPath, "file"));
        } catch (final Exception e) {
        }
      }
    });
    final RunnableTest<Boolean> waitForRun = new RunnableTest<Boolean>() {
      @Override
      public void run() {
        result = false;
        result = fileHandler.waitFor(BASE_PATH_TEST, getFile(realComponentPath, "file"), 1);
      }
    };
    final Thread waitFor = new Thread(waitForRun);

    try {
      create.start();
      waitFor.start();
      Thread.sleep(200);
      assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
      assertThat(waitForRun.getResult(), is(false));
      Thread.sleep(400);
      assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
      assertThat(waitForRun.getResult(), is(false));
      Thread.sleep(500);
    } finally {
      create.interrupt();
      waitFor.interrupt();
    }
    assertThat(waitForRun.getResult(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  /*
   * read
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_read_onFailure_1() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_read_onFailure_2() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, otherFile, "UTF16");
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_read_onFailure_3() throws Exception {
    fileHandler.readFileToByteArray(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_read_onFailure_4() throws Exception {
    fileHandler.readLines(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_read_onFailure_5() throws Exception {
    fileHandler.readLines(BASE_PATH_TEST, otherFile, "UTF16");
  }

  @Test(expected = FileNotFoundException.class)
  public void test_read_onFailure_6() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, getFile(sessionComponentPath, "readFile"));
  }

  @Test(expected = FileNotFoundException.class)
  public void test_read_onFailure_7() throws Exception {
    final File real = getFile(realComponentPath, "readFile");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(real);
    fileHandler.readFileToString(BASE_PATH_TEST, real);
  }

  @Test
  public void test_read_1() throws Exception {
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n");
    writeStringToFile(real, "real\nligne 2\n");
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
    assertThat(fileContent, is("session\nligne 2\n"));
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
    assertThat(fileContent, is("real\nligne 2\n"));
  }

  @Test
  public void test_read_2() throws Exception {
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", "UTF16");
    writeStringToFile(real, "real\nligne 2\n", "UTF16");
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent, is("session\nligne 2\n"));
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent, is("real\nligne 2\n"));
  }

  @Test
  public void test_read_3() throws Exception {
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n");
    writeStringToFile(real, "real\nligne 2\n");
    List<String> fileContent = fileHandler.readLines(BASE_PATH_TEST, real);
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));
    deleteQuietly(session);
    fileContent = fileHandler.readLines(BASE_PATH_TEST, real);
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("real"));
    assertThat(fileContent.get(1), is("ligne 2"));
  }

  @Test
  public void test_read_4() throws Exception {
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", "UTF16");
    writeStringToFile(real, "real\nligne 2\n", "UTF16");
    List<String> fileContent = fileHandler.readLines(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));
    deleteQuietly(session);
    fileContent = fileHandler.readLines(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("real"));
    assertThat(fileContent.get(1), is("ligne 2"));
  }

  /*
   * writeStringToFile
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_writeStringToFile_onFailure_1() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeStringToFile_onFailure_2() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeStringToFile_onFailure_3() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16");
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeStringToFile_onFailure_4() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16", false);
  }

  @Test
  public void test_writeStringToFile_1() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.writeStringToFile(BASE_PATH_TEST, real, null);

    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent");

    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent", true);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeStringToFile(BASE_PATH_TEST, real, new String("newContent".getBytes("UTF16")),
        "UTF16");

    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * write
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_write_onFailure_1() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_write_onFailure_2() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_write_onFailure_3() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16");
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_write_onFailure_4() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16", false);
  }

  @Test
  public void test_write_1() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.write(BASE_PATH_TEST, real, null);

    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent");

    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent", true);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.write(BASE_PATH_TEST, real, new String("newContent".getBytes("UTF16")), "UTF16");

    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeByteArrayToFile
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_writeByteArrayToFile_onFailure_1() throws Exception {
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeByteArrayToFile_onFailure_2() throws Exception {
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = NullPointerException.class)
  public void test_writeByteArrayToFile_1_onFailure_3() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, null);
  }

  @Test
  public void test_writeByteArrayToFile_1() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes());

    String fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes(), true);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeLines
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_1() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_2() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, true);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_3() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "UTF16");
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_4() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "UTF16", false);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_5() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_6() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, true);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_7() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16");
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_writeLines_onFailure_8() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16", false);
  }

  @Test
  public void test_writeLines_1() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.writeLines(BASE_PATH_TEST, real, null);

    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"));

    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"), true);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "line3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"),
        LINE_SEPARATOR_UNIX);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"),
        LINE_SEPARATOR_UNIX, true);

    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "line3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Encoding

    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"));

    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"), true);

    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "éline3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"),
        LINE_SEPARATOR_UNIX);

    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"),
        LINE_SEPARATOR_UNIX, true);

    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "éline3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * sizeOf
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_sizeOf_onFailure_1() {
    fileHandler.sizeOf(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_sizeOfDirectory_onFailure_2() {
    fileHandler.sizeOfDirectory(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_sizeOfDirectory_onFailure_3() throws Exception {
    buildCommonPathStructure();
    fileHandler.sizeOfDirectory(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"));
  }

  @Test
  public void test_sizeOf() throws Exception {
    buildCommonPathStructure();

    long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"));
    assertThat(size, is(19L));
    assertThat(sizeOf(getFile(realRootPath, "root_file_2")), is(11L));

    size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  @Test
  public void test_sizeOfDirectory() throws Exception {
    buildCommonPathStructure();

    final long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  /*
   * isFileNewer
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileNewer_onFailure_1() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file"));
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileNewer_onFailure_2() {
    fileHandler.isFileNewer(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileNewer_onFailure_3() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, new Date());
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileNewer_onFailure_4() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, 123);
  }

  @Test
  public void test_isFileNewer() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(10);
    touch(file2);
    boolean test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(false));
    Thread.sleep(10);
    writeStringToFile(file1, "toto");
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(true));

    Thread.sleep(10);
    final Date date = new Date();
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, date);
    assertThat(test, is(false));

    final long time = System.currentTimeMillis();
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, time);
    assertThat(test, is(false));

    Thread.sleep(10);
    writeStringToFile(file1, "titi");
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, date);
    assertThat(test, is(true));
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, time);
    assertThat(test, is(true));
  }

  /*
   * isFileOlder
   */

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileOlder_onFailure_1() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file"));
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileOlder_onFailure_2() {
    fileHandler.isFileOlder(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile);
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileOlder_onFailure_3() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, new Date());
  }

  @Test(expected = IOFileHandlerException.class)
  public void test_isFileOlder_onFailure_4() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, 123);
  }

  @Test
  public void test_isFileOlder() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(10);
    touch(file2);
    boolean test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(true));
    Thread.sleep(10);
    writeStringToFile(file1, "toto");
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(false));

    Thread.sleep(10);
    final Date date = new Date();
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, date);
    assertThat(test, is(true));

    final long time = System.currentTimeMillis();
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, time);
    assertThat(test, is(true));

    Thread.sleep(10);
    writeStringToFile(file1, "titi");
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, date);
    assertThat(test, is(false));
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, time);
    assertThat(test, is(false));
  }

  /**
   * --> SESSION
   * /sessionPath/handledPath/root_file_2
   * /sessionPath/handledPath/root_file_3
   * /sessionPath/handledPath/componentInstanceId/file_1
   * /sessionPath/handledPath/componentInstanceId/a/b/file_ab_1
   * /sessionPath/handledPath/componentInstanceId/a/b/file_ab_2.xml
   * /sessionPath/handledPath/componentInstanceId/a/b/c/file_abc_3
   * /sessionPath/handledPath/componentInstanceId/b/file_b_2
   * --> REAL
   * /root_file_1
   * /root_file_2
   * /componentInstanceId/file_1
   * /componentInstanceId/file_2
   * /componentInstanceId/a/file_a_1
   * /componentInstanceId/a/file_a_2 -
   * /componentInstanceId/a/file_a_3.xml
   * /componentInstanceId/a/b/file_ab_1 -
   * /componentInstanceId/a/b/file_ab_2.xml -
   * /componentInstanceId/a/b/c/file_abc_1 -
   * /componentInstanceId/a/b/c/file_abc_2 -
   * /componentInstanceId/b/file_b_1
   * /componentInstanceId/b/c/file_bc_1.test
   * /componentInstanceId/b/c/d/file_bcd_1 -
   * --> DELETED
   * /componentInstanceId/a/file_a_2
   * /componentInstanceId/a/b/
   * /componentInstanceId/b/c/d/
   */
  private void buildCommonPathStructure() throws Exception {
    // --> SESSION
    createSessionFile(getFile(sessionHandledPath, "root_file_2"));
    createSessionFile(getFile(sessionHandledPath, "root_file_3"));
    createSessionFile(getFile(sessionComponentPath, "file_1"));
    createSessionFile(getFile(sessionComponentPath, "a/b/file_ab_1"));
    createSessionFile(getFile(sessionComponentPath, "a/b/file_ab_2.xml"));
    createSessionFile(getFile(sessionComponentPath, "a/b/c/file_abc_3"));
    createSessionFile(getFile(sessionComponentPath, "b/file_b_2"));

    // --> REAL
    createFile(getFile(realRootPath, "root_file_1"));
    createFile(getFile(realRootPath, "root_file_2"));
    createFile(getFile(realComponentPath, "file_1"));
    createFile(getFile(realComponentPath, "file_2"));
    createFile(getFile(realComponentPath, "a/file_a_1"));
    createFile(getFile(realComponentPath, "a/file_a_2"));
    createFile(getFile(realComponentPath, "a/file_a_3.xml"));
    createFile(getFile(realComponentPath, "a/b/file_ab_1"));
    createFile(getFile(realComponentPath, "a/b/file_ab_2.xml"));
    createFile(getFile(realComponentPath, "a/b/c/file_abc_1"));
    createFile(getFile(realComponentPath, "a/b/c/file_abc_2"));
    createFile(getFile(realComponentPath, "b/file_b_1"));
    createFile(getFile(realComponentPath, "b/c/file_bc_1.test"));
    createFile(getFile(realComponentPath, "b/c/d/file_bcd_1"));

    assertSizes(124, 136);

    fileHandler.markToDelete(BASE_PATH_TEST, getFile(realComponentPath, "a", "file_a_2"));
    fileHandler.markToDelete(BASE_PATH_TEST, getFile(realComponentPath, "a/b"));
    fileHandler.markToDelete(BASE_PATH_TEST, getFile(realComponentPath, "b/c/d"));

    assertSizes(64, 136);
  }

  private void createSessionFile(final File file) throws Exception {
    writeStringToFile(file, file.getName() + "_session");
  }

  private void createFile(final File file) throws Exception {
    writeStringToFile(file, file.getName());
  }

  /**
   * Centralizes common code
   * @param sessionSize
   * @param realSize
   */
  private void assertSizes(final long sessionSize, final long realSize) {
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(sessionSize));
    assertThat(sizeOf(realRootPath), is(realSize));
  }

  /**
   * Centralizes asserts
   * @param test
   * @param expected
   */
  private void assertFileNames(final File test, final File expected) {
    assertThat(test, is(expected));
  }

  private static IOSession createSessionTest() {
    return new IOSession() {

      @Override
      public void setAttribute(final String name, final Object value) {
      }

      @Override
      public String getId() {
        return "sessionPathId";
      }

      @Override
      public Object getAttribute(final String name) {
        return null;
      }
    };
  }

  private abstract class RunnableTest<R> implements Runnable {

    protected R result;

    public R getResult() {
      return result;
    }
  }
}

/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.io.file;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Yohann Chastagnier
 */
public class TestHandledFile extends AbstractHandledFileTest {

  /*
   * getHandledFile & getParentHandledFile
   */

  @Test
  public void testGetHandledFileAndGetParentHandledFile() throws Exception {
    buildCommonPathStructure();
    final File expected = getFile(sessionComponentPath, "file_1");
    final HandledFile test = getHandledFile(realComponentPath, "file_1");
    assertFileNames(test.getFile(), expected);
    assertFileNames(test.getHandledFile().getFile(), expected);
    assertFileNames(test.getParentHandledFile().getFile(), expected.getParentFile());
    assertFileNames(test.getParentHandledFile("file_1").getFile(), expected);
  }

  /*
   * openOutputStream
   */

  @Test
  public void testOpenOutputStream() throws Exception {
    HandledFile file = getHandledFile(realComponentPath, "file");
    OutputStream test = file.openOutputStream();
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("toto"));

    // Not append
    test = file.openOutputStream();
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("toto"));

    // Append
    file = getHandledFile(realComponentPath, "file");
    test = file.openOutputStream(true);
    try {
      write("toto", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file")), is("totototo"));

    // Append
    file = getHandledFile(realComponentPath, "file2");
    writeStringToFile(getFile(realComponentPath, "file2"), "file2");
    test = file.openOutputStream(true);
    try {
      write("line 2", test);
    } finally {
      closeQuietly(test);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file2")), is("file2line 2"));
  }

  @Test
  public void testOpenInputStream() throws Exception {
    // File exists in real path only
    HandledFile file = getHandledFile(realComponentPath, "file");
    touch(getFile(realComponentPath, "file"));
    InputStream test = file.openInputStream();
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(getFile(realComponentPath, "file"));
    }

    // File exists in session path only
    file = getHandledFile(sessionComponentPath, "file");
    touch(getFile(realComponentPath, "file"));
    test = file.openInputStream();
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(getFile(realComponentPath, "file"));
    }

    // File exists in session path only
    file = getHandledFile(sessionComponentPath, "file");
    touch(getFile(sessionComponentPath, "file"));
    touch(getFile(realComponentPath, "file"));
    test = file.openInputStream();
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(getFile(sessionComponentPath, "file"));
      deleteQuietly(getFile(realComponentPath, "file"));
    }
  }

  /*
   * touch
   */

  @Test
  public void testTouch() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    final HandledFile test = getHandledFile(realFile);
    assertThat(sessionFile.exists(), is(false));
    assertThat(realFile.exists(), is(false));
    test.touch();
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(false));
    deleteQuietly(sessionFile);
    writeStringToFile(realFile, "content");
    test.touch();
    test.touch();
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(true));
    assertThat(readFileToString(sessionFile), is("content"));
    assertThat(readFileToString(realFile), is("content"));
  }

  /*
   * listFiles
   */

  @Test
  public void testListFiles() throws Exception {
    buildCommonPathStructure();
    HandledFile test = getHandledFile(realRootPath);
    Collection<HandledFile> files = test.listFiles();
    assertThat(files.size(), is(13));
    assertThat(listFiles(sessionHandledPath, null, true).size(), is(7));
    assertThat(listFiles(realRootPath, null, true).size(), is(14));

    test = getHandledFile(realComponentPath, "a/b");
    files = test.listFiles();
    assertThat(files.size(), is(3));
    assertThat(listFiles(getFile(sessionComponentPath, "a/b"), null, true).size(), is(3));
    assertThat(listFiles(getFile(realComponentPath, "a/b"), null, true).size(), is(4));

    test = getHandledFile(realComponentPath, "a/b/doesNotExist");
    files = test.listFiles();
    assertThat(files.size(), is(0));

    // Non recursive
    test = getHandledFile(realRootPath);
    files = test.listFiles(false);
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, null, false).size(), is(2));
    assertThat(listFiles(realRootPath, null, false).size(), is(2));

    test = getHandledFile(realComponentPath, "a/b");
    files = test.listFiles(false);
    assertThat(files.size(), is(2));
    assertThat(listFiles(getFile(sessionComponentPath, "a/b"), null, false).size(), is(2));
    assertThat(listFiles(getFile(realComponentPath, "a/b"), null, false).size(), is(2));

    // Extension
    test = getHandledFile(realRootPath);
    files = test.listFiles("test");
    assertThat(files.size(), is(1));
    assertThat(listFiles(sessionHandledPath, new String[] { "test" }, true).size(), is(0));
    assertThat(listFiles(realRootPath, new String[] { "test" }, true).size(), is(1));

    test = getHandledFile(realRootPath);
    files = test.listFiles("test", "xml", "txt");
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, new String[] { "test", "xml", "txt" }, true).size(),
        is(1));
    assertThat(listFiles(realRootPath, new String[] { "test", "xml", "txt" }, true).size(), is(3));
  }

  @Test
  public void testListFilesWithFilters() throws Exception {
    buildCommonPathStructure();
    final HandledFile test = getHandledFile(realRootPath);
    Collection<HandledFile> files =
        test.listFiles(TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    assertThat(files.size(), is(13));
    assertThat(listFiles(sessionHandledPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
        .size(), is(7));
    assertThat(listFiles(realRootPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).size(),
        is(14));

    // Non recursive
    files = test.listFiles(TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);
    assertThat(files.size(), is(3));
    assertThat(listFiles(sessionHandledPath, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE)
        .size(), is(2));
    assertThat(listFiles(realRootPath, TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE).size(),
        is(2));

    // Extension
    files =
        test.listFiles(new SuffixFileFilter(new String[] { ".test", ".xml", ".txt" }),
            TrueFileFilter.INSTANCE);
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

  @Test
  public void testContentEquals() throws Exception {
    buildCommonPathStructure();
    assertThat(
        getHandledFile(realRootPath, "root_file_2").contentEquals(
            getHandledFile(realRootPath, "root_file_3")), is(false));
    writeStringToFile(getFile(sessionHandledPath, "root_file_3"), "root_file_2");
    assertThat(
        getHandledFile(realRootPath, "root_file_2").contentEquals(
            getHandledFile(realRootPath, "root_file_3")), is(false));
    deleteQuietly(getFile(sessionHandledPath, "root_file_2"));
    assertThat(
        getHandledFile(realRootPath, "root_file_2").contentEquals(
            getHandledFile(realRootPath, "root_file_3")), is(true));
  }

  /*
   * copyFile
   */

  @Test
  public void testCopyFile() throws Exception {
    File test = getFile(realComponentPath, "a", otherFile.getName());
    File expected = getFile(sessionComponentPath, "a", otherFile.getName());
    writeStringToFile(getFile(realComponentPath, otherFile.getName()), "other");
    assertThat(expected.exists(), is(false));
    getHandledFile(sessionComponentPath, otherFile.getName()).copyFile(getHandledFile(test));
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "b", otherFile.getName());
    expected = getFile(sessionComponentPath, "b", otherFile.getName());
    assertThat(expected.exists(), is(false));
    final OutputStream os = getHandledFile(test).openOutputStream();
    try {
      getHandledFile(sessionComponentPath, otherFile.getName()).copyFile(os);
    } finally {
      closeQuietly(os);
    }
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "c", otherFile.getName());
    expected = getFile(sessionComponentPath, "c", otherFile.getName());
    assertThat(expected.exists(), is(false));
    getHandledFile(test).copyURLToFile(
        toURLs(new File[] { getFile(realComponentPath, otherFile.getName()) })[0]);
    assertThat(expected.exists(), is(true));
  }

  /*
   * delete
   */

  @Test
  public void testDeleteFromRealPathOnly() throws Exception {

    // Deleting not existing file
    assertThat(getHandledFile(realComponentPath, "file").delete(), is(false));
    assertThat(getHandledFile(sessionComponentPath, "file").delete(), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));

    // Creating for test "file" in real path and verify existence
    touch(getFile(realComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify the deletion of it (but file exists in real path because commit is not yet passed)
    assertThat(getHandledFile(realComponentPath, "file").delete(), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted file has no effect (results have to be identical
    // as the previous part of test)
    assertThat(getHandledFile(realComponentPath, "file").delete(), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void testDeleteFromSessionPathOnly() throws Exception {

    // Creating for test "file" in session path and verify existence
    touch(getFile(sessionComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));

    // Verify the deletion of it (the file in session path is deleted)
    assertThat(getHandledFile(sessionComponentPath, "file").delete(), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted file has no effect (results have to be identical
    // as the previous part of test)
    assertThat(getHandledFile(realComponentPath, "file").delete(), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void testDeleteFileExistingInSessionAndRealPaths() throws Exception {

    // Creating for test "file" in session and real paths and verify existence
    touch(getFile(realComponentPath, "file"));
    touch(getFile(sessionComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));

    // Verify the deletion of these (the file in session path is deleted, and file in real path
    // always exists until commit is done)
    assertThat(getHandledFile(sessionComponentPath, "file").delete(), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted files has no effect (results have to be identical
    // as the previous part of test)
    assertThat(getHandledFile(realComponentPath, "file").delete(), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
  }

  @Test
  public void testDeleteFileFromCommonTestStructure() throws Exception {
    buildCommonPathStructure();

    final File session = getFile(sessionComponentPath);
    final File real = getFile(realComponentPath);

    // Verify file existence before deletion
    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(5));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(3));

    getHandledFile(real).delete();

    // Verify file existence after deletion
    assertThat(session.exists(), is(false));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    for (final File fileMarkedToDelete : listFiles(real, null, true)) {
      assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, fileMarkedToDelete), is(true));
    }
  }

  @Test
  public void testCleanDirectoryFromCommonTestStructure() throws Exception {
    buildCommonPathStructure();

    final File session = getFile(sessionComponentPath);
    final File real = getFile(realComponentPath);

    // Verify file existence before deletion
    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(5));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(3));

    getHandledFile(real).cleanDirectory();

    // Verify file existence after cleaning
    assertThat(session.exists(), is(true));
    assertThat(real.exists(), is(true));
    assertThat(listFiles(session, null, true).size(), is(0));
    assertThat(listFiles(real, null, true).size(), is(12));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(9));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(false));
    for (final File fileMarkedToDelete : listFiles(real, null, true)) {
      assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, fileMarkedToDelete), is(true));
    }
  }

  /*
   * waitFor
   */

  @Test
  public void testWaitForWithSuccessFileCreationInTime() throws Exception {
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
        result = getHandledFile(realComponentPath, "file").waitFor(1);
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
  public void testWaitForForWithFailFileCreationInTime() throws Exception {
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
        result = getHandledFile(realComponentPath, "file").waitFor(1);
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
   * moveFile
   */

  @Test
  public void testMoveFile() throws Exception {
    buildCommonPathStructure();
    final File real = getFile(realRootPath, "root_file_1");
    final File session = getFile(sessionHandledPath, "root_file_1");
    final File realDest = getFile(realRootPath, "root_file_1_renamed");
    final File sessionDest = getFile(sessionHandledPath, "root_file_1_renamed");

    // Verify before move
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(false));
    assertThat(readFileToString(real), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(false));

    getHandledFile(real).moveFile(getHandledFile(realDest));

    // Verify after move
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    assertThat(readFileToString(real), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(true));
    assertThat(readFileToString(sessionDest), is("root_file_1"));
  }

  /*
   * read
   */

  @Test
  public void testReadFileToStringFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n");
    writeStringToFile(real, "real\nligne 2\n");

    // Content of session file has to be read
    String fileContent = getHandledFile(real).readFileToString();
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = getHandledFile(real).readFileToString();
    assertThat(fileContent, is("real\nligne 2\n"));
  }

  @Test
  public void testReadFileToStringFromFileExistingInSessionAndRealPathWithSpecificFileEncoding()
      throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", "UTF16");
    writeStringToFile(real, "real\nligne 2\n", "UTF16");

    // Content of session file has to be read
    String fileContent = getHandledFile(real).readFileToString("UTF16");
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = getHandledFile(real).readFileToString("UTF16");
    assertThat(fileContent, is("real\nligne 2\n"));
  }

  @Test
  public void testReadLinesFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n");
    writeStringToFile(real, "real\nligne 2\n");

    // Content of session file has to be read
    List<String> fileContent = getHandledFile(real).readLines();
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = getHandledFile(real).readLines();
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("real"));
    assertThat(fileContent.get(1), is("ligne 2"));
  }

  @Test
  public void testReadLinesFromFileExistingInSessionAndRealPathWithSpecificFileEncoding()
      throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", "UTF16");
    writeStringToFile(real, "real\nligne 2\n", "UTF16");

    // Content of session file has to be read
    List<String> fileContent = getHandledFile(real).readLines("UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = getHandledFile(real).readLines("UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("real"));
    assertThat(fileContent.get(1), is("ligne 2"));
  }

  /*
   * writeStringToFile
   */

  @Test
  public void testWriteStringToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    getHandledFile(real).writeStringToFile(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).writeStringToFile("modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeStringToFile("modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    getHandledFile(real).writeStringToFile(new String("newContent".getBytes("UTF16")), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * write
   */

  @Test
  public void testWrite() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    getHandledFile(real).write(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).write("modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).write("modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    getHandledFile(real).write(new String("newContent".getBytes("UTF16")), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeByteArrayToFile
   */

  @Test
  public void testWriteByteArrayToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    getHandledFile(real).writeByteArrayToFile("modifiedContent".getBytes());
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeByteArrayToFile("modifiedContent".getBytes(), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeLines
   */

  @Test
  public void testWriteLines() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    getHandledFile(real).writeLines(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).writeLines(Arrays.asList("line1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeLines(Arrays.asList("line3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "line3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and specific line ending
    getHandledFile(real).writeLines(Arrays.asList("line1", "line2"), LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific line ending
    getHandledFile(real).writeLines(Arrays.asList("line3", "line4"), LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "line3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Encoding

    // Writing new content by specifying real file and specific file encoding
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific file encoding
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "éline3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and specific file encoding and specific line
    // ending
    getHandledFile(real)
        .writeLines("Latin1", Arrays.asList("éline1", "line2"), LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific file encoding and specific line
    // ending
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline3", "line4"),
        LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "éline3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * sizeOf
   */

  @Test
  public void testSizeOf() throws Exception {
    buildCommonPathStructure();

    long size = getHandledFile(realRootPath, "root_file_2").size();
    assertThat(size, is(19L));
    assertThat(sizeOf(getFile(realRootPath, "root_file_2")), is(11L));

    size = getHandledFile(realRootPath).size();
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  /*
   * isFileNewer
   */

  @Test
  public void testIsFileNewer() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(1001);
    touch(file2);
    boolean test = getHandledFile(real1).isFileNewer(file2);
    assertThat(test, is(false));
    test = getHandledFile(real1).isFileNewer(getHandledFile(file2));
    assertThat(test, is(false));
    Thread.sleep(1001);
    writeStringToFile(file1, "toto");
    test = getHandledFile(real1).isFileNewer(file2);
    assertThat(test, is(true));
    test = getHandledFile(real1).isFileNewer(getHandledFile(file2));
    assertThat(test, is(true));

    Thread.sleep(1001);
    final Date date = new Date();
    test = getHandledFile(real1).isFileNewer(date);
    assertThat(test, is(false));

    final long time = System.currentTimeMillis();
    test = getHandledFile(real1).isFileNewer(time);
    assertThat(test, is(false));

    Thread.sleep(1001);
    writeStringToFile(file1, "titi");
    test = getHandledFile(real1).isFileNewer(date);
    assertThat(test, is(true));
    test = getHandledFile(real1).isFileNewer(time);
    assertThat(test, is(true));
  }

  /*
   * isFileOlder
   */

  @Test
  public void testIsFileOlder() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(1001);
    touch(file2);
    boolean test = getHandledFile(real1).isFileOlder(file2);
    assertThat(test, is(true));
    test = getHandledFile(real1).isFileOlder(getHandledFile(file2));
    Thread.sleep(1001);
    assertThat(test, is(true));
    writeStringToFile(file1, "toto");
    test = getHandledFile(real1).isFileOlder(file2);
    assertThat(test, is(false));
    test = getHandledFile(real1).isFileOlder(getHandledFile(file2));
    assertThat(test, is(false));

    Thread.sleep(1001);
    final Date date = new Date();
    test = getHandledFile(real1).isFileOlder(date);
    assertThat(test, is(true));

    final long time = System.currentTimeMillis();
    test = getHandledFile(real1).isFileOlder(time);
    assertThat(test, is(true));

    Thread.sleep(1001);
    writeStringToFile(file1, "titi");
    test = getHandledFile(real1).isFileOlder(date);
    assertThat(test, is(false));
    test = getHandledFile(real1).isFileOlder(time);
    assertThat(test, is(false));
  }

  private HandledFile getHandledFile(final File file, final String... names) {
    return fileHandler.getHandledFile(BASE_PATH_TEST, file, names);
  }

  private abstract class RunnableTest<R> implements Runnable {

    protected R result;

    public R getResult() {
      return result;
    }
  }
}

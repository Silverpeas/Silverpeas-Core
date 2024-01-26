/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.process.io.file;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.UnitTest;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.core.util.Charsets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_16;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.*;
import static org.awaitility.Awaitility.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
@EnableSilverTestEnv(context = JEETestContext.class)
class TestHandledFile extends AbstractHandledFileTest {

  /*
   * getHandledFile & getParentHandledFile
   */

  @Test
  void testGetHandledFileAndGetParentHandledFile() throws Exception {
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
  void testOpenOutputStream() throws Exception {
    HandledFile file = getHandledFile(realComponentPath, "file");
    try(OutputStream test = file.openOutputStream()) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8), is("toto"));

    // Not append
    try(OutputStream test = file.openOutputStream()) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8), is("toto"));

    // Append
    file = getHandledFile(realComponentPath, "file");
    try(OutputStream test = file.openOutputStream(true)) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8),
        is("totototo"));

    // Append
    file = getHandledFile(realComponentPath, "file2");
    writeStringToFile(getFile(realComponentPath, "file2"), "file2", Charsets.UTF_8);
    try(OutputStream test = file.openOutputStream(true)) {
      write("line 2", test,Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file2"), Charsets.UTF_8),
        is("file2line 2"));
  }

  @Test
  void testOpenInputStream() throws Exception {
    // File exists in real path only
    HandledFile file = getHandledFile(realComponentPath, "file");
    touch(getFile(realComponentPath, "file"));
    try (InputStream test = file.openInputStream()) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(getFile(realComponentPath, "file"));
    }

    // File exists in session path only
    file = getHandledFile(sessionComponentPath, "file");
    touch(getFile(realComponentPath, "file"));
    try (InputStream test = file.openInputStream()) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(getFile(realComponentPath, "file"));
    }

    // File exists in session path only
    file = getHandledFile(sessionComponentPath, "file");
    touch(getFile(sessionComponentPath, "file"));
    touch(getFile(realComponentPath, "file"));
    try (InputStream test = file.openInputStream()) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(getFile(sessionComponentPath, "file"));
      deleteQuietly(getFile(realComponentPath, "file"));
    }
  }

  /*
   * touch
   */

  @Test
  void testTouch() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    final HandledFile test = getHandledFile(realFile);
    assertThat(sessionFile.exists(), is(false));
    assertThat(realFile.exists(), is(false));
    test.touch();
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(false));
    deleteQuietly(sessionFile);
    writeStringToFile(realFile, "content", Charsets.UTF_8);
    test.touch();
    test.touch();
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(true));
    assertThat(readFileToString(sessionFile, Charsets.UTF_8), is("content"));
    assertThat(readFileToString(realFile, Charsets.UTF_8), is("content"));
  }

  /*
   * listFiles
   */

  @Test
  void testListFiles() throws Exception {
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
  void testListFilesWithFilters() throws Exception {
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
        test.listFiles(new SuffixFileFilter(".test", ".xml", ".txt"),
            TrueFileFilter.INSTANCE);
    assertThat(files.size(), is(3));
    assertThat(
        listFiles(sessionHandledPath, new SuffixFileFilter("test", ".xml", "txt"),
            TrueFileFilter.INSTANCE).size(), is(1));
    assertThat(
        listFiles(realRootPath, new SuffixFileFilter("test", "xml", "txt"),
            TrueFileFilter.INSTANCE).size(), is(3));
  }

  /*
   * listFiles
   */

  @Test
  void testContentEquals() throws Exception {
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
  void testCopyFile() throws Exception {
    File test = getFile(realComponentPath, "a", otherFile.getName());
    File expected = getFile(sessionComponentPath, "a", otherFile.getName());
    writeStringToFile(getFile(realComponentPath, otherFile.getName()), "other", Charsets.UTF_8);
    assertThat(expected.exists(), is(false));
    getHandledFile(sessionComponentPath, otherFile.getName()).copyFile(getHandledFile(test));
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "b", otherFile.getName());
    expected = getFile(sessionComponentPath, "b", otherFile.getName());
    assertThat(expected.exists(), is(false));
    try (final OutputStream os = getHandledFile(test).openOutputStream()) {
      getHandledFile(sessionComponentPath, otherFile.getName()).copyFile(os);
    }
    assertThat(expected.exists(), is(true));

    test = getFile(realComponentPath, "c", otherFile.getName());
    expected = getFile(sessionComponentPath, "c", otherFile.getName());
    assertThat(expected.exists(), is(false));
    getHandledFile(test).copyURLToFile(
        toURLs(getFile(realComponentPath, otherFile.getName()))[0]);
    assertThat(expected.exists(), is(true));
  }

  /*
   * delete
   */

  @Test
  void testDeleteFromRealPathOnly() throws Exception {

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
  void testDeleteFromSessionPathOnly() throws Exception {

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
  void testDeleteFileExistingInSessionAndRealPaths() throws Exception {

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
  void testDeleteFileFromCommonTestStructure() throws Exception {
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
  void testCleanDirectoryFromCommonTestStructure() throws Exception {
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
  void testWaitForWithSuccessFileCreationInTime() {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    given().pollThread(Thread::new).with().pollDelay(400, TimeUnit.MILLISECONDS).await().until(() -> {
      touch(getFile(sessionComponentPath, "file"));
      return true;
    });

    await().atMost(1, TimeUnit.SECONDS).until(() -> getHandledFile(realComponentPath, "file").waitFor(1));

    assertThat(getFile(sessionComponentPath, "file").exists(), is(true));
  }

  @Test
  void testWaitForForWithFailFileCreationInTime() throws InterruptedException {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    final ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
      boolean isCreated = getHandledFile(realComponentPath, "file").waitFor(1);
      assertThat(isCreated, is(false));
      assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
    });

    given().pollThread(Thread::new).with().pollDelay(2, TimeUnit.SECONDS).await().until(() -> {
      touch(getFile(sessionComponentPath, "file"));
      return true;
    });

    executor.awaitTermination(1, TimeUnit.SECONDS);
  }

  /*
   * moveFile
   */

  @Test
  void testMoveFile() throws Exception {
    buildCommonPathStructure();
    final File real = getFile(realRootPath, "root_file_1");
    final File session = getFile(sessionHandledPath, "root_file_1");
    final File realDest = getFile(realRootPath, "root_file_1_renamed");
    final File sessionDest = getFile(sessionHandledPath, "root_file_1_renamed");

    // Verify before move
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(false));
    assertThat(readFileToString(real, Charsets.UTF_8), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(false));

    getHandledFile(real).moveFile(getHandledFile(realDest));

    // Verify after move
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    assertThat(readFileToString(real, Charsets.UTF_8), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(true));
    assertThat(readFileToString(sessionDest, Charsets.UTF_8), is("root_file_1"));
  }

  /*
   * read
   */

  @Test
  void testReadFileToStringFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", Charsets.UTF_8);
    writeStringToFile(real, "real\nligne 2\n", Charsets.UTF_8);

    // Content of session file has to be read
    String fileContent = getHandledFile(real).readFileToString();
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = getHandledFile(real).readFileToString();
    assertThat(fileContent, is("real\nligne 2\n"));
  }

  @Test
  void testReadFileToStringFromFileExistingInSessionAndRealPathWithSpecificFileEncoding()
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
  void testReadLinesFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", Charsets.UTF_8);
    writeStringToFile(real, "real\nligne 2\n", Charsets.UTF_8);

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
  void testReadLinesFromFileExistingInSessionAndRealPathWithSpecificFileEncoding()
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
  void testWriteStringToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    getHandledFile(real).writeStringToFile(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).writeStringToFile("modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeStringToFile("modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    getHandledFile(real).writeStringToFile(new String("newContent".getBytes(UTF_16)), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes(UTF_16))));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * write
   */

  @Test
  void testWrite() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    getHandledFile(real).write(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).write("modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).write("modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    getHandledFile(real).write(new String("newContent".getBytes(UTF_16)), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes(UTF_16))));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * writeByteArrayToFile
   */

  @Test
  void testWriteByteArrayToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    getHandledFile(real).writeByteArrayToFile("modifiedContent".getBytes());
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeByteArrayToFile("modifiedContent".getBytes(), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * writeLines
   */

  @Test
  void testWriteLines() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    getHandledFile(real).writeLines(null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    getHandledFile(real).writeLines(Arrays.asList("line1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    getHandledFile(real).writeLines(Arrays.asList("line3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "line3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and specific line ending
    getHandledFile(real).writeLines(Arrays.asList("line1", "line2"), LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific line ending
    getHandledFile(real).writeLines(Arrays.asList("line3", "line4"), LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "line3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Encoding

    // Writing new content by specifying real file and specific file encoding
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific file encoding
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "éline3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and specific file encoding and specific line
    // ending
    getHandledFile(real)
        .writeLines("Latin1", Arrays.asList("éline1", "line2"), LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific file encoding and specific line
    // ending
    getHandledFile(real).writeLines("Latin1", Arrays.asList("éline3", "line4"),
        LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "éline3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * sizeOf
   */

  @Test
  void testSizeOf() throws Exception {
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
  void aHandledFileCreatedRecentlyIsYoungerThanAnotherFile() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");

    touch(file1); // to create it
    with().pollDelay(400, TimeUnit.MILLISECONDS).await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      touch(file2);
      assertThat(getHandledFile(real1).isFileNewer(file2), is(false));
      assertThat(getHandledFile(real1).isFileNewer(getHandledFile(file2)), is(false));
    });
  }

  @Test
  void aHandledFileModifiedRecentlyIsYoungerThanAnotherFile()
      throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");

    touch(file2); // to create it
    with().pollDelay(1, TimeUnit.SECONDS).await().until(() -> {
      writeStringToFile(file1, "toto", Charsets.UTF_8);
      return true;
    });
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(getHandledFile(real1).isFileNewer(file2), is(true));
      assertThat(getHandledFile(real1).isFileNewer(getHandledFile(file2)), is(true));
    });
  }

  @Test
  void AHandledFileModifiedRecentlyCannotBeYoungerThanNow() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    writeStringToFile(file1, "toto", Charsets.UTF_8);
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      final Date date = new Date();
      assertThat(getHandledFile(real1).isFileNewer(date), is(false));
    });
  }

  @Test
  void AHandledFileModifiedRecentlyCannotBeYoungerThanCurrentTimestamp() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    writeStringToFile(file1, "toto", Charsets.UTF_8);
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      final long time = System.currentTimeMillis();
      assertThat(getHandledFile(real1).isFileNewer(time), is(false));
    });
  }

  @Test
  void AHandledFileRecentlyModifiedIsYounger() {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");

    final Date date = new Date();
    final long time = System.currentTimeMillis();
    with().pollDelay(1, TimeUnit.SECONDS).await().untilAsserted(() -> {
      writeStringToFile(file1, "titi", Charsets.UTF_8);
      assertThat(getHandledFile(real1).isFileNewer(date), is(true));
      assertThat(getHandledFile(real1).isFileNewer(time), is(true));
    });
  }

  /*
   * isFileOlder
   */
  @Test
  void anyExistingFileIsOlderThanAHandledFileCreatedRecently() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");

    touch(file1); // to create it
    with().pollDelay(1, TimeUnit.SECONDS).await().until(() -> {
      System.out.println("TOUCH AT " + LocalDateTime.now());
      touch(file2);
      return true;
    });

    System.out.println("ASSERTS AT " + LocalDateTime.now());
    assertThat(getHandledFile(real1).isFileOlder(file2), is(true));
    assertThat(getHandledFile(real1).isFileOlder(getHandledFile(file2)), is(true));
  }

  @Test
  void anyExistingHandledFileIsOlderThanAFileModifiedRecently() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");

    touch(file2); // to create it
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      writeStringToFile(file1, "toto", Charsets.UTF_8);
      assertThat(getHandledFile(real1).isFileOlder(file2), is(false));
      assertThat(getHandledFile(real1).isFileOlder(getHandledFile(file2)), is(false));
    });
  }

  @Test
  void AHandledFileModifiedInThePastIsOlderThanNow() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    writeStringToFile(file1, "toto", Charsets.UTF_8);
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      final Date date = new Date();
      assertThat(getHandledFile(real1).isFileOlder(date), is(true));
    });
  }

  @Test
  void AHandledFileModifiedInThePastIsOlderThanCurrentTimestamp() throws IOException {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    writeStringToFile(file1, "toto", Charsets.UTF_8);
    await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
      final long time = System.currentTimeMillis();
      assertThat(getHandledFile(real1).isFileOlder(time), is(true));
    });
  }

  @Test
  void AHandledFileRecentlyModifiedCannotBeOlder() {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");

    final Date date = new Date();
    final long time = System.currentTimeMillis();
    with().pollDelay(1, TimeUnit.SECONDS).await().untilAsserted(() -> {
      writeStringToFile(file1, "titi", Charsets.UTF_8);
      assertThat(getHandledFile(real1).isFileOlder(date), is(false));
      assertThat(getHandledFile(real1).isFileOlder(time), is(false));
    });
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

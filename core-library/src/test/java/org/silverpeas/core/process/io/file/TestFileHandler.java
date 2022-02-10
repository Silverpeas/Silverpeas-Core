/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.process.io.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.silverpeas.core.process.io.IOAccess;
import org.silverpeas.core.process.io.file.exception.FileHandlerException;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.Charsets;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_16;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.write;
import static org.apache.commons.io.IOUtils.*;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.process.io.file.DelayedExecutor.in;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
@EnableSilverTestEnv
class TestFileHandler extends AbstractHandledFileTest {

  private static final Duration ONE_SECOND = Duration.of(1, ChronoUnit.SECONDS);
  private static final Duration TWO_SECONDS = Duration.of(2, ChronoUnit.SECONDS);

  /*
   * getAttachment
   */

  @Test
  void testGetHandledFileWhenTechnicalNullIsPassedAsFileParameter() {
    assertThrows(NullPointerException.class,
        () -> fileHandler.getHandledFile(BASE_PATH_TEST, (File) null));
  }

  @Test
  void testGetHandledFileWhenNotHandledFileIsPassedAsFileParameter() {
    assertThrows(FileHandlerException.class,
        () -> fileHandler.getHandledFile(BASE_PATH_TEST, otherFile));
  }

  private <T extends Throwable> void assertThrows(final Class<T> exceptionClass, final Executable executable) {
    org.junit.jupiter.api.Assertions.assertThrows(exceptionClass, executable);
  }

  @Test
  void testGetHandledFileFromSessionComponentPathWhenExistingInSession() {
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, sessionComponentPath);
    final File expected = sessionComponentPath;
    assertFileNames(test.getFile(), expected);
  }

  @Test
  void testGetHandledFileFromRealFileWhenExistingInSessionPath() {
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, realComponentPath, "file");
    final File expected = getFile(sessionComponentPath, "file");
    assertFileNames(test.getFile(), expected);
  }

  @Test
  void testGetHandledFileFromRealFileWhenExistingInRealPathOnly() throws Exception {
    touch(getFile(realComponentPath, "file"));
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, realComponentPath, "file");
    final File expected = getFile(realComponentPath, "file");
    assertFileNames(test.getFile(), expected);
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_ONLY));
  }

  @Test
  void testGetFileFromNotHandledFile() {
    final File otherTest = fileHandler.getFile(otherFile);
    final File expected = otherFile;
    assertFileNames(otherTest, expected);
  }

  @Test
  void testGetFileFromNotHandledSubdirectoryAndFile() {
    final File otherTest = fileHandler.getFile(otherFile, "file");
    final File expected = getFile(otherFile, "file");
    assertFileNames(otherTest, expected);
  }

  @Test
  void testGetHandledTemporaryFile() {
    final File test = fileHandler.getSessionTemporaryFile("tmpFile");
    final File expected = FileUtils.getFile(sessionRootPath, currentSession.getId(), "@#@work@#@", "tmpFile");
    assertFileNames(test, expected);
    assertThat(expected.exists(), is(false));
  }

  /*
   * openOutputStream
   */

  @Test
  void testOpenOutputStreamWhenNotHandledFileIsPassedAsFileParameter() {
    assertThrows(FileHandlerException.class, () -> {
      touch(otherFile);
      closeQuietly(fileHandler.openOutputStream(BASE_PATH_TEST, otherFile));
    });
  }

  @Test
  void testOpenOutputStreamWhenFileDoesntExistInSessionOrRealPath() {
    assertThrows(FileNotFoundException.class, () -> {
      final File file = getFile(realComponentPath, "file");
      try (final OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file)) {
        write("toto", test, Charsets.UTF_8);
      }
      assertThat(readFileToString(file, Charsets.UTF_8), is("toto"));
    });
  }

  @Test
  void testOpenOutputStream() throws Exception {
    File file = getFile(realComponentPath, "file");
    try (OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file)) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8), is("toto"));

    // Not append
    try (OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file)) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8), is("toto"));

    // Append
    file = getFile(realComponentPath, "file");
    try (OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file, true)) {
      write("toto", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file"), Charsets.UTF_8),
        is("totototo"));

    // Append
    file = getFile(realComponentPath, "file2");
    writeStringToFile(file, "file2", Charsets.UTF_8);
    try (OutputStream test = fileHandler.openOutputStream(BASE_PATH_TEST, file, true)) {
      write("line 2", test, Charsets.UTF_8);
    }
    assertThat(readFileToString(getFile(sessionComponentPath, "file2"), Charsets.UTF_8),
        is("file2line 2"));
  }

  /*
   * openInputStream
   */

  @Test
  void testOpenInputStreamWhenNotExistingNotHandledFileIsPassedAsFileParameter() {
    assertThrows(FileHandlerException.class, () ->
        closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile)));
  }

  @Test
  void testOpenInputStreamWhenExistingNotHandledFileIsPassedAsFileParameter() {
    assertThrows(FileHandlerException.class, () -> {
      touch(otherFile);
      closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile));
    });
  }

  @Test
  void testOpenInputStreamWhenFileDoesntExistInSessionPath() {
    assertThrows(FileNotFoundException.class, () ->
        closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(sessionComponentPath, "file"))));
  }

  @Test
  void testOpenInputStreamWhenFileDoesntExistInRealPath() {
    assertThrows(FileNotFoundException.class, () ->
        closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(realComponentPath, "file"))));
  }

  @Test
  void testOpenInputStreamWhenFileExistsInRealPathOnly() throws Exception {
    final File file = getFile(realComponentPath, "file");
    touch(file);
    try (final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file)) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(file);
    }
  }

  @Test
  void testOpenInputStreamWhenFileExistsInSessionPathOnly() throws Exception {
    final File file = getFile(sessionComponentPath, "file");
    touch(file);
    try (final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file)) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(file);
    }
  }

  @Test
  void testOpenInputStreamWhenFileExistsInSessionAndRealPath() throws Exception {
    final File file = getFile(sessionComponentPath, "file");
    final File file2 = getFile(realComponentPath, "file");
    touch(file);
    touch(file2);
    try (final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file)) {
      assertThat(test, notNullValue());
    } finally {
      deleteQuietly(file);
      deleteQuietly(file2);
    }
  }

  /*
   * touch
   */

  @Test
  void testTouchWhenExistingNotHandledFileIsPassedAsFileParameter() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.touch(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testTouchWhenNoBasePathIsPassed() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.touch(null, otherFile));
  }

  @Test
  void testTouchWhenFileDoesntExistAtAll() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    assertThat(sessionFile.exists(), is(false));
    assertThat(realFile.exists(), is(false));
    fileHandler.touch(BASE_PATH_TEST, realFile);
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(false));
  }

  @Test
  void testTouchWhenFileExistsInRealPath() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    writeStringToFile(realFile, "content", Charsets.UTF_8);
    fileHandler.touch(BASE_PATH_TEST, realFile);
    fileHandler.touch(BASE_PATH_TEST, realFile);
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(true));
    assertThat(readFileToString(sessionFile, Charsets.UTF_8), is("content"));
    assertThat(readFileToString(realFile, Charsets.UTF_8), is("content"));
  }

  /*
   * listFiles
   */

  @Test
  void testListFilesWhenExistingNotHandledFileIsPassedAsFileParameter() throws Exception {
    buildCommonPathStructure();
    assertThrows(FileHandlerException.class, () ->
        fileHandler.listFiles(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testListFilesWithNotDirectoryParameter() throws Exception {
    buildCommonPathStructure();
    assertThrows(IllegalArgumentException.class, () ->
        fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b/file_ab_1")));
  }

  @Test
  void testListFilesFromSessionAndRealPath() throws Exception {
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
  void testListFilesFromSessionAndRealPathWithFilters() throws Exception {
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
        fileHandler.listFiles(BASE_PATH_TEST, realRootPath, new SuffixFileFilter(".test", ".xml",
            ".txt"), TrueFileFilter.INSTANCE);
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
  void testContentEqualsWhenFirstFileToCompareIsNotHandled() throws Exception {
    buildCommonPathStructure();
    assertThrows(FileHandlerException.class, () ->
        fileHandler.contentEquals(BASE_PATH_TEST, otherFile, getFile(realRootPath, "root_file_2")));
  }

  @Test
  void testContentEqualsWhenSecondFileToCompareIsNotHandled() throws Exception {
    buildCommonPathStructure();
    assertThrows(FileHandlerException.class, () ->
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"), otherFile));
  }

  @Test
  void testContentEqualsWhenSecondFileToCompareIsNotHandledEvenIfHandledBasePathIsSpecified()
      throws Exception {
    buildCommonPathStructure();
    assertThrows(FileHandlerException.class, () ->
        fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST, otherFile));
  }

  @Test
  void testContentEqualsFromUniqueHandledPath() throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(false));
    writeStringToFile(getFile(sessionHandledPath, "root_file_3"), "root_file_2", Charsets.UTF_8);
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(false));
    deleteQuietly(getFile(sessionHandledPath, "root_file_2"));
    assertThat(
        fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"),
            getFile(realRootPath, "root_file_3")), is(true));
  }

  @Test
  void testContentEqualsBetweenHandledFilesButOnlyTheSecondFileKnownAsHandled()
      throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST,
            getFile(realRootPath, "root_file_3")), is(false));
    writeStringToFile(getFile(sessionHandledPath, "root_file_3"), "root_file_2", Charsets.UTF_8);
    assertThat(
        fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST,
            getFile(realRootPath, "root_file_3")), is(true));
  }

  @Test
  void testContentEqualsBetweenNotHandledFileAndHandledFile() throws Exception {
    buildCommonPathStructure();
    assertThat(
        fileHandler.contentEquals(otherFile, BASE_PATH_TEST, getFile(realRootPath, "root_file_3")),
        is(false));
    writeStringToFile(otherFile, "root_file_3_session", Charsets.UTF_8);
    assertThat(
        fileHandler.contentEquals(otherFile, BASE_PATH_TEST, getFile(realRootPath, "root_file_3")),
        is(true));
  }

  /*
   * copyFile
   */

  @Test
  void testCopyFileWithNotHandledFilesWhereasTheyHaveToBe() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionHandledPath, "file"), otherFile));
  }

  @Test
  void testCopyFileFromNotHandledFileWhereasItHasToBe() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.copyFile(BASE_PATH_TEST, otherFile, getFile(sessionHandledPath, "file")));
  }

  @Test
  void testCopyFileToNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.copyFile(BASE_PATH_TEST, sessionComponentPath, otherFile));
  }

  @Test
  void testCopyFileWhereasTheFileToCopyIsDirectory() {
    assertThrows(IOException.class, () ->
        fileHandler.copyFile(BASE_PATH_TEST, sessionHandledPath, getFile(sessionHandledPath, "file")));
  }

  @Test
  void testCopyFileWhereasTheDestinationFileIsDirectory() {
    assertThrows(IOException.class, () ->
        fileHandler.copyFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, sessionHandledPath)));
  }

  @Test
  void testCopyFileFromNotHandledFileWhereasTheDestinationFileIsDirectory() {
    assertThrows(IOException.class, () ->
        fileHandler.copyFile(otherFile.getParentFile(),
            fileHandler.getHandledFile(BASE_PATH_TEST, sessionHandledPath)));
  }

  @Test
  void testCopyFile() throws Exception {

    // Copy from existing handled file to handled file and verify existence in session
    File test = getFile(realComponentPath, otherFile.getName());
    File expected = getFile(sessionComponentPath, otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, test));
    assertThat(expected.exists(), is(true));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));

    // Copy from existing handled file (in subdirectory) to handled file and
    // verify existence in session
    test = getFile(realComponentPath, "a", otherFile.getName());
    expected = getFile(sessionComponentPath, "a", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionComponentPath, otherFile.getName()), test);
    assertThat(expected.exists(), is(true));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));

    // Copy handled file to output stream open in session and verify existence in session
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
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));

    // Copy file from URL to handled file and verify existence in session
    test = getFile(realComponentPath, "c", otherFile.getName());
    expected = getFile(sessionComponentPath, "c", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyURLToFile(
        toURLs(getFile(sessionComponentPath, otherFile.getName()))[0],
        BASE_PATH_TEST, test);
    assertThat(expected.exists(), is(true));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));
  }

  /*
   * delete
   */

  @Test
  void testDeleteOnNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.delete(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testCleanDirectoryOnNotHandledPath() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile.getParentFile()));
  }

  @Test
  void testCleanDirectoryOnNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testDeleteFromRealPathOnly() throws Exception {

    // Deleting not existing file
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(false));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));

    // Creating for test "file" in real path and verify existence
    touch(getFile(realComponentPath, "file"));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify the deletion of it (but file exists in real path because commit is not yet passed)
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted file has no effect (results have to be identical
    // as the previous part of test)
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
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
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(getFile(realComponentPath, "file").exists(), is(false));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted file has no effect (results have to be identical
    // as the previous part of test)
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
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
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(sessionComponentPath, "file")), is(true));
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(getFile(realComponentPath, "file").exists(), is(true));
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    // Verify that a new deletion on same deleted files has no effect (results have to be identical
    // as the previous part of test)
    assertThat(fileHandler.delete(BASE_PATH_TEST, getFile(realComponentPath, "file")), is(false));
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

    fileHandler.delete(BASE_PATH_TEST, real);

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

    fileHandler.cleanDirectory(BASE_PATH_TEST, real);

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
  void testWaitForOnNotHandledFile() {
    assertThrows(FileHandlerException.class,
        () -> fileHandler.waitFor(BASE_PATH_TEST, otherFile, 10));
  }

  @Test
  void testWaitForWithSuccessFileCreationInTime() {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    final Thread create = new Thread(() -> {
      try {
        in(450, TimeUnit.MILLISECONDS).execute(() -> touch(getFile(sessionComponentPath, "file")));
      } catch (final Exception e) {
        // ignore
      }
    });
    final RunnableTest<Boolean> waitForRun = new RunnableTest<>() {
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
      with().pollDelay(200, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
        assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
        assertThat(waitForRun.getResult(), is(false));
      });

      with().pollDelay(400, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
        assertThat(getFile(sessionComponentPath, "file").exists(), is(true));
        assertThat(waitForRun.getResult(), is(true));
      });
    } finally {
      create.interrupt();
      waitFor.interrupt();
    }

    with().pollDelay(500, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
      assertThat(waitForRun.getResult(), is(true));
      assertThat(getFile(sessionComponentPath, "file").exists(), is(true));
    });
  }

  @Test
  void testWaitForForWithFailFileCreationInTime() {
    assertThat(getFile(sessionComponentPath, "file").exists(), is(false));

    final Thread create = new Thread(() -> {
      try {
        in(1200, TimeUnit.MILLISECONDS).execute(
            () -> touch(getFile(sessionComponentPath, "file")));
      } catch (final Exception e) {
        // ignore
      }
    });
    final RunnableTest<Boolean> waitForRun = new RunnableTest<>() {
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

      with().pollDelay(200, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
        assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
        assertThat(waitForRun.getResult(), is(false));
      });

      with().pollDelay(400, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
        assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
        assertThat(waitForRun.getResult(), is(false));
      });

    } finally {
      create.interrupt();
      waitFor.interrupt();
    }

    with().pollDelay(500, TimeUnit.MILLISECONDS).await().atMost(ONE_SECOND).untilAsserted(() -> {
      assertThat(waitForRun.getResult(), is(false));
      assertThat(getFile(sessionComponentPath, "file").exists(), is(false));
    });
  }

  /*
   * moveFile
   */

  @Test
  void testMoveFileFromNotHandledFile() {
    assertThrows(FileHandlerException.class, () -> 
        fileHandler.moveFile(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file")));
  }

  @Test
  void testMoveFileToNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
        fileHandler.moveFile(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile));
  }

  @Test
  void testMoveFileFromNotHandledFileInMultiHandledBasePathMethod() {
    assertThrows(FileHandlerException.class, () -> 
      fileHandler.moveFile(BASE_PATH_TEST, otherFile, BASE_PATH_TEST,
          getFile(sessionComponentPath, "file")));
  }

  @Test
  void testMoveFileToNotHandledFileInMultiHandledBasePathMethod() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.moveFile(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), BASE_PATH_TEST,
          otherFile));
  }

  @Test
  void testMoveFileWithNullFileParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.moveFile(null, fileHandler.getHandledFile(BASE_PATH_TEST, otherFile)));
  }

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

    fileHandler.moveFile(BASE_PATH_TEST, real, realDest);

    // Verify after move
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    assertThat(readFileToString(real, Charsets.UTF_8), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(true));
    assertThat(readFileToString(sessionDest, Charsets.UTF_8), is("root_file_1"));

    // Verify the moving of not handled file to handled file
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));
    assertThat(real.exists(), is(true));
    assertThat(session.exists(), is(false));
    fileHandler.moveFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, real));
    assertThat(real.exists(), is(true));
    assertThat(session.exists(), is(true));
  }

  /*
   * read
   */

  @Test
  void testReadFileToStringWithNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.readFileToString(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testReadFileToStringWithNotHandlefFileAndSpecificFileEncoding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.readFileToString(BASE_PATH_TEST, otherFile, "UTF16"));
  }

  @Test
  void testReadFileToByteArrayWithNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.readFileToByteArray(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testReadLinesWithNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.readLines(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testReadLinesWithNotHandlefFileAndSpecificFileEncoding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.readLines(BASE_PATH_TEST, otherFile, "UTF16"));
  }

  @Test
  void testReadFileToStringWithNotExistingFile() {
    assertThrows(FileNotFoundException.class, () ->
      fileHandler.readFileToString(BASE_PATH_TEST, getFile(sessionComponentPath, "readFile")));
  }

  @Test
  void testReadFileWhithExistingFileMarkedAsDeleted() {
    assertThrows(FileNotFoundException.class, () -> {
      final File real = getFile(realComponentPath, "readFile");
      fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(real);
      fileHandler.readFileToString(BASE_PATH_TEST, real);
    });
  }

  @Test
  void testReadFileToStringFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n", Charsets.UTF_8);
    writeStringToFile(real, "real\nligne 2\n", Charsets.UTF_8);

    // Content of session file has to be read
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
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
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
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
    List<String> fileContent = fileHandler.readLines(BASE_PATH_TEST, real);
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readLines(BASE_PATH_TEST, real);
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
    List<String> fileContent = fileHandler.readLines(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("session"));
    assertThat(fileContent.get(1), is("ligne 2"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readLines(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent.size(), is(2));
    assertThat(fileContent.get(0), is("real"));
    assertThat(fileContent.get(1), is("ligne 2"));
  }

  /*
   * writeStringToFile
   */

  @Test
  void testWriteStringToFileThatIsNotHandled() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null));
  }

  @Test
  void testWriteStringToFileThatIsNotHandledWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, true));
  }

  @Test
  void testWriteStringToFileThatIsNotHandledWithSpecificFileEncoding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16"));
  }

  @Test
  void testWriteStringToFileThatIsNotHandledWithSpecificFileEncodingAndExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16", false));
  }

  @Test
  void testWriteStringToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    assertThat(readFileToString(session, Charsets.UTF_8), is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent");
    // Session file contains this new content and real file is not modified
    assertThat(readFileToString(session, Charsets.UTF_8), is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    assertThat(readFileToString(session, Charsets.UTF_8), is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, new String("newContent".getBytes(UTF_16)),
        "UTF16");
    // Session file contains this new content and real file is not modified
    assertThat(readFileToString(session, "UTF16"), is(new String("newContent".getBytes(UTF_16))));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * write
   */

  @Test
  void testWriteFileThatIsNotHandled() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.write(BASE_PATH_TEST, otherFile, null));
  }

  @Test
  void testWriteFileThatIsNotHandledWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.write(BASE_PATH_TEST, otherFile, null, true));
  }

  @Test
  void testWriteFileThatIsNotHandledWithSpecificFileEncoding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16"));
  }

  @Test
  void testWriteFileThatIsNotHandledWithSpecificFileEncodingAndExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16", false));
  }

  @Test
  void testWrite() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    fileHandler.write(BASE_PATH_TEST, real, new String("newContent".getBytes(UTF_16)), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes(UTF_16))));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * writeByteArrayToFile
   */

  @Test
  void testWriteByteArrayToFileThatIsNotHandled() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null));
  }

  @Test
  void testWriteByteArrayToFileThatIsNotHandledWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null, true));
  }

  @Test
  void testWriteByteArrayToFileWithNullData() {
    assertThrows(NullPointerException.class, () -> {
      final File session = getFile(sessionComponentPath, "writeFile");
      final File real = getFile(realComponentPath, "writeFile");
      writeStringToFile(session, "notModified", Charsets.UTF_8);
      writeStringToFile(real, "notModified", Charsets.UTF_8);

      fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, null);
    });
  }

  @Test
  void testWriteByteArrayToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes());
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes(), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));
  }

  /*
   * writeLines
   */

  @Test
  void testWriteLinesToFileThatIsNotHandled() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, true));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecifiedLineEnding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "lineEnding"));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecifiedLineEndingAndWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "lineEnding", false));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAnd() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, true));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithSpecifiedLineEnding() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16"));
  }

  @Test
  void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithSpecifiedLineEndingAndWithExplicitAppendParameter() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16", false));
  }

  @Test
  void testWriteLines() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified", Charsets.UTF_8);
    writeStringToFile(real, "notModified", Charsets.UTF_8);

    // Writing empty content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "line3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and specific line ending
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"),
        LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific line ending
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"),
        LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, Charsets.UTF_8);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "line3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Encoding

    // Writing new content by specifying real file and specific file encoding
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific file encoding
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "éline3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Writing new content by specifying real file and specific file encoding and specific line
    // ending
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"),
        LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real, Charsets.UTF_8), is("notModified"));

    // Appending new content by specifying real file and specific file encoding and specific line
    // ending
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"),
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
  void testSizeOfNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.sizeOf(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testSizeOfDirectoryOnNotHandledFile() {
    assertThrows(IllegalArgumentException.class, () ->
      fileHandler.sizeOfDirectory(BASE_PATH_TEST, otherFile));
  }

  @Test
  void testSizeOfDirectoryOnFile() throws Exception {
    buildCommonPathStructure();
    assertThrows(IllegalArgumentException.class, () ->
      fileHandler.sizeOfDirectory(BASE_PATH_TEST, getFile(realRootPath, "root_file_2")));
  }

  @Test
  void testSizeOf() throws Exception {
    buildCommonPathStructure();

    long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"));
    assertThat(size, is(19L));
    assertThat(sizeOf(getFile(realRootPath, "root_file_2")), is(11L));

    size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  @Test
  void testSizeOfDirectory() throws Exception {
    buildCommonPathStructure();

    final long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  /*
   * isFileNewer
   */

  @Test
  void testIsFileNewerBetweenNotHandledFileAndHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file")));
  }

  @Test
  void testIsFileNewerBetweenHandledFileAndNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileNewer(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile));
  }

  @Test
  void testIsFileNewerWithNotHandledFileFromDate() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, new Date()));
  }

  @Test
  void testIsFileNewerWithNotHandledFileFromDateInMilliseconds() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, 123));
  }

  @Test
  void testIsFileNewer() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);

    in(ONE_SECOND).execute(() -> touch(file2));
    assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2), is(false));

    in(ONE_SECOND).execute(() -> writeStringToFile(file1, "toto", Charsets.UTF_8));
    assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2), is(true));
    
    await().atMost(TWO_SECONDS).untilAsserted(() -> {
      final Date date = new Date();
      assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, date), is(false));

      final long time = System.currentTimeMillis();
      assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, time), is(false));

      in(ONE_SECOND).execute(() -> writeStringToFile(file1, "titi", Charsets.UTF_8));
      assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, date), is(true));
      assertThat(fileHandler.isFileNewer(BASE_PATH_TEST, real1, time), is(true));
    });

  }

  /*
   * isFileOlder
   */

  @Test
  void testIsFileOlderBetweenNotHandledFileAndHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file")));
  }

  @Test
  void testIsFileOlderBetweenHandledFileAndNotHandledFile() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileOlder(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile));
  }

  @Test
  void testIsFileOlderWithNotHandledFileFromDate() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, new Date()));
  }

  @Test
  void testIsFileOlderWithNotHandledFileFromDateInMilliseconds() {
    assertThrows(FileHandlerException.class, () ->
      fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, 123));
  }

  @Test
  void testIsFileOlder() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);

    in(ONE_SECOND).execute(() -> touch(file2));
    assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2), is(true));

    in(ONE_SECOND).execute(() -> writeStringToFile(file1, "toto", Charsets.UTF_8));
    assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2), is(false));

    await().atMost(TWO_SECONDS).untilAsserted(() -> {
      final Date date = new Date();
      assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, date), is(true));

      final long time = System.currentTimeMillis();
      assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, time), is(true));

      in(ONE_SECOND).execute(() -> writeStringToFile(file1, "titi", Charsets.UTF_8));
      assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, date), is(false));
      assertThat(fileHandler.isFileOlder(BASE_PATH_TEST, real1, time), is(false));
    });
  }

  @Test
  void testGetSessionHandledRootPathNames() throws Exception {
    buildCommonPathStructure();
    final Collection<String> test = new TreeSet<>(fileHandler.getSessionHandledRootPathNames());
    assertThat(test.size(), is(1));
    assertThat(test, contains("componentInstanceId"));
  }

  @Test
  void testListAllSessionHandledRootPathFiles() throws Exception {
    buildCommonPathStructure();
    final Collection<File> test = new TreeSet<>(fileHandler.listAllSessionHandledRootPathFiles());
    assertThat(test.size(), is(1));
    assertThat(test.iterator().next().getName(), is("componentInstanceId"));
  }

  private abstract static class RunnableTest<R> implements Runnable {

    protected R result;

    public R getResult() {
      return result;
    }
  }

  private void closeQuietly(final Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (final IOException ioe) {
      // ignore
    }
  }

}

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;
import org.silverpeas.core.process.io.file.exception.FileHandlerException;

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
import java.util.TreeSet;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.*;
import static org.apache.commons.io.IOUtils.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * @author Yohann Chastagnier
 */
public class TestFileHandler extends AbstractHandledFileTest {

  /*
   * getAttachment
   */

  @Test(expected = NullPointerException.class)
  public void testGetHandledFileWhenTechnicalNullIsPassedAsFileParameter() {
    fileHandler.getHandledFile(BASE_PATH_TEST, (File) null);
  }

  @Test(expected = FileHandlerException.class)
  public void testGetHandledFileWhenNotHandledFileIsPassedAsFileParameter() {
    fileHandler.getHandledFile(BASE_PATH_TEST, otherFile);
  }

  @Test
  public void testGetHandledFileFromSessionComponentPathWhenExistingInSession() throws Exception {
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, sessionComponentPath);
    final File expected = sessionComponentPath;
    assertFileNames(test.getFile(), expected);
  }

  @Test
  public void testGetHandledFileFromRealFileWhenExistingInSessionPath() throws Exception {
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, realComponentPath, "file");
    final File expected = getFile(sessionComponentPath, "file");
    assertFileNames(test.getFile(), expected);
  }

  @Test
  public void testGetHandledFileFromRealFileWhenExistingInRealPathOnly() throws Exception {
    touch(getFile(realComponentPath, "file"));
    final HandledFile test = fileHandler.getHandledFile(BASE_PATH_TEST, realComponentPath, "file");
    final File expected = getFile(realComponentPath, "file");
    assertFileNames(test.getFile(), expected);
  }

  @Test
  public void testGetFileFromNotHandledFile() throws Exception {
    final File otherTest = fileHandler.getFile(otherFile);
    final File expected = otherFile;
    assertFileNames(otherTest, expected);
  }

  @Test
  public void testGetFileFromNotHandledSubdirectoryAndFile() throws Exception {
    final File otherTest = fileHandler.getFile(otherFile, "file");
    final File expected = getFile(otherFile, "file");
    assertFileNames(otherTest, expected);
  }

  @Test
  public void testGetHandledTemporaryFile() throws Exception {
    final File test = fileHandler.getSessionTemporaryFile("tmpFile");
    final File expected = FileUtils.getFile(sessionRootPath, currentSession.getId(), "@#@work@#@", "tmpFile");
    assertFileNames(test, expected);
    assertThat(expected.exists(), is(false));
  }

  /*
   * openOutputStream
   */

  @Test(expected = FileHandlerException.class)
  public void testOpenOutputStreamWhenNotHandledFileIsPassedAsFileParameter() throws Exception {
    touch(otherFile);
    closeQuietly(fileHandler.openOutputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = FileNotFoundException.class)
  public void testOpenOutputStreamWhenFileDoesntExistInSessionOrRealPath() throws Exception {
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
  public void testOpenOutputStream() throws Exception {
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

  @Test(expected = FileHandlerException.class)
  public void testOpenInputStreamWhenNotExistingNotHandledFileIsPassedAsFileParameter()
      throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = FileHandlerException.class)
  public void testOpenInputStreamWhenExistingNotHandledFileIsPassedAsFileParameter()
      throws Exception {
    touch(otherFile);
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, otherFile));
  }

  @Test(expected = FileNotFoundException.class)
  public void testOpenInputStreamWhenFileDoesntExistInSessionPath() throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(sessionComponentPath, "file")));
  }

  @Test(expected = FileNotFoundException.class)
  public void testOpenInputStreamWhenFileDoesntExistInRealPath() throws Exception {
    closeQuietly(fileHandler.openInputStream(BASE_PATH_TEST, getFile(realComponentPath, "file")));
  }

  @Test
  public void testOpenInputStreamWhenFileExistsInRealPathOnly() throws Exception {
    final File file = getFile(realComponentPath, "file");
    touch(file);
    final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file);
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(file);
    }
  }

  @Test
  public void testOpenInputStreamWhenFileExistsInSessionPathOnly() throws Exception {
    final File file = getFile(sessionComponentPath, "file");
    touch(file);
    final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file);
    try {
      assertThat(test, notNullValue());
    } finally {
      closeQuietly(test);
      deleteQuietly(file);
    }
  }

  @Test
  public void testOpenInputStreamWhenFileExistsInSessionAndRealPath() throws Exception {
    final File file = getFile(sessionComponentPath, "file");
    final File file2 = getFile(realComponentPath, "file");
    touch(file);
    touch(file2);
    final InputStream test = fileHandler.openInputStream(BASE_PATH_TEST, file);
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

  @Test(expected = FileHandlerException.class)
  public void testTouchWhenExistingNotHandledFileIsPassedAsFileParameter() throws Exception {
    fileHandler.touch(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testTouchWhenNoBasePathIsPassed() throws Exception {
    fileHandler.touch(null, otherFile);
  }

  @Test
  public void testTouchWhenFileDoesntExistAtAll() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
    assertThat(sessionFile.exists(), is(false));
    assertThat(realFile.exists(), is(false));
    fileHandler.touch(BASE_PATH_TEST, realFile);
    assertThat(sessionFile.exists(), is(true));
    assertThat(realFile.exists(), is(false));
  }

  @Test
  public void testTouchWhenFileExistsInRealPath() throws Exception {
    final File sessionFile = getFile(sessionComponentPath, "file");
    final File realFile = getFile(realComponentPath, "file");
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

  @Test(expected = FileHandlerException.class)
  public void testListFilesWhenExistingNotHandledFileIsPassedAsFileParameter() throws Exception {
    buildCommonPathStructure();
    fileHandler.listFiles(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testListFilesWithNotDirectoryParameter() throws Exception {
    buildCommonPathStructure();
    fileHandler.listFiles(BASE_PATH_TEST, getFile(realComponentPath, "a/b/file_ab_1"));
  }

  @Test
  public void testListFilesFromSessionAndRealPath() throws Exception {
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
  public void testListFilesFromSessionAndRealPathWithFilters() throws Exception {
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

  @Test(expected = FileHandlerException.class)
  public void testContentEqualsWhenFirstFileToCompareIsNotHandled() throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(BASE_PATH_TEST, otherFile, getFile(realRootPath, "root_file_2"));
  }

  @Test(expected = FileHandlerException.class)
  public void testContentEqualsWhenSecondFileToCompareIsNotHandled() throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"), otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testContentEqualsWhenSecondFileToCompareIsNotHandledEvenIfHandledBasePathIsSpecified()
      throws Exception {
    buildCommonPathStructure();
    fileHandler.contentEquals(getFile(realRootPath, "root_file_2"), BASE_PATH_TEST, otherFile);
  }

  @Test
  public void testContentEqualsFromUniqueHandledPath() throws Exception {
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
  public void testContentEqualsBetweenHandledFilesButOnlyTheSecondFileKnownAsHandled()
      throws Exception {
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
  public void testContentEqualsBetweenNotHandledFileAndHandledFile() throws Exception {
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

  @Test(expected = FileHandlerException.class)
  public void testCopyFileWithNotHandledFilesWhereasTheyHaveToBe() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionHandledPath, "file"), otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testCopyFileFromNotHandledFileWhereasItHasToBe() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, otherFile, getFile(sessionHandledPath, "file"));
  }

  @Test(expected = FileHandlerException.class)
  public void testCopyFileToNotHandledFile() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, sessionComponentPath, otherFile);
  }

  @Test(expected = IOException.class)
  public void testCopyFileWhereasTheFileToCopyIsDirectory() throws Exception {
    fileHandler.copyFile(BASE_PATH_TEST, sessionHandledPath, getFile(sessionHandledPath, "file"));
  }

  @Test(expected = IOException.class)
  public void testCopyFileWhereasTheDestinationFileIsDirectory() throws Exception {
    fileHandler.copyFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, sessionHandledPath));
  }

  @Test(expected = IOException.class)
  public void testCopyFileFromNotHandledFileWhereasTheDestinationFileIsDirectory() throws Exception {
    fileHandler.copyFile(otherFile.getParentFile(),
        fileHandler.getHandledFile(BASE_PATH_TEST, sessionHandledPath));
  }

  @Test
  public void testCopyFile() throws Exception {

    // Copy from existing handled file to handled file and verify existence in session
    File test = getFile(realComponentPath, otherFile.getName());
    File expected = getFile(sessionComponentPath, otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, test));
    assertThat(expected.exists(), is(true));

    // Copy from existing handled file (in subdirectory) to handled file and
    // verify existence in session
    test = getFile(realComponentPath, "a", otherFile.getName());
    expected = getFile(sessionComponentPath, "a", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyFile(BASE_PATH_TEST, getFile(sessionComponentPath, otherFile.getName()), test);
    assertThat(expected.exists(), is(true));

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

    // Copy file from URL to handled file and verify existence in session
    test = getFile(realComponentPath, "c", otherFile.getName());
    expected = getFile(sessionComponentPath, "c", otherFile.getName());
    assertThat(expected.exists(), is(false));
    fileHandler.copyURLToFile(
        toURLs(new File[] { getFile(sessionComponentPath, otherFile.getName()) })[0],
        BASE_PATH_TEST, test);
    assertThat(expected.exists(), is(true));
  }

  /*
   * delete
   */

  @Test(expected = FileHandlerException.class)
  public void testDeleteOnNotHandledFile() throws Exception {
    fileHandler.delete(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testCleanDirectoryOnNotHandledPath() throws Exception {
    fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile.getParentFile());
  }

  @Test(expected = FileHandlerException.class)
  public void testCleanDirectoryOnNotHandledFile() throws Exception {
    fileHandler.cleanDirectory(BASE_PATH_TEST, otherFile);
  }

  @Test
  public void testDeleteFromRealPathOnly() throws Exception {

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
  public void testDeleteFromSessionPathOnly() throws Exception {

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
  public void testDeleteFileExistingInSessionAndRealPaths() throws Exception {

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

  @Test(expected = FileHandlerException.class)
  public void testWaitForOnNotHandledFile() {
    fileHandler.waitFor(BASE_PATH_TEST, otherFile, 10);
  }

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
   * moveFile
   */

  @Test(expected = FileHandlerException.class)
  public void testMoveFileFromNotHandledFile() throws Exception {
    fileHandler.moveFile(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file"));
  }

  @Test(expected = FileHandlerException.class)
  public void testMoveFileToNotHandledFile() throws Exception {
    fileHandler.moveFile(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testMoveFileFromNotHandledFileInMultiHandledBasePathMethod() throws Exception {
    fileHandler.moveFile(BASE_PATH_TEST, otherFile, BASE_PATH_TEST,
        getFile(sessionComponentPath, "file"));
  }

  @Test(expected = FileHandlerException.class)
  public void testMoveFileToNotHandledFileInMultiHandledBasePathMethod() throws Exception {
    fileHandler.moveFile(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), BASE_PATH_TEST,
        otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testMoveFileWithNullFileParameter() throws Exception {
    fileHandler.moveFile(null, fileHandler.getHandledFile(BASE_PATH_TEST, otherFile));
  }

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

    fileHandler.moveFile(BASE_PATH_TEST, real, realDest);

    // Verify after move
    assertThat(real.exists(), is(true));
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, real), is(true));
    assertThat(readFileToString(real), is("root_file_1"));
    assertThat(session.exists(), is(false));
    assertThat(sessionDest.exists(), is(true));
    assertThat(readFileToString(sessionDest), is("root_file_1"));

    // Verify the moving of not handled file to handled file
    assertThat(real.exists(), is(true));
    assertThat(session.exists(), is(false));
    fileHandler.moveFile(otherFile, fileHandler.getHandledFile(BASE_PATH_TEST, real));
    assertThat(real.exists(), is(true));
    assertThat(session.exists(), is(true));
  }

  /*
   * read
   */

  @Test(expected = FileHandlerException.class)
  public void testReadFileToStringWithNotHandledFile() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testReadFileToStringWithNotHandlefFileAndSpecificFileEncoding() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, otherFile, "UTF16");
  }

  @Test(expected = FileHandlerException.class)
  public void testReadFileToByteArrayWithNotHandledFile() throws Exception {
    fileHandler.readFileToByteArray(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testReadLinesWithNotHandledFile() throws Exception {
    fileHandler.readLines(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testReadLinesWithNotHandlefFileAndSpecificFileEncoding() throws Exception {
    fileHandler.readLines(BASE_PATH_TEST, otherFile, "UTF16");
  }

  @Test(expected = FileNotFoundException.class)
  public void testReadFileToStringWithNotExistingFile() throws Exception {
    fileHandler.readFileToString(BASE_PATH_TEST, getFile(sessionComponentPath, "readFile"));
  }

  @Test(expected = FileNotFoundException.class)
  public void testReadFileWhithExistingFileMarkedAsDeleted() throws Exception {
    final File real = getFile(realComponentPath, "readFile");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(real);
    fileHandler.readFileToString(BASE_PATH_TEST, real);
  }

  @Test
  public void testReadFileToStringFromFileExistingInSessionAndRealPath() throws Exception {

    // Creating file with different content depending of session or real paths
    final File session = getFile(sessionComponentPath, "readFile");
    final File real = getFile(realComponentPath, "readFile");
    writeStringToFile(session, "session\nligne 2\n");
    writeStringToFile(real, "real\nligne 2\n");

    // Content of session file has to be read
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real);
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
    String fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
    assertThat(fileContent, is("session\nligne 2\n"));

    // When file doesn't exist in session, content of real file has to be read
    deleteQuietly(session);
    fileContent = fileHandler.readFileToString(BASE_PATH_TEST, real, "UTF16");
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
  public void testReadLinesFromFileExistingInSessionAndRealPathWithSpecificFileEncoding()
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

  @Test(expected = FileHandlerException.class)
  public void testWriteStringToFileThatIsNotHandled() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteStringToFileThatIsNotHandledWithExplicitAppendParameter() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteStringToFileThatIsNotHandledWithSpecificFileEncoding() throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16");
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteStringToFileThatIsNotHandledWithSpecificFileEncodingAndExplicitAppendParameter()
      throws Exception {
    fileHandler.writeStringToFile(BASE_PATH_TEST, otherFile, null, "UTF16", false);
  }

  @Test
  public void testWriteStringToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    assertThat(readFileToString(session), is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent");
    // Session file contains this new content and real file is not modified
    assertThat(readFileToString(session), is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, "modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    assertThat(readFileToString(session), is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    fileHandler.writeStringToFile(BASE_PATH_TEST, real, new String("newContent".getBytes("UTF16")),
        "UTF16");
    // Session file contains this new content and real file is not modified
    assertThat(readFileToString(session, "UTF16"), is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * write
   */

  @Test(expected = FileHandlerException.class)
  public void testWriteFileThatIsNotHandled() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteFileThatIsNotHandledWithExplicitAppendParameter() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteFileThatIsNotHandledWithSpecificFileEncoding() throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16");
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteFileThatIsNotHandledWithSpecificFileEncodingAndExplicitAppendParameter()
      throws Exception {
    fileHandler.write(BASE_PATH_TEST, otherFile, null, "UTF16", false);
  }

  @Test
  public void testWrite() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.write(BASE_PATH_TEST, real, "modifiedContent", true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and a specific file encoding
    fileHandler.write(BASE_PATH_TEST, real, new String("newContent".getBytes("UTF16")), "UTF16");
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "UTF16");
    assertThat(fileContent, is(new String("newContent".getBytes("UTF16"))));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeByteArrayToFile
   */

  @Test(expected = FileHandlerException.class)
  public void testWriteByteArrayToFileThatIsNotHandled() throws Exception {
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteByteArrayToFileThatIsNotHandledWithExplicitAppendParameter()
      throws Exception {
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, otherFile, null, true);
  }

  @Test(expected = NullPointerException.class)
  public void testWriteByteArrayToFileWithNullData() throws Exception {
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, null);
  }

  @Test
  public void testWriteByteArrayToFile() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes());
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContent"));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeByteArrayToFile(BASE_PATH_TEST, real, "modifiedContent".getBytes(), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("modifiedContentmodifiedContent"));
    assertThat(readFileToString(real), is("notModified"));
  }

  /*
   * writeLines
   */

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandled() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithExplicitAppendParameter() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, true);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecifiedLineEnding() throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "lineEnding");
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecifiedLineEndingAndWithExplicitAppendParameter()
      throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, Collections.EMPTY_LIST, "lineEnding", false);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAnd()
      throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithExplicitAppendParameter()
      throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, true);
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithSpecifiedLineEnding()
      throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16");
  }

  @Test(expected = FileHandlerException.class)
  public void testWriteLinesToFileThatIsNotHandledWithSpecificEncodingParameterAndWithSpecifiedLineEndingAndWithExplicitAppendParameter()
      throws Exception {
    fileHandler.writeLines(BASE_PATH_TEST, otherFile, null, Collections.EMPTY_LIST, "UTF16", false);
  }

  @Test
  public void testWriteLines() throws Exception {

    // Creating file with same content in session and real paths
    final File session = getFile(sessionComponentPath, "writeFile");
    final File real = getFile(realComponentPath, "writeFile");
    writeStringToFile(session, "notModified");
    writeStringToFile(real, "notModified");

    // Writing empty content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, null);
    // Session file is now empty and real file is not modified
    String fileContent = readFileToString(session);
    assertThat(fileContent, is(""));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "line3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and specific line ending
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line1", "line2"),
        LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific line ending
    fileHandler.writeLines(BASE_PATH_TEST, real, Arrays.asList("line3", "line4"),
        LINE_SEPARATOR_UNIX, true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session);
    assertThat(fileContent, is("line1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX +
        "line3" + LINE_SEPARATOR_UNIX + "line4" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Encoding

    // Writing new content by specifying real file and specific file encoding
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"));
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific file encoding
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"), true);
    // Session file contains the new appended content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR + "line2" + LINE_SEPARATOR + "éline3" +
        LINE_SEPARATOR + "line4" + LINE_SEPARATOR));
    assertThat(readFileToString(real), is("notModified"));

    // Writing new content by specifying real file and specific file encoding and specific line
    // ending
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline1", "line2"),
        LINE_SEPARATOR_UNIX);
    // Session file contains this new content and real file is not modified
    fileContent = readFileToString(session, "Latin1");
    assertThat(fileContent, is("éline1" + LINE_SEPARATOR_UNIX + "line2" + LINE_SEPARATOR_UNIX));
    assertThat(readFileToString(real), is("notModified"));

    // Appending new content by specifying real file and specific file encoding and specific line
    // ending
    fileHandler.writeLines(BASE_PATH_TEST, real, "Latin1", Arrays.asList("éline3", "line4"),
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

  @Test(expected = FileHandlerException.class)
  public void testSizeOfNotHandledFile() {
    fileHandler.sizeOf(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSizeOfDirectoryOnNotHandledFile() {
    fileHandler.sizeOfDirectory(BASE_PATH_TEST, otherFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSizeOfDirectoryOnFile() throws Exception {
    buildCommonPathStructure();
    fileHandler.sizeOfDirectory(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"));
  }

  @Test
  public void testSizeOf() throws Exception {
    buildCommonPathStructure();

    long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath, "root_file_2"));
    assertThat(size, is(19L));
    assertThat(sizeOf(getFile(realRootPath, "root_file_2")), is(11L));

    size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  @Test
  public void testSizeOfDirectory() throws Exception {
    buildCommonPathStructure();

    final long size = fileHandler.sizeOf(BASE_PATH_TEST, getFile(realRootPath));
    assertThat(size, is(183L));
    assertSizes(64, 136);
  }

  /*
   * isFileNewer
   */

  @Test(expected = FileHandlerException.class)
  public void testIsFileNewerBetweenNotHandledFileAndHandledFile() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file"));
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileNewerBetweenHandledFileAndNotHandledFile() {
    fileHandler.isFileNewer(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileNewerWithNotHandledFileFromDate() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, new Date());
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileNewerWithNotHandledFileFromDateInMilliseconds() {
    fileHandler.isFileNewer(BASE_PATH_TEST, otherFile, 123);
  }

  @Test
  public void testIsFileNewer() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(1001);
    touch(file2);
    boolean test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(false));
    Thread.sleep(1001);
    writeStringToFile(file1, "toto");
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(true));

    Thread.sleep(1001);
    final Date date = new Date();
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, date);
    assertThat(test, is(false));

    final long time = System.currentTimeMillis();
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, time);
    assertThat(test, is(false));

    Thread.sleep(1001);
    writeStringToFile(file1, "titi");
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, date);
    assertThat(test, is(true));
    test = fileHandler.isFileNewer(BASE_PATH_TEST, real1, time);
    assertThat(test, is(true));
  }

  /*
   * isFileOlder
   */

  @Test(expected = FileHandlerException.class)
  public void testIsFileOlderBetweenNotHandledFileAndHandledFile() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, getFile(sessionComponentPath, "file"));
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileOlderBetweenHandledFileAndNotHandledFile() {
    fileHandler.isFileOlder(BASE_PATH_TEST, getFile(sessionComponentPath, "file"), otherFile);
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileOlderWithNotHandledFileFromDate() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, new Date());
  }

  @Test(expected = FileHandlerException.class)
  public void testIsFileOlderWithNotHandledFileFromDateInMilliseconds() {
    fileHandler.isFileOlder(BASE_PATH_TEST, otherFile, 123);
  }

  @Test
  public void testIsFileOlder() throws Exception {
    final File real1 = getFile(realComponentPath, "file1");
    final File file1 = getFile(sessionComponentPath, "file1");
    final File file2 = getFile(sessionComponentPath, "file2");
    touch(file1);
    Thread.sleep(1001);
    touch(file2);
    boolean test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(true));
    Thread.sleep(1001);
    writeStringToFile(file1, "toto");
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, file2);
    assertThat(test, is(false));

    Thread.sleep(1001);
    final Date date = new Date();
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, date);
    assertThat(test, is(true));

    final long time = System.currentTimeMillis();
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, time);
    assertThat(test, is(true));

    Thread.sleep(1001);
    writeStringToFile(file1, "titi");
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, date);
    assertThat(test, is(false));
    test = fileHandler.isFileOlder(BASE_PATH_TEST, real1, time);
    assertThat(test, is(false));
  }

  @Test
  public void testGetSessionHandledRootPathNames() throws Exception {
    buildCommonPathStructure();
    final Collection<String> test =
        new TreeSet<String>(fileHandler.getSessionHandledRootPathNames());
    assertThat(test.size(), is(1));
    assertThat(test, contains("componentInstanceId"));
  }

  @Test
  public void testListAllSessionHandledRootPathFiles() throws Exception {
    buildCommonPathStructure();
    final Collection<File> test =
        new TreeSet<File>(fileHandler.listAllSessionHandledRootPathFiles());
    assertThat(test.size(), is(1));
    assertThat(test.iterator().next().getName(), is("componentInstanceId"));
  }

  private abstract class RunnableTest<R> implements Runnable {

    protected R result;

    public R getResult() {
      return result;
    }
  }
}

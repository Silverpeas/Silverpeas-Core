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
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.process.io.IOAccess;
import org.silverpeas.core.process.io.file.exception.FileHandlerException;
import org.silverpeas.core.process.session.DefaultProcessSession;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.ResourceLocator;

import java.io.File;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public class TestAbstractFileHandler {

  private FileBasePath BASE_PATH_TEST;
  private File sessionRootPath;
  private File realRootPath;
  private File otherFile;
  private ProcessSession currentSession;
  private FileHandlerTest fileHandler;
  private String componentInstanceId;
  private File realPath;
  private File sessionPath;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void beforeTest() throws Exception {
    BASE_PATH_TEST = FileBasePath.UPLOAD_PATH;
    sessionRootPath = new File(ResourceLocator.getGeneralSettingBundle().getString("tempPath"));
    realRootPath = new File(BASE_PATH_TEST.getPath());
    otherFile = new File(new File(BASE_PATH_TEST.getPath()).getParentFile(), "other");

    componentInstanceId = "component" + String.valueOf(System.identityHashCode(this)).
        substring(0, 2);
    currentSession = createSessionTest();
    fileHandler = new FileHandlerTest(currentSession);
    realPath = getFile(realRootPath, componentInstanceId);
    realPath.mkdirs();
    sessionPath = getFile(sessionRootPath, currentSession.getId(), BASE_PATH_TEST.
        getHandledNodeName(), componentInstanceId);
    sessionPath.mkdirs();
    touch(otherFile);
  }

  @After
  public void afterTest() throws Exception {
    cleanTest();
  }

  /**
   * Cleaning files handled by a test
   */
  private void cleanTest() {
    deleteQuietly(sessionRootPath);
    deleteQuietly(realRootPath);
    deleteQuietly(otherFile);
  }

  @Test
  public void testMarkToDeleteWhenNoFileExistsInRealPathAndSessionPath() throws Exception {
    final File test = getFile(realPath, "file");
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), Matchers.is(IOAccess.READ_ONLY));
  }

  @Test
  public void testMarkToDeleteWhenFileExistsOnlyInSessionPath() throws Exception {
    final File test = getFile(fileHandler.getRootPathForTest(), "file");
    touch(test);
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_ONLY));
  }

  @Test
  public void testMarkToDeleteWhenFileExistsOnlyInRealPath() throws Exception {
    final File test = getFile(realPath, "file");
    touch(test);
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(true));
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.DELETE_ONLY));
  }

  @Test
  public void testMarkToDeleteWhenFileExistsOnlyInRealPath2TimesFollowing() throws Exception {
    final File test = getFile(fileHandler.getRootPathForTest(), "file");
    touch(getFile(realPath, "file"));
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(true));
    assertThat(fileHandler.markToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.DELETE_ONLY));
  }

  @Test
  public void testIsMarkedToDeleteWhenNoFileExistsInRealPathAndSessionPath() {
    final File test = getFile(realPath, "file");
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_ONLY));
  }

  @Test
  public void testIsMarkedToDeleteWhenFileExistsOnlyInSessionPath() throws Exception {
    final File test = getFile(fileHandler.getRootPathForTest(), "file");
    touch(test);
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, test), is(false));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_ONLY));
  }

  @Test
  public void testIsMarkedToDeleteWhenFileExistsOnlyInRealPath() throws Exception {
    final File test = getFile(realPath, "file");
    touch(test);
    fileHandler.markToDelete(BASE_PATH_TEST, test);
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, test), is(true));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.DELETE_ONLY));
  }

  @Test
  public void testIsMarkedToDeleteWhenFileExistsOnlyInSubdirectoriedOfRealPath() throws Exception {
    final File directory = getFile(realPath, "directoryDeleted");
    final File test = getFile(realPath, "directoryDeleted", "file");
    touch(test);
    fileHandler.markToDelete(BASE_PATH_TEST, directory);
    assertThat(fileHandler.isMarkedToDelete(BASE_PATH_TEST, test), is(true));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.DELETE_ONLY));
  }

  @Test
  public void testTranslateToRealPathFromSessionBasePath() {
    final File test =
        fileHandler.translateToRealPath(BASE_PATH_TEST, fileHandler.getSessionPath(BASE_PATH_TEST));
    final File expected = realRootPath;
    assertFileNames(test, expected);
  }

  @Test
  public void testTranslateToRealPathFromSessionFile() {
    final File test =
        fileHandler.translateToRealPath(BASE_PATH_TEST,
        getFile(fileHandler.getRootPathForTest(), "file"));
    final File expected = getFile(realPath, "file");
    assertFileNames(test, expected);
  }

  @Test
  public void testTranslateToRealPathFromRealFile() {
    final File test = fileHandler.translateToRealPath(BASE_PATH_TEST, getFile(realPath, "file"));
    final File expected = getFile(realPath, "file");
    assertFileNames(test, expected);
  }

  @Test
  public void testTranslateToSessionPathFromRealBasePath() {
    final File test = fileHandler.translateToSessionPath(BASE_PATH_TEST, realRootPath);
    final File expected =
        getFile(sessionRootPath, currentSession.getId(), BASE_PATH_TEST.getHandledNodeName());
    assertFileNames(test, expected);
  }

  @Test
  public void testTranslateToSessionPathFromRealFile() {
    final File test = fileHandler.translateToSessionPath(BASE_PATH_TEST, getFile(realPath, "file"));
    final File expected = getFile(sessionPath, "file");
    assertFileNames(test, expected);
  }

  @Test
  public void testTranslateToSessionPathFromSessionFile() {
    final File test =
        fileHandler.translateToSessionPath(BASE_PATH_TEST,
        getFile(fileHandler.getRootPathForTest(), "file"));
    final File expected = getFile(sessionPath, "file");
    assertFileNames(test, expected);
  }

  @Test
  public void testGetExistingFile() throws Exception {
    final File test = getFile(realPath, "file");
    File expected = getFile(sessionPath, "file");
    assertFileNames(fileHandler.getExistingFile(BASE_PATH_TEST, test), expected);
    expected = getFile(realPath, "file");
    touch(test);
    assertFileNames(fileHandler.getExistingFile(BASE_PATH_TEST, test), expected);
    expected = getFile(sessionPath, "file");
    touch(expected);
    assertFileNames(fileHandler.getExistingFile(BASE_PATH_TEST, test), expected);
    deleteQuietly(expected);
    expected = getFile(realPath, "file");
    assertFileNames(fileHandler.getExistingFile(BASE_PATH_TEST, test), expected);
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(getFile(realPath, "file"));
    expected = getFile(sessionPath, "file");
    assertFileNames(fileHandler.getExistingFile(BASE_PATH_TEST, test), expected);
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_ONLY));
  }

  @Test
  public void testGetFileForWriting() throws Exception {
    final File test = getFile(realPath, "file");
    final File expected = getFile(sessionPath, "file");
    assertFileNames(fileHandler.getFileForWriting(BASE_PATH_TEST, test), expected);
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    touch(test);
    assertFileNames(fileHandler.getFileForWriting(BASE_PATH_TEST, test), expected);
    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.getIoAccess(), is(IOAccess.READ_WRITE));
  }

  @Test
  public void testVerify() {
    fileHandler.verify(BASE_PATH_TEST, getFile(fileHandler.getRootPathForTest(), "file"));
    fileHandler.verify(BASE_PATH_TEST, getFile(realRootPath, "file"));
    fileHandler.verify(BASE_PATH_TEST, otherFile, true);
  }

  @Test(expected = FileHandlerException.class)
  public void testVerifyInFailure() {
    fileHandler.verify(BASE_PATH_TEST, otherFile);
  }

  @Test
  public void testGetSessionPath() {
    final File test = fileHandler.getSessionPath(BASE_PATH_TEST);
    final File expected =
        getFile(sessionRootPath, currentSession.getId(), BASE_PATH_TEST.getHandledNodeName());
    assertFileNames(test, expected);
  }

  @Test
  public void testGetSessionTemporaryPath() {
    final File test = fileHandler.getSessionTemporaryPath();
    final File expected = getFile(sessionRootPath, currentSession.getId(), "@#@work@#@");
    assertFileNames(test, expected);
    assertThat(expected.exists(), is(false));
  }

  @Test
  public void testSizeOfSessionWorkingPath() throws Exception {
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(0L));

    final File file1 = getFile(sessionRootPath, currentSession.getId(), "handledFile1");
    writeStringToFile(file1, "handledFile1");

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(0L));

    final File notHandledFile = getFile(realPath, "file1");
    writeStringToFile(notHandledFile, "real1");

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(0L));

    final File file2 = getFile(sessionPath, "handledFile2");
    writeStringToFile(file2, "handledFile22");

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(13L));

    final File file2real = getFile(realPath, "handledFile2");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(file2real);

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(13L));

    writeStringToFile(file2real, "real22");

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(7L));

    fileHandler.getMarkedToDelete(BASE_PATH_TEST).clear();

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(13L));
  }

  @Test
  public void testSizeOfSessionWorkingPathWithVirtualHandledFiles() throws Exception {
    DummyHandledFile dummyHandledFile1 = new DummyHandledFileTest(50, false);
    DummyHandledFile dummyHandledFile2 = new DummyHandledFileTest(25, false);
    DummyHandledFile dummyHandledFile3Deleted = new DummyHandledFileTest(3, true);
    DummyHandledFile dummyHandledFile4Deleted = new DummyHandledFileTest(4, true);

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(0L));
    fileHandler.addDummyHandledFile(dummyHandledFile1);
    fileHandler.addDummyHandledFile(dummyHandledFile2);
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(75L));
    fileHandler.addDummyHandledFile(dummyHandledFile3Deleted);
    fileHandler.addDummyHandledFile(dummyHandledFile4Deleted);
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(68L));

    final File file1 = getFile(sessionRootPath, currentSession.getId(), "handledFile1");
    writeStringToFile(file1, "handledFile1");

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(68L));

    final File notHandledFile = getFile(realPath, "file1");
    writeStringToFile(notHandledFile, "real1");

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(68L));

    final File file2 = getFile(sessionPath, "handledFile2");
    writeStringToFile(file2, "handledFile22");

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(81L));

    final File file2real = getFile(realPath, "handledFile2");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(file2real);

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(81L));

    writeStringToFile(file2real, "real22");

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(1));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(75L));

    fileHandler.getMarkedToDelete(BASE_PATH_TEST).clear();

    assertThat(fileHandler.getMarkedToDelete(BASE_PATH_TEST).size(), is(0));
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(81L));
  }

  @Test
  public void testDeleteSessionWorkingPath() {
    assertThat(realPath.exists(), is(true));
    assertThat(sessionPath.exists(), is(true));
    assertThat(getFile(sessionRootPath, currentSession.getId()).exists(), is(true));
    assertThat(otherFile.exists(), is(true));
    fileHandler.deleteSessionWorkingPath();
    assertThat(realPath.exists(), is(true));
    assertThat(sessionPath.exists(), is(false));
    assertThat(getFile(sessionRootPath, currentSession.getId()).exists(), is(false));
    assertThat(otherFile.exists(), is(true));
  }

  @Test
  public void testCheckinSessionWorkingPathSimpleCase() throws Exception {
    assertThat(otherFile.exists(), is(true));
    assertThat(sessionPath.exists(), is(true));
    assertThat(sessionRootPath.exists(), is(true));

    fileHandler.checkinSessionWorkingPath();

    assertThat(otherFile.exists(), is(true));
    assertThat(sessionPath.exists(), is(false));
    assertThat(sessionRootPath.exists(), is(true));
  }

  @Test
  public void testCheckinSessionWorkingPathComplexCase() throws Exception {
    final File tmpFile = getFile(fileHandler.getSessionTemporaryPath(), "tempFile.file");
    writeStringToFile(tmpFile,
        "This is a session temporary file. It is not taken in account in checkin operations.");
    assertThat(otherFile.exists(), is(true));
    assertThat(sessionPath.exists(), is(true));
    assertThat(sessionRootPath.exists(), is(true));
    assertThat(tmpFile.exists(), is(true));

    final File file1session = getFile(sessionPath, "file1");
    final File file1real = getFile(realPath, "file1");
    writeStringToFile(file1real, "1");
    assertThat(file1session.exists(), is(false));
    assertThat(file1real.exists(), is(true));

    final File file2session = getFile(sessionPath, "file2");
    final File file2real = getFile(realPath, "file2");
    writeStringToFile(file2session, "22");
    assertThat(file2session.exists(), is(true));
    assertThat(file2real.exists(), is(false));

    final File file3session = getFile(sessionPath, "subPath", "file3");
    final File file3real = getFile(realPath, "subPath", "file3");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(file3real);
    writeStringToFile(file3session, "333");
    writeStringToFile(file3real, "333333");
    assertThat(file3session.exists(), is(true));
    assertThat(file3real.exists(), is(true));

    final File file4session = getFile(sessionPath, "file4");
    final File file4real = getFile(realPath, "file4");
    fileHandler.getMarkedToDelete(BASE_PATH_TEST).add(file4real);
    writeStringToFile(file4real, "1234567890");
    assertThat(file4session.exists(), is(false));
    assertThat(file4real.exists(), is(true));

    // Test

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(-11L));
    assertThat(FileUtils.sizeOf(sessionPath), is(5L));
    assertThat(FileUtils.sizeOf(realPath), is(17L));

    fileHandler.checkinSessionWorkingPath();

    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(0L));
    assertThat(FileUtils.sizeOf(realPath), is(6L));

    // Asserts

    assertThat(file1session.exists(), is(false));
    assertThat(file1real.exists(), is(true));
    assertThat(file2session.exists(), is(false));
    assertThat(file2real.exists(), is(true));
    assertThat(file3session.exists(), is(false));
    assertThat(file3real.exists(), is(true));
    assertThat(file4session.exists(), is(false));
    assertThat(file4real.exists(), is(false));

    assertThat(otherFile.exists(), is(true));
    assertThat(sessionPath.exists(), is(false));
    assertThat(sessionRootPath.exists(), is(true));
    assertThat(tmpFile.exists(), is(false));
  }

  /**
   * Centralizes asserts
   *
   * @param test
   * @param expected
   */
  private void assertFileNames(final File test, final File expected) {
    assertThat(test, is(expected));
  }

  private ProcessSession createSessionTest() {
    return DefaultProcessSession.create();
  }

  private class FileHandlerTest extends AbstractFileHandler {

    protected FileHandlerTest(final ProcessSession session) {
      super(session);
    }

    /**
     * @return
     */
    public File getRootPathForTest() {
      return getFile(fileHandler.getSessionPath(BASE_PATH_TEST), componentInstanceId);
    }
  }

  private class DummyHandledFileTest extends AbstractDummyHandledFile {

    private final long size;
    private final boolean deleted;

    private DummyHandledFileTest(final long size, final boolean deleted) {
      this.size = size;
      this.deleted = deleted;
    }

    @Override
    public String getComponentInstanceId() {
      return "dummyComponentInstanceId";
    }

    @Override
    public String getPath() {
      return getName();
    }

    @Override
    public String getName() {
      return "dummyName_" + UUID.randomUUID().toString();
    }

    @Override
    public long getSize() {
      return size;
    }

    @Override
    public String getMimeType() {
      return null;
    }

    @Override
    public boolean isDeleted() {
      return deleted;
    }
  }
}

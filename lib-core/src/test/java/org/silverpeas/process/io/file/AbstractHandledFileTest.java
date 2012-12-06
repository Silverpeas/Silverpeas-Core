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
package org.silverpeas.process.io.file;

import static com.stratelia.webactiv.util.GeneralPropertiesManager.getString;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.sizeOf;
import static org.apache.commons.io.FileUtils.touch;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.silverpeas.process.session.ProcessSession;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractHandledFileTest {

  protected static final FileBasePath BASE_PATH_TEST = FileBasePath.UPLOAD_PATH;
  protected static final ProcessSession currentSession = createSessionTest();
  protected static final String componentInstanceId = "componentInstanceId";
  protected static final File sessionRootPath = new File(getString("tempPath"));
  protected static final File realRootPath = new File(BASE_PATH_TEST.getPath());
  protected static final File otherFile = new File(
      new File(BASE_PATH_TEST.getPath()).getParentFile(), "other");
  protected static final File sessionHandledPath = FileUtils.getFile(sessionRootPath,
      currentSession.getId(), BASE_PATH_TEST.getHandledNodeName());
  protected static final File realComponentPath = FileUtils.getFile(realRootPath,
      componentInstanceId);
  protected static final File sessionComponentPath = FileUtils.getFile(sessionRootPath,
      currentSession.getId(), BASE_PATH_TEST.getHandledNodeName(), componentInstanceId);

  protected FileHandler fileHandler;

  @Before
  public void beforeTest() throws Exception {
    cleanTest();
    fileHandler = new FileHandler(currentSession);
    realComponentPath.mkdirs();
    sessionComponentPath.mkdirs();
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
  protected void buildCommonPathStructure() throws Exception {
    // --> SESSION
    createSessionFile(FileUtils.getFile(sessionHandledPath, "root_file_2"));
    createSessionFile(FileUtils.getFile(sessionHandledPath, "root_file_3"));
    createSessionFile(FileUtils.getFile(sessionComponentPath, "file_1"));
    createSessionFile(FileUtils.getFile(sessionComponentPath, "a/b/file_ab_1"));
    createSessionFile(FileUtils.getFile(sessionComponentPath, "a/b/file_ab_2.xml"));
    createSessionFile(FileUtils.getFile(sessionComponentPath, "a/b/c/file_abc_3"));
    createSessionFile(FileUtils.getFile(sessionComponentPath, "b/file_b_2"));

    // --> REAL
    createFile(FileUtils.getFile(realRootPath, "root_file_1"));
    createFile(FileUtils.getFile(realRootPath, "root_file_2"));
    createFile(FileUtils.getFile(realComponentPath, "file_1"));
    createFile(FileUtils.getFile(realComponentPath, "file_2"));
    createFile(FileUtils.getFile(realComponentPath, "a/file_a_1"));
    createFile(FileUtils.getFile(realComponentPath, "a/file_a_2"));
    createFile(FileUtils.getFile(realComponentPath, "a/file_a_3.xml"));
    createFile(FileUtils.getFile(realComponentPath, "a/b/file_ab_1"));
    createFile(FileUtils.getFile(realComponentPath, "a/b/file_ab_2.xml"));
    createFile(FileUtils.getFile(realComponentPath, "a/b/c/file_abc_1"));
    createFile(FileUtils.getFile(realComponentPath, "a/b/c/file_abc_2"));
    createFile(FileUtils.getFile(realComponentPath, "b/file_b_1"));
    createFile(FileUtils.getFile(realComponentPath, "b/c/file_bc_1.test"));
    createFile(FileUtils.getFile(realComponentPath, "b/c/d/file_bcd_1"));

    assertSizes(124, 136);

    fileHandler.markToDelete(BASE_PATH_TEST, FileUtils.getFile(realComponentPath, "a", "file_a_2"));
    fileHandler.markToDelete(BASE_PATH_TEST, FileUtils.getFile(realComponentPath, "a/b"));
    fileHandler.markToDelete(BASE_PATH_TEST, FileUtils.getFile(realComponentPath, "b/c/d"));

    assertSizes(64, 136);

    assertThat(
        FileUtils.getFile(sessionRootPath, currentSession.getId(), "@#@work@#@", "temporaryFile")
            .exists(), is(false));
    writeStringToFile(fileHandler.getSessionTemporaryFile("temporaryFile"),
        "this is a session temporary file !");
    assertThat(
        FileUtils.getFile(sessionRootPath, currentSession.getId(), "@#@work@#@", "temporaryFile")
            .exists(), is(true));

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
  protected void assertSizes(final long sessionSize, final long realSize) {
    assertThat(fileHandler.sizeOfSessionWorkingPath(), is(sessionSize));
    assertThat(sizeOf(realRootPath), is(realSize));
  }

  /**
   * Centralizes asserts
   * @param test
   * @param expected
   */
  protected void assertFileNames(final File test, final File expected) {
    assertThat(test, is(expected));
  }

  protected static ProcessSession createSessionTest() {
    return new ProcessSession() {

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

      @Override
      public <C> C getAttribute(final String name, final Class<C> expectedReturnedClass) {
        return null;
      }
    };
  }

}

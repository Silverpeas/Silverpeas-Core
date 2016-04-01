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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.ResourceLocator;

import java.io.File;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractHandledFileTest {

  protected static final String componentInstanceId = "componentInstanceId";
  protected FileBasePath BASE_PATH_TEST;
  protected ProcessSession currentSession;
  protected File sessionRootPath;
  protected File realRootPath;
  protected File otherFile;
  protected File sessionHandledPath;
  protected File realComponentPath;
  protected File sessionComponentPath;

  protected FileHandler fileHandler;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Before
  public void beforeTest() throws Exception {
    BASE_PATH_TEST = FileBasePath.UPLOAD_PATH;
    currentSession = createSessionTest();
    sessionRootPath = new File(ResourceLocator.getGeneralSettingBundle().getString("tempPath"));
    realRootPath = new File(BASE_PATH_TEST.getPath());
    otherFile = new File(new File(BASE_PATH_TEST.getPath()).getParentFile(), "other");
    sessionHandledPath = FileUtils
        .getFile(sessionRootPath, currentSession.getId(), BASE_PATH_TEST.getHandledNodeName());
    realComponentPath = FileUtils.getFile(realRootPath, componentInstanceId);
    sessionComponentPath = FileUtils
        .getFile(sessionRootPath, currentSession.getId(), BASE_PATH_TEST.getHandledNodeName(),
            componentInstanceId);

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

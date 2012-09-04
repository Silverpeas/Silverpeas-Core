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
package org.silverpeas.io;

import static com.stratelia.webactiv.util.GeneralPropertiesManager.getString;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.getFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.io.check.IOCheck;
import org.silverpeas.io.file.FileHandler;
import org.silverpeas.io.file.IOBasePath;
import org.silverpeas.io.process.AbstractIOProcess;
import org.silverpeas.io.session.IOSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-io.xml" })
public class IOProcessManagementTest {

  private static final IOBasePath BASE_PATH_TEST = IOBasePath.UPLOAD_PATH;
  private static final String componentInstanceId = "componentInstanceId";
  private static final File sessionRootPath = new File(getString("tempPath"));
  private static final File testResultFile = getFile(new File(BASE_PATH_TEST.getPath()),
      componentInstanceId, "testResult");
  private static final File testSecondFile = getFile(new File(BASE_PATH_TEST.getPath()),
      componentInstanceId, "testSecondResult");
  private static String testSecondResultContent = "File check in has not been done.";

  @Before
  public void beforeTest() throws Exception {
    FileUtils.writeStringToFile(testSecondFile, "File check in has not been done.");
  }

  @After
  public void afterTest() throws Exception {
    deleteQuietly(sessionRootPath);
    deleteQuietly(new File(BASE_PATH_TEST.getPath()));
  }

  @Test
  public void testBeforeProcessingException() throws Exception {
    final AbstractIOProcessTest test = new AbstractIOProcessTest() {

      @Override
      public void processBefore(final IOSession session) throws Exception {
        super.processBefore(session);
        throw new RuntimeException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    IOFactory.getProcessManagement().execute(test);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(IOErrorType.DURING_BEFORE_PROCESSING));
    assertThat(test.getException(), instanceOf(RuntimeException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testFilesProcessingException() throws Exception {
    final AbstractIOProcessTest test = new AbstractIOProcessTest() {

      @Override
      public void processFiles(final IOSession session, final FileHandler fileHandler)
          throws Exception {
        super.processFiles(session, fileHandler);
        throw new FileNotFoundException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    IOFactory.getProcessManagement().execute(test);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(IOErrorType.DURING_FILES_PROCESSING));
    assertThat(test.getException(), instanceOf(FileNotFoundException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testChecksProcessingException() throws Exception {
    final AbstractIOProcessTest test = new AbstractIOProcessTest() {
    };
    final IOCheck check = new IOCheckTest();
    check.register();
    try {
      assertThat(testResultFile.exists(), is(false));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
      IOFactory.getProcessManagement().execute(test);
      assertThat(testResultFile.exists(), is(false));
      assertThat(test.getErrorType(), is(IOErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test.getException(), instanceOf(IOException.class));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    } finally {
      check.unregister();
    }
  }

  @Test
  public void testSuccessfulProcessingException() throws Exception {
    final AbstractIOProcessTest test = new AbstractIOProcessTest() {

      @Override
      public Object onSuccessful(final IOSession session) throws Exception {
        super.onSuccessful(session);
        throw new IllegalArgumentException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    IOFactory.getProcessManagement().execute(test);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(IOErrorType.DURING_ON_SUCESSFULL_PROCESSING));
    assertThat(test.getException(), instanceOf(IllegalArgumentException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testSuccessfulProcessing() throws Exception {
    final AbstractIOProcessTest test = new AbstractIOProcessTest() {
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    IOFactory.getProcessManagement().execute(test);
    assertThat(testResultFile.exists(), is(true));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done."));
  }

  /**
   * Mock
   * @author Yohann Chastagnier
   */
  private abstract class AbstractIOProcessTest extends AbstractIOProcess<Object> {
    private FileHandler fileHandler = null;
    private IOErrorType errorType = null;
    private Exception exception = null;

    /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.io.process.AbstractIOProcess#processFiles(org.silverpeas.io.session.IOSession,
     * org.silverpeas.io.file.FileHandler)
     */
    @Override
    public void processFiles(final IOSession session, final FileHandler fileHandler)
        throws Exception {
      this.fileHandler = fileHandler;
      fileHandler.writeStringToFile(BASE_PATH_TEST, testResultFile, " processFiles", true);
      fileHandler.writeStringToFile(BASE_PATH_TEST, testSecondFile, LINE_SEPARATOR +
          "File check in has been done.", true);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.io.process.AbstractIOProcess#onFailure(org.silverpeas.io.session.IOSession,
     * org.silverpeas.io.IOErrorType, java.lang.Exception)
     */
    @Override
    public void onFailure(final IOSession session, final IOErrorType errorType,
        final Exception exception) throws Exception {
      this.errorType = errorType;
      this.exception = exception;
      if (fileHandler != null) {
        fileHandler.writeStringToFile(BASE_PATH_TEST, testResultFile, " onFailure", true);
      }
    }

    /**
     * @return the errorType
     */
    public IOErrorType getErrorType() {
      return errorType;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
      return exception;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.io.process.AbstractIOProcess#onSuccessful(org.silverpeas.io.session.IOSession)
     */
    @Override
    public Object onSuccessful(final IOSession session) throws Exception {
      fileHandler.writeStringToFile(BASE_PATH_TEST, testResultFile, " onSuccessful", true);
      return super.onSuccessful(session);
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private class IOCheckTest implements IOCheck {

    @Override
    public void unregister() {
      IOFactory.getChecker().unregister(this);
    }

    @Override
    public void register() {
      IOFactory.getChecker().register(this);
    }

    @Override
    public void check(final IOSession session, final FileHandler fileHandler) throws Exception {
      throw new IOException();
    }
  }
}

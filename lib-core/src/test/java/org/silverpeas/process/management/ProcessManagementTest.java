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
package org.silverpeas.process.management;

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
import org.silverpeas.process.ProcessFactory;
import org.silverpeas.process.check.Check;
import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.Session;
import org.silverpeas.process.util.ProcessList;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-process.xml" })
public class ProcessManagementTest {

  private static final FileBasePath BASE_PATH_TEST = FileBasePath.UPLOAD_PATH;
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
  public void testFilesProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {

      @Override
      public void processFiles(final ProcessExecutionContextTest context, final Session session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(new AbstractFileProcessTest() {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final Session session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        throw new FileNotFoundException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    executeTest(test);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(ProcessErrorType.DURING_MAIN_PROCESSING));
    assertThat(test.getException(), instanceOf(FileNotFoundException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testChecksProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {
    };
    final Check check = new CheckFileTest();
    check.register();
    try {
      assertThat(testResultFile.exists(), is(false));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
      executeTest(test);
      assertThat(testResultFile.exists(), is(false));
      assertThat(test.getErrorType(), is(ProcessErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test.getException(), instanceOf(IOException.class));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    } finally {
      check.unregister();
    }
  }

  @Test
  public void testSuccessfulProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {

      @Override
      public void onSuccessful(final ProcessExecutionContextTest context, final Session session)
          throws Exception {
        super.onSuccessful(context, session);
        throw new IllegalArgumentException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    executeTest(test);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(ProcessErrorType.DURING_ON_SUCESSFULL_PROCESSING));
    assertThat(test.getException(), instanceOf(IllegalArgumentException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testSuccessfulMultiProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {
    };
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest() {

      @Override
      public void onSuccessful(final ProcessExecutionContextTest context, final Session session)
          throws Exception {
        super.onSuccessful(context, session);
        throw new IllegalArgumentException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    executeTest(test, test2);
    assertThat(testResultFile.exists(), is(false));
    assertThat(test.getErrorType(), is(ProcessErrorType.OTHER_PROCESS_FAILED));
    assertThat(test.getException(), instanceOf(IllegalArgumentException.class));
    assertThat(test2.getErrorType(), is(ProcessErrorType.DURING_ON_SUCESSFULL_PROCESSING));
    assertThat(test2.getException(), instanceOf(IllegalArgumentException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testSuccessfulProcessing() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {
      @Override
      public void onSuccessful(final ProcessExecutionContextTest context, final Session session)
          throws Exception {
        super.onSuccessful(context, session);

        // Session attach
        executeTest(new AbstractFileProcessTest() {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final Session session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        // Session attach : new Thread.
        // It is a unit test case but not a real case. Here, the aim is to test the internal
        // mechanism of transaction context managing.
        executeTest(true, new AbstractFileProcessTest() {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final Session session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        Thread.sleep(500);
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    executeTest(test);
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile),
        is(" processFiles onSuccessful processFiles onSuccessful"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done." + LINE_SEPARATOR + "File check in has been done."));
  }

  @Test
  public void testSuccessfulMultiProcessing() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest() {
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    executeTest(test, test, test);
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile),
        is(" processFiles processFiles processFiles onSuccessful onSuccessful onSuccessful"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done." + LINE_SEPARATOR + "File check in has been done." +
        LINE_SEPARATOR + "File check in has been done."));
  }

  /**
   * Centralized testing method
   * @param processes
   */
  private void executeTest(final AbstractFileProcessTest... processes) throws Exception {
    executeTest(false, processes);
  }

  /**
   * Centralized testing method
   * @param processes
   */
  private void executeTest(final boolean newThread, final AbstractFileProcessTest... processes)
      throws Exception {
    if (newThread) {
      final Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            ProcessFactory.getProcessManagement().execute(
                new ProcessList<ProcessExecutionContextTest>(processes),
                new ProcessExecutionContextTest());
          } catch (final Exception e) {
            throw new RuntimeException(e);
          }
        }
      });
      thread.start();
    } else {
      ProcessFactory.getProcessManagement().execute(
          new ProcessList<ProcessExecutionContextTest>(processes),
          new ProcessExecutionContextTest());
    }
  }

  /**
   * Mock
   * @author Yohann Chastagnier
   */
  private abstract class AbstractFileProcessTest extends
      AbstractFileProcess<ProcessExecutionContextTest> {
    private FileHandler fileHandler = null;
    private ProcessErrorType errorType = null;
    private Exception exception = null;

    /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.process.management.AbstractFileProcess#processFiles(org.silverpeas.process
     * .management.ProcessExecutionContext, org.silverpeas.process.session.Session,
     * org.silverpeas.process.io.file.FileHandler)
     */
    @Override
    public void processFiles(final ProcessExecutionContextTest context, final Session session,
        final FileHandler fileHandler) throws Exception {
      this.fileHandler = fileHandler;
      fileHandler.getHandledFile(BASE_PATH_TEST, testResultFile).writeStringToFile(" processFiles",
          true);
      fileHandler.getHandledFile(BASE_PATH_TEST, testSecondFile).writeStringToFile(
          LINE_SEPARATOR + "File check in has been done.", true);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.process.AbstractProcess#onFailure(org.silverpeas.process.management.
     * ProcessExecutionContext, org.silverpeas.process.session.Session,
     * org.silverpeas.process.management.ProcessErrorType, java.lang.Exception)
     */
    @Override
    public void onFailure(final ProcessExecutionContextTest context, final Session session,
        final ProcessErrorType errorType, final Exception exception) throws Exception {
      this.errorType = errorType;
      this.exception = exception;
      if (fileHandler != null) {
        fileHandler.getHandledFile(BASE_PATH_TEST, testResultFile).writeStringToFile(" onFailure",
            true);
      }
    }

    /**
     * @return the errorType
     */
    public ProcessErrorType getErrorType() {
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
     * @see org.silverpeas.process.AbstractProcess#onSuccessful(org.silverpeas.process.management.
     * ProcessExecutionContext, org.silverpeas.process.session.Session)
     */
    @Override
    public void onSuccessful(final ProcessExecutionContextTest context, final Session session)
        throws Exception {
      fileHandler.getHandledFile(BASE_PATH_TEST, testResultFile).writeStringToFile(" onSuccessful",
          true);
      super.onSuccessful(context, session);
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private class CheckFileTest extends AbstractFileCheck {

    @Override
    public void checkFiles(final ProcessExecutionContext context, final Session session,
        final FileHandler fileHandler) throws Exception {
      throw new IOException();
    }
  }

  private class ProcessExecutionContextTest extends ProcessExecutionContext {
    public ProcessExecutionContextTest() {
      super(new UserDetail(), "component1");
      getUser().setId("10");
    }
  }
}

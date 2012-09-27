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
import static org.apache.commons.io.FileUtils.writeStringToFile;
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
import org.silverpeas.process.check.ProcessCheck;
import org.silverpeas.process.io.file.FileBasePath;
import org.silverpeas.process.io.file.FileHandler;
import org.silverpeas.process.session.ProcessSession;
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
  private static final File testSuccessfulFile = getFile(new File(BASE_PATH_TEST.getPath()),
      componentInstanceId, "testSuccessful");
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
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest("A_A") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);
      }
    };
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(test2);

        throw new FileNotFoundException();
      }
    };
    final AbstractFileProcessTest test3 = new AbstractFileProcessTest("B") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test, test3);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(testSuccessfulFile.exists(), is(false));
    assertThat(readFileToString(testResultFile), is(" onFailure(A_A) onFailure(A)"));
    assertThat(test.getErrorType(), is(ProcessErrorType.DURING_MAIN_PROCESSING));
    assertThat(test.getException(), instanceOf(FileNotFoundException.class));
    assertThat(test2.getErrorType(), is(ProcessErrorType.OTHER_PROCESS_FAILED));
    assertThat(test2.getException(), instanceOf(FileNotFoundException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testFilesProcessingException_2() throws Exception {
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest("A_A") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        executeTest(new AbstractFileProcessTest("A_A_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        throw new FileNotFoundException();
      }
    };
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(test2);
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(testSuccessfulFile.exists(), is(false));
    assertThat(readFileToString(testResultFile),
        is(" onFailure(A_A_A) onFailure(A_A) onFailure(A)"));
    assertThat(test.getErrorType(), is(ProcessErrorType.OTHER_PROCESS_FAILED));
    assertThat(test.getException(), instanceOf(FileNotFoundException.class));
    assertThat(test2.getErrorType(), is(ProcessErrorType.DURING_MAIN_PROCESSING));
    assertThat(test2.getException(), instanceOf(FileNotFoundException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testFilesProcessingException_3() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        executeTest(new AbstractFileProcessTest("A_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        }, new AbstractFileProcessTest("A_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });
      }
    };
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest("B") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        executeTest(new AbstractFileProcessTest("B_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        }, new AbstractFileProcessTest("B_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });
        throw new FileNotFoundException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test, test2);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(testSuccessfulFile.exists(), is(false));
    assertThat(
        readFileToString(testResultFile),
        is(" onFailure(B_B) onFailure(B_A) onFailure(A_B) onFailure(A_A) onFailure(B) onFailure(A)"));
    assertThat(test.getErrorType(), is(ProcessErrorType.OTHER_PROCESS_FAILED));
    assertThat(test.getException(), instanceOf(FileNotFoundException.class));
    assertThat(test2.getErrorType(), is(ProcessErrorType.DURING_MAIN_PROCESSING));
    assertThat(test2.getException(), instanceOf(FileNotFoundException.class));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
  }

  @Test
  public void testChecksProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        executeTest(new AbstractFileProcessTest("A_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        }, new AbstractFileProcessTest("A_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });
      }
    };
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest("B") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        executeTest(new AbstractFileProcessTest("B_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        }, new AbstractFileProcessTest("B_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });
      }
    };
    final ProcessCheck check = new CheckFileTest();
    check.register();
    try {
      assertThat(testResultFile.exists(), is(false));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
      try {
        executeTest(test, test2);
      } catch (final Exception e) {
        // Nothing to do
      }
      assertThat(testResultFile.exists(), is(true));
      assertThat(testSuccessfulFile.exists(), is(false));
      assertThat(
          readFileToString(testResultFile),
          is(" onFailure(B_B) onFailure(B_A) onFailure(A_B) onFailure(A_A) onFailure(B) onFailure(A)"));
      assertThat(test.getErrorType(), is(ProcessErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test.getException(), instanceOf(IOException.class));
      assertThat(test2.getErrorType(), is(ProcessErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test2.getException(), instanceOf(IOException.class));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    } finally {
      check.unregister();
    }
  }

  @Test
  public void testSuccessfulProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void onSuccessful() throws Exception {
        super.onSuccessful();
        throw new IllegalArgumentException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile), is(" processFiles(A)"));
    assertThat(testSuccessfulFile.exists(), is(true));
    assertThat(readFileToString(testSuccessfulFile), is(" onSuccessful(A)"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done.(A)"));
  }

  @Test
  public void testSuccessfulMultiProcessingException() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {
    };
    final AbstractFileProcessTest test2 = new AbstractFileProcessTest("B") {

      @Override
      public void onSuccessful() throws Exception {
        throw new IllegalArgumentException();
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test, test2);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile), is(" processFiles(A) processFiles(B)"));
    assertThat(testSuccessfulFile.exists(), is(true));
    assertThat(readFileToString(testSuccessfulFile), is(" onSuccessful(A)"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(test2.getErrorType(), nullValue());
    assertThat(test2.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done.(A)" + LINE_SEPARATOR + "File check in has been done.(B)"));
  }

  @Test
  public void testSuccessfulProcessing() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(new AbstractFileProcessTest("A_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        // Session attach : new Thread.
        // It is a unit test case but not a real case. Here, the aim is to test the internal
        // mechanism of transaction context managing.
        executeTest(true, new AbstractFileProcessTest("A_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });

        Thread.sleep(500);
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile), is(" processFiles(A) processFiles(A_A)"));
    assertThat(testSuccessfulFile.exists(), is(true));
    assertThat(readFileToString(testSuccessfulFile),
        is(" onSuccessful(A_B) onSuccessful(A_A) onSuccessful(A)"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done.(A)" + LINE_SEPARATOR + "File check in has been done.(A_A)"));
  }

  @Test
  public void testSuccessfulProcessing_2() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
          final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(new AbstractFileProcessTest("A_A") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        }, new AbstractFileProcessTest("A_B") {
          @Override
          public void processFiles(final ProcessExecutionContextTest context,
              final ProcessSession session, final FileHandler fileHandler) throws Exception {
            super.processFiles(context, session, fileHandler);
          }
        });
      }
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test);
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile),
        is(" processFiles(A) processFiles(A_A) processFiles(A_B)"));
    assertThat(testSuccessfulFile.exists(), is(true));
    assertThat(readFileToString(testSuccessfulFile),
        is(" onSuccessful(A_A) onSuccessful(A_B) onSuccessful(A)"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done.(A)" + LINE_SEPARATOR + "File check in has been done.(A_A)" +
        LINE_SEPARATOR + "File check in has been done.(A_B)"));
  }

  @Test
  public void testSuccessfulMultiProcessing() throws Exception {
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {
    };

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test, new AbstractFileProcessTest("B") {
      }, new AbstractFileProcessTest("C") {
      });
    } catch (final Exception e) {
      // Nothing to do
    }
    assertThat(testResultFile.exists(), is(true));
    assertThat(readFileToString(testResultFile),
        is(" processFiles(A) processFiles(B) processFiles(C)"));
    assertThat(testSuccessfulFile.exists(), is(true));
    assertThat(readFileToString(testSuccessfulFile),
        is(" onSuccessful(A) onSuccessful(B) onSuccessful(C)"));
    assertThat(test.getErrorType(), nullValue());
    assertThat(test.getException(), nullValue());
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent + LINE_SEPARATOR +
        "File check in has been done.(A)" + LINE_SEPARATOR + "File check in has been done.(B)" +
        LINE_SEPARATOR + "File check in has been done.(C)"));
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
    private final String id;
    private ProcessErrorType errorType = null;
    private Exception exception = null;

    public AbstractFileProcessTest(final String id) {
      this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.silverpeas.process.management.AbstractFileProcess#processFiles(org.silverpeas.process
     * .management.ProcessExecutionContext, org.silverpeas.process.session.ProcessSession,
     * org.silverpeas.process.io.file.FileHandler)
     */
    @Override
    public void processFiles(final ProcessExecutionContextTest context, final ProcessSession session,
        final FileHandler fileHandler) throws Exception {
      fileHandler.getHandledFile(BASE_PATH_TEST, testResultFile).writeStringToFile(
          " processFiles" + "(" + id + ")", true);
      fileHandler.getHandledFile(BASE_PATH_TEST, testSecondFile).writeStringToFile(
          LINE_SEPARATOR + "File check in has been done." + "(" + id + ")", true);
    }

    /*
     * (non-Javadoc)
     * @see org.silverpeas.process.AbstractProcess#onFailure(org.silverpeas.process.management.
     * ProcessExecutionContext, org.silverpeas.process.session.ProcessSession,
     * org.silverpeas.process.management.ProcessErrorType, java.lang.Exception)
     */
    @Override
    public void onFailure(final ProcessErrorType errorType, final Exception exception)
        throws Exception {
      try {
        super.onFailure(errorType, exception);
      } finally {
        this.errorType = errorType;
        this.exception = exception;
        writeStringToFile(testResultFile, " onFailure" + "(" + id + ")", true);
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
     * ProcessExecutionContext, org.silverpeas.process.session.ProcessSession)
     */
    @Override
    public void onSuccessful() throws Exception {
      writeStringToFile(testSuccessfulFile, " onSuccessful" + "(" + id + ")", true);
      super.onSuccessful();
    }
  }

  /**
   * @author Yohann Chastagnier
   */
  private class CheckFileTest extends AbstractFileProcessCheck {

    @Override
    public void checkFiles(final ProcessExecutionContext context, final FileHandler fileHandler) throws Exception {
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

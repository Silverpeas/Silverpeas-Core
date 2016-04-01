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
package org.silverpeas.core.process.management;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.process.ProcessProvider;
import org.silverpeas.core.process.check.ProcessCheck;
import org.silverpeas.core.process.io.file.FileBasePath;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.process.util.ProcessList;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.ResourceLocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class ProcessManagementTest {
  private static String testSecondResultContent = "File check in has not been done.";

  private FileBasePath BASE_PATH_TEST;
  private String componentInstanceId;
  private File sessionRootPath;
  private File testResultFile;
  private File testSuccessfulFile;
  private File testSecondFile;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ProcessManagementTest.class)
        .addSilverpeasExceptionBases()
        .addFileRepositoryFeatures()
        .addCommonUserBeans()
        .testFocusedOn((warBuilder) -> warBuilder.addPackages(true, "org.silverpeas.core.process"))
        .build();
  }

  @Before
  public void beforeTest() throws Exception {
    BASE_PATH_TEST = FileBasePath.UPLOAD_PATH;
    componentInstanceId = "componentInstanceId";
    sessionRootPath = new File(ResourceLocator.getGeneralSettingBundle().getString("tempPath"));
    testResultFile = getFile(new File(BASE_PATH_TEST.getPath()), componentInstanceId, "testResult");
    testSuccessfulFile =
        getFile(new File(BASE_PATH_TEST.getPath()), componentInstanceId, "testSuccessful");
    testSecondFile =
        getFile(new File(BASE_PATH_TEST.getPath()), componentInstanceId, "testSecondResult");

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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);
      }
    };
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {

      @Override
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
        super.processFiles(context, session, fileHandler);

        // Session attach
        executeTest(test2);

        throw new FileNotFoundException();
      }
    };
    final AbstractFileProcessTest test3 = new AbstractFileProcessTest("B") {
      @Override
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
    assertThat(readFileToString(testResultFile),
        is(" onFailure(B_B) onFailure(B_A) onFailure(A_B) onFailure(A_A) onFailure(B) onFailure" +
            "(A)"));
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
    final ProcessCheck check = new ThrowIoExceptionCheckFileTest();
    check.init();
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
      assertThat(readFileToString(testResultFile),
          is(" onFailure(B_B) onFailure(B_A) onFailure(A_B) onFailure(A_A) onFailure(B) onFailure" +
              "(A)"));
      assertThat(test.getErrorType(), is(ProcessErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test.getException(), instanceOf(IOException.class));
      assertThat(test2.getErrorType(), is(ProcessErrorType.DURING_CHECKS_PROCESSING));
      assertThat(test2.getException(), instanceOf(IOException.class));
      assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    } finally {
      check.release();
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
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {};
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
      public void processFiles(final ProcessExecutionContextTest context,
          final ProcessSession session, final FileHandler fileHandler) throws Exception {
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
    final AbstractFileProcessTest test = new AbstractFileProcessTest("A") {};

    assertThat(testResultFile.exists(), is(false));
    assertThat(readFileToString(testSecondFile), is(testSecondResultContent));
    try {
      executeTest(test, new AbstractFileProcessTest("B") {}, new AbstractFileProcessTest("C") {});
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
      final Thread thread = new Thread(() -> {
        try {
          ProcessProvider.getProcessManagement()
              .execute(new ProcessList<>(processes), new ProcessExecutionContextTest());
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      });
      thread.start();
    } else {
      ProcessProvider.getProcessManagement()
          .execute(new ProcessList<>(processes), new ProcessExecutionContextTest());
    }
  }

  /**
   * Mock
   * @author Yohann Chastagnier
   */
  private abstract class AbstractFileProcessTest
      extends AbstractFileProcess<ProcessExecutionContextTest> {
    private final String id;
    private ProcessErrorType errorType = null;
    private Exception exception = null;

    public AbstractFileProcessTest(final String id) {
      this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see
     * AbstractFileProcess#processFiles(org.silverpeas.process
     * .management.ProcessExecutionContext, ProcessSession,
     * FileHandler)
     */
    @Override
    public void processFiles(final ProcessExecutionContextTest context,
        final ProcessSession session, final FileHandler fileHandler) throws Exception {
      fileHandler.getHandledFile(BASE_PATH_TEST, testResultFile)
          .writeStringToFile(" processFiles" + "(" + id + ")", true);
      fileHandler.getHandledFile(BASE_PATH_TEST, testSecondFile)
          .writeStringToFile(LINE_SEPARATOR + "File check in has been done." + "(" + id + ")",
              true);
    }

    /*
     * (non-Javadoc)
     * @see AbstractProcess#onFailure(org.silverpeas.process.management.
     * ProcessExecutionContext, ProcessSession,
     * ProcessErrorType, java.lang.Exception)
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
     * @see AbstractProcess#onSuccessful(org.silverpeas.process.management.
     * ProcessExecutionContext, ProcessSession)
     */
    @Override
    public void onSuccessful() throws Exception {
      writeStringToFile(testSuccessfulFile, " onSuccessful" + "(" + id + ")", true);
      super.onSuccessful();
    }
  }

  private class ProcessExecutionContextTest extends ProcessExecutionContext {
    public ProcessExecutionContextTest() {
      super(new UserDetail(), "component1");
      getUser().setId("10");
    }
  }
}

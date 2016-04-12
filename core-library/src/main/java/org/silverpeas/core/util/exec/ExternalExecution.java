package org.silverpeas.core.util.exec;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ExternalExecution {

  /**
   * Execute the given external command into the context defined by a default {@link
   * ExternalExecution.Config} (provided by {@link Config#init()}).
   * @param commandLine the external command to execute.
   * @return a {@link List} of console lines written by the external command.
   */
  public static List<String> exec(final CommandLine commandLine) throws ExternalExecutionException {
    return exec(commandLine, Config.init());
  }

  /**
   * Execute the given external command into the context defined by the given {@link
   * ExternalExecution.Config}.
   * @param commandLine the external command to execute.
   * @param config the configuration that permits to perform the execution of the command with
   * some flexibility.
   * @return a {@link List} of console lines written by the external command.
   */
  public static List<String> exec(final CommandLine commandLine, final Config config)
      throws ExternalExecutionException {

    final List<String> result = new LinkedList<>();
    final List<String> errors = new LinkedList<>();
    CollectingLogOutputStream logErrors = new CollectingLogOutputStream(errors);
    final Process process;
    try {
      process = Runtime.getRuntime().exec(commandLine.toStrings());
      final Thread errEater = new Thread(() -> {
        try {
          errors.addAll(IOUtils.readLines(process.getErrorStream()));
        } catch (final IOException e) {
          throw new ExternalExecutionException(e);
        }
      });
      errEater.start();
      final Thread outEater = new Thread(() -> {
        try {
          result.addAll(IOUtils.readLines(process.getInputStream()));
        } catch (final IOException e) {
          throw new ExternalExecutionException(e);
        }
      });
      outEater.start();
      process.waitFor();
      int exitStatus = process.exitValue();
      if (exitStatus != config.getSuccessfulExitStatusValue()) {
        throw new RuntimeException(
            "Exit error status : " + exitStatus + " " + logErrors.getMessage());
      }
    } catch (final IOException | InterruptedException | RuntimeException e) {
      performExternalExecutionException(config, e);
    } finally {
      IOUtils.closeQuietly(logErrors);
    }
    return result;
  }

  private static void performExternalExecutionException(Config config, Exception e) {
    if (config.isDisplayErrorTraceEnabled()) {
      SilverTrace.error("util", "ExternalExecution.exec", "Command execution error", e);
    }
    throw new ExternalExecutionException(e);
  }

  /**
   * This class permits to parametrize the external execution of a command.
   */
  public static class Config {
    private int successfulExitStatusValue = 0;
    private boolean displayErrorTrace = true;

    /**
     * Initializes a config with default values:
     * <ul>
     * <li>the code value of a successful exit status is <b>0</b></li>
     * <li>the exception errors are traced by {@link SilverTrace#error(String, String,
     * String)}</li>
     * </ul>
     * @return an instance of {@link ExternalExecution.Config} initialized with default values.
     */
    public static Config init() {
      return new Config();
    }

    private Config() {
    }

    /**
     * Sets the code value of a successful exit status.
     * @param successfulExitStatusValue an {@link Integer} that represents the code value of a
     * successful exit status.
     * @return the {@link ExternalExecution.Config} instance completed with the given information.
     */
    public Config successfulExitStatusValueIs(final int successfulExitStatusValue) {
      this.successfulExitStatusValue = successfulExitStatusValue;
      return this;
    }

    /**
     * Gets the code value of a successful exit status.
     * @return an integer that represents the code value.
     */
    public int getSuccessfulExitStatusValue() {
      return successfulExitStatusValue;
    }

    /**
     * Calling this method avoids to log errors thrown during the execution of a command into log
     * handlers of the server.<br/>
     * It is useful for a command for which an execution error can be interpreted as a functional
     * information. For example, a command that verifies the existence of an external tool.
     * @return the {@link ExternalExecution.Config} instance completed with the given information.
     */
    public Config doNotDisplayErrorTrace() {
      this.displayErrorTrace = false;
      return this;
    }

    /**
     * Indicates if the Silverpeas error trace must be displayed when an execution error is
     * detected.
     * @return true if errors must be traced, false otherwise.
     */
    public boolean isDisplayErrorTraceEnabled() {
      return displayErrorTrace;
    }
  }
}
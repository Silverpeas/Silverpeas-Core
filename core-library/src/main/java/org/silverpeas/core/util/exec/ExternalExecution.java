/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.util.exec;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class ExternalExecution {

  protected ExternalExecution() {

  }

  /**
   * Execute the given external command into the context defined by a default {@link
   * ExternalExecution.Config} (provided by {@link Config#init()}).
   * @param commandLine the external command to execute.
   * @return a {@link List} of console lines written by the external command.
   */
  public static List<String> exec(final CommandLine commandLine) {
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
  public static List<String> exec(final CommandLine commandLine, final Config config) {

    final List<String> result = new LinkedList<>();
    final List<String> errors = new LinkedList<>();
    final Process process;
    Thread errEater;
    Thread outEater = null;
    try (CollectingLogOutputStream logErrors = new CollectingLogOutputStream(errors)) {
      process = Runtime.getRuntime().exec(commandLine.toStrings());
      errEater = new Thread(() -> {
        try {
          errors.addAll(IOUtils.readLines(process.getErrorStream(), Charset.defaultCharset()));
        } catch (final IOException e) {
          throw new ExternalExecutionException(e);
        }
      });
      errEater.start();
      outEater = new Thread(() -> {
        try {
          result.addAll(IOUtils.readLines(process.getInputStream(), Charset.defaultCharset()));
        } catch (final IOException e) {
          throw new ExternalExecutionException(e);
        }
      });
      outEater.start();
      process.waitFor();
      int exitStatus = process.exitValue();
      if (exitStatus != config.getSuccessfulExitStatusValue()) {
        return stopAll(logErrors, errEater, exitStatus);
      }
    } catch (final IOException | RuntimeException e) {
      performExternalExecutionException(config, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      performExternalExecutionException(config, e);
    }
    try {
      outEater.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return result;
  }

  private static List<String> stopAll(final CollectingLogOutputStream logErrors,
      final Thread errEater, final int exitStatus) {
    try {
      errEater.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    throw new SilverpeasRuntimeException(
        "Exit error status : " + exitStatus + " " + logErrors.getMessage());
  }

  private static void performExternalExecutionException(Config config, Exception e) {
    if (config.isDisplayErrorTraceEnabled()) {
      SilverLogger.getLogger(ExternalExecution.class).error("Command execution error: ", e);
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
     * handlers of the server.<br>
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
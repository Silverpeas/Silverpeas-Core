package org.silverpeas.exec;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.silverpeas.viewer.util.CollectingLogOutputStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ExternalExecution {

  /**
   * Centralizing command execution code
   * @param commandLine
   * @return
   */
  public static List<String> exec(final CommandLine commandLine) throws ExternalExecutionException {
    return exec(commandLine, 0);
  }

  /**
   * Centralizing command execution code
   * @param commandLine
   * @return
   */
  public static List<String> exec(final CommandLine commandLine, final int exitStatusOk)
      throws ExternalExecutionException {
    SilverTrace.info("util", "ExternalExecution.exec", "Command " + commandLine);
    final List<String> result = new LinkedList<String>();
    final List<String> errors = new LinkedList<String>();
    CollectingLogOutputStream logErrors = new CollectingLogOutputStream(errors);
    final Process process;
    try {
      process = Runtime.getRuntime().exec(commandLine.toStrings());
      final Thread errEater = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            errors.addAll(IOUtils.readLines(process.getErrorStream()));
          } catch (final IOException e) {
            throw new ExternalExecutionException(e);
          }
        }
      });
      errEater.start();
      final Thread outEater = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            result.addAll(IOUtils.readLines(process.getInputStream()));
          } catch (final IOException e) {
            throw new ExternalExecutionException(e);
          }
        }
      });
      outEater.start();
      process.waitFor();
      int exitStatus = process.exitValue();
      if (exitStatus != exitStatusOk) {
        throw new RuntimeException(
            "Exit error status : " + exitStatus + " " + logErrors.getMessage());
      }
    } catch (final IOException e) {
      SilverTrace.error("util", "ExternalExecution.exec", "Command execution error", e);
      throw new ExternalExecutionException(e);
    } catch (final InterruptedException e) {
      SilverTrace.error("util", "ExternalExecution.exec", "Command execution error", e);
      throw new ExternalExecutionException(e);
    } catch (final RuntimeException e) {
      SilverTrace.error("util", "ExternalExecution.exec", "Command execution error", e);
      throw new ExternalExecutionException(e);
    } finally {
      IOUtils.closeQuietly(logErrors);
    }
    return result;
  }
}
package org.silverpeas.exec;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.IOUtils;

import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class ExternalExecution {

  /**
   * Centralizing command execution code
   * @param commandLine the apache command line
   * @return
   */
  protected static List<String> exec(final CommandLine commandLine)
      throws ExternalExecutionException {
    SilverTrace.info("util", "ExternalExecution.exec", "Command " + commandLine);
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
      if (exitStatus != 0) {
        throw new RuntimeException("Exit error status : " + exitStatus + " "
            + logErrors.getMessage());
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

  /**
   * Alternative execution command using apache commons exec
   * @param cmdLine the command line
   * @return exit value
   */
  protected static int execAlternative(final CommandLine cmdLine) {
    Executor executor = new DefaultExecutor();
    executor.setExitValue(0);
    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    int exitValue;
    try {
      executor.execute(cmdLine, resultHandler);
      resultHandler.waitFor();
      exitValue = resultHandler.getExitValue();
    } catch (ExecuteException e) {
      SilverTrace.error("util", "FFmpegUtil.exec",
          "Command execution error : " + cmdLine.toString(), e);
      exitValue = e.getExitValue();
      throw new ExternalExecutionException(e);
    } catch (IOException e) {
      SilverTrace.error("util", "FFmpegUtil.exec",
          "Command execution error : " + cmdLine.toString(), e);
      throw new ExternalExecutionException(e);
    } catch (InterruptedException e) {
      SilverTrace.error("util", "FFmpegUtil.exec",
          "Command execution error : " + cmdLine.toString(), e);
      throw new ExternalExecutionException(e);
    }
    return exitValue;
  }
}
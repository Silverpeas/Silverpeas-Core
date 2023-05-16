package org.silverpeas.core.util.logging;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.silverpeas.core.util.logging.SilverLoggerProvider.ROOT_NAMESPACE;

/**
 * Context for the tests on the logging in Silverpeas
 */
public class TestContext {

  public void setLoggerLevel(Level level) {
    final ConsoleHandler handler = new ConsoleHandler();
    setLoggerHandler(handler);
    handler.setFormatter(new SimpleFormatter());
    switch (level) {
      case INFO:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.INFO);
        handler.setLevel(java.util.logging.Level.INFO);
        break;
      case DEBUG:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.FINE);
        handler.setLevel(java.util.logging.Level.FINE);
        break;
      case WARNING:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.WARNING);
        handler.setLevel(java.util.logging.Level.WARNING);
        break;
      case ERROR:
        java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setLevel(java.util.logging.Level.SEVERE);
        handler.setLevel(java.util.logging.Level.SEVERE);
        break;
    }
  }

  private void setLoggerHandler(final Handler handler) {
    java.util.logging.Logger.getLogger(ROOT_NAMESPACE).setUseParentHandlers(false);
    if (Arrays.stream(java.util.logging.Logger.getLogger(ROOT_NAMESPACE).getHandlers())
        .noneMatch(h -> handler.getClass().isInstance(h))) {
      Logger.getLogger(ROOT_NAMESPACE).addHandler(handler);
    }
  }
}

package org.silverpeas.core.test;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.logging.SilverLoggerFactory;

/**
 * @author Yohann Chastagnier
 */
public class TestLoggerFactory implements SilverLoggerFactory {

  @Override
  public SilverLogger getLogger(final String namespace,
      final LoggerConfigurationManager.LoggerConfiguration configuration) {

    return new SilverLogger() {
      @Override
      public String getNamespace() {
        return null;
      }

      @Override
      public Level getLevel() {
        return null;
      }

      @Override
      public void setLevel(final Level level) {

      }

      @Override
      public boolean isLoggable(final Level level) {
        return false;
      }

      @Override
      public void log(final Level level, final String message, final Object[] parameters,
          final Throwable error) {

      }

      @Override
      public void log(final Level level, final String message, final Object... parameters) {

      }
    };
  }
}

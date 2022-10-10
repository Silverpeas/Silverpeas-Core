/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.test.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.util.logging.SilverLoggerProvider;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Stream;

/**
 * This rule permits to get all the logs written during the execution of a test.
 * <p>
 *   It tries to take care of the server logging configuration about formatting. If the formatter
 *   can not be guessed, the {@link SimpleFormatter} is used by default.
 * </p>
 * <p>
 *   To get logs written during a test, just call {@link LoggerReaderRule#getReader()} method
 *   from the rule instance.
 * </p>
 * @author silveryocha
 */
public class LoggerReaderRule implements TestRule {

  private final Level level;
  private Logger rootLogger;
  private StreamHandler handler;
  private ByteArrayOutputStream writer;

  public LoggerReaderRule() {
    this(Level.ALL);
  }

  public LoggerReaderRule(final Level level) {
    this.level = level;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          beforeEvaluate();
          base.evaluate();
        } finally {
          afterEvaluate();
        }
      }
    };
  }

  protected synchronized void beforeEvaluate() {
    rootLogger = Logger.getLogger(SilverLoggerProvider.ROOT_NAMESPACE);
    writer = new ByteArrayOutputStream();
    handler = new StreamHandler(writer, Optional.ofNullable(rootLogger)
        .map(Logger::getParent)
        .map(Logger::getHandlers)
        .stream()
        .flatMap(Stream::of)
        .map(Handler::getFormatter)
        .findFirst()
        .orElse(new SimpleFormatter()));
    handler.setLevel(level);
    if (rootLogger != null) {
      rootLogger.addHandler(handler);
    }
  }

  protected synchronized void afterEvaluate() {
    if (rootLogger != null) {
      rootLogger.removeHandler(handler);
    }
  }

  public Reader getReader() {
    handler.flush();
    return new StringReader(writer.toString());
  }
}

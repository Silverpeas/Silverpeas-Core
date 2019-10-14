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
package org.silverpeas.core.util.logging;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.util.file.ReversedFileLineReader.readLastLines;

/**
 * An accessor to the logs of Silverpeas.
 * @author mmoquillon
 */
@Singleton
public class LogsAccessor {

  /**
   * The system property referring the directory in which are stored all the Silverpeas logs.
   */
  private static final String SILVERPEAS_LOG_DIR = "SILVERPEAS_LOG";

  LogsAccessor() {
  }

  public static LogsAccessor get() {
    return ServiceProvider.getSingleton(LogsAccessor.class);
  }

  /**
   * Gets the name of the logs in used in Silverpeas.
   * @return a set of Silverpeas logs.
   * @throws IOException if an error occurs while accessing the logs.
   */
  public Set<String> getAllLogs() throws IOException {
    String logPath = SystemWrapper.get().getProperty(SILVERPEAS_LOG_DIR);
    try (final Stream<Path> paths = Files.list(Paths.get(logPath))) {
      return paths.filter(
          path -> "log".equalsIgnoreCase(FilenameUtils.getExtension(path.toString())))
          .map(path -> path.getFileName().toString())
          .collect(Collectors.toSet());
    }
  }

  /**
   * Gets the specified last number of records from the specified log.<br>
   * Empty line at end of file is skipped.
   * @param log the log to access.
   * @param recordCount the number of records to get. O or a negative value means all the records.
   * @return an array of the last log records in the log at the time it was accessed.
   * @throws SilverpeasException if either the specified log doesn't exist or an error occurred
   * wile or if the log name contains relative path.
   */
  public List<String> getLastLogRecords(String log, int recordCount)
      throws SilverpeasException {
    try {
      FileUtil.assertPathNotRelative(log);
      String logPath = SystemWrapper.get().getProperty(SILVERPEAS_LOG_DIR);
      Path logFile = Paths.get(logPath, log);
      return readLastLines(logFile, recordCount);
    } catch (RelativeFileAccessException | IOException e) {
      throw new SilverpeasException(e);
    }
  }
}

/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.silverpeas.core.util.file.ReversedFileLineReader.readLastLines;

/**
 * An accessor to the logs of Silverpeas.
 * @author mmoquillon
 */
@Singleton
public class LogsAccessor {

  public static LogsAccessor get() {
    return ServiceProvider.getService(LogsAccessor.class);
  }

  /**
   * The system property referring the directory in which are stored all the Silverpeas logs.
   */
  private static final String SILVERPEAS_LOG_DIR = "SILVERPEAS_LOG";

  protected LogsAccessor() {
  }

  /**
   * Gets the name of the logs in used in Silverpeas.
   * @return a set of Silverpeas logs.
   * @throws IOException if an error occurs while accessing the logs.
   */
  public Set<String> getAllLogs() throws IOException {
    String logPath = SystemWrapper.get().getProperty(SILVERPEAS_LOG_DIR);
    return Files.list(Paths.get(logPath))
        .filter(path -> "log".equalsIgnoreCase(FilenameUtils.getExtension(path.toString())))
        .map(path -> path.getFileName().toString())
        .collect(Collectors.toSet());
  }

  /**
   * Gets the specified last number of records from the specified log.<br/>
   * Empty line at end of file is skipped.
   * @param log the log to access.
   * @param recordCount the number of records to get. O or a negative value means all the records.
   * @return an array of the last log records in the log at the time it was accessed.
   * @throws IOException if either the specified log doesn't exist or an error occurred wile
   * getting the log records.
   */
  public String[] getLastLogRecords(String log, int recordCount) throws IOException {
    String logPath = SystemWrapper.get().getProperty(SILVERPEAS_LOG_DIR);
    Path logFile = Paths.get(logPath, log);
    List<String> records = readLastLines(logFile, recordCount);
    return records.toArray(new String[records.size()]);
  }
}

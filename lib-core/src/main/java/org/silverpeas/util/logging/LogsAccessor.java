/**
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.util.logging;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.lang.SystemWrapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * An accessor to the logs of Silverpeas.
 * @author mmoquillon
 */
@Singleton
public class LogsAccessor {

  public static final LogsAccessor get() {
    return ServiceProvider.getService(LogsAccessor.class);
  }

  /**
   * The system property referring the directory in which are stored all the Silverpeas logs.
   */
  private static final String SILVERPEAS_LOG_DIR = "SILVERPEAS_LOG";

  /**
   * Estimation of the average size in bytes of a record in a log file by taking into account each
   * character is encoded in UTF-8 (1 to 4 bytes; we expects the characters are really encoded
   * in no more that 2 bytes) and that a line is made up of an average of 80 characters.
   */
  private static final long LINE_SIZE = 160;

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
   * Gets the specified last number of records from the specified log.
   * @param log the log to access.
   * @param recordCount the number of records to get. O or a negative value means all the records.
   * @return an array of the last log records in the log at the time it was accessed.
   * @throws IOException if either the specified log doesn't exist or an error occurred wile
   * getting the log records.
   */
  public String[] getLastLogRecords(String log, int recordCount) throws IOException {
    String logPath = SystemWrapper.get().getProperty(SILVERPEAS_LOG_DIR);
    Path logFile = Paths.get(logPath, log);
    long estimatedLineNb = Math.round(Files.size(logFile) / LINE_SIZE) - recordCount;
    List<String> records;
    try (Stream<String> stream = Files.lines(logFile)) {
      if (estimatedLineNb <= 0 || recordCount <= 0) {
        records = stream.collect(Collectors.toList());
      } else {
        LinesRingBuffer buffer = new LinesRingBuffer(recordCount);
        stream.skip(estimatedLineNb).forEach(buffer::put);
        records = buffer.getLines();
      }
    }
    return records.toArray(new String[records.size()]);
  }

  private static class LinesRingBuffer {
    private int count = 0;
    private int threshold;
    private String[] lines;

    public LinesRingBuffer(int capacity) {
      this.threshold = capacity;
      lines = new String[capacity];
    }

    public void put(String line) {
      lines[count++ % threshold] = line;
    }

    public List<String>getLines() {
      List<String> content = IntStream.range(count < threshold ? 0 : count - threshold, count)
          .mapToObj(index -> lines[index % threshold])
          .collect(Collectors.toList());
      return content;
    }
  }
}

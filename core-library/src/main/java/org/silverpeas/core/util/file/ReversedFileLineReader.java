/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.util.file;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * This tool reads a file from its end to its start.
 * @author Yohann Chastagnier
 */
public class ReversedFileLineReader extends ReversedLinesFileReader {

  /*
  By taking into account an average of 160 characters per line, this limit represents a file of
  15Mb.
   */
  static final int FULL_FILE_NB_LINE_LIMIT = 100000;

  private ReversedFileLineReader(final Path path) throws IOException {
    super(path.toFile());
  }

  /**
   * Reads a specified number of last lines of a file represented by a {@link Path} instance.<br>
   * The result will never contains more than {@link #FULL_FILE_NB_LINE_LIMIT} lines.<br>
   * Last lines are sorted from oldest to the most recent.<br>
   * Empty line at end of file is skipped.
   * @param path the path where the file data are located.
   * @param lastLines the number of last lines to read. 0 or negative value to get all the lines of
   * the file. However, the result contains at most {@link #FULL_FILE_NB_LINE_LIMIT} lines in order
   * to avoid memory problems.
   * @return a list containing the last requested lines. The result contains at most {@link
   * #FULL_FILE_NB_LINE_LIMIT} lines.
   * @throws IOException if an I/O error occurs.
   */
  public static List<String> readLastLines(Path path, int lastLines) throws IOException {
    return readLastLines(path, lastLines, true);
  }

  /**
   * Reads a specified number of last lines of a file represented by a {@link Path} instance.<br>
   * The result will never contains more than {@link #FULL_FILE_NB_LINE_LIMIT} lines.<br>
   * Empty line at end of file is skipped.
   * @param path the path where the file data are located.
   * @param lastLines the number of last lines to read. 0 or negative value to get all the lines of
   * the file. However, the result contains at most {@link #FULL_FILE_NB_LINE_LIMIT} lines in order
   * to avoid memory problems.
   * @param sortedFromOldestToMostRecent true to sort the last lines from oldest to most recent,
   * false to sort in the reverse way.
   * @return a list containing the last requested lines. The result contains at most {@link
   * #FULL_FILE_NB_LINE_LIMIT} lines.
   * @throws IOException if an I/O error occurs.
   */
  public static List<String> readLastLines(Path path, int lastLines,
      final boolean sortedFromOldestToMostRecent) throws IOException {
    List<String> lastLinesList = new LinkedList<>();
    int nbLinesRemaining = lastLines;
    if (nbLinesRemaining <= 0 || nbLinesRemaining > FULL_FILE_NB_LINE_LIMIT) {
      nbLinesRemaining = FULL_FILE_NB_LINE_LIMIT;
    }
    try (ReversedFileLineReader reader = new ReversedFileLineReader(path)) {
      while (nbLinesRemaining > 0) {
        String currentLine = reader.readLine();
        if (currentLine == null) {
          break;
        }
        if (sortedFromOldestToMostRecent) {
          lastLinesList.add(0, currentLine);
        } else {
          lastLinesList.add(currentLine);
        }
        nbLinesRemaining--;
      }
    }
    return lastLinesList;
  }
}

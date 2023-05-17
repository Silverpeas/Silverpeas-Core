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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.file;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.UnitTest;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.StringTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
class ReversedFileLineReaderTest {

  @Test
  void readFromNotExistingFile() throws Exception {
    Path notExistingFile = Files.createTempDirectory("toto").resolve("file.log");
    Assertions.assertThrows(IOException.class,
        () -> ReversedFileLineReader.readLastLines(notExistingFile, 10));
  }

  @Test
  void readLinesWithAccentInContent() throws Exception {
    Path fileWithAccentInContent = createFileWithContent("éùï" + System.lineSeparator() + "ô\n\n");
    List<String> lastLines = ReversedFileLineReader.readLastLines(fileWithAccentInContent, 0);
    assertThat(lastLines, contains("éùï", "ô", ""));
  }

  @Test
  void readLinesWithAccentInContentWithEncodingConflict() throws Exception {
    String utf8String = "éùï" + System.lineSeparator() + "ô\n\n";
    String usAsciiString = new String(utf8String.getBytes(Charsets.US_ASCII));
    assertThat(utf8String, not(is(usAsciiString)));
    Path fileWithAccentInContent = createFileWithContent(utf8String, Charsets.US_ASCII);
    List<String> lastLines = ReversedFileLineReader.readLastLines(fileWithAccentInContent, 0);
    StringTokenizer tokenizer = new StringTokenizer(usAsciiString, "\r\n");
    assertThat(lastLines, contains(tokenizer.nextToken(), tokenizer.nextToken(), ""));
  }

  @Test
  void read10LinesFromEmptyFile() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(0);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 10);
    assertThat(lastLines, empty());
  }

  @Test
  void read10LinesFromFileContaining1Line() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(1);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 10);
    assertThat(lastLines, contains("1"));
  }

  @Test
  void readAllLinesFromFileContaining10Lines() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 0);
    assertThat(lastLines, contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
  }

  @Test
  void read5LinesFromFileContaining10LinesFromOldestToMostRecent() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 5, false);
    assertThat(lastLines, contains("10", "9", "8", "7", "6"));
  }

  @Test
  void read5LinesFromFileContaining10LinesFromMostRecentToOldest() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 5);
    assertThat(lastLines, contains("6", "7", "8", "9", "10"));
  }

  @Test
  void readAllLinesFromHugeFileContent() throws Exception {
    Path hugeFile = createFileOfSpecifiedLines(ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT * 2);
    for (int nbLastLines : new int[]{0, -1, (ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT * 2)}) {
      List<String> lastLines = ReversedFileLineReader.readLastLines(hugeFile, nbLastLines);
      assertThat(lastLines, hasSize(ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT));
      assertThat(lastLines.get(0), is("100001"));
      assertThat(lastLines.get(lastLines.size() - 1), is("200000"));
    }
  }

  private Path createFileOfSpecifiedLines(int nbLines) throws Exception {
    Path path = Files.createTempFile("test-" + System.currentTimeMillis(), "txt");
    if (nbLines > 0) {
      try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(path.toFile()))) {
        for (int i = 0; i < nbLines; i++) {
          fileWriter.write(String.valueOf(i + 1));
          fileWriter.write(System.lineSeparator());
        }
      }
    }
    return path;
  }

  private Path createFileWithContent(String content) throws Exception {
    return createFileWithContent(content, Charset.defaultCharset());
  }

  private Path createFileWithContent(String content, Charset charset) throws Exception {
    Path path = Files.createTempFile("test-" + System.currentTimeMillis(), "txt");
    if (StringUtil.isDefined(content)) {
      try (BufferedOutputStream fileWriter = new BufferedOutputStream(
          new FileOutputStream(path.toFile()))) {
        fileWriter.write(content.getBytes(charset));
      }
    }
    return path;
  }
}
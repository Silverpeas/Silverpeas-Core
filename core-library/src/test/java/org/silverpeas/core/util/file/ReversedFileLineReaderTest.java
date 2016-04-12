package org.silverpeas.core.util.file;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.ReversedFileLineReader;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
public class ReversedFileLineReaderTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void readFromNotExistingFile() throws Exception {
    Path notExistingFile = Paths.get(temporaryFolder.newFolder().getPath(), "file.log");

    thrown.expect(IOException.class);
    thrown.expectMessage(startsWith(String.format("%1$s (", notExistingFile.toFile().getPath())));

    ReversedFileLineReader.readLastLines(notExistingFile, 10);
  }

  @Test
  public void readLinesWithAccentInContent() throws Exception {
    Path fileWithAccentInContent = createFileWithContent("éùï" + System.lineSeparator() + "ô\n\n");
    List<String> lastLines = ReversedFileLineReader.readLastLines(fileWithAccentInContent, 0);
    assertThat(lastLines, contains("éùï", "ô", ""));
  }

  @Test
  public void readLinesWithAccentInContentWithEncodingConflict() throws Exception {
    String utf8String = "éùï" + System.lineSeparator() + "ô\n\n";
    String usAsciiString = new String(utf8String.getBytes(Charsets.US_ASCII));
    assertThat(utf8String, not(is(usAsciiString)));
    Path fileWithAccentInContent = createFileWithContent(utf8String, Charsets.US_ASCII);
    List<String> lastLines = ReversedFileLineReader.readLastLines(fileWithAccentInContent, 0);
    StringTokenizer tokenizer = new StringTokenizer(usAsciiString, "\r\n");
    assertThat(lastLines, contains(tokenizer.nextToken(), tokenizer.nextToken(), ""));
  }

  @Test
  public void read10LinesFromEmptyFile() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(0);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 10);
    assertThat(lastLines, empty());
  }

  @Test
  public void read10LinesFromFileContaining1Line() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(1);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 10);
    assertThat(lastLines, contains("1"));
  }

  @Test
  public void readAllLinesFromFileContaining10Lines() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 0);
    assertThat(lastLines, contains("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
  }

  @Test
  public void read5LinesFromFileContaining10LinesFromOldestToMostRecent() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 5, false);
    assertThat(lastLines, contains("10", "9", "8", "7", "6"));
  }

  @Test
  public void read5LinesFromFileContaining10LinesFromMostRecentToOldest() throws Exception {
    Path emptyFile = createFileOfSpecifiedLines(10);
    List<String> lastLines = ReversedFileLineReader.readLastLines(emptyFile, 5);
    assertThat(lastLines, contains("6", "7", "8", "9", "10"));
  }

  @Test
  public void readAllLinesFromHugeFileContent() throws Exception {
    Path hugeFile = createFileOfSpecifiedLines(ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT * 2);
    for (int nbLastLines : new int[]{0, -1, (ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT * 2)}) {
      List<String> lastLines = ReversedFileLineReader.readLastLines(hugeFile, nbLastLines);
      assertThat(lastLines, hasSize(ReversedFileLineReader.FULL_FILE_NB_LINE_LIMIT));
      assertThat(lastLines.get(0), is("100001"));
      assertThat(lastLines.get(lastLines.size() - 1), is("200000"));
    }
  }

  private Path createFileOfSpecifiedLines(int nbLines) throws Exception {
    Path path = Paths.get(temporaryFolder.newFile().toURI());
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
    Path path = Paths.get(temporaryFolder.newFile().toURI());
    if (StringUtil.isDefined(content)) {
      try (BufferedOutputStream fileWriter = new BufferedOutputStream(
          new FileOutputStream(path.toFile()))) {
        fileWriter.write(content.getBytes(charset));
      }
    }
    return path;
  }
}
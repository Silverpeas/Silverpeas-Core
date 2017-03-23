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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.lang.SystemWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the LogAccessor instances.
 * @author mmoquillon
 */
public class LogsAccessorTest {

  private static final String LOG_FILE = "jboss_output.log";
  private static final int LOG_FILE_LINE_COUNT = 1058;

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  private LogsAccessor logsAccessor = new LogsAccessor();

  @Before
  public void initEnvVariables() {
    SystemWrapper.get()
        .setProperty("SILVERPEAS_LOG", mavenTargetDirectory.getResourceTestDirFile().getPath());
  }

  @Test
  public void testListAllLogs() throws IOException {
    Set<String> logs = logsAccessor.getAllLogs();
    assertThat(logs.size(), is(1));
    assertThat(logs.iterator().next(), is(LOG_FILE));
  }

  @Test
  public void readAllLogRecords() throws IOException, RelativeFileAccessException {
    List<String> content = logsAccessor.getLastLogRecords(LOG_FILE, 0);
    assertThat(content, hasSize(LOG_FILE_LINE_COUNT));
    assertThat(content.get(0), is("========================================================================="));
    assertThat(content.get(content.size() - 1), is("\u001B[0m"));

    content = logsAccessor.getLastLogRecords(LOG_FILE, -38);
    assertThat(content, hasSize(LOG_FILE_LINE_COUNT));
    assertThat(content.get(0), is("========================================================================="));
    assertThat(content.get(content.size() - 1), is("\u001B[0m"));
  }

  @Test
  public void readThe1LastLogRecord() throws IOException, RelativeFileAccessException {
    final int COUNT = 1;
    List<String> content = logsAccessor.getLastLogRecords(LOG_FILE, COUNT);
    assertThat(content, hasSize(COUNT));
    assertThat(content.get(0), is("\u001B[0m"));
  }

  @Test
  public void readThe100LastLogRecords() throws IOException, RelativeFileAccessException {
    final int COUNT = 100;
    List<String> content = logsAccessor.getLastLogRecords(LOG_FILE, COUNT);
    assertThat(content, hasSize(COUNT));
    assertThat(content.get(0), is("\tat org.quartz.core.JobRunShell.run(JobRunShell.java:207)"));
    assertThat(content.get(content.size() - 1), is("\u001B[0m"));
  }

  @Test
  public void askForMuchMoreLogRecordsThatThereIsInLog()
      throws IOException, RelativeFileAccessException {
    final int COUNT = 2000;
    List<String> content = logsAccessor.getLastLogRecords(LOG_FILE, COUNT);
    assertThat(content, hasSize(LOG_FILE_LINE_COUNT));
    assertThat(content.get(0), is("========================================================================="));
    assertThat(content.get(content.size()- 1), is("\u001B[0m"));
  }
}

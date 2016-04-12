/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.temp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.LibCoreCommonAPI4Test;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * @author Yohann Chastagnier
 */
public class TestLastModifiedDateFileTask {
  private File tempPath;

  @Rule
  public LibCoreCommonAPI4Test commonAPI4Test = new LibCoreCommonAPI4Test();

  @After
  public void cleanTest() {
    FileUtils.deleteQuietly(new File(getTemporaryPath()));
  }

  @Before
  public void setup() {
    cleanTest();
    tempPath = new File(getTemporaryPath());
    assertThat(LastModifiedDateFileTask.isRunning(), is(false));
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void verifyLastModifiedDate() throws Exception {
    List<File> files = new ArrayList<File>();
    File fileTest = new File(tempPath, "file.txt");
    FileUtils.touch(fileTest);
    files.add(fileTest);

    fileTest = new File(tempPath, "folder");
    fileTest.mkdirs();
    files.add(fileTest);

    fileTest = new File(fileTest, "file1.txt");
    FileUtils.touch(fileTest);
    files.add(fileTest);

    fileTest = new File(fileTest.getParentFile(), "file2.txt");
    FileUtils.touch(fileTest);
    files.add(fileTest);

    fileTest = new File(fileTest.getParentFile(), "otherFolder");
    fileTest.mkdirs();
    files.add(fileTest);

    fileTest = new File(fileTest, "otherFile1.txt");
    FileUtils.touch(fileTest);
    files.add(fileTest);

    fileTest = new File(fileTest.getParentFile(), "otherFile2.txt");
    FileUtils.touch(fileTest);
    files.add(fileTest);

    Thread.sleep(200);
    long oneSecondAfterFileCreation = System.currentTimeMillis();

    List<Pair<File, Long>> fileLastModifiedDate = new ArrayList<Pair<File, Long>>();
    for (File file : files) {
      fileLastModifiedDate.add(Pair.of(file, file.lastModified()));
    }

    for (Pair<File, Long> fileOrFolder : fileLastModifiedDate) {
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          is(fileOrFolder.getValue()));
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          lessThan(oneSecondAfterFileCreation));
    }

    Thread.sleep(1001);
    File[] tempRootFiles = tempPath.listFiles();
    assertThat(tempRootFiles, arrayWithSize(2));
    for (File tempRootFile : tempRootFiles) {
      LastModifiedDateFileTask.addFile(tempRootFile);
    }

    long l = 0;
    while (LastModifiedDateFileTask.isRunning()) {
      l++;
    }
    assertThat("This assertion shows that the thread stops after all the files are performed", l,
        greaterThan(0l));

    Logger.getAnonymousLogger().info(MessageFormat
        .format("Calling LastModifiedDateFileThread.isRunning() {0} times", String.valueOf(l)));

    for (Pair<File, Long> fileOrFolder : fileLastModifiedDate) {
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          greaterThan(fileOrFolder.getValue()));
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          greaterThan(oneSecondAfterFileCreation));
    }
  }
}
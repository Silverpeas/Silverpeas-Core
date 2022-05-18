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
package org.silverpeas.core.io.temp;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.util.file.FileRepositoryManager.getTemporaryPath;

/**
 * @author Yohann Chastagnier
 */
@EnableSilverTestEnv
class TestLastModifiedDateFileTask {
  private Path tempPath;

  @AfterEach
  void cleanTest() {
    FileUtils.deleteQuietly(tempPath.toFile());
  }

  @BeforeEach
  void setup() throws IOException {
    tempPath = Paths.get(getTemporaryPath());
    cleanTest();
    Files.createDirectories(tempPath);
    assertThat(LastModifiedDateFileTask.isRunning(), is(false));
  }

  @Test
  void verifyLastModifiedDate() throws Exception {
    List<File> files = createFilesForTest();
    long currentTime = System.currentTimeMillis();

    List<Pair<File, Long>> fileLastModifiedDate = new ArrayList<>();
    for (File file : files) {
      fileLastModifiedDate.add(Pair.of(file, file.lastModified()));
    }

    for (Pair<File, Long> fileOrFolder : fileLastModifiedDate) {
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          is(fileOrFolder.getValue()));
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          lessThan(currentTime));
    }

    File[] tempRootFiles = tempPath.toFile().listFiles();
    assertThat(tempRootFiles, arrayWithSize(2));
    for (File tempRootFile : tempRootFiles) {
      LastModifiedDateFileTask.addFile(tempRootFile);
    }

    long l = 0;
    while (LastModifiedDateFileTask.isRunning()) {
      l++;
    }
    assertThat("This assertion shows that the thread stops after all the files are performed", l,
        greaterThan(0L));

    Logger.getAnonymousLogger()
        .info(MessageFormat.format("Calling LastModifiedDateFileThread.isRunning() {0} times",
            String.valueOf(l)));

    for (Pair<File, Long> fileOrFolder : fileLastModifiedDate) {
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          greaterThan(fileOrFolder.getValue()));
      assertThat(fileOrFolder.getKey().getName(), fileOrFolder.getKey().lastModified(),
          greaterThan(currentTime));
    }
  }

  @Nonnull
  private List<File> createFilesForTest() throws IOException {
    final List<File> files = new ArrayList<>();
    final Path folder1 = tempPath.resolve("folder");
    final Path folder2 = folder1.resolve("otherFolder");

    files.add(Files.createFile(tempPath.resolve("file.txt")).toFile());
    files.add(Files.createDirectory(folder1).toFile());
    files.add(Files.createFile(folder1.resolve("file1.txt")).toFile());
    files.add(Files.createFile(folder1.resolve("file2.txt")).toFile());
    files.add(Files.createDirectory(folder2).toFile());
    files.add(Files.createFile(folder2.resolve("otherFile1.txt")).toFile());
    File lastCreatedFile = Files.createFile(folder2.resolve("otherFile2.txt")).toFile();
    files.add(lastCreatedFile);

    await().atMost(1, TimeUnit.SECONDS).until(lastCreatedFile::exists);

    return files;
  }
}
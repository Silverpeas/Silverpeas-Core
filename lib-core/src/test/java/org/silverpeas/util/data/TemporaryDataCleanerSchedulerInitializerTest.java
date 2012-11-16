/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.util.data;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.FileUtils.touch;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.scheduler.Scheduler;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * @author Yohann Chastagnier
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-temporarydatacleaner-scheduler.xml")
public class TemporaryDataCleanerSchedulerInitializerTest {

  private final static String rootTempPath = FileRepositoryManager.getTemporaryPath();

  @Inject
  private Scheduler scheduler;

  @BeforeClass
  public static void beforeAll() throws Exception {
    deleteQuietly(new File(FileRepositoryManager.getTemporaryPath()));

    // Prepare files
    for (final String fileName : new String[] { "file.jpg", "rep1/file.jpg", "rep2/file.jpg" }) {
      InputStream inputStream = null;
      FileOutputStream outputStream = null;
      try {
        inputStream =
            TemporaryDataCleanerSchedulerInitializerTest.class.getClassLoader()
                .getResourceAsStream("org/silverpeas/util/data/" + fileName);
        final File outputFile = new File(rootTempPath, fileName);
        outputStream = openOutputStream(outputFile);
        copy(inputStream, outputStream);
      } finally {
        if (inputStream != null) {
          closeQuietly(inputStream);
        }
        if (outputStream != null) {
          closeQuietly(outputStream);
        }
      }
    }
    final File fileIntoRoot = new File(rootTempPath, "file");
    touch(fileIntoRoot);
    write(fileIntoRoot, "toto");
    final File fileIntoNotEmptyDirectory = new File(rootTempPath, "notEmpty/file");
    touch(fileIntoNotEmptyDirectory);
    write(fileIntoNotEmptyDirectory, "titi");
  }

  @After
  public void aftetTest() {
    deleteQuietly(new File(FileRepositoryManager.getTemporaryPath()));
  }

  @Test
  public void test() {
    assertThat(scheduler.isJobScheduled(TemporaryDataCleanerSchedulerInitializer.JOB_NAME),
        is(true));
    final Collection<File> files =
        FileUtils
            .listFilesAndDirs(new File(rootTempPath), TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    assertThat(files.size(), is(1));
  }
}

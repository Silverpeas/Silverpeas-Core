/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.io.temp;

import org.silverpeas.core.scheduler.Scheduler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.test.util.SilverProperties;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class TemporaryDataCleanerSchedulerInitializerTest {

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  private File rootTempFile;

  @Inject
  private Scheduler scheduler;

  @Inject
  private TemporaryDataCleanerSchedulerInitializer initializer;

  @Deployment
  public static Archive<?> createTestArchive() throws IOException {
    beforeAll();
    return WarBuilder4LibCore.onWarForTestClass(TemporaryDataCleanerSchedulerInitializerTest.class)
        .addSilverpeasExceptionBases()
        .addCommonBasicUtilities()
        .addSchedulerFeatures()
        .addFileRepositoryFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.io.temp")
              .addAsResource("org/silverpeas/util/data");
        }).build();
  }

  private static void beforeAll() throws IOException {
    SilverProperties properties = MavenTargetDirectoryRule
        .loadPropertiesForTestClass(TemporaryDataCleanerSchedulerInitializerTest.class);
    properties.load("org/silverpeas/general.properties");
    File rootTempFile = new File(properties.getProperty("tempPath"));

    deleteQuietly(rootTempFile);

    // Prepare files
    for (final String fileName : new String[]{"file.jpg", "rep1/file.jpg", "rep2/file.jpg"}) {
      try (InputStream inputStream = TemporaryDataCleanerSchedulerInitializerTest.class
          .getClassLoader().getResourceAsStream("org/silverpeas/core/io/temp/" + fileName)) {
        FileUtils.copyInputStreamToFile(inputStream, new File(rootTempFile, fileName));
      }
    }
    final File fileIntoRoot = new File(rootTempFile, "file");
    touch(fileIntoRoot);
    write(fileIntoRoot, "toto");
    final File fileIntoNotEmptyDirectory = new File(rootTempFile, "notEmpty/file");
    touch(fileIntoNotEmptyDirectory);
    write(fileIntoNotEmptyDirectory, "titi");
  }

  @Before
  public void before() throws Exception {
    rootTempFile = new File(FileRepositoryManager.getTemporaryPath());
    initializer.init();
  }

  @After
  public void afterTest() {
    deleteQuietly(rootTempFile);
  }

  @Test
  public void test() throws Exception {
    assertThat(scheduler.isJobScheduled(TemporaryDataCleanerSchedulerInitializer.JOB_NAME),
        is(true));
    initializer.startTask.join();
    final Collection<File> files =
        FileUtils.listFilesAndDirs(rootTempFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
    assertThat(files, contains(rootTempFile));
  }
}

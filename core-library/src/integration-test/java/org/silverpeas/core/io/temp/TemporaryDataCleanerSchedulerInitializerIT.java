/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.io.temp;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.io.temp.TemporaryDataCleanerSchedulerInitializer.TemporaryDataCleanerJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerInitializer;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.unit.extention.SettingBundleStub;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.commons.io.filefilter.TrueFileFilter.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.io.temp.TemporaryDataCleanerSchedulerInitializer.JOB_NAME;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class TemporaryDataCleanerSchedulerInitializerIT {

  private long lastLogTime = -1;

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  private File rootTempFile;

  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  private Scheduler scheduler;

  @Inject
  private TemporaryDataCleanerSchedulerInitializer initializer;

  private SettingBundleStub settings;

  @Deployment
  public static Archive<?> createTestArchive() throws IOException {
    return WarBuilder4LibCore.onWarForTestClass(TemporaryDataCleanerSchedulerInitializerIT.class)
        .addSilverpeasExceptionBases()
        .addCommonBasicUtilities()
        .addSchedulerFeatures()
        .addFileRepositoryFeatures()
        .testFocusedOn((warBuilder) -> warBuilder.addPackages(true, "org.silverpeas.core.io.temp")
            .addAsResource("org/silverpeas/util/data")
            .addAsResource("org/silverpeas/core/io/temp/")).build();
  }

  @Before
  public void before() throws Exception {
    settings = new SettingBundleStub(TemporaryDataManagementSetting.class, "settings");
    settings.beforeEach(null);
    settings.put("temporaryData.cleaner.job.start.file.age.hours", "0");
    SchedulerInitializer.get().init(SchedulerInitializer.SchedulerType.VOLATILE);
    rootTempFile = new File(FileRepositoryManager.getTemporaryPath());
    prepareFiles();
  }

  @After
  public void afterTest() throws Exception {
    settings.afterEach(null);
    deleteQuietly(rootTempFile);
  }

  @Test
  public void jobInitializationWithDeletion() throws Exception {
    initializer.init();
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
    waitingForStartTaskProcessing();
    final Collection<File> files = listFilesAndDirs(rootTempFile, TRUE, TRUE);
    assertThat(files, contains(rootTempFile));
  }

  @Test
  public void jobInitializationWithoutDeletion() throws Exception {
    settings.put("temporaryData.cleaner.job.start.file.age.hours", "-1");
    initializer.init();
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
    waitingForStartTaskProcessing();
    final Collection<File> files = listFilesAndDirs(rootTempFile, TRUE, TRUE);
    assertThat(files, hasItem(rootTempFile));
    assertThat(files.size(), greaterThan(1));
  }

  @Test
  public void jobInitializationWithOneHourOffset() throws Exception {
    settings.put("temporaryData.cleaner.job.start.file.age.hours", "1");
    initializer.init();
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
    waitingForStartTaskProcessing();
    final Collection<File> files = listFilesAndDirs(rootTempFile, TRUE, TRUE);
    assertThat(files, hasItem(rootTempFile));
    assertThat(files.size(), greaterThan(5));
  }

  @Test
  public void severalInvocationsAtSameInstant() throws Exception {
    final TemporaryDataCleanerJob job = new TemporaryDataCleanerJob();
    final int nbThreads = 1000;
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicInteger counter = new AtomicInteger(0);
    final List<Thread> threads = new ArrayList<>(nbThreads);
    final List<Future<Void>> results = synchronizedList(new ArrayList<>(nbThreads));
    IntStream.range(0, nbThreads).forEach(i -> threads.add(new Thread(() -> {
      try {
        latch.await();
        results.add(job.startCleanProcess(0));
        final int count = counter.incrementAndGet();
        log(String.format("job execution n°%s", count));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new SilverpeasRuntimeException(e);
      }
    })));

    log("STARTING THREADS...");
    Collections.shuffle(threads);
    for (Thread thread : threads) {
      thread.start();
    }
    log(threads.size() + " THREADS STARTED");
    log("WAITING 1s...");
    awaitUntil(1, SECONDS);
    latch.countDown();

    log("WAITING ENDING OF THREADS...");
    for (Thread thread : threads) {
      thread.join(60000);
    }
    log(threads.size() + " THREADS STOPPED");
    assertThat(counter.get(), is(nbThreads));
    assertThat(results.size(), is(nbThreads));
    final List<Future<Void>> distinctResults = results.stream().distinct().collect(toList());
    assertThat(distinctResults.size(), lessThan((int) (nbThreads / 1.5)));
    final List<Future<Void>> cancelledResults = distinctResults.stream()
        .filter(Future::isCancelled)
        .collect(toList());
    // 10 is arbitrary, it is just to mean several and not only one
    assertThat(cancelledResults.size(), greaterThan(10));
    final Collection<File> files = listFilesAndDirs(rootTempFile, TRUE, TRUE);
    assertThat(files, contains(rootTempFile));
  }

  private void waitingForStartTaskProcessing()
      throws InterruptedException, ExecutionException, TimeoutException {
    if(initializer.getStartTask().isPresent()) {
      initializer.getStartTask().get().get(10L, TimeUnit.SECONDS);
    }
  }

  private void prepareFiles() throws IOException {
    deleteQuietly(rootTempFile);
    // Prepare files
    for (final String fileName : new String[]{"file.jpg", "rep1/file.jpg", "rep2/file.jpg"}) {
      try (InputStream inputStream = TemporaryDataCleanerSchedulerInitializerIT.class
          .getClassLoader().getResourceAsStream("org/silverpeas/core/io/temp/" + fileName)) {
        assertThat(inputStream, is(notNullValue()));
        FileUtils.copyInputStreamToFile(inputStream, new File(rootTempFile, fileName));
      }
    }
    Stream.of(
        Pair.of("file", "toto"),
        Pair.of("notEmpty/file", "titi"),
        Pair.of("notEmpty/subSubDir/file", "tata")).forEach(p -> {
      final File file = new File(rootTempFile, p.getFirst());
      try {
        touch(file);
        write(file, p.getSecond(), Charsets.UTF_8);
      } catch (IOException e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  private void log(String message) {
    long currentTime = System.currentTimeMillis();
    if (lastLogTime < 0) {
      lastLogTime = currentTime;
    }
    System.out.println(
        StringUtil.leftPad(String.valueOf(currentTime - lastLogTime), 6, " ") + " ms -> " +
            message);
  }
}

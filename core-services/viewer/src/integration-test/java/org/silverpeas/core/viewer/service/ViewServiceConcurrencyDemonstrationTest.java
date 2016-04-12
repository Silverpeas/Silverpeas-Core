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
package org.silverpeas.core.viewer.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.model.ViewerSettings;
import org.silverpeas.core.test.rule.MockByReflectionRule;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class ViewServiceConcurrencyDemonstrationTest extends AbstractViewerTest {

  @Rule
  public MockByReflectionRule reflectionRule = new MockByReflectionRule();

  @Inject
  private ViewService viewService;

  @Before
  public void setup() throws Exception {
    FileUtils.deleteQuietly(getTemporaryPath());
    getTemporaryPath().mkdirs();
    final SettingBundle mockedSettings =
        reflectionRule.mockField(ViewerSettings.class, SettingBundle.class, "settings");
    when(mockedSettings.getInteger(eq("preview.width.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getInteger(eq("preview.height.max"), anyInt())).thenReturn(1000);
    when(mockedSettings.getBoolean(eq("viewer.cache.enabled"), anyBoolean())).thenReturn(true);
    when(mockedSettings.getBoolean(eq("viewer.cache.conversion.silent.enabled"), anyBoolean()))
        .thenReturn(false);
    when(mockedSettings.getBoolean(eq("viewer.conversion.strategy.split.enabled"), anyBoolean()))
        .thenReturn(false);
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteQuietly(getTemporaryPath());
  }

  @Test
  public void demonstrateConcurrencyManagement() throws Exception {
    if (canPerformViewConversionTest()) {
      final List<Throwable> throwables = new ArrayList<>();

      final ConcurrentMap serviceCache =
          (ConcurrentMap) FieldUtils.readField(viewService, "cache", true);
      assertThat(serviceCache.size(), is(0));

      final long startTime = System.currentTimeMillis();
      final int NB_VIEW_CALLS = 100;
      final int LAST_REQUEST_INDEX = NB_VIEW_CALLS - 1;
      final long durationToIncreaseChancesToPerformThreadBefore = 50;

      final long[] durationTimes = new long[NB_VIEW_CALLS];
      final long[] endTimes = new long[NB_VIEW_CALLS];
      final DocumentView[] results = new DocumentView[NB_VIEW_CALLS];

      for (int i = 0; i < LAST_REQUEST_INDEX; i++) {
        final int index = i;
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              final long startThreadTime = System.currentTimeMillis();
              final DocumentView viewFirstRequest = viewService
                  .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
              final long endThreadTime = System.currentTimeMillis();
              durationTimes[index] = endThreadTime - startThreadTime;
              endTimes[index] = endThreadTime;
              results[index] = viewFirstRequest;
            } catch (Exception e) {
              throwables.add(e);
              SubThreadManager.killAll();
            }
          }
        }));
      }

      SubThreadManager.addAndStart(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(500);

            // Technical verification
            assertThat("At this level, the cache has 2 elements", serviceCache.size(), is(2));
            assertThat(getTemporaryPath().listFiles(), arrayWithSize(2));

          } catch (Throwable e) {
            throwables.add(e);
            SubThreadManager.killAll();
          }
        }
      }));

      Thread.sleep(durationToIncreaseChancesToPerformThreadBefore);

      final long startLastRequestTime = System.currentTimeMillis();
      final DocumentView viewLastRequest =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
      final long endLastRequestTime = System.currentTimeMillis();
      durationTimes[LAST_REQUEST_INDEX] = endLastRequestTime - startLastRequestTime;
      endTimes[LAST_REQUEST_INDEX] = endLastRequestTime;
      results[LAST_REQUEST_INDEX] = viewLastRequest;

      // Waiting for all thread ends
      SubThreadManager.joinAll(60000);

      for (Throwable throwable : throwables) {
        throw new RuntimeException(throwable);
      }

      for (long endTime : endTimes) {
        assertThat(endTime, greaterThan(startTime));
      }

      long minEndTime = Long.MAX_VALUE;
      long maxEndTime = 0;
      for (long endTime : endTimes) {
        assertThat(endTime, greaterThan(startTime));
        minEndTime = Math.min(minEndTime, endTime);
        maxEndTime = Math.max(maxEndTime, endTime);
      }
      assertThat((maxEndTime - minEndTime), lessThan(250l));
      for (DocumentView documentView : results) {
        assertThat(documentView, notNullValue());
      }

      for (int i = 0; i < LAST_REQUEST_INDEX; i++) {
        assertThat(results[i].getPhysicalFile(), is(viewLastRequest.getPhysicalFile()));
        assertThat(results[i].getNbPages(), is(viewLastRequest.getNbPages()));
        assertThat(results[i].getWidth(), is(viewLastRequest.getWidth()));
        assertThat(results[i].getHeight(), is(viewLastRequest.getHeight()));
        assertThat(results[i].getDisplayLicenseKey(), is(viewLastRequest.getDisplayLicenseKey()));
        assertThat(results[i].getOriginalFileName(), is(viewLastRequest.getOriginalFileName()));
        assertThat(results[i].getURLAsString(), is(viewLastRequest.getURLAsString()));
        assertThat(results[i].isDocumentSplit(), is(viewLastRequest.isDocumentSplit()));
        assertThat(results[i].areSearchDataComputed(), is(viewLastRequest.areSearchDataComputed()));
      }

      assertThat(serviceCache.size(), is(0));
      assertThat(getTemporaryPath().listFiles(), arrayWithSize(2));
    }
  }

  @Test
  public void demonstrateConcurrencyManagementWithTwoDifferentConversionsAtSameTime()
      throws Exception {
    if (canPerformViewConversionTest()) {
      final List<Throwable> throwables = new ArrayList<>();

      final ConcurrentMap serviceCache =
          (ConcurrentMap) FieldUtils.readField(viewService, "cache", true);
      assertThat(serviceCache.size(), is(0));

      final int NB_VIEW_CALLS = 100;

      for (int i = 0; i < NB_VIEW_CALLS; i++) {
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
            } catch (Exception ignore) {
            }
          }
        }));
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
            } catch (Exception ignore) {
            }
          }
        }));
      }

      SubThreadManager.addAndStart(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(500);

            // Technical verification
            assertThat(getTemporaryPath().listFiles(), arrayWithSize(4));

          } catch (Throwable e) {
            throwables.add(e);
            SubThreadManager.killAll();
          }
        }
      }));

      // Waiting for all thread ends
      SubThreadManager.joinAll(60000);

      for (Throwable throwable : throwables) {
        throw new RuntimeException(throwable);
      }

      assertThat(serviceCache.size(), is(0));
      assertThat(getTemporaryPath().listFiles(), arrayWithSize(4));
    }
  }

  @Test
  public void demonstrateConcurrencyManagementWithThreeDifferentConversionsAtSameTime()
      throws Exception {
    if (canPerformViewConversionTest()) {
      final List<Throwable> throwables = new ArrayList<>();

      final ConcurrentMap serviceCache =
          (ConcurrentMap) FieldUtils.readField(viewService, "cache", true);
      assertThat(serviceCache.size(), is(0));

      final int NB_VIEW_CALLS = 100;

      for (int i = 0; i < NB_VIEW_CALLS; i++) {
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
            } catch (Exception ignore) {
            }
          }
        }));
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
            } catch (Exception ignore) {
            }
          }
        }));
        SubThreadManager.addAndStart(new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
            } catch (Exception ignore) {
            }
          }
        }));
      }

      SubThreadManager.addAndStart(new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(500);

            // Technical verification
            assertThat(getTemporaryPath().listFiles(), arrayWithSize(6));

          } catch (Throwable e) {
            throwables.add(e);
            SubThreadManager.killAll();
          }
        }
      }));

      // Waiting for all thread ends
      SubThreadManager.joinAll(60000);

      for (Throwable throwable : throwables) {
        throw new RuntimeException(throwable);
      }

      assertThat(serviceCache.size(), is(0));
      assertThat(getTemporaryPath().listFiles(), arrayWithSize(6));
    }
  }

  private static class SubThreadManager {
    private static List<Thread> threads = new ArrayList<>();

    public static void add(Thread thread) {
      threads.add(thread);
    }

    public static void addAndStart(Thread thread) {
      threads.add(thread);
      thread.start();
    }

    public static void joinAll() throws Exception {
      for (Thread thread : threads) {
        thread.join();
      }
    }

    public static void joinAll(long timeout) throws Exception {
      for (Thread thread : threads) {
        thread.join(timeout);
      }
    }

    public static void killAll() {
      for (Thread thread : threads) {
        if (thread.isAlive()) {
          try {
            thread.interrupt();
          } catch (Throwable ignore) {
          }
        }
      }
    }
  }
}

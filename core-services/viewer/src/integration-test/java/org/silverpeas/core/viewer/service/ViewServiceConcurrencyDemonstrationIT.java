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
package org.silverpeas.core.viewer.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.core.viewer.model.DocumentView;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

public class ViewServiceConcurrencyDemonstrationIT extends AbstractViewerIT {

  @Inject
  private ViewService viewService;

  @Resource
  private ManagedThreadFactory managedThreadFactory;

  @Before
  public void setup() {
    clearTemporaryPath();
    boolean isOk = getTemporaryPath().mkdirs();
    assertThat(isOk, is(true));
  }

  @After
  public void tearDown() {
    clearTemporaryPath();
  }

  @SuppressWarnings("rawtypes")
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

      final long[] endTimes = new long[NB_VIEW_CALLS];
      final DocumentView[] results = new DocumentView[NB_VIEW_CALLS];

      for (int i = 0; i < LAST_REQUEST_INDEX; i++) {
        final int index = i;
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            final DocumentView viewFirstRequest = viewService
                .getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
            final long endThreadTime = System.currentTimeMillis();
            endTimes[index] = endThreadTime;
            results[index] = viewFirstRequest;
          } catch (Exception e) {
            throwables.add(e);
            SubThreadManager.killAll();
          }
        }));
      }

      SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
        try {
          awaitUntil(500, MILLISECONDS);

          // Technical verification
          assertThat("At this level, the cache has 2 elements", serviceCache.size(), is(2));
          assertThat(getTemporaryPath().listFiles(), arrayWithSize(2));

        } catch (Throwable e) {
          throwables.add(e);
          SubThreadManager.killAll();
        }
      }));

      awaitUntil(durationToIncreaseChancesToPerformThreadBefore, MILLISECONDS);

      final DocumentView viewLastRequest =
          viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.ppt")));
      final long endLastRequestTime = System.currentTimeMillis();
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

  @SuppressWarnings("rawtypes")
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
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
          } catch (Exception ignore) {
          }
        }));
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
          } catch (Exception ignore) {
          }
        }));
      }

      SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
        try {
          awaitUntil(500, MILLISECONDS);

          // Technical verification
          assertThat(getTemporaryPath().listFiles(), arrayWithSize(4));

        } catch (Throwable e) {
          throwables.add(e);
          SubThreadManager.killAll();
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

  @SuppressWarnings("rawtypes")
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
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odp")));
          } catch (Exception ignore) {
          }
        }));
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.pdf")));
          } catch (Exception ignore) {
          }
        }));
        SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
          try {
            viewService.getDocumentView(ViewerContext.from(getSimpleDocumentNamed("file.odt")));
          } catch (Exception ignore) {
          }
        }));
      }

      SubThreadManager.addAndStart(managedThreadFactory.newThread(() -> {
        try {
          awaitUntil(500, MILLISECONDS);

          // Technical verification
          assertThat(getTemporaryPath().listFiles(), arrayWithSize(6));

        } catch (Throwable e) {
          throwables.add(e);
          SubThreadManager.killAll();
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
    private static final List<Thread> threads = new ArrayList<>();

    public static void addAndStart(Thread thread) {
      threads.add(thread);
      thread.start();
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

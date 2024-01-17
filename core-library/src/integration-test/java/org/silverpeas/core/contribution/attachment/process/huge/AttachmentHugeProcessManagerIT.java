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

package org.silverpeas.core.contribution.attachment.process.huge;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.annotation.SourceObject;
import org.silverpeas.core.util.annotation.SourcePK;
import org.silverpeas.core.util.annotation.TargetPK;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.silverpeas.core.contribution.attachment.process.huge.AttachmentHugeProcessManagerIT.AttachmentServiceImpl4Test.getService;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

/**
 * @author silveryocha
 */
@RunWith(Arquillian.class)
public class AttachmentHugeProcessManagerIT {

  private final static AppSemaphore APP_A = new AppSemaphore("A");
  private final static ResourceReference PK_B = new ResourceReference("idB", "B");
  private final static ResourceReference REF_C = new ResourceReference("idC", "C");
  private final static AppSemaphore APP_D = new AppSemaphore("D");
  private static final int TIMEOUT_MS = 60000;

  @Inject
  private AttachmentHugeProcessManager manager;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AttachmentHugeProcessManagerIT.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addPackages(true, "org.silverpeas.core.contribution.attachment")
        .build();
  }

  @Before
  public void setup() throws Exception {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(false));
  }

  @DisplayName("Verifying all kind of managed sources or targets")
  @Test
  public void verifyingAllSources() throws InterruptedException {
    APP_A.acquire();
    var thread = new ManagedThread(
        () -> getService().doHugeTreatmentWithAllKindOfSourcesOrTargets(APP_A, PK_B, REF_C));
    try {
      thread.start();
      waitFor1Second();
      assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(true));
      assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(true));
      assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(true));
      assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(false));
    } finally {
      APP_A.release();
    }
    waitFor1Second();
    thread.end();
  }

  @DisplayName("Checking if huge treatment is running while one is effectively processing")
  @Test
  public void verifyingHugeTreatmentOnOneInstance() throws InterruptedException {
    APP_A.acquire();
    final var thread = new ManagedThread(() -> getService().doHugeTreatment(APP_A));
    try {
      thread.start();
      waitFor1Second();
      assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(true));
      assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(false));
      assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(false));
      assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(false));
    } finally {
      APP_A.release();
    }
    waitFor1Second();
    thread.end();
  }

  @DisplayName("In concurrency context, a huge treatment has performed and then verifying if a " +
      "huge treatment is currently performing")
  @Test
  public void callHugeTreatmentWithConcurrency() throws InterruptedException {
    APP_A.acquire();
    final var thread = new ManagedThread(() -> getService().doHugeTreatment(APP_A));
    try {
      thread.start();
      waitFor1Second();
    } finally {
      APP_A.release();
    }
    waitFor1Second();
    assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(false));
    thread.end();
  }

  @DisplayName("In a same thread, a huge treatment has performed and then verifying if a huge " +
      "treatment is currently performing")
  @Test
  public void callHugeTreatmentButNoConcurrency() {
    getService().doHugeTreatment(APP_A);
    assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(false));
    assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(false));
  }

  @DisplayName("In a same thread, calling several huge treatments on same instance is not OK")
  @Test
  public void callSeveralHugeTreatmentsInSameThreadForSameInstance() {
    try {
      getService().doHugeTreatment(APP_A, () -> {
        APP_A.release();
        try {
          APP_A.acquire();
          getService().doHugeTreatment(APP_A);
          fail("Expected an AttachmentException to be thrown");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      });
    } catch (AttachmentException e) {
      assertThat(e.getMessage(),
          is("Huge process over attachments of instance A is currently running."));
    }
  }

  @DisplayName("In a same thread, calling several huge treatments on different instances is OK")
  @Test
  public void callSeveralHugeTreatmentsInSameThreadForDifferentInstances() {
    getService().doHugeTreatment(APP_A, () -> {
      APP_A.release();
      try {
        APP_A.acquire();
        getService().doHugeTreatment(APP_D, () -> {
          assertThat(manager.isOneRunningOnInstance(APP_A.getInstanceId()), is(true));
          assertThat(manager.isOneRunningOnInstance(PK_B.getInstanceId()), is(false));
          assertThat(manager.isOneRunningOnInstance(REF_C.getInstanceId()), is(false));
          assertThat(manager.isOneRunningOnInstance(APP_D.getInstanceId()), is(true));
        });
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new SilverpeasRuntimeException(e);
      }
    });
  }


  private static void waitFor1Second() {
    awaitUntil(1, SECONDS);
  }

  public static class AppSemaphore extends Semaphore {
    private static final long serialVersionUID = 1203945621575149453L;

    private final String instanceId;

    public AppSemaphore(final String instanceId) {
      super(1);
      this.instanceId = instanceId;
    }

    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public String toString() {
      return instanceId;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final AppSemaphore that = (AppSemaphore) o;
      return instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(instanceId);
    }
  }

  public static class ManagedThread {
    private final Runnable[] runnables;
    private List<Thread> threads;

    public ManagedThread(final Runnable... runnables) {
      this.runnables = runnables;
    }

    void start() {
      threads = ManagedThreadPool.getPool().invoke(runnables);
    }

    void end() {
      for (var thread : threads) {
        try {
          thread.join(TIMEOUT_MS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SilverpeasRuntimeException(e);
        }
      }
    }
  }

  /**
   * @author silveryocha
   */
  @Service
  public static class AttachmentServiceImpl4Test {

    public static AttachmentServiceImpl4Test getService() {
      return ServiceProvider.getService(AttachmentServiceImpl4Test.class);
    }

    @SuppressWarnings("unused")
    @AttachmentHugeProcess
    public void doHugeTreatmentWithAllKindOfSourcesOrTargets(
        @SourceObject AppSemaphore app, @SourcePK ResourceReference pk,
        @TargetPK ResourceReference ref) {
      doHugeTreatment(app);
    }

    @AttachmentHugeProcess
    public void doHugeTreatment(@SourceObject AppSemaphore app) {
      doHugeTreatment(app, null);
    }

    @AttachmentHugeProcess
    public void doHugeTreatment(@SourceObject AppSemaphore app, Runnable runnable) {
      try {
        app.acquire();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new SilverpeasRuntimeException(e);
      }
      try {
        if (runnable != null) {
          runnable.run();
        }
      } finally {
        app.release();
      }
    }
  }
}
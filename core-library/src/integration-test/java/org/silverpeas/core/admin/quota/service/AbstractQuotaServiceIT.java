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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.quota.service;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.hamcrest.Matchers;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaFullException;
import org.silverpeas.core.admin.quota.exception.QuotaNotEnoughException;
import org.silverpeas.core.admin.quota.exception.QuotaOutOfBoundsException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.admin.quota.offset.SimpleQuotaCountingOffset;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class AbstractQuotaServiceIT {

  private final TestQuotaKey dummyKey = new TestQuotaKey("dummy");
  private final TestQuotaKey existingKey = new TestQuotaKey("38");
  private final TestQuotaKey newKey = new TestQuotaKey("26");

  /**
   * This integration test is one of the firsts that have been done. This dummy service exists here
   * to verify the right behaviour of CDI injection according to generic type specifying.
   */
  @Inject
  private QuotaService<TestQuotaKey> quotaServiceForInjectionVerification;
  @Inject
  private QuotaService<TestDummyQuotaKey> dummyQuotaServiceForInjectionVerification;

  @Inject
  private TestQuotaServiceWithAdditionalTools quotaService;

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/admin/quota/service/create_table.sql";
  public static final Operation QUOTA_SET_UP = Operations.insertInto("st_quota")
      .columns("id", "quotaType", "resourceId", "minCount", "maxCount", "currentCount", "saveDate")
      .values(4L, "TYPE_THAT_NO_EXISTS", "38", 0L, 100L, 10L, "2012-07-19 00:00:00.0")
      .values(24L, "USERS_IN_DOMAIN", "38", 10L, 500L, 23L, "2012-07-19 00:00:00.0").build();

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLES_CREATION).loadInitialDataSetFrom(QUOTA_SET_UP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AbstractQuotaServiceIT.class)
        .addSilverpeasExceptionBases()
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .testFocusedOn(
            (warBuilder) -> warBuilder.addPackages(true, "org.silverpeas.core.admin.quota"))
        .build();
  }

  @Before
  public void setUp() throws Exception {
    assertThat(quotaServiceForInjectionVerification,
        not(sameInstance(dummyQuotaServiceForInjectionVerification)));
    assertThat(quotaServiceForInjectionVerification,
        instanceOf(TestQuotaServiceWithAdditionalTools.class));
    assertThat(dummyQuotaServiceForInjectionVerification,
        instanceOf(TestDummyQuotaServiceWithAdditionalTools.class));
    assertThat(quotaService, sameInstance(quotaServiceForInjectionVerification));
    quotaService.setCount(100);
  }

  private void waitForTenMillis() {
    long start = System.currentTimeMillis();
    await().atLeast(10, TimeUnit.MILLISECONDS).untilTrue(new AtomicBoolean(true));
    Logger.getLogger(AbstractQuotaServiceIT.class.getName())
        .log(Level.INFO, "waitForTenMillis -> {0}",
            new Object[]{System.currentTimeMillis() - start});
  }

  @Test
  public void testGet() throws QuotaException {
    Date date = new Date();
    assertThat(quotaService.get(dummyKey), notNullValue());
    assertThat(quotaService.get(dummyKey).exists(), is(false));
    waitForTenMillis();
    final Quota quota = quotaService.get(existingKey);
    assertThat(quota, notNullValue());
    assertThat(quota.getCount(), is(100L));
    assertThat(quota.getSaveDate().getTime(), greaterThan(date.getTime()));
    date = quota.getSaveDate();
    waitForTenMillis();
    final Quota quotaNotChanged = quotaService.get(existingKey);
    assertThat(quotaNotChanged, notNullValue());
    assertThat(quotaNotChanged, not(sameInstance(quota)));
    assertThat(quotaNotChanged.getCount(), is(100L));
    assertThat(quotaNotChanged.getSaveDate().getTime(), Matchers.equalTo(date.getTime()));
    // Becoming unlimited
    quota.setMinCount(0);
    quota.setMaxCount(0);
    waitForTenMillis();
    date = quotaService.initialize(existingKey, quota).getSaveDate();
    assertThat(quota.getCount(), is(100L));
    waitForTenMillis();
    final Quota quotaChanged = quotaService.get(existingKey);
    assertThat(quotaChanged, notNullValue());
    assertThat(quotaChanged, not(sameInstance(quota)));
    assertThat(quotaChanged.getCount(), is(0L));
    assertThat(quotaChanged.getSaveDate().getTime(), greaterThan(date.getTime()));
    date = quotaChanged.getSaveDate();
    waitForTenMillis();
    assertThat(quotaService.get(existingKey).getSaveDate().getTime(), Matchers.equalTo(date.getTime()));
  }

  @Test
  public void testInitializeNotValid() throws Exception {
    assertThat(quotaService.initialize(newKey, -1, 0).exists(), is(false));
    assertInitializeException(newKey, 0, -1);
    assertInitializeException(newKey, 2, 1);
  }

  private <T extends SilverpeasException> void assertInitializeException(
      final TestQuotaKey quotaKey, final int minCount, final int maxCount) {
    boolean isException = false;
    try {
      quotaService.initialize(quotaKey, minCount, maxCount);
    } catch (final QuotaException qe) {
      isException = true;
    }
    assertThat(isException, is(true));
  }

  @Test
  public void testInitializeMaxCountAlreadyExists() {
    try {
      final Date date = new Date();
      final Quota existingQuota = quotaService.get(existingKey);
      assertThat(existingQuota, notNullValue());
      assertThat(existingQuota.getId(), is("24"));
      assertThat(existingQuota.getMaxCount(), is(500L));
      quotaService.initialize(existingKey, 690);
      final Quota quota = quotaService.get(existingKey);
      assertThat(quota, notNullValue());
      assertThat(quota.getId(), is("24"));
      assertThat(quota.getType(), is(existingKey.getQuotaType()));
      assertThat(quota.getResourceId(), is(existingKey.getResourceId()));
      assertThat(quota.getMinCount(), is(0L));
      assertThat(quota.getMaxCount(), is(690L));
      assertThat(quota.getCount(), is(100L));
      assertThat(quota.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
    } catch (final QuotaException qe) {
      assertThat("No quota exception should have been thrown", false);
    }
  }

  @Test
  public void testInitializeMaxCount() {
    try {
      final Date date = new Date();
      assertThat(quotaService.get(newKey), notNullValue());
      assertThat(quotaService.get(newKey).exists(), is(false));
      quotaService.initialize(newKey, 260);
      final Quota quota = quotaService.get(newKey);
      assertThat(quota, notNullValue());
      assertThat(quota.getId(), is("25"));
      assertThat(quota.getType(), is(newKey.getQuotaType()));
      assertThat(quota.getResourceId(), is(newKey.getResourceId()));
      assertThat(quota.getMinCount(), is(0L));
      assertThat(quota.getMaxCount(), is(260L));
      assertThat(quota.getCount(), is(100L));
      assertThat(quota.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
    } catch (final QuotaException qe) {
      assertThat("No quota exception should have been thrown", false);
    }
  }

  @Test
  public void testInitializeMinCountMaxCount() {
    try {
      final Date date = new Date();
      assertThat(quotaService.get(newKey), notNullValue());
      assertThat(quotaService.get(newKey).exists(), is(false));
      quotaService.initialize(newKey, 100, 380);
      final Quota quota = quotaService.get(newKey);
      assertThat(quota, notNullValue());
      assertThat(quota.getId(), is("25"));
      assertThat(quota.getType(), is(newKey.getQuotaType()));
      assertThat(quota.getResourceId(), is(newKey.getResourceId()));
      assertThat(quota.getMinCount(), is(100L));
      assertThat(quota.getMaxCount(), is(380L));
      assertThat(quota.getCount(), is(100L));
      assertThat(quota.getSaveDate().getTime(), greaterThanOrEqualTo(date.getTime()));
    } catch (final QuotaException qe) {
      assertThat("No quota exception should have been thrown", false);
    }
  }

  @Test
  public void testVerify() throws QuotaException {
    quotaService.setCount(23);
    Quota notExistingQuota = quotaService.verify(newKey);
    assertThat(notExistingQuota, notNullValue());
    assertThat(notExistingQuota.exists(), is(false));
    final Quota existingQuota = quotaService.get(existingKey);
    quotaService.setCount(100);
    final Quota verifiedExistingQuota = quotaService.verify(existingKey);
    assertThat(verifiedExistingQuota, notNullValue());
    assertThat(verifiedExistingQuota, not(sameInstance(existingQuota)));
    assertThat(verifiedExistingQuota.exists(), is(true));
    assertThat(existingQuota.getCount(), is(23L));
    assertThat(verifiedExistingQuota.getCount(), is(100L));
    // Verifying exceptions (loading the quota count when verifying)
    assertVerifyException(QuotaOutOfBoundsException.class, existingKey, 1000);
    assertVerifyException(QuotaNotEnoughException.class, existingKey, 1);
    assertVerifyException(QuotaFullException.class, existingKey, 500);
    Quota verifiedQuota;
    // Verifying that the quota count is not again computed when giving directly a loaded quota
    quotaService.setCount(10000);
    verifiedQuota = quotaService.verify(existingKey, existingQuota);
    assertThat(verifiedQuota, notNullValue());
    assertThat(verifiedQuota, not(sameInstance(existingQuota)));
    assertThat(verifiedQuota.getCount(), is(existingQuota.getCount()));
    assertThat(verifiedQuota.getCount(), is(23L));
    // Verifying offset (loading the quota count when verifying)
    final SimpleQuotaCountingOffset offset = SimpleQuotaCountingOffset.from(10L);
    quotaService.setCount(50);
    verifiedQuota = quotaService.verify(existingKey, offset);
    assertThat(verifiedQuota, notNullValue());
    assertThat(verifiedQuota, not(sameInstance(existingQuota)));
    assertThat(verifiedQuota.getCount(), not(is(existingQuota.getCount())));
    assertThat(verifiedQuota.getCount(), is(50L + 10L));
    // Verifying offset (quota count is not again performed when giving directly a loaded quota)
    quotaService.setCount(50);
    verifiedQuota = quotaService.verify(existingKey, existingQuota, offset);
    assertThat(verifiedQuota, notNullValue());
    assertThat(verifiedQuota, not(sameInstance(existingQuota)));
    assertThat(verifiedQuota.getCount(), not(is(existingQuota.getCount())));
    assertThat(verifiedQuota.getCount(), is(23L + 10L));

  }

  private <T extends SilverpeasException> void assertVerifyException(final Class<T> exceptionClass,
      final TestQuotaKey quotaKey, final int count) {
    boolean isException = false;
    try {
      quotaService.setCount(count);
      quotaService.verify(quotaKey);
    } catch (final QuotaException qe) {
      isException = true;
      assertThat(true, is(qe.getClass().isAssignableFrom(exceptionClass)));
    }
    assertThat(isException, is(true));
  }

  @Test
  public void testRemove() throws QuotaException {
    Quota quota = quotaService.get(existingKey);
    assertThat(quota, notNullValue());
    assertThat(quota.exists(), is(true));
    quotaService.remove(existingKey);
    quota = quotaService.get(existingKey);
    assertThat(quota, notNullValue());
    assertThat(quota.exists(), is(false));
  }
}

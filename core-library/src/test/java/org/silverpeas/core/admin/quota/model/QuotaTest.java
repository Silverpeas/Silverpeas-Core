/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.admin.quota.model;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.quota.constant.QuotaLoad;
import org.silverpeas.core.admin.quota.constant.QuotaType;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnableSilverTestEnv
public class QuotaTest {

  @Test
  public void testValidate() {
    Quota quota = initializeQuota();
    assertValidate(quota, true);

    quota = initializeQuota();
    assertValidate(quota, true);

    quota = initializeQuota();
    quota.setSaveDate(null);
    assertValidate(quota, true);

    quota = initializeQuota();
    quota.setCount(-100);
    assertValidate(quota, true);

    quota = initializeQuota();
    quota.setType((QuotaType) null);
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setResourceId(null);
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setResourceId("");
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setResourceId(" ");
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setMinCount(-1);
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setMaxCount(-1);
    assertValidate(quota, false);

    quota = initializeQuota();
    quota.setMinCount(2);
    quota.setMaxCount(1);
    assertValidate(quota, false);
  }

  private <T extends SilverpeasException> void assertValidate(final Quota quota,
      final boolean isValid) {
    boolean isException = false;
    try {
      quota.validate();
    } catch (final QuotaException qe) {
      isException = true;
    }
    assertThat(isException, is(!isValid));
  }

  @Test
  public void testGetLoad() {
    Quota quota = initializeQuota();
    quota.setMaxCount(0);
    assertThat(quota.getLoad(), is(QuotaLoad.UNLIMITED));

    quota = initializeQuota();
    quota.setCount(0);
    assertThat(quota.getLoad(), is(QuotaLoad.EMPTY));

    quota = initializeQuota();
    quota.setCount(1);
    assertThat(quota.getLoad(), is(QuotaLoad.NOT_ENOUGH));

    quota = initializeQuota();
    quota.setCount(quota.getMaxCount() - 1);
    assertThat(quota.getLoad(), is(QuotaLoad.NOT_FULL));

    quota = initializeQuota();
    quota.setCount(quota.getMaxCount());
    assertThat(quota.getLoad(), is(QuotaLoad.FULL));

    quota = initializeQuota();
    quota.setMinCount(0);
    quota.setCount(0);
    assertThat(quota.getLoad(), is(QuotaLoad.EMPTY));

    quota = initializeQuota();
    quota.setCount(1000);
    assertThat(quota.getLoad(), is(QuotaLoad.OUT_OF_BOUNDS));
  }

  @Test
  public void testGetLoadRate() {
    Quota quota = initializeQuota();
    BigDecimal loadRate = quota.getLoadRate();
    assertThat(loadRate, is(new BigDecimal("0.60869565217391304348")));
  }

  @Test
  public void testGetLoadPercentage() {
    Quota quota = initializeQuota();
    BigDecimal loadPercentage = quota.getLoadPercentage();
    assertThat(loadPercentage, is(new BigDecimal("60.87")));
  }

  private Quota initializeQuota() {
    final Quota quota = new Quota();
    quota.setQuotaId(26L);
    quota.setType(QuotaType.USERS_IN_DOMAIN);
    quota.setResourceId("26");
    quota.setMinCount(10);
    quota.setMaxCount(23);
    quota.setCount(14);
    quota.setSaveDate(java.sql.Date.valueOf("2012-01-01"));
    return quota;
  }
}

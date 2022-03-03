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
package org.silverpeas.core.subscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedBean;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.UNKNOWN;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
public class SubscriptionResourceTypeRegistryTest {

  @TestManagedBean
  private SubscriptionResourceTypeRegistry registry;

  @BeforeEach
  void commonsAsserts() {
    assertThat(SubscriptionResourceTypeRegistry.get(), is(registry));
    assertThat(SubscriptionResourceType.from(null), is(UNKNOWN));
    assertThat(SubscriptionResourceType.from("toto"), is(UNKNOWN));
    assertThat(registry.streamAll().count(), is(2L));
  }

  @Test
  void commons() {
    SubscriptionResourceTypeRegistry.get().streamAll().forEach(s -> {
      assertThat(s, not(is(UNKNOWN)));
      assertThat(SubscriptionResourceType.from(s.getName()), is(s));
      assertThat(registry.getByName(s.getName()), is(s));
      assertThat(s.isValid(), is(true));
    });
  }

  @Test
  void withOtherThanCommons() {
    registry.add(new SubscriptionResourceType() {
      private static final long serialVersionUID = -9142052086485634286L;

      @Override
      public int priority() {
        return 200;
      }

      @Override
      public String getName() {
        return "ADDED_TYPE";
      }
    });
    registry.add(new SubscriptionResourceType() {
      private static final long serialVersionUID = 4065017699598254761L;

      @Override
      public int priority() {
        return -200;
      }

      @Override
      public String getName() {
        return "OTHER_ADDED_TYPE";
      }
    });
    registry.add(new SubscriptionResourceType() {
      private static final long serialVersionUID = 6154543477111474298L;

      @Override
      public int priority() {
        return -200;
      }

      @Override
      public String getName() {
        return "02_OTHER_ADDED_TYPE";
      }
    });
    assertThat(registry.streamAll().count(), is(5L));
    assertThat(
        registry.streamAll().map(SubscriptionResourceType::getName).collect(Collectors.toList()),
        contains("02_OTHER_ADDED_TYPE", "OTHER_ADDED_TYPE", "COMPONENT", "NODE", "ADDED_TYPE"));
  }
}

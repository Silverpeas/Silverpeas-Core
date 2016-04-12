/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.cache.service;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VolatileResourceCacheServiceTest {

  private VolatileResourceCacheService instance = new VolatileResourceCacheService();

  @Test
  public void testNewIntegerIdentifier() {
    Integer newIdentifier = instance.newVolatileIntegerIdentifier();
    String newIdentifierAsString = String.valueOf(newIdentifier);
    assertThat(newIdentifierAsString.length(), lessThanOrEqualTo(10));
    assertThat(newIdentifier, lessThan(0));
    Set<Integer> generatedIds = new HashSet<Integer>();
    for (int i = 0; i < 1000; i++) {
      int newId = instance.newVolatileIntegerIdentifier();
      generatedIds.add(newId);
    }
    assertThat(generatedIds, hasSize(1000));
  }

  @Test
  public void testNewLongIdentifier() {
    Long newIdentifier = instance.newVolatileLongIdentifier();
    assertThat(newIdentifier, lessThan(0L));
    Set<Long> generatedIds = new HashSet<Long>();
    for (int i = 0; i < 1000; i++) {
      long newId = instance.newVolatileLongIdentifier();
      generatedIds.add(newId);
    }
    assertThat(generatedIds, hasSize(1000));
  }

  @Test
  public void testNewStringIdentifier() {
    String newIdentifier = instance.newVolatileStringIdentifier();
    assertThat(newIdentifier, startsWith("volatile-"));
    Set<String> generatedIds = new HashSet<String>();
    for (int i = 0; i < 1000; i++) {
      String newId = instance.newVolatileStringIdentifier();
      generatedIds.add(newId);
      System.out.println(newId);
    }
    assertThat(generatedIds, hasSize(1000));
  }
}
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
package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yohann Chastagnier
 */
public class SilverpeasBundleListTest {

  @Test
  public void fromEmptyList() {
    SilverpeasBundleList list = SilverpeasBundleList.with();
    assertThat(list.asStringArray(), emptyArray());
    assertThat(list.asStringArray("defaultValue"), emptyArray());
    assertThat(list.asIntegerArray(), emptyArray());
    assertThat(list.asIntegerArray(26), emptyArray());
  }

  @Test
  public void asStringArray() {
    SilverpeasBundleList list = SilverpeasBundleList.with("A", null, "", "1");
    assertThat(list.asStringArray(), arrayContaining("A", null, "", "1"));
  }

  @Test
  public void asStringArrayWithDefaultValue() {
    SilverpeasBundleList list = SilverpeasBundleList.with("A", null, "", "1");
    assertThat(list.asStringArray("defaultValue"),
        arrayContaining("A", "defaultValue", "defaultValue", "1"));
  }

  @Test
  public void asIntegerList() {
    SilverpeasBundleList list = SilverpeasBundleList.with("1", null, "", "4");
    assertThat(list.asIntegerList(), contains(1, null, null, 4));
  }

  @Test
  public void asIntegerArray() {
    SilverpeasBundleList list = SilverpeasBundleList.with("1", null, "", "4");
    assertThat(list.asIntegerArray(), arrayContaining(1, null, null, 4));
  }

  @Test
  public void asIntegerArrayWithDefaultValue() {
    SilverpeasBundleList list = SilverpeasBundleList.with("1", null, "", "4");
    assertThat(list.asIntegerArray(26), arrayContaining(1, 26, 26, 4));
  }

  @Test
  public void asIntegerArrayFromNotParsableIntegerValue() {
    assertThrows(NumberFormatException.class, () -> {
      SilverpeasBundleList list = SilverpeasBundleList.with("1", "A");
      list.asIntegerArray();
    });
  }
}
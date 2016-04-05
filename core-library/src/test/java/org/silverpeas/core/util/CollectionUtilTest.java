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
package org.silverpeas.core.util;

import org.junit.Test;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtilTest {

  @Test
  public void testSplit() {
    assertThat(CollectionUtil.split(null, 0).size(), is(0));
    assertThat(CollectionUtil.split(new ArrayList<>(), 0).size(), is(0));
    final List<?> list = createListOfArray();
    Collection<?> test = CollectionUtil.split(list, 0);
    assertThat(test.size(), is(1));
    test = CollectionUtil.split(list, 1);
    assertThat(test.size(), is(3));
    test = CollectionUtil.split(list, 2);
    assertThat(test.size(), is(2));
    test = CollectionUtil.split(list, 3);
    assertThat(test.size(), is(1));
    test = CollectionUtil.split(list, 500);
    assertThat(test.size(), is(1));
  }

  /**
   * Creating an list of TestElement.
   * @throws Exception
   */
  private List<TestElement> createListOfTestElement() {

    // Initialization
    final List<TestElement> list = new ArrayList<>();

    // Alimentation
    TestElement testElement;

    testElement = new TestElement();
    testElement.setProperty1("100");
    testElement.setProperty2("2");
    list.add(testElement);

    testElement = new TestElement();
    testElement.setProperty1("110");
    testElement.setProperty2("2");
    list.add(testElement);

    testElement = new TestElement();
    testElement.setProperty1(null);
    testElement.setProperty2("3");
    list.add(testElement);

    testElement = new TestElement();
    testElement.setProperty1("120");
    testElement.setProperty2(null);
    list.add(testElement);

    // Returning the result
    return list;
  }

  /**
   * Creating an array list.
   * @throws Exception
   */
  private List<Object[]> createListOfArray() {

    // Initialization
    final List<Object[]> arrayList = new ArrayList<>();

    // Alimentation
    arrayList.add(new Object[] { "1_1", "1_2" });
    arrayList.add(new Object[] { "2_1", "2_2" });
    arrayList.add(new Object[] { "3_1", "3_2" });

    // Returning the result
    return arrayList;
  }

  /**
   * Object to collect.
   */
  public class TestElement {

    /** First property */
    private String property1 = "";

    /** Second property */
    private String property2 = "";

    /**
     * @return the property1
     */
    public String getProperty1() {
      return property1;
    }

    /**
     * @param property1 the property1 to set
     */
    public void setProperty1(final String property1) {
      this.property1 = property1;
    }

    /**
     * @return the property2
     */
    public String getProperty2() {
      return property2;
    }

    /**
     * @param property2 the property2 to set
     */
    public void setProperty2(final String property2) {
      this.property2 = property2;
    }
  }
}

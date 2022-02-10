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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.CollectionUtil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.util.CollectionUtil.*;

/**
 * @author Yohann Chastagnier
 */
public class CollectionUtilTest {

  @Test
  public void testSplitList() {
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

  @Test
  public void testSplitSet() {
    assertThat(CollectionUtil.split(null, 0).size(), is(0));
    assertThat(CollectionUtil.split(new ArrayList<>(), 0).size(), is(0));
    final Set<?> set = createSetOfArray();
    Collection<?> test = CollectionUtil.split(set, 0);
    assertThat(test.size(), is(1));
    test = CollectionUtil.split(set, 1);
    assertThat(test.size(), is(3));
    test = CollectionUtil.split(set, 2);
    assertThat(test.size(), is(2));
    test = CollectionUtil.split(set, 3);
    assertThat(test.size(), is(1));
    test = CollectionUtil.split(set, 500);
    assertThat(test.size(), is(1));
  }

  @Test
  public void unionOfTwoListsShouldContainOnlyUniqueValues() {
    List<String> A = Arrays.asList("A", "B", "C", "D", "D", "E", "Z");
    List<String> B = Arrays.asList("A", "A", "A", "D", "E", "E", "Y");
    List<String> union = CollectionUtil.union(A, B);
    assertThat(union, contains("A", "B", "C", "D", "E", "Z", "Y"));
    union = CollectionUtil.union(B, A);
    assertThat(union, contains("A", "D", "E", "Y", "B", "C", "Z"));
  }

  @Test
  public void intersectionOfTwoListsShouldContainOnlyUniqueValues() {
    List<String> A = Arrays.asList("A", "B", "C", "D", "D", "E", "Z");
    List<String> B = Arrays.asList("Y", "E", "E", "D", "A", "A", "A");
    List<String> intersection = CollectionUtil.intersection(A, B);
    assertThat(intersection, contains("E", "D", "A"));
    intersection = CollectionUtil.intersection(B, A);
    assertThat(intersection, contains("A", "D", "E"));
  }

  @Test
  public void indexOfItem() {
    List<TestElement> ITEMS = createListOfTestElement();
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("100")), is(0));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("100"), 0), is(0));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("100"), 1), is(-1));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110")), is(1));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110"), 1), is(1));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110"), 2), is(3));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110"), 3), is(3));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110"), 4), is(4));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty1("110"), 5), is(-1));

    assertThat(indexOf(ITEMS, e -> e.equalsProperty2("110"), 3), is(-1));
    assertThat(indexOf(ITEMS, e -> e.equalsProperty2("110")), is(-1));
  }

  @Test
  public void findFirstItem() {
    List<TestElement> ITEMS = createListOfTestElement();
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("100")).orElse(null), is(ITEMS.get(0)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("100"), 0).orElse(null), is(ITEMS.get(0)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("100"), 1).isPresent(), is(false));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110")).orElse(null), is(ITEMS.get(1)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110"), 1).orElse(null), is(ITEMS.get(1)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110"), 2).orElse(null), is(ITEMS.get(3)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110"), 3).orElse(null), is(ITEMS.get(3)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110"), 4).orElse(null), is(ITEMS.get(4)));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty1("110"), 5).isPresent(), is(false));

    assertThat(findFirst(ITEMS, e -> e.equalsProperty2("110"), 3).isPresent(), is(false));
    assertThat(findFirst(ITEMS, e -> e.equalsProperty2("110")).isPresent(), is(false));
  }

  @Test
  public void indexOfVsFindFirstItem() {
    List<TestElement> ITEMS = createListOfTestElement();
    ITEMS.add(null);
    final int indexOfNull = indexOf(ITEMS, Objects::isNull);
    final Optional<TestElement> optionalOfNull = findFirst(ITEMS, Objects::isNull);
    assertThat(indexOfNull, is(6));
    assertThat(optionalOfNull.isPresent(), is(false));
  }

  @Test
  public void findNextRuptureItem() {
    List<TestElement> ITEMS = createListOfTestElement();
    RuptureContext<TestElement> context = RuptureContext.newOne(ITEMS);

    final TestElement first = findNextRupture(context, e -> e.equalsProperty1("100")).orElse(null);
    assertThat(first, is(ITEMS.get(0)));
    assertThat(context.isTerminated(), is(false));

    TestElement next = findNextRupture(context, e -> e.equalsProperty1("100")).orElse(null);
    assertThat(next, sameInstance(first));
    assertThat(context.isTerminated(), is(false));

    next = findNextRupture(context, e -> e.equalsProperty1("110")).orElse(null);
    assertThat(next, not(sameInstance(first)));
    assertThat(next, is(ITEMS.get(1)));
    assertThat(context.isTerminated(), is(false));

    next = findNextRupture(context, e -> e.equalsProperty1("100")).orElse(null);
    assertThat(next, nullValue());
    assertThat(context.isTerminated(), is(true));
  }

  @Test
  public void findNextRuptureItemWhenTerminatedShouldThrowError() {
    assertThrows(IllegalStateException.class, () -> {
      List<TestElement> ITEMS = createListOfTestElement();
      RuptureContext<TestElement> context = RuptureContext.newOne(ITEMS);

      final TestElement item = findNextRupture(context, e -> e.equalsProperty1("none")).orElse(null);
      assertThat(item, nullValue());
      assertThat(context.isTerminated(), is(true));

      findNextRupture(context, e -> e.equalsProperty1("100"));
    });
  }

  @Test
  public void findNextRuptureItemWhenResetShouldWork() {
    List<TestElement> ITEMS = createListOfTestElement();
    RuptureContext<TestElement> context = RuptureContext.newOne(ITEMS);

    TestElement item = findNextRupture(context, e -> e.equalsProperty1("none")).orElse(null);
    assertThat(item, nullValue());
    assertThat(context.isTerminated(), is(true));

    context.reset();
    assertThat(context.isTerminated(), is(false));

    item = findNextRupture(context, e -> e.equalsProperty1("100")).orElse(null);
    assertThat(item, is(ITEMS.get(0)));
    assertThat(context.isTerminated(), is(false));
  }

  /**
   * Creating an list of TestElement.<br>
   * [{100,2},{110,2},{null,3},{110,3},{110,null},{120,null}]
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
    testElement.setProperty1("110");
    testElement.setProperty2("3");
    list.add(testElement);

    testElement = new TestElement();
    testElement.setProperty1("110");
    testElement.setProperty2(null);
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
   * Creating an array list.
   * @throws Exception
   */
  private Set<Object[]> createSetOfArray() {

    // Initialization
    final Set<Object[]> arraySet = new HashSet<>();

    // Alimentation
    arraySet.add(new Object[] { "1_1", "1_2" });
    arraySet.add(new Object[] { "2_1", "2_2" });
    arraySet.add(new Object[] { "3_1", "3_2" });

    // Returning the result
    return arraySet;
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
     * @return the property1
     */
    public boolean equalsProperty1(String other) {
      return new EqualsBuilder().append(property1, other).build();
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
     * @return the property1
     */
    public boolean equalsProperty2(String other) {
      return new EqualsBuilder().append(property2, other).build();
    }

    /**
     * @param property2 the property2 to set
     */
    public void setProperty2(final String property2) {
      this.property2 = property2;
    }
  }
}

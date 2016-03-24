package org.silverpeas.core.util;

import org.junit.Test;
import org.silverpeas.core.util.SilverpeasBundleList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyArray;

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

  @Test(expected = NumberFormatException.class)
  public void asIntegerArrayFromNotParsableIntegerValue() {
    SilverpeasBundleList list = SilverpeasBundleList.with("1", "A");
    list.asIntegerArray();
  }
}
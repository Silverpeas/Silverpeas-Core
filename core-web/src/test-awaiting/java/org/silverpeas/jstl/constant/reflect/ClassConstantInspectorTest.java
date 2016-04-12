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
package org.silverpeas.jstl.constant.reflect;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author: Yohann Chastagnier
 */
public class ClassConstantInspectorTest {

  @Test(expected = NoSuchFieldException.class)
  public void testUnexistingConstant() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.UnknownConstant");
  }

  @Test
  public void testLowercaseConstantAccessTest() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest" +
            ".lowerCaseConstantAccessTest"
    );
    assertThat(inspector.getValue().toString(), is("lowerCaseConstantAccessTestSuccess"));
  }

  @Test
  public void testUppercaseConstantAccessTest() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest" +
            ".UPPERCASE_CONSTANT_ACCESS_TEST"
    );
    assertThat(inspector.getValue().toString(), is("UPPERCASE_CONSTANT_ACCESS_TEST_SUCCESS"));
  }

  @Test
  public void testEnumAccessTest() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.EnumAccessTest.success");
    assertThat(inspector.getValue().toString(), is("success"));
  }

  @Test(expected = NoSuchFieldException.class)
  public void testProtectedEnumAccessTest() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.ProtectedEnumAccessTest" +
            ".failed"
    );
  }

  @Test
  public void testClassAccessTest() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.ClassAccessTest.SUCCESS");
    assertThat(inspector.getValue().toString(), is("ClassAccessTestSuccess"));
  }

  @Test(expected = NoSuchFieldException.class)
  public void testFieldOfNonPublicClass() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.NonPublicClass.FAILED");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonStaticClass() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.NonStaticClass");
  }

  @Test
  public void testFieldOfNonStaticClass() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.NonStaticClass.SUCCESS");
    assertThat(inspector.getValue().toString(), is("NonStaticClassSuccess"));
  }

  @Test
  public void testFieldOfNonFinalClass() throws Exception {
    ClassConstantInspector inspector = new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.NonFinalClass.SUCCESS");
    assertThat(inspector.getValue().toString(), is("NonFinalClassSuccess"));
  }

  @Test(expected = NoSuchFieldException.class)
  public void testNonPublicField() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.nonPublicField");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonStaticField() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.nonStaticField");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonFinalField() throws Exception {
    new ClassConstantInspector(
        "org.silverpeas.jstl.constant.reflect.ClassConstantInspectorTest.nonFinalField");
  }

  public enum EnumAccessTest {
    success
  }

  protected enum ProtectedEnumAccessTest {
    failed
  }

  public class ClassAccessTest {
    public static final String SUCCESS = "ClassAccessTestSuccess";
  }

  public static final String lowerCaseConstantAccessTest = "lowerCaseConstantAccessTestSuccess";
  public static final String UPPERCASE_CONSTANT_ACCESS_TEST =
      "UPPERCASE_CONSTANT_ACCESS_TEST_SUCCESS";

  protected static final String nonPublicField = "failed";
  public final String nonStaticField = "failed";
  public static String nonFinalField = "failed";

  private static final class NonPublicClass {
    public static final String FAILED = "failed";
  }

  public final class NonStaticClass {
    public static final String SUCCESS = "NonStaticClassSuccess";
  }

  public static class NonFinalClass {
    public static final String SUCCESS = "NonFinalClassSuccess";
  }
}

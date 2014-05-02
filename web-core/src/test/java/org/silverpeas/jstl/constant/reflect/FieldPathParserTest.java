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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * @author: Yohann Chastagnier
 */
public class FieldPathParserTest {

  @Test
  public void testLowercaseConstantAccessTest() {
    FieldPathParser parser = new FieldPathParser(
        "org.silverpeas.jstl.constant.reflect.FieldPathParserTest.lowerCaseConstantAccessTest");
    assertThat(parser.getDeclaringClassName(),
        is("org.silverpeas.jstl.constant.reflect.FieldPathParserTest"));
    assertThat(parser.getFieldOrClassNames(), contains("lowerCaseConstantAccessTest"));
  }

  @Test
  public void testUppercaseConstantAccessTest() {
    FieldPathParser parser = new FieldPathParser(
        "org.silverpeas.jstl.constant.reflect.FieldPathParserTest.UPPERCASE_CONSTANT_ACCESS_TEST");
    assertThat(parser.getDeclaringClassName(),
        is("org.silverpeas.jstl.constant.reflect.FieldPathParserTest"));
    assertThat(parser.getFieldOrClassNames(), contains("UPPERCASE_CONSTANT_ACCESS_TEST"));
  }

  @Test
  public void testEnumAccessTest() {
    FieldPathParser parser = new FieldPathParser(
        "org.silverpeas.jstl.constant.reflect.FieldPathParserTest.EnumAccessTest.success");
    assertThat(parser.getDeclaringClassName(),
        is("org.silverpeas.jstl.constant.reflect.FieldPathParserTest"));
    assertThat(parser.getFieldOrClassNames(), contains("EnumAccessTest", "success"));
  }
}

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ehugonnet
 */
class StringUtilTest {

  /**
   * Test of isDefined method, of class StringUtil.
   */
  @Test
  void testIsDefined() {
    assertTrue(StringUtil.isDefined("1"));
    assertFalse(StringUtil.isDefined("   "));
    assertFalse(StringUtil.isDefined(""));
    assertFalse(StringUtil.isDefined("null"));
    assertFalse(StringUtil.isDefined("NuLl"));
    assertFalse(StringUtil.isDefined(null));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  void testIsInteger() {
    assertTrue(StringUtil.isInteger("1"));
    assertTrue(StringUtil.isInteger("00100"));
    assertFalse(StringUtil.isInteger("1.1"));
    assertFalse(StringUtil.isInteger("a"));
    assertTrue(StringUtil.isInteger("0"));
    assertTrue(StringUtil.isInteger("-1"));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  void testIsFloat() {
    assertTrue(StringUtil.isFloat("1"));
    assertTrue(StringUtil.isFloat("00100"));
    assertTrue(StringUtil.isFloat("1.1"));
    assertFalse(StringUtil.isFloat("a"));
    assertTrue(StringUtil.isFloat("0"));
    assertTrue(StringUtil.isFloat("-1"));
    assertFalse(StringUtil.isFloat("1,1"));
  }

  /**
   * Test of isInteger method, of class StringUtil.
   */
  @Test
  void testConvertFloat() {
    assertEquals(1.0f, StringUtil.asFloat("1"), 0.001f);
    assertEquals(1.1f, StringUtil.asFloat("1.1"), 0.001f);
    assertEquals(0f, StringUtil.asFloat("a"), 0.001f);
    assertEquals(-1.0f, StringUtil.asFloat("-1"), 0.001f);
    assertEquals(1.1f, StringUtil.asFloat("1,1"), 0.001f);
  }

  /**
   * Test of escapeQuote method, of class StringUtil.
   */
  @Test
  void testEscapeQuote() {
    String text = "'hello'";
    String expResult = " hello ";
    String result = StringUtil.escapeQuote(text);
    assertEquals(expResult, result);
  }

  /**
   * Test of isValidEmailAddress method, of class StringUtil.
   */
  @Test
  void testIsValidEmailAddress() {
    // Test variations in the email name
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven.haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven-haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven+haines@javasrc.com"));
    assertTrue(StringUtil.isValidEmailAddress("steven_haines@javasrc.com"));
    assertFalse(StringUtil.isValidEmailAddress("steven#haines@javasrc.com"));

    // Test variations in the domain name
    assertTrue(StringUtil.isValidEmailAddress("steve@java-src.com"));
    assertTrue(StringUtil.isValidEmailAddress("steve@java.src.com"));
    assertFalse(StringUtil.isValidEmailAddress("steve@java\\src.com"));

    // Test variations in the domain name
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.a"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aa"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aaa"));
    assertTrue(StringUtil.isValidEmailAddress("steve@javasrc.aaaa"));
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.aaaaa"));

    // Test that the email address marks the beginning of the string
    assertFalse(StringUtil.isValidEmailAddress("aaa steve@javasrc.com"));

    // Test that the email address marks the end of the string
    assertFalse(StringUtil.isValidEmailAddress("steve@javasrc.com aaa"));
  }

  /**
   * Test of convertToEncoding method, of class StringUtil.
   */
  @Test
  void testConvertToEncoding() {
    assertEquals("élève",
        StringUtil.convertToEncoding(new String("élève".getBytes(StandardCharsets.UTF_8)),
            "UTF-8"));
    assertNotSame("élève",
        StringUtil.convertToEncoding(new String("élève".getBytes(StandardCharsets.ISO_8859_1)),
            "UTF-8"));
  }

  /**
   * Test of getBooleanValue method, of class StringUtil.
   */
  @Test
  void testGetBooleanValue() {
    assertTrue(StringUtil.getBooleanValue("1"));
    assertTrue(StringUtil.getBooleanValue("yes"));
    assertTrue(StringUtil.getBooleanValue("YeS"));
    assertTrue(StringUtil.getBooleanValue("trUe"));
    assertTrue(StringUtil.getBooleanValue("oUi"));
    assertTrue(StringUtil.getBooleanValue("Y"));
    assertFalse(StringUtil.getBooleanValue(""));
    assertFalse(StringUtil.getBooleanValue("no"));
    assertFalse(StringUtil.getBooleanValue("0"));
    assertFalse(StringUtil.getBooleanValue(null));
  }

  @Test
  void testDefaultStringIfNotDefinedReturningEmptyStringAsDefault() {
    assertThat(StringUtil.defaultStringIfNotDefined(null), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined(""), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined("   "), is(StringUtil.EMPTY));
    assertThat(StringUtil.defaultStringIfNotDefined(" A "), is(" A "));
  }

  @Test
  void testDefaultStringIfNotDefinedReturningGivenStringAsDefault() {
    String defaultString = "givenString";
    assertThat(StringUtil.defaultStringIfNotDefined(null, defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined("", defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined("   ", defaultString), is(defaultString));
    assertThat(StringUtil.defaultStringIfNotDefined(" A ", defaultString), is(" A "));
  }

  @Test
  void testIsValidHour() {
    assertTrue(StringUtil.isValidHour("10:30"));
    assertTrue(StringUtil.isValidHour("24:59"));
    assertTrue(StringUtil.isValidHour("00:00"));
    assertTrue(StringUtil.isValidHour("8:01"));
    assertTrue(StringUtil.isValidHour("0:01"));
    // Check limit hour
    assertFalse(StringUtil.isValidHour("25:00"));
    assertFalse(StringUtil.isValidHour("24:60"));
    assertFalse(StringUtil.isValidHour("10-30"));
    assertFalse(StringUtil.isValidHour(null));
    assertFalse(StringUtil.isValidHour(""));
  }

  @Test
  void normalize() {
    String allInput =
        " \r\t\n²1&234567890°+=})]aàäãâ@ç\\_`eéèëê-oöôõòiïîìnnñuüûù|[({'#\"~?,.;:!§ù%*µ¤$£├®é";
    String resInput =
        " \r\t\n²1&234567890°+=})]aàäãâ@ç\\_`eéèëê-oöôõòiïîìnnñuüûù|[({'#\"~?,.;:!§ù%*µ¤$£├®é";
    String result = StringUtil.normalize(allInput);
    assertEquals(resInput, result);
  }

  @Test
  void normalizeByRemovingAccent() {
    String allInput =
        " \r\t\n²1&234567890°+=})]aàäãâ@ç\\_`eéèëê-oöôõòiïîìnnñuüûù|[({'#\"~?,.;:!§ù%*µ¤$£├®é";
    String resInput =
        " \r\t\n²1&234567890°+=})]aaaaa@c\\_`eeeee-oooooiiiinnnuuuu|[({'#\"~?,.;:!§u%*µ¤$£├®e";
    String result = StringUtil.normalizeByRemovingAccent(allInput);
    assertEquals(resInput, result);
  }

  @Test
  void normalizeWhenUppercase() {
    String allInput =
        " \r\t\n²1&234567890°+=})]AÀÄÃÂ@Ç\\_`EÉÈËÊ-OÖÔÕÒIÏÎÌNNÑUÜÛÙ|[({'#\"~?,.;:!§Ù%*Μ¤$£├®É";
    String resInput =
        " \r\t\n²1&234567890°+=})]AÀÄÃÂ@Ç\\_`EÉÈËÊ-OÖÔÕÒIÏÎÌNNÑUÜÛÙ|[({'#\"~?,.;:!§Ù%*Μ¤$£├®É";
    String result = StringUtil.normalize(allInput);
    assertEquals(resInput, result);
  }

  @Test
  void normalizeByRemovingAccentWhenUppercase() {
    String allInput =
        " \r\t\n²1&234567890°+=})]AÀÄÃÂ@Ç\\_`EÉÈËÊ-OÖÔÕÒIÏÎÌNNÑUÜÛÙ|[({'#\"~?,.;:!§Ù%*Μ¤$£├®É";
    String resInput =
        " \r\t\n²1&234567890°+=})]AAAAA@C\\_`EEEEE-OOOOOIIIINNNUUUU|[({'#\"~?,.;:!§U%*Μ¤$£├®E";
    String result = StringUtil.normalizeByRemovingAccent(allInput);
    assertEquals(resInput, result);
  }

  @Test
  void likeIgnoreCaseWithWildcardAny() {
    assertThat(StringUtil.likeIgnoreCase(null, null), is(true));
    assertThat(StringUtil.likeIgnoreCase("", ""), is(true));
    assertThat(StringUtil.likeIgnoreCase("", null), is(true));
    assertThat(StringUtil.likeIgnoreCase(null, ""), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Oto%%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Oto%\\%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo%", "%Oto%\\%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo\\%", "%Oto%\\%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo\\%", "%Oto\\%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo\\%", "%Oto\\\\%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to%To%", "%O\\%to%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to%%To%", "%O\\%\\%to%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to%To%", "%O\\%to%x"), is(false));
    assertThat(StringUtil.likeIgnoreCase("to%To%", "%O\\%tox"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toToBool%", "%Oto%\\%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toToX", "%Oto%X"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toToY", "%Oto%X"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Oto%i"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Oto%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Ot%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "%Ot%o"), is(true));
    assertThat(StringUtil.likeIgnoreCase("%%%toTo", "\\%\\%\\%%%%%%%%%%%%%%%%%Ot%%%%%%%%%%%%%%%%%%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("%%%La petite maison", "\\%\\%\\%LA PETITE MAISON"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "LA PETITE MAISON"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%E%E%S"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%E%E%S%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "La%E%E%S%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "La%E%E%S"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "a%E%E%S"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "a%E%E%S%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "La%E%E%S"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "La%E%E%S%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%E%P%S"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%E%P%S%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%MAIson"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%pet%MAIson"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "%pet%MAIson%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "L%pet%MAIson%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite \\maison", "L%pet%MAIson%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite maison", "L %pet%MAIson%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("/SIEGE/EXCLUSION", "%exclusion%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("/SIEGE/EXCLUSION/TEST", "%exclusion%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("/SIEGE/EXCLUSION/TEST", "%exclusion"), is(false));
    assertThat(StringUtil.likeIgnoreCase("/SIEGE/EXCLUSION/TEST", "exclusion%"), is(false));
    assertThat(StringUtil.likeIgnoreCase("EXCLUSION", "%exclusion%"), is(true));
    assertThat(StringUtil.likeIgnoreCase("&é\"\\m\\p\\a'(-è_ç^à@)°]+=}$£¤^¨*µù\\%!§:/;.,?<>", "&é\"\\m\\p\\a'(-è_ç^à@)°]+=}$£¤^¨*µù\\\\%!§:/;.,?<>"), is(true));
  }

  @Test
  void like() {
    assertThat(StringUtil.like("La petite maison", "LA PETITE MAISON"), is(false));
    assertThat(StringUtil.like("La petite maison", "la petite maison"), is(false));
    assertThat(StringUtil.like("LA PETITE MAISON", "La petite maison"), is(false));
    assertThat(StringUtil.like("la petite maison", "La petite maison"), is(false));
    assertThat(StringUtil.like("La petite maison", "La petite maison"), is(true));
  }

  @Test
  void likeIgnoreCaseWithWildcardOneCharacter() {
    assertThat(StringUtil.likeIgnoreCase("toTo", "_Ot_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "_Oto_\\_"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo_", "_Ot_\\_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo_", "_Oto\\_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo\\_", "_Oto_\\_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo\\_", "_Oto\\_"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo\\_", "_Oto\\\\_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to_To_", "_O\\_to_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to__To_", "_O\\_\\_to_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("to_To_", "_O\\_to\\_x"), is(false));
    assertThat(StringUtil.likeIgnoreCase("to_To_", "_O\\_to_x"), is(false));
    assertThat(StringUtil.likeIgnoreCase("to_To_", "_O\\_tox"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toToBool_", "_Oto____\\_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toToX", "_OtoX"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toToY", "_Oto_Y"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo", "_Oto_i"), is(false));
    assertThat(StringUtil.likeIgnoreCase("toTo", "_Oto"), is(true));
    assertThat(StringUtil.likeIgnoreCase("toTo", "_Ot_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("___toTo", "\\_\\_\\__Ot_"), is(true));
    assertThat(StringUtil.likeIgnoreCase("___La petite maison", "\\_\\_\\_LA PETITE MAISON"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite \\maison", "L__pet_____MAIson"), is(true));
    assertThat(StringUtil.likeIgnoreCase("La petite \\maison", "L__pet____\\MAIson"), is(true));
    assertThat(StringUtil.likeIgnoreCase("/SIEGE/EXCLUSION", "/_____/exclusion"), is(true));
    assertThat(StringUtil.likeIgnoreCase("&é\"\\m\\p\\a'(-è_ç^à@)°]+=}$£¤^¨*µù\\_!§:/;.,?<>", "&é\"\\m\\p\\a'(-è_ç^à@)°]+=}$£¤^¨*µù\\\\_!§:/;.,?<>"), is(true));
  }

  @Test
  void likeWithWildcards() {
    assertThat(StringUtil.like("La petite maison", "La%petite%"), is(true));
    assertThat(StringUtil.like("La petite maison", "% petite%"), is(true));
    assertThat(StringUtil.like("La petite maison", "%_petit_%"), is(true));
    assertThat(StringUtil.like("La petite maison", "La % mais%"), is(true));
    assertThat(StringUtil.like("La petite maison", "La _etite maison"), is(true));
    assertThat(StringUtil.like("La petite maison", "_a _etite ma_so_"), is(true));
    assertThat(StringUtil.like("La petite maison", "__ petite %"), is(true));
    assertThat(StringUtil.like("La petite maison", "__ %"), is(true));
    assertThat(StringUtil.like("La petite maison", "La _etit maison"), is(false));
    assertThat(StringUtil.like("La petite maison", "_ petite maison"), is(false));
    assertThat(StringUtil.like("La petite maison", "_ petite %"), is(false));
    assertThat(StringUtil.like("La petite maison", "_a pet_te ma__o%"), is(true));
    assertThat(StringUtil.like("La petite maison", "__ ______ ______"), is(true));
    assertThat(StringUtil.like("La petite maison", "________________"), is(true));
    assertThat(StringUtil.like("La petite maison", "____%___________"), is(true));
    assertThat(StringUtil.like("La petite maison", "___%%%______%_______"), is(true));
    assertThat(StringUtil.like("La petite maison", "____%_____%%___%___"), is(true));
    assertThat(StringUtil.like("La petite maison", "%___%_____%%___%___"), is(true));
    assertThat(StringUtil.like("La petite maison", "%__%_petit_%"), is(true));
    assertThat(StringUtil.like("La petite maison", "%____petit_%"), is(false));
    assertThat(StringUtil.like("La petite maison", "%"), is(true));
    assertThat(StringUtil.like("La petite maison", "%_%"), is(true));
    assertThat(StringUtil.like("La", "%_%"), is(true));
    assertThat(StringUtil.like("La", "%___%"), is(false));
    assertThat(StringUtil.like("La", "%_%_%"), is(true));
  }

  @Test
  void debug() {
    assertThat(StringUtil.like("La%", "%_%_\\%"), is(true));
  }

}

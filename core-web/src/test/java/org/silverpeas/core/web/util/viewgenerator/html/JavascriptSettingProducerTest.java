/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.UnitTest;
import org.silverpeas.core.util.SilverpeasBundle;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
@UnitTest
public class JavascriptSettingProducerTest {

  @Test
  public void produce() {
    SilverpeasBundle bundle = mock(SilverpeasBundle.class);
    when(bundle.getString("unit.test.label")).thenReturn("Ceci est un test unitaire");
    when(bundle.getString("unit.test.special.chars")).thenReturn("Apostrophe ' et guillemets\"");
    when(bundle.getString("unit.test.html.chars")).thenReturn("<br/>{0}<span/>");

    String producedJs = JavascriptSettingProducer
        .settingVariableName("UnitTestSettings")
        .add(bundle, "unit.test.label", "unit.test.special.chars", "unit.test.html.chars")
        .add("unit.test.simple.key.string", "simpleValue")
        .add("unit.test.simple.key.boolean", false)
        .add("unit.test.simple.key.integer", 25)
        .add("unit.test.simple.key.long", 65L)
        .add("unit.test.simple.key.float", 26.63)
        .add("unit.test.simple.key.bigdecimal", new BigDecimal(String.valueOf(38.56)))
        .produce();

    assertThat(producedJs, is("window.UnitTestSettings=new SilverpeasPluginSettings({" +
        "\"unit.test.label\":'Ceci est un test unitaire'," +
        "\"unit.test.special.chars\":'Apostrophe \\' et guillemets\\\"'," +
        "\"unit.test.html.chars\":'<br\\/>{0}<span\\/>'," +
        "\"unit.test.simple.key.string\":'simpleValue'," +
        "\"unit.test.simple.key.boolean\":false," +
        "\"unit.test.simple.key.integer\":25," +
        "\"unit.test.simple.key.long\":65," +
        "\"unit.test.simple.key.float\":26.63," +
        "\"unit.test.simple.key.bigdecimal\":38.56" +
        "});"));
  }
}
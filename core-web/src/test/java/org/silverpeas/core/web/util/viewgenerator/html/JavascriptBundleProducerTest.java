/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html;

import org.junit.Test;
import org.silverpeas.core.util.SilverpeasBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class JavascriptBundleProducerTest {

  @Test
  public void produce() {
    SilverpeasBundle bundle = mock(SilverpeasBundle.class);
    when(bundle.getString("unit.test.label")).thenReturn("Ceci est un test unitaire");
    when(bundle.getString("unit.test.special.chars")).thenReturn("Apostrophe ' et guillemets\"");
    when(bundle.getString("unit.test.html.chars")).thenReturn("<br/>{0}<span/>");

    String producedJs = JavascriptBundleProducer
        .bundleVariableName("UnitTestBundle")
        .add(bundle, "unit.test.label", "unit.test.special.chars", "unit.test.html.chars")
        .produce();

    assertThat(producedJs, is("window.UnitTestBundle=new SilverpeasPluginBundle({" +
        "\"unit.test.label\":\"Ceci est un test unitaire\"," +
        "\"unit.test.special.chars\":\"Apostrophe \\' et guillemets\\\"\"," +
        "\"unit.test.html.chars\":\"<br\\/>{0}<span\\/>\"" +
        "});"));
  }
}
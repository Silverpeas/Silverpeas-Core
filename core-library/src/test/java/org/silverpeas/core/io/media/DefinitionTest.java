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
package org.silverpeas.core.io.media;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class DefinitionTest {

  @Test
  public void test() {
    Definition definition = Definition.fromZero();
    assertThat(definition, is(Definition.NULL));
    assertThat(definition, not(sameInstance(Definition.NULL)));
    assertThat(definition.isDefined(), is(false));
    assertThat(definition.isWidthDefined(), is(false));
    assertThat(definition.isHeightDefined(), is(false));

    definition = Definition.of(1, 2);
    assertThat(definition, not(is(Definition.NULL)));
    assertThat(definition.getWidth(), is(1));
    assertThat(definition.getHeight(), is(2));
    assertThat(definition.isDefined(), is(true));
    assertThat(definition.isWidthDefined(), is(true));
    assertThat(definition.isHeightDefined(), is(true));

    definition = Definition.fromZero().widthOf(1).heightOf(2);
    assertThat(definition, is(Definition.of(1, 2)));
    assertThat(definition, not(is(Definition.NULL)));
    assertThat(definition.getWidth(), is(1));
    assertThat(definition.getHeight(), is(2));
    assertThat(definition.isDefined(), is(true));
    assertThat(definition.isWidthDefined(), is(true));
    assertThat(definition.isHeightDefined(), is(true));

    definition = Definition.fromZero().widthOf(1);
    assertThat(definition, is(Definition.of(1, 0)));
    assertThat(definition, not(is(Definition.NULL)));
    assertThat(definition.getWidth(), is(1));
    assertThat(definition.getHeight(), is(0));
    assertThat(definition.isDefined(), is(true));
    assertThat(definition.isWidthDefined(), is(true));
    assertThat(definition.isHeightDefined(), is(false));

    definition = Definition.fromZero().heightOf(2);
    assertThat(definition, is(Definition.of(0, 2)));
    assertThat(definition, not(is(Definition.NULL)));
    assertThat(definition.getWidth(), is(0));
    assertThat(definition.getHeight(), is(2));
    assertThat(definition.isDefined(), is(true));
    assertThat(definition.isWidthDefined(), is(false));
    assertThat(definition.isHeightDefined(), is(true));
  }
}
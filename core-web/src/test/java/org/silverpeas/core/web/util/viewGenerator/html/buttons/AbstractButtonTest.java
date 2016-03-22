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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AbstractButtonTest {

  @Test
  public void printJavascriptEventActionWithoutPreProcessing() {
    ButtonForTest buttonForTest = ButtonForTest.withAction("javascript:onSomething=functionCall()");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("javascript:onSomething=functionCall()"));
    assertThat(printResult, is("<ACTION>javascript:onSomething=functionCall()</ACTION>"));
  }

  @Test
  public void printJavascriptEventActionWithPreProcessing() {
    ButtonForTest buttonForTest =
        ButtonForTest.withAction("javascript :onSomething=functionCall();")
            .andPreProcessing("if(componentId!==null){{action}}else{return false;}");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("javascript:onSomething=handleButtonAction();"));
    String expectedScript =
        "<script type='text/javascript' language='Javascript'>function handleButtonAction() " +
            "{\nif(componentId!==null){functionCall();}else{return false;}\n}</script>\n";
    assertThat(printResult,
        is(expectedScript + "<ACTION>javascript:onSomething=handleButtonAction();</ACTION>"));
  }

  @Test
  public void printAngularEventActionWithoutPreProcessing() {
    ButtonForTest buttonForTest = ButtonForTest.withAction("angularjs:onSomething=functionCall()");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("angularjs:onSomething=functionCall()"));
    assertThat(printResult, is("<ACTION>angularjs:onSomething=functionCall()</ACTION>"));
  }

  @Test
  public void printAngularEventActionWithPreProcessing() {
    ButtonForTest buttonForTest = ButtonForTest.withAction("angularjs :onSomething=functionCall();")
        .andPreProcessing("if(componentId!==null){{action}}else{return false;}");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("angularjs:onSomething=handleButtonAction();"));
    String expectedScript =
        "<script type='text/javascript' language='Javascript'>function handleButtonAction() " +
            "{\nif(componentId!==null){functionCall();}else{return false;}\n}</script>\n";
    assertThat(printResult,
        is(expectedScript + "<ACTION>angularjs:onSomething=handleButtonAction();</ACTION>"));
  }


  @Test
  public void printSimpleHrefActionWithoutPreProcessing() {
    ButtonForTest buttonForTest = ButtonForTest.withAction("/silverpeas/service/resource/id");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("/silverpeas/service/resource/id"));
    assertThat(printResult, is("<ACTION>/silverpeas/service/resource/id</ACTION>"));
  }

  @Test
  public void printSimpleHrefActionWithPreProcessing() {
    ButtonForTest buttonForTest = ButtonForTest.withAction("/silverpeas/service/resource/id")
        .andPreProcessing("if(componentId!==null){{action}}else{return false;}");
    String printResult = buttonForTest.print();
    assertThat(buttonForTest.getAction(), is("javascript:onClick=handleButtonAction();"));
    String expectedScript =
        "<script type='text/javascript' language='Javascript'>function handleButtonAction() " +
            "{\nif(componentId!==null){jQuery('<form>', {'method':'GET', " +
            "'action':'/silverpeas/service/resource/id'}).submit();}else{return false;" +
            "}\n}</script>\n";
    assertThat(printResult,
        is(expectedScript + "<ACTION>javascript:onClick=handleButtonAction();</ACTION>"));
  }


  /**
   * Implementation of {@link Button}
   */
  private static class ButtonForTest extends AbstractButton {

    public static ButtonForTest withAction(String action) {
      ButtonForTest buttonForTest = new ButtonForTest();
      buttonForTest.init("label", action, false);
      return buttonForTest;
    }

    private ButtonForTest() {
    }

    @Override
    public String renderButtonHtml() {
      return "<ACTION>" + getAction() + "</ACTION>";
    }

    public ButtonForTest andPreProcessing(String preProcessing) {
      setActionPreProcessing(preProcessing);
      return this;
    }
  }
}
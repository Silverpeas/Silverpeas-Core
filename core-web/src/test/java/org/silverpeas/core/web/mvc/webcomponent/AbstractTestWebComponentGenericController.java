/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author: Yohann Chastagnier
 */
public abstract class AbstractTestWebComponentGenericController<WEB_COMPONENT_REQUEST_CONTEXT
    extends WebComponentRequestContext>
    extends WebComponentController<WEB_COMPONENT_REQUEST_CONTEXT> {

  private LocalizationBundle4Test resourceLocatorMock;

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public AbstractTestWebComponentGenericController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext);
    resourceLocatorMock = new LocalizationBundle4Test();
  }

  @Override
  public LocalizationBundle getMultilang() {
    return resourceLocatorMock;
  }

  @Override
  protected void onInstantiation(final WEB_COMPONENT_REQUEST_CONTEXT context) {
  }

  /**
   * @author Yohann Chastagnier
   */
  public static class LocalizationBundle4Test extends LocalizationBundle {

    private Map<String, Integer> counters = new HashMap<>();

    private LocalizationBundle4Test() {
      this(null, null, null);
    }

    protected LocalizationBundle4Test(final String name, final Locale locale,
        final BiFunction<String, Locale, ResourceBundle> loader) {
      super(name, locale, loader, true);
    }

    @Override
    public Object handleGetObject(final String key) {
      counters.put(key, (counters.getOrDefault(key, 0) + 1));
      return "";
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.emptyEnumeration();
    }

    public void verifyNbCallOf(String key, int times) {
      assertThat(counters.getOrDefault(key, 0), is(times));
    }
  }
}

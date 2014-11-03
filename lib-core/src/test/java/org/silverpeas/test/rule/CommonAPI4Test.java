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

package org.silverpeas.test.rule;

import org.junit.Before;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.test.TestBeanContainer;
import org.silverpeas.test.lang.TestSystemWrapper;
import org.silverpeas.util.lang.DefaultSystemWrapper;
import org.silverpeas.util.lang.SystemWrapper;

import static org.mockito.Mockito.when;

/**
 * @author Yohann Chastagnier
 */
public class CommonAPI4Test implements TestRule {

  @Override
  public Statement apply(final Statement base, final Description description) {

    systemWrapper();

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
      }
    };
  }

  public void systemWrapper() {
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(SystemWrapper.class))
        .thenReturn(new TestSystemWrapper());
  }
}

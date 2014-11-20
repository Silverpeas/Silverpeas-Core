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

package org.silverpeas.web.environment;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.silverpeas.web.environment.SilverpeasEnvironmentTest.getSilverpeasEnvironmentTest;


/**
 * This useful rule permits to specify the environment context of a Silverpeas test.
 * It deals with {@link SilverpeasEnvironmentTest} which provides all methods to specify an
 * Silverpeas environment. Indeed, after each test method, this rule calls the method from {@link
 * SilverpeasEnvironmentTest} that permits to clear the database populated by this tool.
 * @author Yohann Chastagnier
 */
public class SilverpeasEnvironmentTestRule implements TestRule {


  /**
   * Default constructor.
   */
  public SilverpeasEnvironmentTestRule() {
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          initializeData();
          base.evaluate();
        } finally {
          clearData();
        }
      }
    };
  }

  /**
   * Initialize treatments that must be done after a test execution.
   */
  private void initializeData() {
    getSilverpeasEnvironmentTest().initialize();
  }

  /**
   * Clear treatments that must be done after a test execution.
   */
  private void clearData() {
    getSilverpeasEnvironmentTest().clear();
  }
}

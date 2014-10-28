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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;

import static org.apache.commons.io.FileUtils.getFile;

/**
 * @author Yohann Chastagnier
 */
public class MavenTargetDirectoryRule implements TestRule {

  private Object testInstance;

  /**
   * Gets the target directory of the test.
   * @return the target directory of the test.
   */
  public File getBuildDirFile() {
    if (testInstance == null) {
      return null;
    }
    File targetDir = new File(
        testInstance.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
    while (targetDir != null && (!targetDir.getName().equalsIgnoreCase("target") &&
        !targetDir.getName().equalsIgnoreCase("content"))) {
      targetDir = targetDir.getParentFile();
    }
    if (targetDir != null && targetDir.getName().equalsIgnoreCase("content")) {
      targetDir = getFile(targetDir.getParentFile(), "target");
    }
    return targetDir;
  }

  public MavenTargetDirectoryRule(Object testInstance) {
    this.testInstance = testInstance;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
      }
    };
  }
}

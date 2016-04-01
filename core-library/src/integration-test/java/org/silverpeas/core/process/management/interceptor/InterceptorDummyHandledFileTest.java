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

package org.silverpeas.core.process.management.interceptor;

import org.silverpeas.core.process.io.file.AbstractDummyHandledFile;

/**
 * @author Yohann Chastagnier
 */
public class InterceptorDummyHandledFileTest extends AbstractDummyHandledFile {

  private final InterceptorTestFile file;
  private final int oldOperand;

  public InterceptorDummyHandledFileTest(final InterceptorTestFile file, final boolean isOld) {
    this.file = file;
    this.oldOperand = isOld ? 1 : -1;
  }

  @Override
  public String getComponentInstanceId() {
    return "dummy";
  }

  @Override
  public String getPath() {
    return "dummy";
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public long getSize() {
    return 26 * oldOperand;
  }

  @Override
  public String getMimeType() {
    return "dummy";
  }
}

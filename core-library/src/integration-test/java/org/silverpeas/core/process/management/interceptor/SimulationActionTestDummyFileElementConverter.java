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

import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public class SimulationActionTestDummyFileElementConverter
    extends AbstractDummyHandledFileConverter<InterceptorTestSimulationElement> {

  @Override
  public void init() {
    super.init();
  }

  @Override
  public void release() {
    super.release();
  }

  @Override
  public List<DummyHandledFile> convert(final List<InterceptorTestSimulationElement> elements,
      final WAPrimaryKey targetPK, final ActionType actionType) {
    List<DummyHandledFile> dummyHandledFiles = new ArrayList<>();
    for (InterceptorTestSimulationElement element : elements) {

      // Adding the dummy representation of the source element in case of update and if the
      // current element is an old one (deletion)
      // Adding the dummy representation of the source element in case of move and if the
      // current element is not an old one(deletion)
      if ((actionType.isUpdate() && element.isOld()) || (actionType.isMove() && !element.isOld())) {
        dummyHandledFiles.add(new InterceptorDummyHandledFileTest(element.getElement(), true));
      }

      // Adding the dummy representation of the target file, if the current element is not an
      // old one
      if (!element.isOld()) {
        dummyHandledFiles.add(new InterceptorDummyHandledFileTest(element.getElement(), false));
      }
    }
    return dummyHandledFiles;
  }
}

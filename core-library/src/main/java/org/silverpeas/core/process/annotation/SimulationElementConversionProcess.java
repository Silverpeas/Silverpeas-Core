/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.process.io.file.FileHandler;
import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.session.ProcessSession;
import org.silverpeas.core.process.management.AbstractFileProcess;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;

import java.util.List;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 17/10/13
 */
public class SimulationElementConversionProcess
    extends AbstractFileProcess<ProcessExecutionContext> {

  private final Map<Class<SimulationElement>, List<SimulationElement>> elements;
  private final WAPrimaryKey targetPK;
  private final ActionType actionType;

  /**
   * Default constructor.
   * @param elements
   * @param targetPK
   * @param actionType
   */
  SimulationElementConversionProcess(
      final Map<Class<SimulationElement>, List<SimulationElement>> elements,
      final WAPrimaryKey targetPK, final ActionType actionType) {
    this.elements = elements;
    this.targetPK = targetPK;
    this.actionType = actionType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processFiles(final ProcessExecutionContext processExecutionContext,
      final ProcessSession session, final FileHandler fileHandler) throws Exception {

    // Converting each element
    for (Map.Entry<Class<SimulationElement>, List<SimulationElement>> typeElements : elements
        .entrySet()) {

      // Getting the right converter according to the type of elements
      DummyHandledFileConverter<? extends SimulationElement> converter =
          DummyHandledFileConverterRegistration.getConverter(typeElements.getKey());

      // Technical assertion
      if (converter == null) {
        throw new AssertionError(
            "SimulationElementConversionProcess.processFiles : converter is null " +
                "(converter must exist for a type of element)");
      }

      // Convert elements and add each one converted to the file handler
      List<DummyHandledFile> handledFiles =
          converter.convert((List) typeElements.getValue(), targetPK, actionType);
      for (DummyHandledFile dummyHandledFile : handledFiles) {
        fileHandler.addDummyHandledFile(dummyHandledFile);
      }
    }
  }
}

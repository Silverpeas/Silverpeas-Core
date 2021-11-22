/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;

import java.util.List;

/**
 * This interface provides services to convert an element from any type to a dummy handled file.
 * <p>
 * @author Yohann Chastagnier
 */
public interface DummyHandledFileConverter<S extends SimulationElement<?>> extends Initialization {

  /**
   * Method that contains the treatment of the conversion.
   * @param elements elements to convert
   * @param target reference to the targeted resource in Silverpeas
   * @param actionType the action type used for the conversion
   * @return the list of converted elements.
   */
  List<DummyHandledFile> convert(final List<S> elements, final ResourceReference target,
      final ActionType actionType);
}

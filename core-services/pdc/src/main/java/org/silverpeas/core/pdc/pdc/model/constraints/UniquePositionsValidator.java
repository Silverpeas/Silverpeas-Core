/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.model.constraints;

import org.silverpeas.core.pdc.pdc.model.PdcPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * The validator associated with the {@link UniquePositions} constraint.
 *
 * @author mmoquillon
 */
public class UniquePositionsValidator implements
    ConstraintValidator<UniquePositions, Set<PdcPosition>> {

  @Override
  public void initialize(UniquePositions constraintAnnotation) {
  }

  @Override
  public boolean isValid(Set<PdcPosition> pdcPositions, ConstraintValidatorContext context) {
    List<PdcPosition> positions = new ArrayList<PdcPosition>(pdcPositions);
    for (int p = 0; p < positions.size(); p++) {
      PdcPosition currentPosition = positions.get(p);
      for (int i = 0; i < positions.size(); i++) {
        if (p != i && currentPosition.getValues().size() == positions.get(i).getValues().size()) {
          if (currentPosition.getValues().containsAll(positions.get(i).getValues())) {
            return false;
          }
        }
      }
    }
    return true;
  }
}

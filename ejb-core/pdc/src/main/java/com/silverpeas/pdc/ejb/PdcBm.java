/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.ejb;

import java.util.List;

import javax.ejb.Local;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;

/**
 * Interface declaration
 *
 * @author neysseri
 */
@Local
public interface PdcBm {

  public List<Value> getDaughters(String axisId, String valueId);

  public List<Value> getSubAxisValues(String axisId, String valueId);

  public List<GlobalSilverContent> findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List<String> componentIds,
      boolean recursiveSearch, boolean visibilitySensitive);

  public List<GlobalSilverContent> findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List<String> componentIds, String authorId,
      String afterDate, String beforeDate, boolean recursiveSearch, boolean visibilitySensitive);

  public int getSilverContentId(String objectId, String componentId);

  public List<ClassifyPosition> getPositions(int silverContentId, String componentId);

  public Value getValue(String axisId, String valueId);

  public List<Integer> getSilverContentIds(List<String> docFeatures);

  public String getInternalContentId(int silverContentId);

  public AxisHeader getAxisHeader(String axisId);

  public String createDaughterValueWithId(String axisId, Value value);

  public int addPosition(int pubId, ClassifyPosition position, String componentId,
      boolean alertSubscribers);

  public void removeAllPositions(int pubId, String componentId);

  public List<Value> getAxisValues(int treeId);

  public int addUsedAxis(UsedAxis usedAxis);
}

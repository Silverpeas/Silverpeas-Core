/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.contentManager.*;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.*;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

@Stateless(name = "Pdc", description = "Stateless session bean to manage the pdc.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class PdcBmEJB implements PdcBm {

  private static final long serialVersionUID = -1829121357409874581L;
  com.stratelia.silverpeas.pdc.control.PdcBm pdc = null;
  ContentManager contentManager = null;

  public PdcBmEJB() {
  }

  private com.stratelia.silverpeas.pdc.control.PdcBm getPdcBm() {
    if (pdc == null) {
      pdc = new PdcBmImpl();
    }
    return pdc;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        System.out.println(e.toString());
      }
    }
    return contentManager;
  }

  @Override
  public List<GlobalSilverContent> findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List<String> componentIds,
      boolean recursiveSearch, boolean visibilitySensitive) {
    List<Integer> silverContentIds = new ArrayList<Integer>();
    try {
      // get the silverContentids classified in the context
      silverContentIds.addAll(getPdcBm().findSilverContentIdByPosition(containerPosition,
          componentIds, recursiveSearch, visibilitySensitive));
    } catch (ContainerManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.findGlobalSilverContents",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    return getSilverContentsByIds(silverContentIds);
  }

  @Override
  public List<GlobalSilverContent> findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List<String> componentIds,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) {
    List<Integer> silverContentIds = new ArrayList<Integer>();
    try {
      // get the silverContentids classified in the context
      silverContentIds.addAll(getPdcBm().findSilverContentIdByPosition(
          containerPosition, componentIds, authorId, afterDate, beforeDate,
          recursiveSearch, visibilitySensitive));
    } catch (ContainerManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.findGlobalSilverContents",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    return getSilverContentsByIds(silverContentIds);
  }

  @Override
  public Value getValue(String axisId, String valueId) {
    SilverTrace.info("Pdc", "PdcBmEJB.getValue", "root.MSG_GEN_PARAM_VALUE",
        "axisId = " + axisId + ", valueId = " + valueId);
    Value value = null;
    try {
      value = getPdcBm().getValue(axisId, valueId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getValue",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    if (value != null) {
      SilverTrace.info("Pdc", "PdcBmEJB.getValue", "root.MSG_GEN_PARAM_VALUE",
          "returned value name = " + value.getName());
    }
    return value;
  }

  @Override
  public String createDaughterValueWithId(String axisId, Value value) {
    try {
      return getPdcBm().createDaughterValueWithId(value, value.getFatherId(), axisId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.createDaughterValueWithId",
          SilverpeasRuntimeException.ERROR, "Pdc.CANNOT_CREATE_AXE", e);
    }
  }

  @Override
  public int addPosition(int pubId, ClassifyPosition position, String componentId,
      boolean alertSubscribers) {
    try {
      return getPdcBm().addPosition(pubId, position, componentId, alertSubscribers);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.addPosition",
          SilverpeasRuntimeException.ERROR, "Pdc.CANNOT_CREATE_VALUE", e);
    }
  }

  @Override
  public List<Value> getDaughters(String axisId, String valueId) {
    return getPdcBm().getDaughters(axisId, valueId);
  }

  @Override
  public List<Value> getSubAxisValues(String axisId, String valueId) {
    return getPdcBm().getSubAxisValues(axisId, valueId);
  }

  @Override
  public int getSilverContentId(String objectId, String componentId) {
    try {
      return getContentManager().getSilverContentId(objectId, componentId);
    } catch (ContentManagerException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentId",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public List<ClassifyPosition> getPositions(int silverContentId, String componentId) {
    try {
      return getPdcBm().getPositions(silverContentId, componentId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getPositions",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public List<Integer> getSilverContentIds(List<String> docFeatures) {
    try {
      return new ArrayList<Integer>(getContentManager().getSilverContentId(docFeatures));
    } catch (ContentManagerException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentIds",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public String getInternalContentId(int silverContentId) {
    try {
      return getContentManager().getInternalContentId(silverContentId);
    } catch (ContentManagerException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getInternalContentId",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public AxisHeader getAxisHeader(String axisId) {
    try {
      return getPdcBm().getAxisHeader(axisId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getAxisHeader",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public void removeAllPositions(int silverContentId, String componentId) {
    List<ClassifyPosition> positions = getPositions(silverContentId, componentId);
    if (positions != null) {
      for (ClassifyPosition position : positions) {
        try {
          getPdcBm().deletePosition(position.getPositionId(), componentId);
        } catch (PdcException e) {
          throw new PdcBmRuntimeException("PdcBmEJB.getAxisHeader",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
      }
    }
  }

  @Override
  public List<Value> getAxisValues(int treeId) {
    try {
      return getPdcBm().getAxisValues(treeId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getAxisValues",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  @Override
  public int addUsedAxis(UsedAxis usedAxis) {
    try {
      return getPdcBm().addUsedAxis(usedAxis);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.usedAxis",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private List<GlobalSilverContent> getSilverContentsByIds(List<Integer> silverContentIds) {
    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds", "root.MSG_GEN_PARAM_VALUE",
        "silverContentIds = " + silverContentIds);
    // recherche des componentId a partir de silverContentId
    ContentPeas contentP = null;
    List<GlobalSilverContent> alSilverContents = new ArrayList<GlobalSilverContent>();
    List<String> alInstanceIds = new ArrayList<String>();

    try {
      // on récupère la liste de instance contenant tous les documents
      alInstanceIds = getContentManager().getInstanceId(silverContentIds);
      SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
          "root.MSG_GEN_PARAM_VALUE", "alInstanceIds = " + alInstanceIds);
    } catch (ContentManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    // une fois la liste des instanceId définie, on parcourt cette liste pour
    // en retirer les SilverContentIds propre à chaque instanceId.
    List<Integer> allSilverContentIds = new ArrayList<Integer>();
    List<Integer> newAlSilverContentIds = new ArrayList<Integer>();

    for (String instanceId : alInstanceIds) {
      try {
        contentP = getContentManager().getContentPeas(instanceId);
        // On récupère tous les silverContentId d'un instanceId
        allSilverContentIds = getContentManager().getSilverContentIdByInstanceId(instanceId);
        SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds", "root.MSG_GEN_PARAM_VALUE",
            "allSilverContentIds = " + allSilverContentIds + " in instance " + instanceId);
      } catch (ContentManagerException c) {
        throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
      }

      // une fois les SilverContentId de l'instanceId récupérés, on ne garde
      // que ceux qui sont dans la liste résultat (alSilverContentIds).
      allSilverContentIds.retainAll(silverContentIds);

      List<SilverContentInterface> silverContentTempo = null;
      if (contentP != null) {
        try {
          // we are going to search only SilverContent of this instanceId
          ContentInterface contentInterface = contentP.getContentInterface();
          silverContentTempo = contentInterface.getSilverContentById(
              allSilverContentIds, instanceId, null, new ArrayList<String>());
        } catch (ContentManagerException c) {
          throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
        } catch (Exception e) {
          throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }
        alSilverContents.addAll(transformSilverContentsToGlobalSilverContents(silverContentTempo));
      }
      newAlSilverContentIds.addAll(allSilverContentIds);
    }
    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds", "root.MSG_GEN_PARAM_VALUE",
        "silverContent size= " + alSilverContents.size());

    // replace old SilverContentId list by the new one, to assure the same order
    silverContentIds.clear();
    silverContentIds.addAll(newAlSilverContentIds);
    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds", "root.MSG_GEN_PARAM_VALUE",
        "silverContentIds = " + silverContentIds);
    return alSilverContents;
  }

  /*
   * @return a List of GlobalSilverContent
   */
  private List<GlobalSilverContent> transformSilverContentsToGlobalSilverContents(
      List<SilverContentInterface> silverContentTempo) {
    ArrayList<GlobalSilverContent> silverContents = new ArrayList<GlobalSilverContent>();
    GlobalSilverContent gsc = null;
    for (SilverContentInterface sci : silverContentTempo) {
      gsc = new GlobalSilverContent(sci, "useless", null, null);
      silverContents.add(gsc);
    }
    return silverContents;
  }
}
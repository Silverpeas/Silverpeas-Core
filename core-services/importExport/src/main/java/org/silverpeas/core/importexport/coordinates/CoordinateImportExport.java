/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.importexport.coordinates;

import org.silverpeas.core.node.coordinates.service.CoordinatesService;
import org.silverpeas.core.node.coordinates.model.CoordinatePK;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.coordinates.model.CoordinateRuntimeException;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Classe gerant la manipulation des axes de coordinates pour le module d'importExport.
 *
 * @author dlesimple
 */
public class CoordinateImportExport {

  @Inject
  private NodeService nodeService;

  @Inject
  private CoordinatesService coordinatesService;

  protected CoordinateImportExport() {

  }

  /**
   * @param componentId
   * @param axisPath (/0/1024/1043,/0/1036,/0/1040)
   * @return coordinateId
   * @throws CoordinateRuntimeException
   */
  public int addPositions(String componentId, String axisPath)
      throws CoordinateRuntimeException {
    List<String> combination = getArrayCombination(axisPath);
    int coordinateId = 0;
    NodePK axisPK = new NodePK("toDefine", componentId);
    CoordinatePK coordinatePK = new CoordinatePK("unknown", axisPK);
    try {
      // enrich combination by get ancestors
      List<CoordinatePoint> allnodes = new ArrayList<CoordinatePoint>();
      int i = 1;
      for (String nodeId : combination) {

        NodePK nodePK = new NodePK(nodeId, componentId);

        Collection<NodeDetail> path = nodeService.getPath(nodePK);

        for (NodeDetail nodeDetail : path) {
          String anscestorId = nodeDetail.getNodePK().getId();
          int nodeLevel = nodeDetail.getLevel();
          if (!nodeDetail.getNodePK().isRoot()) {
            CoordinatePoint point;
            if (anscestorId.equals(nodeId)) {
              point = new CoordinatePoint(-1, Integer.parseInt(anscestorId), true, nodeLevel, i);
            } else {
              point = new CoordinatePoint(-1, Integer.parseInt(anscestorId), false, nodeLevel, i);
            }
            allnodes.add(point);
          }
        }
        i++;
      }
      coordinateId = getCoordinatesService().addCoordinate(coordinatePK, allnodes);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.addPositions()",
          SilverpeasRuntimeException.ERROR, "coordinates.ADDING_COORDINATES_COMBINATION_FAILED", e);
    }
    return coordinateId;
  }

  /**
   * Get a node by its name.
   *
   * @param name
   * @param nodeRootId
   * @param componentId
   * @return
   */
  public NodeDetail getNodeDetailByName(String name, int nodeRootId, String componentId) {
    try {
      NodeDetail nodeDetail = getNodeService().getDetailByNameAndFatherId(
          new NodePK("useless", componentId), name, nodeRootId);
      return nodeDetail;
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.addNodesToPublication()",
          SilverpeasRuntimeException.ERROR, "coordinates.ATTACHING_NODES_TO_PUBLICATION_FAILED", e);
    }
  }

  /**
   * Get ArrayList of valuePath (/xx/yyy, /xx/yyy/zzzz/ to arrayList with /xx/yy then /xx/yyy/zzzz)
   *
   * @param valuePath
   * @return ArrayList
   */
  private List<String> getArrayCombination(String valuePath) {
    StringTokenizer st = new StringTokenizer(valuePath, ",");
    List<String> combination = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String axisValue = st.nextToken();
      axisValue = axisValue.substring(axisValue.lastIndexOf('/') + 1, axisValue.length());
      combination.add(axisValue);
    }
    return combination;
  }

  /**
   * Get ArrayList of valuePath labels (/xx/yyy, /xx/yyy/zzzz/ to arrayList with Axe1 > value15 then
   * Axe2 > value5)
   *
   * @param combination
   * @param componentId
   * @return a List of displayName: Axe1 > value15
   * @throws RemoteException
   */
  public List<String> getCombinationLabels(List<String> combination, String componentId)
      throws RemoteException {
    List<String> coordinatesLabels = new ArrayList<String>();
    for (String position : combination) {
      StringTokenizer st = new StringTokenizer(position, "/");
      StringBuilder pathName = new StringBuilder();
      while (st.hasMoreTokens()) {
        String nodeId = st.nextToken();
        NodeDetail nodeDetail = getNodeHeader(nodeId, componentId);
        // Don't take the root
        if (nodeDetail.getLevel() > 1) {
          if (nodeDetail.getLevel() == 2) {
            pathName.append("<b>").append(nodeDetail.getName()).append("</b>").append(" > ");
          } else if (!st.hasMoreTokens()) {
            pathName.append(nodeDetail.getName()).append("<br />");
          } else {
            pathName.append(nodeDetail.getName()).append(" > ");
          }
        }
      }
      coordinatesLabels.add(pathName.toString());
    }
    return coordinatesLabels;
  }

  public Collection<NodeDetail> getPathOfNode(NodePK nodePk) {
    return getNodeService().getPath(nodePk);
  }

  /**
   * Get axis of the component
   *
   * @param componentId
   * @return
   */
  public List<NodeDetail> getAxis(String componentId) {
    SettingBundle nodeSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List<NodeDetail> axis = new ArrayList<>();
    try {
      List headers = getAxisHeaders(componentId);
      NodeDetail header = null;
      for (int h = 0; h < headers.size(); h++) {
        header = (NodeDetail) headers.get(h);
        // Do not get hidden nodes (Basket and unclassified)
        if (!NodeDetail.STATUS_INVISIBLE.equals(header.getStatus())) // get content of this axis
        {
          axis.addAll(getNodeService().getSubTree(header.getNodePK(), sortField + " " + sortOrder));
        }
      }
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getAxis()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
    }
    return axis;
  }

  /**
   * Get axis header with Children
   *
   * @param componentId
   * @return
   */
  public List<NodeDetail> getAxisHeadersWithChildren(String componentId,
      boolean includeUnclassified,
      boolean takeAxisInChildrenList) {
    SettingBundle nodeSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List<NodeDetail> axis = new ArrayList<>();
    try {
      List<NodeDetail> headers = getAxisHeaders(componentId);
      for (NodeDetail header : headers) {
        Collection<NodeDetail> children = new ArrayList<NodeDetail>();
        if (Integer.parseInt(header.getNodePK().getId()) > 1 && includeUnclassified) {
          children = getNodeService().getSubTree(header.getNodePK(), sortField + " " + sortOrder);
          if (!takeAxisInChildrenList) {
            children.remove(children.iterator().next());
          }
          header.setChildrenDetails(children);
          axis.add(header);
        }
      }
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getAxisHeadersWithChildren()",
          SilverpeasRuntimeException.ERROR, "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
    }
    return axis;
  }

  /**
   * Get children of an axis
   *
   * @param nodePK
   * @param takeAxisInChildrenList
   * @return
   */
  public List getAxisChildren(NodePK nodePK, boolean takeAxisInChildrenList) {
    SettingBundle nodeSettings =
        ResourceLocator.getSettingBundle("org.silverpeas.node.nodeSettings");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List<NodeDetail> children = new ArrayList<NodeDetail>();
    try {
      children = getNodeService().getSubTree(nodePK, sortField + " " + sortOrder);
      if (!takeAxisInChildrenList) {
        children.remove(children.iterator().next());
      }
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getAxisChildren()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_VALEURS_DUN_AXE",
          e);
    }
    return children;
  }

  /**
   * @param componentId
   * @return
   */
  public List<NodeDetail> getAxisHeaders(String componentId) {
    try {
      return getNodeService().getHeadersByLevel(new NodePK("useless", componentId), 2);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getAxisHeaders()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_ENTETES_DES_AXES", e);
    }
  }

  /**
   * Add position top the axis
   *
   * @param position , ComponentId
   * @return nodePK
   */
  public NodeDetail addPosition(NodeDetail position, String axisId, String componentId) {
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(DateUtil.today2SQLDate());
    NodeDetail fatherDetail = null;
    NodePK positionPK = null;
    NodeDetail positionDetail = null;

    fatherDetail = getNodeHeader(axisId, componentId);
    try {
      fatherDetail = getNodeHeader(axisId, componentId);
      positionPK = getNodeService().createNode(position, fatherDetail);

      positionDetail = getNodeHeader(positionPK);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.addPosition()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DAJOUTER_UNE_COMPOSANTE_A_L_AXE", e);
    }
    return positionDetail;
  }

  /**
   * Get node Detail
   */
  public NodeDetail getNodeHeader(NodePK pk) {
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeService().getHeader(pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getNodeHeader()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  /**
   * Get node Detail
   *
   * @param id
   * @param componentId
   * @return
   */
  public NodeDetail getNodeHeader(String id, String componentId) {
    NodePK pk = new NodePK(id, componentId);
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeService().getHeader(pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getNodeHeader()",
          SilverpeasRuntimeException.ERROR, "coordinates.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  /**
   * @return CoordinatesService layer
   * @throws CoordinateRuntimeException
   */
  private CoordinatesService getCoordinatesService() {
    try {
      return coordinatesService;
    } catch (Exception e) {
      throw new CoordinateRuntimeException("CoordinateImportExport.getCoordinatesService()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * @return l'EJB NodeBm
   */
  private NodeService getNodeService() {
    return nodeService;
  }

  /**
   * Generate index files with combination
   *
   * @param filesNames
   * @param nodeIds
   * @param cur
   * @param loop
   * @param nTuple
   * @param prefixeId
   * @return
   */
  public List<String> coupleIds(List<String> filesNames, List<String> nodeIds, int cur, int loop,
      int nTuple, String prefixeId, int nbAxis) {
    for (int i = cur; i < nodeIds.size(); i++) {
      String terme = nodeIds.get(i);
      String tmp;
      if (prefixeId == null) {
        tmp = "" + terme;
      } else {
        tmp = prefixeId + "-" + terme;
      }
      if (loop < nTuple - 1) {
        coupleIds(filesNames, nodeIds, i + 1, loop + 1, nTuple, tmp, nbAxis);
      } else {
        String positionNameId = "index-" + tmp + ".html";
        StringTokenizer st = new StringTokenizer(positionNameId, "-");
        if (st.countTokens() - 1 == nbAxis) // don't consider first token: index
        {
          filesNames.add(positionNameId);
        }
      }
    }
    return filesNames;
  }
}
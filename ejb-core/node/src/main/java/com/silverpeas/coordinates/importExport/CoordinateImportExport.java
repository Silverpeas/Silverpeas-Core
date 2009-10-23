/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.coordinates.importExport;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBm;
import com.stratelia.webactiv.util.coordinates.control.CoordinatesBmHome;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePK;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.coordinates.model.CoordinateRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome; /*import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationPK;*/

import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Classe gerant la manipulation des axes de coordinates pour le module
 * d'importExport.
 * 
 * @author dlesimple
 */
public class CoordinateImportExport {

  /**
   * @param componentId
   * @param axisPath
   *          (/0/1024/1043,/0/1036,/0/1040)
   * @return coordinateId
   * @throws CoordinateRuntimeException
   */
  public int addPositions(String componentId, String axisPath)
      throws CoordinateRuntimeException {
    ArrayList combination = getArrayCombination(axisPath);
    SilverTrace.debug("coordinates", "CoordinateImportExport.addPositions",
        "root.MSG_GEN_PARAM_VALUE", "combination = " + combination);

    int coordinateId = 0;
    NodePK axisPK = new NodePK("toDefine", componentId);
    CoordinatePK coordinatePK = new CoordinatePK("unknown", axisPK);
    try {
      NodeBm nodeBm = getNodeBm();
      NodeDetail nodeDetail = null;
      // enrich combination by get ancestors
      Iterator it = combination.iterator();
      ArrayList allnodes = new ArrayList();
      CoordinatePoint point = null;
      String anscestorId = "";
      int nodeLevel;
      int i = 1;
      while (it.hasNext()) {
        String nodeId = (String) it.next();
        SilverTrace.info("coordinates",
            "CoordinateImportExport.addPositions()",
            "root.MSG_GEN_PARAM_VALUE", "nodeId = " + nodeId);
        NodePK nodePK = new NodePK(nodeId, componentId);
        SilverTrace.info("coordinates",
            "CoordinateImportExport.addPositions()",
            "root.MSG_GEN_PARAM_VALUE", "avant nodeBm.getPath() ! i = " + i);
        Collection path = nodeBm.getPath(nodePK);
        SilverTrace.info("coordinates",
            "CoordinateImportExport.addPositions()",
            "root.MSG_GEN_PARAM_VALUE", "path for nodeId " + nodeId + " = "
                + path.toString());
        Iterator pathIt = path.iterator();
        while (pathIt.hasNext()) {
          nodeDetail = (NodeDetail) pathIt.next();
          anscestorId = nodeDetail.getNodePK().getId();
          nodeLevel = nodeDetail.getLevel();
          if (!anscestorId.equals("0")) {
            if (anscestorId.equals(nodeId))
              point = new CoordinatePoint(-1, new Integer(anscestorId)
                  .intValue(), true, nodeLevel, i);
            else
              point = new CoordinatePoint(-1, new Integer(anscestorId)
                  .intValue(), false, nodeLevel, i);
            allnodes.add(point);
          }
        }
        i++;
      }
      coordinateId = getCoordinatesBm().addCoordinate(coordinatePK, allnodes);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.addPositions()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ADDING_COORDINATES_COMBINATION_FAILED", e);
    }
    SilverTrace.debug("coordinates", "CoordinateImportExport.addPositions()",
        "root.MSG_GEN_PARAM_VALUE", "coordinateId = " + coordinateId);
    return coordinateId;
  }

  /**
   * Add nodes (coordinatesId) to a publication
   * 
   * @param PublicationDetail
   *          , List of coordinateId
   * @return nothing
   */
  /*
   * public void addNodesToPublication(PublicationPK pubPK, List nodes) { try {
   * Iterator nodesIterator = nodes.iterator(); while (nodesIterator.hasNext())
   * { Integer coordinateId = (Integer) nodesIterator.next();
   * getPublicationBm().addFather(pubPK, new NodePK(coordinateId.toString(),
   * pubPK)); } } catch (Exception e) { throw new
   * CoordinateRuntimeException("CoordinateImportExport.addNodesToPublication()"
   * ,SilverpeasRuntimeException.ERROR,
   * "coordinates.ATTACHING_NODES_TO_PUBLICATION_FAILED", e); } }
   */

  /**
   * Get NodeByName
   */
  public NodeDetail getNodeDetailByName(String name, int nodeRootId,
      String componentId) {
    SilverTrace.debug("coordinates",
        "CoordinateImportExport.getNodeDetailByName()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + name + " nodeRootId="
            + nodeRootId + " componentId=" + componentId);
    try {
      NodeDetail nodeDetail = getNodeBm().getDetailByNameAndFatherId(
          new NodePK("useless", componentId), name, nodeRootId);
      return nodeDetail;
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.addNodesToPublication()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.ATTACHING_NODES_TO_PUBLICATION_FAILED", e);
    }
  }

  /**
   * Get ArrayList of valuePath (/xx/yyy, /xx/yyy/zzzz/ to arrayList with /xx/yy
   * then /xx/yyy/zzzz)
   * 
   * @param valuePath
   * @return ArrayList
   */
  private ArrayList getArrayCombination(String valuePath) {
    StringTokenizer st = new StringTokenizer(valuePath, ",");
    ArrayList combination = new ArrayList();
    String axisValue = "";
    while (st.hasMoreTokens()) {
      axisValue = st.nextToken();
      // axisValue is xx/xx/xx where xx are nodeId
      axisValue = axisValue.substring(axisValue.lastIndexOf('/') + 1, axisValue
          .length());
      combination.add(axisValue);
    }
    return combination;
  }

  /**
   * Get ArrayList of valuePath labels (/xx/yyy, /xx/yyy/zzzz/ to arrayList with
   * Axe1 > value15 then Axe2 > value5)
   * 
   * @param valuePath
   * @return ArrayList of displayName: Axe1 > value15
   */
  public ArrayList getCombinationLabels(ArrayList combination,
      String componentId) throws RemoteException {
    ArrayList coordinatesLabels = new ArrayList();
    for (int i = 0; i < combination.size(); i++) {
      String position = (String) combination.get(i);
      StringTokenizer st = new StringTokenizer(position, "/");
      StringBuffer pathName = new StringBuffer();
      while (st.hasMoreTokens()) {
        String nodeId = st.nextToken();
        NodeDetail nodeDetail = getNodeHeader(nodeId, componentId);
        // Don't take the root
        if (nodeDetail.getLevel() > 1) {
          if (nodeDetail.getLevel() == 2)
            pathName.append("<b>").append(nodeDetail.getName()).append("</b>")
                .append(" > ");
          else if (!st.hasMoreTokens())
            pathName.append(nodeDetail.getName()).append("<br>");
          else
            pathName.append(nodeDetail.getName()).append(" > ");
        }
      }
      coordinatesLabels.add(pathName.toString());
    }
    return coordinatesLabels;
  }

  public Collection getPathOfNode(NodePK nodePk) {
    Collection listNodeDetail = null;
    try {
      listNodeDetail = getNodeBm().getPath(nodePk);
    } catch (RemoteException ex) {
      throw new NodeRuntimeException("NodeImportExport.getPathOfNode()",
          SilverpeasRuntimeException.ERROR, "node.GETTING_NODE_PATH_FAILED", ex);
    }
    return listNodeDetail;
  }

  /**
   * Get axis of the component
   * 
   * @param componentId
   * @return
   */
  public List getAxis(String componentId) {
    ResourceLocator nodeSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.node.nodeSettings", "");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List axis = new ArrayList();
    try {
      List headers = getAxisHeaders(componentId);
      NodeDetail header = null;
      for (int h = 0; h < headers.size(); h++) {
        header = (NodeDetail) headers.get(h);
        // Do not get hidden nodes (Basket and unclassified)
        if (!NodeDetail.STATUS_INVISIBLE.equals(header.getStatus()))
          // get content of this axis
          axis.addAll(getNodeBm().getSubTree(header.getNodePK(),
              sortField + " " + sortOrder));
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
  public List getAxisHeadersWithChildren(String componentId,
      boolean includeUnclassified, boolean takeAxisInChildrenList) {
    ResourceLocator nodeSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.node.nodeSettings", "");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List axis = new ArrayList();
    try {
      List headers = getAxisHeaders(componentId);
      NodeDetail header = null;
      for (int h = 0; h < headers.size(); h++) {
        header = (NodeDetail) headers.get(h);
        Collection children = new ArrayList();
        if (new Integer(header.getNodePK().getId()).intValue() > 1
            && includeUnclassified) {
          // children = getNodeBm().getSubTree(header.getNodePK(),
          // sortField+" "+sortOrder);
          children = getNodeBm().getSubTree(header.getNodePK(),
              sortField + " " + sortOrder);
          if (!takeAxisInChildrenList)
            children.remove((NodeDetail) children.iterator().next());
          header.setChildrenDetails(children);
          axis.add(header);
        }
      }
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getAxisHeadersWithChildren()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_AXES", e);
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
    ResourceLocator nodeSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.node.nodeSettings", "");
    String sortField = nodeSettings.getString("sortField", "nodepath");
    String sortOrder = nodeSettings.getString("sortOrder", "asc");
    List children = new ArrayList();
    try {
      children = getNodeBm().getSubTree(nodePK, sortField + " " + sortOrder);
      if (!takeAxisInChildrenList)
        children.remove((NodeDetail) children.iterator().next());
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getAxisChildren()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_VALEURS_DUN_AXE", e);
    }
    return children;
  }

  /**
   * 
   * @param componentId
   * @return
   */
  public List getAxisHeaders(String componentId) {
    List axisHeaders = null;
    try {
      axisHeaders = getNodeBm().getHeadersByLevel(
          new NodePK("useless", componentId), 2);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getAxisHeaders()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LES_ENTETES_DES_AXES", e);
    }
    return axisHeaders;
  }

  /**
   * Add position top the axis
   * 
   * @param position
   *          , ComponentId
   * @return nodePK
   */
  public NodeDetail addPosition(NodeDetail position, String axisId,
      String componentId) {
    SilverTrace.info("coordinates", "CoordinateImportExport.addPosition()",
        "root.MSG_GEN_PARAM_VALUE", "fatherId = " + axisId + " And position = "
            + position.toString());
    position.getNodePK().setComponentName(componentId);
    position.setCreationDate(DateUtil.today2SQLDate());
    NodeDetail fatherDetail = null;
    NodePK positionPK = null;
    NodeDetail positionDetail = null;

    fatherDetail = getNodeHeader(axisId, componentId);
    SilverTrace
        .info("coordinates", "CoordinateImportExport.addPosition()",
            "root.MSG_GEN_PARAM_VALUE", "fatherDetail = "
                + fatherDetail.toString());
    try {
      fatherDetail = getNodeHeader(axisId, componentId);
      positionPK = getNodeBm().createNode(position, fatherDetail);
      SilverTrace.info("coordinates", "CoordinateImportExport.addPosition()",
          "root.MSG_GEN_PARAM_VALUE", "positionPK = " + positionPK.toString());
      positionDetail = getNodeHeader(positionPK);
      SilverTrace.info("coordinates", "CoordinateImportExport.addPosition()",
          "root.MSG_GEN_PARAM_VALUE", "positionDetail = "
              + positionDetail.toString());
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
   * 
   * @param nodePK
   * @param componentId
   * @return
   */
  public NodeDetail getNodeHeader(NodePK pk) {
    NodeDetail nodeDetail = null;
    try {
      nodeDetail = getNodeBm().getHeader(pk);
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
      nodeDetail = getNodeBm().getHeader(pk);
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getNodeHeader()",
          SilverpeasRuntimeException.ERROR,
          "coordinates.EX_IMPOSSIBLE_DOBTENIR_LE_NOEUD", e);
    }
    return nodeDetail;
  }

  /**
   * @return l'EJB CoordinatesBm
   * @throws CoordinateRuntimeException
   */
  private CoordinatesBm getCoordinatesBm() {
    CoordinatesBm coordinatesBm = null;
    try {
      CoordinatesBmHome kscEjbHome = (CoordinatesBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.COORDINATESBM_EJBHOME,
              CoordinatesBmHome.class);
      coordinatesBm = kscEjbHome.create();
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getCoordinatesBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return coordinatesBm;
  }

  /**
   * @return l'EJB NodeBm
   * @throws CoordinateRuntimeException
   */
  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new CoordinateRuntimeException(
          "CoordinateImportExport.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }

  /**
   * @return l'EJB PublicationBm
   * @throws CoordinateRuntimeException
   */
  /*
   * private PublicationBm getPublicationBm() { PublicationBm publicationBm =
   * null; try { PublicationBmHome publicationBmHome = (PublicationBmHome)
   * EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
   * PublicationBmHome.class); publicationBm = publicationBmHome.create(); }
   * catch (Exception e) { throw new
   * CoordinateRuntimeException("CoordinateImportExport.getPublicationBm()"
   * ,SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e); }
   * return publicationBm; }
   */

  /**
   * Get unbalanced publications
   * 
   * @param componentId
   * @return ArrayList of publicationDetail
   */
  /*
   * public Collection getUnbalancedPublications(String componentId) {
   * PublicationPK pk = new PublicationPK("useless", componentId); Collection
   * publications = null; try { publications =
   * getPublicationBm().getOrphanPublications(pk); } catch (Exception e) { throw
   * new
   * CoordinateRuntimeException("CoordinateImportExport.getUnbalancedPublications()"
   * ,SilverpeasRuntimeException.ERROR,
   * "importExport.EX_IMPOSSIBLE_DOBTENIR_LA_LISTE_DES_PUBLICATIONS_NON_CLASSEES"
   * , e); } return publications; }
   */

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
  public List coupleIds(List filesNames, List nodeIds, int cur, int loop,
      int nTuple, String prefixeId, int nbAxis) {
    String tmp;
    for (int i = cur; i < nodeIds.size(); i++) {
      String terme = (String) nodeIds.get(i);
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
        // Take only file with used positions
        if (st.countTokens() - 1 == nbAxis) // don't consider first token: index
          filesNames.add(positionNameId);
      }
    }
    return filesNames;
  }

}
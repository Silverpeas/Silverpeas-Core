package com.silverpeas.tools.checkAttachments;

import com.silverpeas.tools.checkAttachments.model.CheckAttachmentDetail;
import com.silverpeas.tools.checkAttachments.model.OrphanAttachment;
import com.silverpeas.util.ComponentHelper;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.CompletePublication;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CheckAttachmentsBatch {

  private static final String contextAttachment = "Attachment";
  private String language = null;

  public CheckAttachmentsBatch() {
  }

  public List<CheckAttachmentDetail> check(String attachmentType, String reqLanguage) throws
      Exception {
    List<CheckAttachmentDetail> listAttachments = null;
    try {
      // get all attachments
      this.language = reqLanguage;
      listAttachments = getAttachments(attachmentType);
    } catch (Exception e) {
      e.printStackTrace();
      throw new AttachmentRuntimeException("CheckAttachmentsBatch.check()",
          SilverpeasException.ERROR,
          "Erreur lors de la recuperation des attachments", e);
    }
    return listAttachments;
  }

  /**
   * Get orphans attachments (physicalName without logicalName in db) 
   */
  public List<OrphanAttachment> getOrphansFiles(String context) throws RemoteException {
    if (!StringUtil.isDefined(context)) {
      context = "Images";
    }

    List<OrphanAttachment> orphansList = new ArrayList<OrphanAttachment>();
    OrganizationController oc = new OrganizationController();

    //Get all spaces
    String[] spaces = oc.getAllRootSpaceIds();
    if (spaces != null) {
      for (String space : spaces) {
        SilverTrace.info("admin", "CheckAttachmentBatch.getOrphansFiles(context)",
            "root.MSG_GEN_PARAM_VALUE", "spaceid = " + space);
        String[] componentsId = oc.getAllComponentIdsRecur(space);
        //get all components name Folders
        for (int j = 0; componentsId != null && j < componentsId.length; j++) {
          String pathFolder = FileRepositoryManager.getAbsolutePath(componentsId[j])
              + contextAttachment + File.separator + context + File.separator;
          SilverTrace.info("admin", "CheckAttachmentBatch.getOrphansFiles(context)",
              "root.MSG_GEN_PARAM_VALUE", "pathFolder = " + pathFolder);
          //get all files inside
          File path = new File(pathFolder);
          File[] listFiles = path.listFiles();

          if (listFiles != null) {
            for (File listFile : listFiles) {
              SilverTrace.info("admin", "CheckAttachmentBatch.getOrphansFiles(context)",
                  "root.MSG_GEN_PARAM_VALUE", "file = " + listFile.getName());
              if (getAttachmentsByPhysicalName(listFile.getName()).isEmpty()) {
                OrphanAttachment orphanAttachment = new OrphanAttachment();
                orphanAttachment.setPhysicalName(listFile.getName());
                ComponentInstLight componentInst = oc.getComponentInstLight(componentsId[j]);
                SpaceInstLight spaceInst = oc.getSpaceInstLightById(
                    componentInst.getDomainFatherId());
                orphanAttachment.setSpaceLabel(spaceInst.getName(language));
                orphanAttachment.setComponentLabel(
                    componentInst.getLabel(language) + " (" + componentsId[j] + ")");
                orphanAttachment.setSize(listFile.length());
                orphanAttachment.setContext(context);
                orphanAttachment.setPath(pathFolder + File.separator + listFile.getName());
                orphansList.add(orphanAttachment);
              }
            }
          } else {
            SilverTrace.info("admin", "CheckAttachmentBatch.getOrphansFiles(context)",
                "root.MSG_GEN_PARAM_VALUE", "path vide = " + path);
          }
        }
      }
    }
    return orphansList;
  }

  /**
   * Get attachments with the same physicalName
   * @param physicalName
   * @return
   */
  public List<AttachmentDetail> getAttachmentsByPhysicalName(String physicalName) {
    List<AttachmentDetail> listAttachements = new ArrayList<AttachmentDetail>();
    AttachmentDetail attDetail = null;
    String query = "select attachmentId, instanceId from sb_attachment_attachment where attachmentPhysicalName=?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Connection con = openConnection();
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, physicalName);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String attachmentId = String.valueOf(rs.getInt(1));
        String instanceId = rs.getString(2);
        AttachmentPK attPK = new AttachmentPK(attachmentId, instanceId);
        attDetail = AttachmentController.searchAttachmentByPK(attPK);
        if (attDetail != null) {
          SilverTrace.info("admin",
              "CheckAttachmentBatch.getAttachmentsByPhysicalName(physicalName)",
              "root.MSG_GEN_PARAM_VALUE", "attDetail = " + attDetail.getPK().getId());
          listAttachements.add(attDetail);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new AttachmentRuntimeException("CheckAttachmentsBatch.getAttachmentsByPhysicalName()",
          SilverpeasException.ERROR,
          "Erreur lors de la recuperation des attachments", e);
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
      closeConnection(con);
    }
    return listAttachements;
  }

  /**
   * Get List of attachments (list form DB)
   * @param attachmentType 
   * @return List of CheckAttachmentDetail
   */
  public List<CheckAttachmentDetail> getAttachments(String attachmentType) throws SQLException,
      RemoteException {
    Connection con = openConnection();
    List<CheckAttachmentDetail> results = new ArrayList<CheckAttachmentDetail>();
    String query = "select attachmentId, attachmentPhysicalName, attachmentLogicalName, instanceId, attachmentcontext from sb_attachment_attachment order by attachmentLogicalName";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    OrganizationController orgaController = new OrganizationController();
    try {
      prepStmt = con.prepareStatement(query);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        boolean display = false;
        String attachmentId = String.valueOf(rs.getInt(1));
        String instanceId = rs.getString(2);
        AttachmentPK attPK = new AttachmentPK(attachmentId, instanceId);
        AttachmentDetail attDetail = AttachmentController.searchAttachmentByPK(attPK);
        if (attDetail != null) {
          if (attDetail.getContext().equals(attachmentType)) {
            display = true;
          }
          if (attachmentType.equals("ImagesWysiwyg")) //Images from pub and node wysiwyg
          {
            if (attDetail.getContext().endsWith("Images") && !attDetail.getContext().equals("Images")
                && !attDetail.getContext().equals("XMLFormImages")) {
              display = true;
            }
          }
          if (attachmentType.equals("All")) {
            display = true;
          }

          if (display) {
            CheckAttachmentDetail cad = new CheckAttachmentDetail();
            cad.setAttachmentId(Long.parseLong(attachmentId));
            cad.setPhysicalName(attDetail.getPhysicalName());
            cad.setLogicalName(attDetail.getLogicalName(language));
            cad.setSize(attDetail.getSize());

            ComponentInstLight componentInst = orgaController.getComponentInstLight(attDetail.
                getInstanceId());
            SpaceInstLight spaceInst = orgaController.getSpaceInstLightById(componentInst.
                getDomainFatherId());

            cad.setSpaceLabel(spaceInst.getName(language));
            cad.setComponentLabel(
                componentInst.getLabel(language) + " (" + componentInst.getId() + ")");
            cad.setContext(attDetail.getContext());

            boolean isPublication = false;
            ComponentHelper helper = ComponentHelper.getInstance();
            String componentName = helper.extractComponentName(attDetail.getInstanceId());
            if (helper.isKmelia(componentName) || helper.isToolbox(componentName)) {
              if (StringUtil.isInteger(attDetail.getForeignKey().getId())) {
                isPublication = true;
              }
            }

            if (isPublication) {
              PublicationPK pubPK = new PublicationPK(attDetail.getForeignKey().getId(), attDetail.
                  getInstanceId());
              PublicationDetail pubDetail = getPublicationDetail(pubPK);

              if (pubDetail != null) {
                String publicationPath = spaceInst.getName(language) + " > " + componentInst.
                    getLabel(language) + " > "
                    + displayPath(pubDetail, false, 3) + " > " + pubDetail.getName(language);
                cad.setPublicationPath(publicationPath);

                String actionsDate = orgaController.getUserDetail(pubDetail.getCreatorId()).
                    getDisplayedName() + " le " + DateUtil.getOutputDate(pubDetail.getCreationDate(),
                    language);
                if (pubDetail.getUpdateDate() != null) {
                  actionsDate += "- Revu par " + orgaController.getUserDetail(
                      pubDetail.getUpdaterId()).getDisplayedName() + " le " + DateUtil.getOutputDate(pubDetail.
                      getUpdateDate(), language);
                }
                cad.setActionsDate(actionsDate);
              } else {
                cad.setPublicationPath("Publication introuvable pubPK=" + pubPK.toString());
              }

            }
            String pathFile = FileRepositoryManager.getAbsolutePath(attDetail.getInstanceId())
                + contextAttachment + File.separator + attDetail.getContext() + File.separator
                + attDetail.getPhysicalName();
            cad.setPath(pathFile);
            File att = new File(pathFile);

            if (att.exists()) {
              if (att.length() != attDetail.getSize()) {
                cad.setStatus(
                    "Taille en BD: " + attDetail.getSize() + " octets, Lue: " + att.length() + " octets");
              } else {
                cad.setStatus("OK");
              }
            } else {
              cad.setStatus("Fichier ABSENT");
            }
            results.add(cad);
          }
        }
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
      closeConnection(con);
    }
    return results;
  }

  public String displayPath(PublicationDetail pubDetail, boolean linked, int beforeAfter) throws
      RemoteException {
    List<NodePK> nodesPK = (List<NodePK>) pubDetail.getPublicationBm().getAllFatherPK(pubDetail.
        getPK());
    Collection<NodeDetail> path = null;
    StringBuffer linkedPathString = new StringBuffer();
    StringBuffer pathString = new StringBuffer();
    if (nodesPK != null && !nodesPK.isEmpty()) {
      NodePK firstNodePK = nodesPK.get(0);
      path = getPath(firstNodePK.getId(), firstNodePK.getInstanceId());
      if (path != null) {
        int nbItemInPath = path.size();
        Iterator<NodeDetail> iterator = path.iterator();
        boolean alreadyCut = false;
        int i = 0;
        NodeDetail nodeInPath = null;
        while (iterator.hasNext()) {
          nodeInPath = iterator.next();
          if ((i <= beforeAfter) || (i + beforeAfter >= nbItemInPath - 1)) {
            if (!nodeInPath.getNodePK().getId().equals("0")) {
              linkedPathString.append("<a href=\"javascript:onClick=topicGoTo('").append(nodeInPath.
                  getNodePK().getId()).append("')\">").append(EncodeHelper.javaStringToHtmlString(nodeInPath.
                  getName())).append("</a>");
              pathString.append(nodeInPath.getName());
              if (iterator.hasNext()) {
                linkedPathString.append(" > ");
                pathString.append(" > ");
              }
            }
          } else {
            if (!alreadyCut) {
              linkedPathString.append(" ... > ");
              pathString.append(" ... > ");
              alreadyCut = true;
            }
          }
          i++;
        }
      }
    }
    if (linked) {
      return linkedPathString.toString();
    } else {
      return pathString.toString();
    }
  }

  public PublicationDetail getPublicationDetail(PublicationPK pubPK) throws RemoteException {
    PublicationDetail pubDetail = null;
    try {
      CompletePublication completePublication = getPublicationBm().getCompletePublication(pubPK);
      pubDetail = completePublication.getPublicationDetail();

    } catch (Exception e) {
      SilverTrace.error("admin", "CheckAttachmentBatch.getPublicationDetail()", "publication.GETTING_PUBLICATION_DETAIL_FAILED" + " pubPK=" + pubPK.
          toString(), e);
    }
    return pubDetail;
  }

  public Collection<NodeDetail> getPath(String id, String componentId) {
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    NodePK nodePK = new NodePK(id, componentId);
    // compute path from a to z
    NodeBm nodeBm = getNodeBm();
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeBm.getPath(nodePK);
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception e) {
      SilverTrace.error("admin", "CheckAttachmentBatch.getPath()",
          "admin.EX_IMPOSSIBLE_DOBTENIR_LE_CHEMIN", e);
    }
    return newPath;
  }

  public PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new AttachmentRuntimeException("admin.getPublicationBm()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
    }
    return publicationBm;
  }

  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new AttachmentRuntimeException("admin.getNodeBm()",
          SilverpeasException.ERROR,
          "admin.EX_IMPOSSIBLE_DE_FABRIQUER_NODEBM_HOME", e);
    }
    return nodeBm;
  }

  private static Connection openConnection() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new AttachmentRuntimeException(
          "CheckAttachmentsBatch.openConnection()",
          SilverpeasException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private static void closeConnection(Connection con) {
    try {
      con.close();
    } catch (SQLException e) {
      // traitement des exceptions
      throw new AttachmentRuntimeException(
          "CheckAttachmentsBatch.closeConnection()",
          SilverpeasException.ERROR,
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }
}

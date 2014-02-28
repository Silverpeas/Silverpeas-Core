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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.web;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import java.rmi.RemoteException;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Michael Nikolaenko
 * @version 1.0
 */
public class VersioningSessionController extends AbstractComponentSessionController {

  private SimpleDocument document;
  private String nodeId = null;
  private boolean topicRightsEnabled = false;
  private AdminController adminController = null;
  private String currentProfile = null;
  public static final String ADMIN = SilverpeasRole.admin.toString();
  public static final String PUBLISHER = SilverpeasRole.publisher.toString();
  public static final String READER = SilverpeasRole.user.toString();
  public static final String WRITER = SilverpeasRole.writer.toString();

  public void setComponentId(String compomentId) {
    this.context.setCurrentComponentId(compomentId);
  }

  public String getProfile() {
    if (!StringUtil.isDefined(this.currentProfile)) {
      this.currentProfile = getUserRoleLevel();
    }
    return this.currentProfile;
  }

  public void setProfile(String profile) {
    if (StringUtil.isDefined(profile)) {
      this.currentProfile = profile;
    }
  }

  private boolean isTopicRightsEnabled() {
    return topicRightsEnabled;
  }

  /**
   * Constructor
   *
   * @param mainSessionCtrl
   * @param componentContext
   */
  public VersioningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.versioningPeas.multilang.versioning",
        null);
    setComponentRootName(URLManager.CMP_VERSIONINGPEAS);
  }

  /**
   * to get document from DB
   *
   * @param documentPK
   * @return SimpleDocument
   */
  public SimpleDocument getDocument(SimpleDocumentPK documentPK) {
    return AttachmentServiceFactory.getAttachmentService().searchDocumentById(documentPK, null);
  }

  /**
   * To get all versions of document.
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getDocumentVersions(SimpleDocumentPK documentPK) {
    return ((HistorisedDocument) AttachmentServiceFactory.getAttachmentService()
        .searchDocumentById(documentPK, null)).getHistory();
  }

  /**
   * To get only public versions of document.
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getPublicDocumentVersions(SimpleDocumentPK documentPK) {
    SimpleDocument currentDoc = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
        documentPK, null);
    if (currentDoc.isVersioned()) {
      return ((HistorisedDocument) currentDoc).getPublicVersions();
    }
    return Collections.singletonList(currentDoc);
  }

  /**
   * Store in controller the current edited document.
   *
   * @param document
   */
  public void setEditingDocument(SimpleDocument document) {
    this.document = document;
  }

  /**
   * @param document
   * @param userId
   * @return
   * @throws RemoteException
   */
  public boolean isReader(SimpleDocument document, String userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    setEditingDocument(document);
    if (isWriter(document, userId)) {
      return true;
    }
    // No specific rights activated on document
    // Check rights according to rights of component (or topic)
    if (isTopicRightsEnabled()) {
      ProfileInst profile = getInheritedProfile(READER);
      if (profile.getObjectId() != -1) {
        // topic have no rights defined (on itself or by its fathers) check if user have access
        // to this component
        return isComponentAvailable(userId);
      }
      return false;
    } else {
      // check if user have access to this component
      return isComponentAvailable(userId);
    }

  }

  private boolean isComponentAvailable(String userId) {
    return getOrganisationController().isComponentAvailable(getComponentId(), userId);
  }

  /**
   * Checks if the specified userId is admin for the current component.
   *
   * @param userId the unique id of the user checked for admin role.
   * @return true if the user has admin role - false otherwise.
   */
  public boolean isAdmin(String userId) {
    return isUserInRole(userId, SilverpeasRole.admin);
  }

  public boolean isUserInRole(String userId, SilverpeasRole role) {
    ProfileInst profile = getComponentProfile(role.toString());
    return isUserInRole(userId, profile);
  }

  private boolean useRights() {
    return getComponentId() == null || getComponentId().startsWith("kmelia");
  }

  /**
   * @param document
   * @param userId
   * @return
   * @throws RemoteException
   */
  public boolean isWriter(SimpleDocument document, String userId) throws RemoteException {
    setEditingDocument(document);
    // check document profiles
    boolean isWriter = isUserInRole(userId, getCurrentProfile(WRITER));
    if (!isWriter) {
      isWriter = isUserInRole(userId, getCurrentProfile(PUBLISHER));
      if (!isWriter) {
        isWriter = isUserInRole(userId, getCurrentProfile(ADMIN));
      }
    }
    return isWriter;
  }

  private boolean isUserInRole(String userId, ProfileInst profile) {
    SilverTrace.info("versioningPeas", "VersioningSessionController.isUserInRole()",
        "root.MSG_GEN_ENTER_METHOD", "document = " + document.getPk().getId() + ", userId = "
        + userId + "profile=" + profile.getName());
    boolean userInRole = false;
    if (profile.getAllUsers() != null) {
      userInRole = profile.getAllUsers().contains(userId);
    }
    if (!userInRole) {
      // check in groups
      List<String> groupsIds = profile.getAllGroups();
      for (String groupId : groupsIds) {
        UserDetail[] users = getOrganisationController().getAllUsersOfGroup(groupId);
        for (UserDetail user : users) {
          if (user != null && user.getId().equals(userId)) {
            return true;
          }
        }
      }
    }
    return userInRole;
  }

  /**
   * @return
   */
  public NodeBm getNodeBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getComponentProfile(String role) {
    ComponentInst componentInst = getAdmin().getComponentInst(getComponentId());
    ProfileInst profile = componentInst.getProfileInst(role);
    ProfileInst inheritedProfile = componentInst.getInheritedProfileInst(role);
    if (inheritedProfile == null) {
      if (profile == null) {
        profile = new ProfileInst();
        profile.setName(role);
      }
    } else {
      if (profile == null) {
        profile = inheritedProfile;
      } else {
        profile.addGroups(inheritedProfile.getAllGroups());
        profile.addUsers(inheritedProfile.getAllUsers());
      }
    }
    return profile;
  }

  /**
   * @return
   */
  private AdminController getAdmin() {
    if (adminController == null) {
      adminController = new AdminController(getUserId());
    }

    return adminController;
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getCurrentProfile(String role) throws RemoteException {
    // Rights of the node
    if (isTopicRightsEnabled()) {
      NodeDetail nodeDetail = getNodeBm().getDetail(new NodePK(nodeId, getComponentId()));
      if (nodeDetail.haveRights()) {
        return getTopicProfile(role, Integer.toString(nodeDetail.getRightsDependsOn()));
      }
    }
    // Rights of the component
    return getComponentProfile(role);
  }

  public ProfileInst getInheritedProfile(String role) throws RemoteException {
    ProfileInst profileInst;
    // Rights of the node
    if (isTopicRightsEnabled()) {
      NodeDetail nodeDetail = getNodeBm().getDetail(new NodePK(nodeId, getComponentId()));
      if (nodeDetail.haveRights()) {
        profileInst = getTopicProfile(role, Integer.toString(nodeDetail.getRightsDependsOn()));
      } else {
        // Rights of the component
        profileInst = getComponentProfile(role);
      }
    } else {
      // Rights of the component
      profileInst = getComponentProfile(role);
    }
    return profileInst;
  }

  /**
   * @param role
   * @param topicId
   * @return
   */
  public ProfileInst getTopicProfile(String role, String topicId) {
    List<ProfileInst> profiles =
        getAdmin().getProfilesByObject(topicId, ObjectType.NODE.getCode(), getComponentId());
    for (ProfileInst profile : profiles) {
      if (profile.getName().equals(role)) {
        return profile;
      }
    }
    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    return profile;
  }
}

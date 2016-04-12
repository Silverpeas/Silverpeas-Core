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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.attachment;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Nikolaenko
 * @version 1.0
 */
public class VersioningSessionController extends AbstractComponentSessionController {

  private SimpleDocument document;
  private String contentLanguage;
  private String nodeId = null;
  private AdminController adminController = ServiceProvider.getService(AdminController.class);
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
    return false;
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
    setComponentRootName(URLUtil.CMP_VERSIONINGPEAS);
  }

  /**
   * to get document from DB
   *
   * @param documentPK
   * @return SimpleDocument
   */
  public SimpleDocument getDocument(SimpleDocumentPK documentPK) {
    return AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(documentPK, getContentLanguage());
  }

  /**
   * To get all versions of document.
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getDocumentVersions(SimpleDocumentPK documentPK) {
    return (List)((HistorisedDocument) AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(documentPK, getContentLanguage())).getFunctionalHistory();
  }

  /**
   * To get only public versions of document (according to the content language).
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getPublicDocumentVersions(SimpleDocumentPK documentPK) {
    SimpleDocument currentDoc = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
        documentPK, getContentLanguage());
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
    setComponentId(document.getInstanceId());
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(final String contentLanguage) {
    this.contentLanguage = contentLanguage;
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
  public NodeService getNodeBm() {
    return ServiceProvider.getService(NodeService.class);
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

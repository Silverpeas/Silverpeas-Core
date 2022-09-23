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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.silverpeas.cmis.walkers.CmisObjectsTreeWalkerDelegator;
import org.silverpeas.cmis.walkers.TreeWalkerForComponentInst;
import org.silverpeas.cmis.walkers.TreeWalkerForNodeDetail;
import org.silverpeas.cmis.walkers.TreeWalkerForPublicationDetail;
import org.silverpeas.cmis.walkers.TreeWalkerForSimpleDocument;
import org.silverpeas.cmis.walkers.TreeWalkerForSpaceInst;
import org.silverpeas.cmis.walkers.TreeWalkerSelector;
import org.silverpeas.core.BasicIdentifier;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentDataProvider;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cmis.CmisContributionsProvider;
import org.silverpeas.core.cmis.SilverpeasCmisSettings;
import org.silverpeas.core.cmis.model.CmisFile;
import org.silverpeas.core.cmis.model.CmisFilePath;
import org.silverpeas.core.cmis.model.CmisFilePathProvider;
import org.silverpeas.core.cmis.model.CmisObjectFactory;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.contribution.model.Attachment;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.PersonalizationService;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.NodeAccessControl;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.RequesterProvider;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.util.MavenTestEnv;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.Level;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This class is for defining the whole environment required to run tests against the CMIS support
 * of Silverpeas.
 * @author mmoquillon
 */
@ExtendWith(LoggerExtension.class)
@LoggerLevel(Level.DEBUG)
@TestManagedBeans({CmisObjectFactory.class, SilverpeasCmisTypeManager.class})
@EnableSilverTestEnv
public abstract class CMISEnvForTests {

  // date at which a user account has been created or saved in Silverpeas
  private final Date creationDate = new Date();

  // the organizational schema of Silverpeas
  protected static SilverpeasObjectsTree organization = new SilverpeasObjectsTree();

  // the default file used as the content of any documents for which the file isn't
  private static final String DEFAULT_CONTENT_FILENAME = "HistoryOfSmalltalk.pdf";

  // the personalization service is used through the User interface to get the preferences of
  // the current user behind the request (to get its default language)
  @TestManagedMock
  protected PersonalizationService personalizationService;

  // the organization controller is used by the tree walker. In the test, the organization
  //controller will provide the data by backing an excerpt of an organizational schema of Silverpeas
  @TestManagedMock
  protected WAComponentRegistry waComponentRegistry;

  // the organization controller is used by the tree walker. In the test, the organization
  //controller will provide the data by backing an excerpt of an organizational schema of Silverpeas
  @TestManagedMock
  protected OrganizationController organizationController;

  @TestManagedMock
  protected SilverpeasComponentDataProvider componentDataProvider;

  @TestManagedMock
  protected SilverpeasComponentInstanceProvider componentInstanceProvider;

  @TestManagedMock
  protected NodeService nodeService;

  @TestManagedMock
  protected PublicationService publicationService;

  @TestManagedMock
  protected WebdavService webdavService;

  @TestManagedMock
  protected AttachmentService attachmentService;

  // used to get the current user behind the request
  @TestManagedMock
  protected UserProvider userProvider;

  // controller to check a user has the rights to access a given space
  @TestManagedMock
  protected SpaceAccessControl spaceAccessControl;

  // controller to check a user has the rights to access a given component instance
  @TestManagedMock
  protected ComponentAccessControl componentAccessControl;

  // controller to check a user has the rights to access a given node in a component instance
  @TestManagedMock
  protected NodeAccessControl nodeAccessControl;

  @TestManagedMock
  protected PublicationAccessControl publicationAccessControl;

  @TestManagedMock
  protected SimpleDocumentAccessControl documentAccessControl;

  @TestManagedMock
  protected CmisFilePathProvider cmisFilePathProvider;

  // provider of contributions for a given component
  @TestManagedMock
  @Named("kmelia" + CmisContributionsProvider.Constants.NAME_SUFFIX)
  protected CmisContributionsProvider contributionsProvider;

  @SuppressWarnings("unused")
  @TestManagedBean
  Class<?>[] supplyRequiredManagedBeanTypes() {
    return new Class<?>[]{TreeWalkerSelector.class, TreeWalkerForComponentInst.class,
        TreeWalkerForSpaceInst.class, TreeWalkerForNodeDetail.class,
        TreeWalkerForPublicationDetail.class, TreeWalkerForSimpleDocument.class,
        CmisObjectsTreeWalkerDelegator.class, SilverpeasCmisSettings.class};
  }

  /**
   * Gets the path of the specified node in the organizational schema of Silverpeas used in the unit
   * tests. The path is made up of the names of the identifiable objects wrapped by each node, each
   * of them separated by the {@link CmisFilePath#PATH_SEPARATOR} separator. This path is to be
   * passed to the CMIS objects tree walker that is expected the tokens in a path to be the name of
   * a CMIS object. The root node is here missed as it represents the application itself (virtual
   * root node).
   * @param node a node in an organizational schema of Silverpeas.
   * @param language the language in which the token of the path should be.
   * @return the path of the node in the organizational schema of Silverpeas.
   */
  protected String pathToNode(final TreeNode node, final String language) {
    return CmisFilePath.PATH_SEPARATOR + node.getPath().stream()
        .filter(n -> !(n.getObject() instanceof NodeDetail) ||
            !((NodeDetail) n.getObject()).isRoot())
        .map(n -> n.getLabel(language))
        .collect(Collectors.joining(CmisFilePath.PATH_SEPARATOR));
  }

  @RequesterProvider
  User getCurrentUser() {
    final UserDetail currentUser = new UserDetail();
    currentUser.setId("42");
    currentUser.setFirstName("John");
    currentUser.setLastName("Doo");
    currentUser.setDomainId("0");
    currentUser.setCreationDate(creationDate);
    currentUser.setSaveDate(creationDate);
    currentUser.setAccessLevel(UserAccessLevel.ADMINISTRATOR);
    when(personalizationService.getUserSettings("42")).thenReturn(
        new UserPreferences("42", "en", ZoneId.of("Europe/London"), "Aurora", "WA1", false, true,
            true, UserMenuDisplay.DEFAULT));
    return currentUser;
  }

  @SuppressWarnings("JUnitMalformedDeclaration")
  @BeforeEach
  public void prepareMock(MavenTestEnv mavenTestEnv) {
    mockUserProviders();
    mockUserAuthorization();

    when(componentDataProvider.isWorkflow(anyString())).thenReturn(false);

    when(componentInstanceProvider.getComponentName(anyString())).then(
        (Answer<String>) invocation -> {
          String appId = invocation.getArgument(0);
          return ComponentInst.getComponentName(appId);
        });

    when(waComponentRegistry.getWAComponent(startsWith("kmelia"))).then(m -> {
      final WAComponent kmelia = mock(WAComponent.class);
      when(kmelia.getName()).thenReturn("kmelia");
      when(kmelia.isTopicTracker()).thenReturn(true);
      return Optional.of(kmelia);
    });

    when(organizationController.isComponentAvailableToUser(anyString(), anyString())).thenReturn(
        true);

    when(organizationController.getSpaceInstLightById(anyString())).then(
        (Answer<SpaceInstLight>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> {
            LocalizedResource object = n.getObject();
            return object instanceof SpaceInstLight ? (SpaceInstLight) object : null;
          });
        });

    when(organizationController.getComponentInstLight(anyString())).then(
        (Answer<ComponentInstLight>) invocation -> {
          String compInstId = invocation.getArgument(0);
          return getInTreeAndApply(compInstId, n -> {
            LocalizedResource object = n.getObject();
            return object instanceof ComponentInstLight ? (ComponentInstLight) object : null;
          });
        });

    when(organizationController.getComponentInstance(anyString())).then(
        (Answer<Optional<SilverpeasComponentInstance>>) invocation -> {
          String compInstId = invocation.getArgument(0);
          return Optional.ofNullable(organizationController.getComponentInstLight(compInstId));
        });

    when(organizationController.getAllComponentIds(anyString())).then(
        (Answer<String[]>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> n.getChildren()
              .stream()
              .map(TreeNode::getObject)
              .filter(o -> o instanceof ComponentInstLight)
              .map(o -> o.getIdentifier().asString())
              .toArray(String[]::new));
        });

    when(organizationController.getAllowedSubSpaceIds(anyString(), anyString())).then(
        (Answer<String[]>) invocation -> {
          String spaceId = invocation.getArgument(1);
          return getInTreeAndApply(spaceId, n -> n.getChildren()
              .stream()
              .map(TreeNode::getObject)
              .filter(o -> o instanceof SpaceInstLight)
              .map(o -> o.getIdentifier().asString())
              .toArray(String[]::new));
        });

    when(organizationController.getAllRootSpaceIds(anyString())).then(
        (Answer<String[]>) invocation -> applyOnRootNodes(
            rootNodes -> rootNodes.stream().map(TreeNode::getId).toArray(String[]::new)));

    when(organizationController.getPathToSpace(anyString())).then(
        (Answer<List<SpaceInstLight>>) invocation -> {
          String spaceId = invocation.getArgument(0);
          return getInTreeAndApply(spaceId, n -> n.getPath()
              .stream()
              .map(s -> (SpaceInstLight) s.getObject())
              .collect(Collectors.toList()));
        });

    when(organizationController.getPathToComponent(anyString())).then(
        (Answer<List<SpaceInstLight>>) invocation -> {
          String compInstId = invocation.getArgument(0);
          return getInTreeAndApply(compInstId, n -> n.getParent()
              .getPath()
              .stream()
              .map(s -> (SpaceInstLight) s.getObject())
              .collect(Collectors.toList()));
        });

    when(nodeService.getDetail(any(NodePK.class))).then((Answer<NodeDetail>) invocation -> {
      NodePK nodePK = invocation.getArgument(0);
      ContributionIdentifier id = ContributionIdentifier.from(nodePK, NodeDetail.TYPE);
      return getInTreeAndApply(id.asString(), n -> {
        LocalizedResource object = n.getObject();
        return object instanceof NodeDetail ? (NodeDetail) object : null;
      });
    });

    when(nodeService.getChildrenDetails(any(NodePK.class))).then(
        (Answer<Collection<NodeDetail>>) invocation -> {
          NodePK nodePK = invocation.getArgument(0);
          ContributionIdentifier id = ContributionIdentifier.from(nodePK, NodeDetail.TYPE);
          return getInTreeAndApply(id.asString(), n -> n.getChildren()
              .stream()
              .map(TreeNode::getObject)
              .filter(o -> o instanceof NodeDetail)
              .map(o -> (NodeDetail) o)
              .collect(Collectors.toList()));
        });

    when(nodeService.getPath(any(NodePK.class))).then((Answer<NodePath>) invocation -> {
      NodePK nodePK = invocation.getArgument(0);
      NodePath path = new NodePath();
      while (nodePK != null && !nodePK.isUndefined()) {
        NodeDetail node = nodeService.getDetail(nodePK);
        path.add(node);
        nodePK = node.getFatherPK();
      }
      return path;
    });

    when(contributionsProvider.getAllowedRootContributions(any(ResourceIdentifier.class),
        any(User.class))).then((Answer<List<I18nContribution>>) invocation -> {
      ResourceIdentifier appId = invocation.getArgument(0);
      ContributionIdentifier id =
          ContributionIdentifier.from(appId.asString(), NodePK.ROOT_NODE_ID, NodeDetail.TYPE);
      return getInTreeAndApply(id.asString(), n -> n.getChildren()
          .stream()
          .map(TreeNode::getObject)
          .map(I18nContribution.class::cast)
          .collect(Collectors.toList()));
    });

    when(contributionsProvider.getAllowedContributionsInFolder(any(ContributionIdentifier.class),
        any(User.class))).then((Answer<List<I18nContribution>>) invocation -> {
      ContributionIdentifier id = invocation.getArgument(0);
      return getInTreeAndApply(id.asString(), n -> n.getChildren()
          .stream()
          .map(TreeNode::getObject)
          .map(I18nContribution.class::cast)
          .collect(Collectors.toList()));
    });

    when(contributionsProvider.getContribution(any(ContributionIdentifier.class),
        any(User.class))).then((Answer<I18nContribution>) invocation -> {
      ContributionIdentifier id = invocation.getArgument(0);
      if (id.getType().equals(NodeDetail.TYPE)) {
        return nodeService.getDetail(new NodePK(id.getLocalId(), id.getComponentInstanceId()));
      } else if (id.getType().equals(PublicationDetail.TYPE)) {
        return publicationService.getDetail(
            new PublicationPK(id.getLocalId(), id.getComponentInstanceId()));
      }
      return null;
    });

    when(cmisFilePathProvider.getPath(any(CmisFile.class))).then(
        (Answer<CmisFilePath>) invocation -> {
          CmisFile file = invocation.getArgument(0);
          User requester = User.getCurrentRequester();
          String language = requester.getUserPreferences().getLanguage();
          String pathStr = getInTreeAndApply(file.getId(), n -> pathToNode(n, language));
          CmisFilePath path = mock(CmisFilePath.class);
          when(path.toString()).thenReturn(pathStr);
          return path;
        });

    when(contributionsProvider.createContribution(any(PublicationDetail.class),
        any(BasicIdentifier.class), anyString())).then((Answer<I18nContribution>) invocation -> {
      PublicationDetail pub = invocation.getArgument(0);
      BasicIdentifier appId = invocation.getArgument(1);
      NodeDetail root = getInTreeAndApply(appId.asString(), a ->
          a.getChildren().stream()
              .map(n -> (NodeDetail) n.getObject())
              .filter(NodeDetail::isRoot)
              .findFirst()
              .orElseThrow());
      Objects.requireNonNull(root);
      TreeNode pubNode = organization.addPublication(pub, root.getIdentifier().asString());
      return (I18nContribution) pubNode.getObject();
    });

    when(contributionsProvider.createContributionInFolder(any(PublicationDetail.class),
        any(ContributionIdentifier.class), anyString())).then(
        (Answer<I18nContribution>) invocation -> {
          PublicationDetail pub = invocation.getArgument(0);
          ContributionIdentifier nodeId = invocation.getArgument(1);
          TreeNode pubNode = getInTreeAndApply(nodeId.asString(), n ->
              organization.addPublication(pub, nodeId.asString()));
          Objects.requireNonNull(pubNode);
          return (I18nContribution) pubNode.getObject();
        });

    when(publicationService.getDetail(any(PublicationPK.class))).then(
        (Answer<PublicationDetail>) invocation -> {
          PublicationPK pk = invocation.getArgument(0);
          ContributionIdentifier id = ContributionIdentifier.from(pk, PublicationDetail.TYPE);
          return getInTreeAndApply(id.asString(), n -> {
            LocalizedResource object = n.getObject();
            Objects.requireNonNull(object);
            return object instanceof PublicationDetail ? (PublicationDetail) object : null;
          });
        });

    when(publicationService.getMainLocation(any(PublicationPK.class))).then(
        (Answer<Optional<Location>>) invocation -> {
          PublicationPK pubPk = invocation.getArgument(0);
          ContributionIdentifier id = ContributionIdentifier.from(pubPk, PublicationDetail.TYPE);
          return getInTreeAndApply(id.asString(), n -> {
            LocalizedResource object = n.getParent().getObject();
            Location location;
            if (object instanceof NodeDetail) {
              NodePK nodePk = ((NodeDetail) object).getNodePK();
              location = new Location(nodePk.getId(), nodePk.getInstanceId());
            } else {
              location = null;
            }
            return Optional.ofNullable(location);
          });
        });

    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString())).then(
        (Answer<SimpleDocument>) invocation -> {
          SimpleDocumentPK pk = invocation.getArgument(0);
          ContributionIdentifier id =
              ContributionIdentifier.from(new ResourceReference(pk),
                  DocumentType.attachment.getName());
          return getInTreeAndApply(id.asString(), n -> {
            LocalizedResource doc = n.getObject();
            return doc instanceof SimpleDocument ? (SimpleDocument) doc : null;
          });
        });

    when(attachmentService.listDocumentsByForeignKey(any(ResourceReference.class),
        anyString())).then((Answer<SimpleDocumentList<SimpleDocument>>) invocation -> {
      ResourceReference ref = invocation.getArgument(0);
      ContributionIdentifier id =
          ContributionIdentifier.from(ref.getComponentInstanceId(), ref.getLocalId(),
              PublicationDetail.getResourceType());
      return getInTreeAndApply(id.asString(), n -> n.getChildren()
          .stream()
          .map(TreeNode::getObject)
          .map(a -> (SimpleDocument) a)
          .collect(SimpleDocumentList::new, SimpleDocumentList::add, SimpleDocumentList::addAll));
    });

    when(attachmentService.createAttachment(any(SimpleDocument.class),
        any(File.class), anyBoolean(), anyBoolean())).then((Answer<SimpleDocument>) invocation -> {
      SimpleDocument document = invocation.getArgument(0);
      document.getPk().setId("1000");
      if (document.getLastUpdateDate() == null) {
        document.setLastUpdateDate(document.getCreationDate());
      }
      if (StringUtil.isNotDefined(document.getUpdatedBy())) {
        document.setUpdatedBy(document.getCreatedBy());
      }
      ContributionIdentifier pubId = ContributionIdentifier.from(document.getInstanceId(),
          document.getForeignId(), PublicationDetail.getResourceType());
      getInTreeAndApply(pubId.asString(), n -> n.addChild(document));
      return document;
    });

    doAnswer(i -> {
      OutputStream output = i.getArgument(0);
      SimpleDocumentPK pk = i.getArgument(1);
      long offset = i.getArgument(3);
      long length = i.getArgument(4);
      ContributionIdentifier id =
          ContributionIdentifier.from(new ResourceReference(pk), DocumentType.attachment.getName());
      return getInTreeAndApply(id.asString(), n -> {
        SimpleDocument doc = (SimpleDocument) n.getObject();
        String fileName = doc.getAttachment().getFilename();
        File content = new File(mavenTestEnv.getResourceTestDirFile(), fileName);
        if (!content.exists()) {
          content = new File(mavenTestEnv.getResourceTestDirFile(), DEFAULT_CONTENT_FILENAME);
        }
        try {
          FileInputStream input = new FileInputStream(content);
          IOUtils.copyLarge(input, output, offset, length);
        } catch (IOException e) {
          throw new AttachmentException(e);
        }
        return null;
      });
    }).when(attachmentService)
        .getBinaryContent(any(OutputStream.class), any(SimpleDocumentPK.class),
            any(String.class), anyLong(), anyLong());

    try {
      doAnswer(i -> {
        SimpleDocument document = i.getArgument(0);
        OutputStream output = i.getArgument(1);
        attachmentService.getBinaryContent(output, document.getPk(), document.getLanguage(), 0,
            document.getSize());
        return null;
      }).when(webdavService)
          .loadContentInto(any(SimpleDocument.class), any(OutputStream.class));
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private void mockUserProviders() {
    when(userProvider.getUser(anyString())).then((Answer<User>) invocation -> {
      String userId = invocation.getArgument(0);
      User theUser;
      if (userId.equals("42")) {
        theUser = getCurrentUser();
      } else {
        UserDetail user = new UserDetail();
        user.setId(userId);
        user.setFirstName(userId.equals("0") ? null : "Toto");
        user.setLastName(userId.equals("0") ? "Administrateur" : "Tartempion" + userId);
        user.setDomainId("0");
        user.setCreationDate(creationDate);
        user.setSaveDate(creationDate);
        theUser = user;
      }
      return theUser;
    });

    when(userProvider.getMainAdministrator()).then(
        (Answer<User>) invocation -> userProvider.getUser("0"));
  }

  private void mockUserAuthorization() {
    when(
        spaceAccessControl.isUserAuthorized(anyString(), any(ResourceIdentifier.class))).thenReturn(
        true);
    when(spaceAccessControl.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    when(componentAccessControl.isUserAuthorized(anyString(),
        any(ResourceIdentifier.class))).thenReturn(true);
    when(componentAccessControl.isUserAuthorized(anyString(), anyString())).thenReturn(true);
    when(componentAccessControl.filterAuthorizedByUser(anyCollection(), anyString())).then(
        (Answer<Stream<String>>) invocation -> {
          Collection<String> compInstIds = invocation.getArgument(0);
          return compInstIds.stream();
        });
    when(nodeAccessControl.isUserAuthorized(anyString(), any(ResourceIdentifier.class))).thenReturn(
        true);
    when(nodeAccessControl.isUserAuthorized(anyString(), any(NodeDetail.class))).thenReturn(true);
    when(nodeAccessControl.getUserRoles(anyString(), any(NodePK.class),
        any(AccessControlContext.class))).thenReturn(
        Set.of(SilverpeasRole.WRITER, SilverpeasRole.READER));
    when(publicationAccessControl.isUserAuthorized(anyString(),
        any(ResourceIdentifier.class))).thenReturn(true);
    when(publicationAccessControl.isUserAuthorized(anyString(),
        any(PublicationDetail.class))).thenReturn(true);
    when(publicationAccessControl.isUserAuthorized(anyString(), any(PublicationDetail.class), any(
        AccessControlContext.class))).thenReturn(true);
    when(documentAccessControl.isUserAuthorized(anyString(),
        any(ResourceIdentifier.class))).thenReturn(true);
    when(documentAccessControl.isUserAuthorized(anyString(), any(SimpleDocument.class))).thenReturn(
        true);
    when(documentAccessControl.isUserAuthorized(anyString(), any(SimpleDocument.class), any(
        AccessControlContext.class))).thenReturn(true);
  }

  @BeforeAll
  static void createOrganizationSchema() {
    final String appType = "kmelia";
    final int rootNodeId = Integer.parseInt(NodePK.ROOT_NODE_ID);

    TreeNode wa1Node = organization.addSpace(1, "", 0, "COLLABORATIVE WORKSPACE", "");
    TreeNode wa3Node =
        organization.addSpace(3, wa1Node.getId(), 0, "Business", "Business related works");
    TreeNode wa4Node = organization.addSpace(4, wa1Node.getId(), 1, "Production",
        "Production information and monitoring");
    TreeNode wa6Node = organization.addSpace(6, wa4Node.getId(), 0, "QA", "");
    TreeNode kmelia1 =
        organization.addApplication(appType, 1, wa3Node.getId(), 0, "Documentation", "");
    TreeNode rootKm1 = organization.addFolder(rootNodeId, kmelia1.getId(), 1, "Home", "The root");
    TreeNode folder1 = organization.addFolder(3, rootKm1.getId(), 2, "Folder 1", "");
    TreeNode folderKm1 = organization.addFolder(4, rootKm1.getId(), 2, "Folder 2", "");
    TreeNode folder21 = organization.addFolder(5, folderKm1.getId(), 3, "Folder 2.1", "");
    TreeNode pub1 = organization.addPublication(1, folder1.getId(), "Smalltalk Forever",
        "All about this powerful language");
    TreeNode pub2 = organization.addPublication(2, folder21.getId(), "Java Magazine October 2020",
        "Make it simple; make it better.");
    organization.addDocument(1, pub1.getId(), "HistoryOfSmalltalk",
        "The history of Smalltalk told by Alan Kay himself");

    organization.addDocument(2, pub1.getId(), "Computer Programming Using GNU Smalltalk", "");
    organization.addDocument(3, pub1.getId(), "Developing Applications with Smalltalk and CouchDB",
        "");
    organization.addDocument(4, pub2.getId(), pub2.getObject().getName(),
        pub2.getObject().getDescription());

    TreeNode kmelia2 =
        organization.addApplication(appType, 2, wa4Node.getId(), 0, "Documentation", "");
    TreeNode rootKm2 = organization.addFolder(rootNodeId, kmelia2.getId(), 1, "Home", "The root");
    TreeNode folder1Km2 = organization.addFolder(3, rootKm2.getId(), 2, "Folder 1", "");
    organization.addFolder(4, folder1Km2.getId(), 3, "Folder 1.1", "");
    TreeNode folder2Km2 = organization.addFolder(5, rootKm2.getId(), 2, "Folder 2", "");
    organization.addFolder(6, folder2Km2.getId(), 3, "Folder 2.1", "");

    TreeNode kmelia5 = organization.addApplication(appType, 5, wa6Node.getId(), 0, "Documentation",
        "QA documentation");
    organization.addFolder(rootNodeId, kmelia5.getId(), 1, "Home", "The root");

    TreeNode wa2Node = organization.addSpace(2, "", 1, "PROJECTS", "");
    TreeNode wa5Node = organization.addSpace(5, wa2Node.getId(), 0, "New Manufacturing Process",
        "A new process to improve our owns manufactures");
    TreeNode kmelia3 =
        organization.addApplication(appType, 3, wa2Node.getId(), 1, "Process & Standard", "");
    organization.addFolder(rootNodeId, kmelia3.getId(), 1, "Home", "The root");
    TreeNode kmelia4 =
        organization.addApplication(appType, 4, wa5Node.getId(), 0, "Documentation", "");
    organization.addFolder(rootNodeId, kmelia4.getId(), 1, "Home", "The root");
  }

  @AfterAll
  static void clearOrganizationSchema() {
    organization.clear();
  }

  @SuppressWarnings("unused")
  protected String getObjectName(final LocalizedResource object, final String language) {
    if (object instanceof Attachment) {
      return ((Attachment) object).getFilename();
    } else {
      return object.getTranslation(language).getName();
    }
  }

  private <T> T getInTreeAndApply(String id, Function<TreeNode, T> fun) {
    TreeNode node = organization.findTreeNodeById(id);
    if (node == null) {
      return null;
    }
    return fun.apply(node);
  }

  private <T> T applyOnRootNodes(Function<Set<TreeNode>, T> fun) {
    Set<TreeNode> nodes = organization.getRootNodes();
    return fun.apply(nodes);
  }
}
  
/*
 *  Copyright (C) 2000 - 2013 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.sharing.model;

import com.silverpeas.sharing.mock.NodeSharingTicketService;
import org.silverpeas.core.sharing.security.ShareableAttachment;
import org.silverpeas.core.sharing.security.ShareableNode;
import org.silverpeas.core.sharing.security.ShareableVersionDocument;
import org.silverpeas.core.sharing.services.SharingServiceProvider;
import org.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.node.control.NodeService;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.core.contribution.publication.control.PublicationService;
import org.silverpeas.core.contribution.publication.model.Alias;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({EJBUtilitaire.class, SharingServiceProvider.class})
public class NodeAccessControlTest {

  public NodeAccessControlTest() {
  }

  /**
   * Test of isReadable method, of class NodeAccessControl for a version document.
   */
  @Test
  public void testIsReadableDocument() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    ForeignPK publicationPK = new ForeignPK("100", "kmelia10");
    PublicationService publicationService = PowerMockito.mock(PublicationService.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("110", "kmelia11"),
            new NodePK("120", "kmelia12")));
    Collection<Alias> aliasPks = new ArrayList<Alias>(fatherPks.size());
    for (NodePK nodePK : fatherPks) {
      aliasPks.add(new Alias(nodePK.getId(), nodePK.getInstanceId()));
    }
    PowerMockito.when(publicationService.getAllFatherPK(new PublicationPK("100", "kmelia10")))
        .thenReturn(fatherPks);
    PowerMockito.when(publicationService.getAlias(new PublicationPK("100", "kmelia10")))
        .thenReturn(aliasPks);
    /* to replace with ServiceProvider
    PowerMockito
        .when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
        .thenReturn(publicationService);
        */

    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */

    SimpleDocumentPK resourcePk = new SimpleDocumentPK("5", "kmelia2");
    resourcePk.setOldSilverpeasId(5L);
    HistorisedDocument document = new HistorisedDocument();
    document.setPK(resourcePk);
    document.setForeignId(publicationPK.getId());
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    assertThat(instance.isReadable(resource), is(true));
  }

  /**
   * Test of isReadable method, of class NodeAccessControl for a version document..
   */
  @Test
  public void testIsUnReadableDocument() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    ForeignPK publicationPK = new ForeignPK("100", "kmelia10");
    PublicationService publicationService = PowerMockito.mock(PublicationService.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("110", "kmelia11"),
            new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationService.getAllFatherPK(new PublicationPK("100", "kmelia10")))
        .thenReturn(fatherPks);
    /* to replace with ServiceProvider
    PowerMockito
        .when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
        .thenReturn(publicationService);
        */

    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("10", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */

    SimpleDocumentPK resourcePk = new SimpleDocumentPK("5", "kmelia2");
    resourcePk.setOldSilverpeasId(5L);
    HistorisedDocument document = new HistorisedDocument();
    document.setPK(resourcePk);
    document.setForeignId(publicationPK.getId());
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(false));
  }

  /**
   * Test of isReadable method, of class NodeAccessControl  for an AttachmentDetail.
   */
  @Test
  public void testIsReadableAttachment() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    ForeignPK publicationPK = new ForeignPK("100", "kmelia10");
    PublicationService publicationService = PowerMockito.mock(PublicationService.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("110", "kmelia11"),
            new NodePK("120", "kmelia12")));
    Collection<Alias> aliasPks = new ArrayList<Alias>(fatherPks.size());
    for (NodePK nodePK : fatherPks) {
      aliasPks.add(new Alias(nodePK.getId(), nodePK.getInstanceId()));
    }
    PowerMockito.when(publicationService.getAllFatherPK(new PublicationPK("100", "kmelia10")))
        .thenReturn(fatherPks);
    PowerMockito.when(publicationService.getAlias(new PublicationPK("100", "kmelia10")))
        .thenReturn(aliasPks);
    /* to replace with ServiceProvider
    PowerMockito
        .when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
        .thenReturn(publicationService);
        */

    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */

    SimpleDocumentPK resourcePk = new SimpleDocumentPK("5", "kmelia2");
    SimpleDocument attachmentDetail = new SimpleDocument();
    attachmentDetail.setPK(resourcePk);
    attachmentDetail.setForeignId(publicationPK.getId());
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableAttachment resource = new ShareableAttachment(token, attachmentDetail);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(true));
  }

  /**
   * Test of isReadable method, of class NodeAccessControl for an AttachmentDetail.
   */
  @Test
  public void testIsUnReadableAttachment() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    ForeignPK publicationPK = new ForeignPK("100", "kmelia10");
    PublicationService publicationService = PowerMockito.mock(PublicationService.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("110", "kmelia11"),
            new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationService.getAllFatherPK(new PublicationPK("100", "kmelia10")))
        .thenReturn(fatherPks);
    /* to replace with ServiceProvider
    PowerMockito
        .when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationService.class))
        .thenReturn(publicationService);
        */

    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("10", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */

    SimpleDocumentPK resourcePk = new SimpleDocumentPK("5", "kmelia2");
    SimpleDocument attachmentDetail = new SimpleDocument();
    attachmentDetail.setPK(resourcePk);
    attachmentDetail.setForeignId(publicationPK.getId());
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableAttachment resource = new ShareableAttachment(token, attachmentDetail);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(false));
  }

  /**
   * Test of isReadable method, of class NodeAccessControl  for an AttachmentDetail.
   */
  @Test
  public void testIsReadableNode() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("100", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */
    NodeDetail nodeDetail = new NodeDetail();
    nodeDetail.setNodePK(pk);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableNode resource = new ShareableNode(token, nodeDetail);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(true));
  }

  /**
   * Test of isReadable method, of class NodeAccessControl for an AttachmentDetail.
   */
  @Test
  public void testIsUnReadableNode() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);

    NodePK pk = new NodePK("10", "kmelia10");
    NodeService nodeService = PowerMockito.mock(NodeService.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays
        .asList(new NodePK("10", "kmelia10"), new NodePK("11", "kmelia11"),
            new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeService.getDescendantPKs(pk)).thenReturn(descendants);
    /* to replace with ServiceProvider
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeService.class)).
        thenReturn(nodeService);
        */

    NodeDetail nodeDetail = new NodeDetail();
    nodeDetail.setNodePK(new NodePK("15", "kmelia10"));
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableNode resource = new ShareableNode(token, nodeDetail);
    PowerMockito.mockStatic(SharingServiceProvider.class);

    PowerMockito.when(SharingServiceProvider.getSharingTicketService())
        .thenReturn(new NodeSharingTicketService(token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(false));
  }
}

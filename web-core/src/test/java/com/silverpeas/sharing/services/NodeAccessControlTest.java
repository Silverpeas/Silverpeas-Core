/*
 *  Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.sharing.services;

import com.silverpeas.sharing.mock.NodeSharingTicketService;
import com.silverpeas.sharing.security.ShareableAttachment;
import com.silverpeas.sharing.security.ShareableNode;
import com.silverpeas.sharing.security.ShareableVersionDocument;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({com.stratelia.webactiv.util.EJBUtilitaire.class, SharingServiceFactory.class})
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
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = PowerMockito.mock(PublicationBm.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays.asList(new NodePK("100", "kmelia10"),
            new NodePK("110", "kmelia11"), new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationBm.getAllFatherPK(new PublicationPK("100", "kmelia10"))).thenReturn(fatherPks);
    PowerMockito.when(home.create()). thenReturn(publicationBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).thenReturn(home);

    NodePK pk = new NodePK("10", "kmelia10");
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("100",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);

    DocumentPK resourcePk = new DocumentPK(5, "kmelia2");
    Document document = new Document();
    document.setPk(resourcePk);
    document.setForeignKey(publicationPK);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(true));
  }
  
  /**
   * Test of isReadable method, of class NodeAccessControl for a version document..
   */
  @Test
  public void testIsUnReadableDocument() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    ForeignPK publicationPK = new ForeignPK("100", "kmelia10");
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = PowerMockito.mock(PublicationBm.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays.asList(new NodePK("100", "kmelia10"),
            new NodePK("110", "kmelia11"), new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationBm.getAllFatherPK(new PublicationPK("100", "kmelia10"))).thenReturn(fatherPks);
    PowerMockito.when(home.create()). thenReturn(publicationBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).thenReturn(home);

    NodePK pk = new NodePK("10", "kmelia10");
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("10",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);

    DocumentPK resourcePk = new DocumentPK(5, "kmelia2");
    Document document = new Document();
    document.setPk(resourcePk);
    document.setForeignKey(publicationPK);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
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
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = PowerMockito.mock(PublicationBm.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays.asList(new NodePK("100", "kmelia10"),
            new NodePK("110", "kmelia11"), new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationBm.getAllFatherPK(new PublicationPK("100", "kmelia10"))).thenReturn(fatherPks);
    PowerMockito.when(home.create()). thenReturn(publicationBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).thenReturn(home);

    NodePK pk = new NodePK("10", "kmelia10");
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("100",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);

    AttachmentPK resourcePk = new AttachmentPK("5", "kmelia2");
    AttachmentDetail attachmentDetail = new AttachmentDetail(resourcePk);
    attachmentDetail.setForeignKey(publicationPK);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableAttachment resource = new ShareableAttachment(token, attachmentDetail);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
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
    PublicationBmHome home = Mockito.mock(PublicationBmHome.class);
    PublicationBm publicationBm = PowerMockito.mock(PublicationBm.class);
    Collection<NodePK> fatherPks = new ArrayList<NodePK>(Arrays.asList(new NodePK("100", "kmelia10"),
            new NodePK("110", "kmelia11"), new NodePK("120", "kmelia12")));
    PowerMockito.when(publicationBm.getAllFatherPK(new PublicationPK("100", "kmelia10"))).thenReturn(fatherPks);
    PowerMockito.when(home.create()). thenReturn(publicationBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).thenReturn(home);

    NodePK pk = new NodePK("10", "kmelia10");
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("10",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);

    AttachmentPK resourcePk = new AttachmentPK("5", "kmelia2");
    AttachmentDetail attachmentDetail = new AttachmentDetail(resourcePk);
    attachmentDetail.setForeignKey(publicationPK);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableAttachment resource = new ShareableAttachment(token, attachmentDetail);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
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
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("100",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);
    NodeDetail nodeDetail = new NodeDetail();
    nodeDetail.setNodePK(pk);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableNode resource = new ShareableNode(token, nodeDetail);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
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
    NodeBmHome nodeHome = Mockito.mock(NodeBmHome.class);
    NodeBm nodeBm = PowerMockito.mock(NodeBm.class);
    Collection<NodePK> descendants = new ArrayList<NodePK>(Arrays.asList(new NodePK("10",
            "kmelia10"), new NodePK("11", "kmelia11"), new NodePK("12", "kmelia12")));
    PowerMockito.when(nodeBm.getDescendantPKs(pk)).thenReturn(descendants);
    PowerMockito.when(nodeHome.create()).thenReturn(nodeBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).
            thenReturn(nodeHome);

    NodeDetail nodeDetail = new NodeDetail();
    nodeDetail.setNodePK(new NodePK("15", "kmelia10"));
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableNode resource = new ShareableNode(token, nodeDetail);
    PowerMockito.mockStatic(SharingServiceFactory.class);

    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new NodeSharingTicketService(
            token, pk));
    NodeAccessControl instance = new NodeAccessControl();
    boolean result = instance.isReadable(resource);
    assertThat(result, is(false));
  }
}

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

import com.silverpeas.sharing.mock.VersionSharingTicketService;
import com.silverpeas.sharing.security.ShareableVersionDocument;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
/*
 * @RunWith(SpringJUnit4ClassRunner.class) @ContextConfiguration(locations =
 * {"/spring-sharing-document-datasource.xml", "/spring-sharing-service.xml"})
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({com.stratelia.webactiv.util.EJBUtilitaire.class, SharingServiceFactory.class})
public class VersionFileAccessControlTest {

  public VersionFileAccessControlTest() {
  }

  /**
   * Test of isReadable method, of class SimpleFileAccessControl.
   *
   * @throws Exception
   */
  @Test
  public void testIsReadable() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    VersioningBmHome home = Mockito.mock(VersioningBmHome.class);
    VersioningBm versionBm = Mockito.mock(VersioningBm.class);
    PowerMockito.when(home.create()).thenReturn(versionBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
            VersioningBmHome.class)).thenReturn(home);
    DocumentPK pk = new DocumentPK(5, "kmelia2");
    Document document = new Document();
    document.setPk(pk);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceFactory.class);
    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new VersionSharingTicketService(
            token, pk));
    PowerMockito.when(versionBm.getDocument(pk)).thenReturn(document);
    VersionFileAccessControl instance = new VersionFileAccessControl();
    boolean expResult = true;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }
  
  @Test
  public void testIsNotReadable() throws Exception {
    PowerMockito.mockStatic(EJBUtilitaire.class);
    VersioningBmHome home = Mockito.mock(VersioningBmHome.class);
    VersioningBm versionBm = Mockito.mock(VersioningBm.class);
    PowerMockito.when(home.create()).thenReturn(versionBm);
    PowerMockito.when(EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
            VersioningBmHome.class)).thenReturn(home);
    DocumentPK resourcePk = new DocumentPK(5, "kmelia2");
    Document document = new Document();
    document.setPk(resourcePk);
    final String token = "965e985d-c711-47b3-a467-62779505965e985d-c711-47b3-a467-62779505";
    ShareableVersionDocument resource = new ShareableVersionDocument(token, document);
    PowerMockito.mockStatic(SharingServiceFactory.class);
    DocumentPK pk = new DocumentPK(5, "kmelia2");
    PowerMockito.when(SharingServiceFactory.getSharingTicketService()).thenReturn(new VersionSharingTicketService(
            token, pk));
    PowerMockito.when(versionBm.getDocument(pk)).thenReturn(document);
    VersionFileAccessControl instance = new VersionFileAccessControl();
    boolean expResult = true;
    boolean result = instance.isReadable(resource);
    assertThat(result, is(expResult));
  }
}

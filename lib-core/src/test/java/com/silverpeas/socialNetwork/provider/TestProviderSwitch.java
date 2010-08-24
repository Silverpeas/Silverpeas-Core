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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.socialNetwork.provider;

import com.silverpeas.socialNetwork.model.SocialInformationType;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author azzedine
 */
public class TestProviderSwitch {

  public TestProviderSwitch() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  // TODO add test methods here.
  // The methods must be annotated with annotation @Test. For example:
  //
  @Test
  @SuppressWarnings("empty-statement")
  public void testGetSocialInformationsList() throws SilverpeasException {
    List<String> listEvent = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      listEvent.add("Event" + i);
    }
    List<String> listPub = new ArrayList<String>();
    for (int i = 0; i < 3; i++) {
      listPub.add("Pub" + i);
    }
    List<String> listPhoto = new ArrayList<String>();
    for (int i = 0; i < 2; i++) {
      listPhoto.add("Pub" + i);
    }
    SocialEventsInterface eventsInterface = mock(SocialEventsInterface.class);
    SocialGalleryInterface galleryInterface = mock(SocialGalleryInterface.class);
    SocialPublicationsInterface publicationsInterface = mock(SocialPublicationsInterface.class);
    SocialStatusInterface statusInterface = mock(SocialStatusInterface.class);
    SocialRelationShipsInterface socialRelationShipsInterface = mock(SocialRelationShipsInterface.class);
    ProviderSwitch switch1 = new ProviderSwitch();
    switch1.setSocialEventsInterface(eventsInterface);
    switch1.setSocialGalleryInterface(galleryInterface);
    switch1.setSocialPublicationsInterface(publicationsInterface);
    switch1.setSocialStatusInterface(statusInterface);
    switch1.setSocialRelationShipsInterface(socialRelationShipsInterface);
    when(switch1.getSocialEventsInterface().getSocialInformationsList(null, null, 0, 0)).thenReturn(
        listEvent);
    when(switch1.getSocialGalleryInterface().getSocialInformationsList(null, 0, 0)).thenReturn(
        listPhoto);
    when(switch1.getSocialPublicationsInterface().getSocialInformationsList(null, 0, 0)).thenReturn(
        listPub);
     when(switch1.getSocialStatusInterface().getSocialInformationsList(null, 0, 0)).thenReturn(
        null);
     when(switch1.getSocialRelationShipsInterface().getSocialInformationsList(null, 0, 0)).thenReturn(
        null);
     
    List list = (List) switch1.getSocialInformationsList(SocialInformationType.EVENT, null,
        null, 0, 0);
    assertEquals("must be equal 4", list.size(), 4);
    list = (List) switch1.getSocialInformationsList(SocialInformationType.PUBLICATION, null,
        null, 0, 0);
    assertEquals("must be equal 3", list.size(), 3);
    list = (List) switch1.getSocialInformationsList(SocialInformationType.PHOTO, null,
        null, 0, 0);
    assertNotNull("must be not null", list);
    list = (List) switch1.getSocialInformationsList(SocialInformationType.ALL, null,
        null, 0, 0);
    assertEquals("must be equal 3", list.size(), 5);
    when(switch1.getSocialGalleryInterface().getSocialInformationsList(null, 0, 0)).thenReturn(null);
    list = (List) switch1.getSocialInformationsList(SocialInformationType.ALL, null,
        null, 0, 0);
    assertEquals("must be equal 3", list.size(), 3);
  }
}

/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.socialnetwork.provider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.model.SocialInformationType;
import com.silverpeas.socialnetwork.status.SocialInformationStatus;
import com.silverpeas.socialnetwork.status.Status;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
  public void testGetSocialInformationsList() throws SilverpeasException {
    List<SocialInformation> listEvent = new ArrayList<SocialInformation>();
    Status status = new Status(0, new Date(), "description");
    for (int i = 0; i < 4; i++) {
      SocialInformationStatus event = new SocialInformationStatus(status);
      listEvent.add(event);
    }
    List<SocialInformation> listPub = new ArrayList<SocialInformation>();
    for (int i = 0; i < 3; i++) {
      SocialInformationStatus publi = new SocialInformationStatus(status);
      listPub.add(publi);
    }
    List<SocialInformation> listPhoto = new ArrayList<SocialInformation>();
    for (int i = 0; i < 2; i++) {
      SocialInformationStatus image = new SocialInformationStatus(status);
      listPhoto.add(image);
    }
    SocialEventsInterface eventsInterface = mock(SocialEventsInterface.class);
    SocialGalleryInterface galleryInterface = mock(SocialGalleryInterface.class);
    SocialPublicationsInterface publicationsInterface = mock(SocialPublicationsInterface.class);
    SocialStatusInterface statusInterface = mock(SocialStatusInterface.class);
    SocialRelationShipsInterface socialRelationShipsInterface = mock(
        SocialRelationShipsInterface.class);
    ProviderSwitch switch1 = new ProviderSwitch();
    switch1.setSocialEventsInterface(eventsInterface);
    switch1.setSocialGalleryInterface(galleryInterface);
    switch1.setSocialPublicationsInterface(publicationsInterface);
    switch1.setSocialStatusInterface(statusInterface);
    switch1.setSocialRelationShipsInterface(socialRelationShipsInterface);

    com.silverpeas.calendar.Date begin = null;
    com.silverpeas.calendar.Date end = null;
    try {
      begin = new com.silverpeas.calendar.Date(DateUtil.parse("2011/02/28"));
      end = new com.silverpeas.calendar.Date(DateUtil.parse("2011/02/01"));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    when(switch1.getSocialEventsInterface().getSocialInformationsList(null, null, begin, end)).thenReturn(
        listEvent);
    when(switch1.getSocialGalleryInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listPhoto);
    when(switch1.getSocialPublicationsInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listPub);
    when(switch1.getSocialStatusInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        null);
    when(switch1.getSocialRelationShipsInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        null);

    List<SocialInformation> list = switch1.getSocialInformationsList(SocialInformationType.EVENT, null,
        null, begin, end);
    assertEquals("must be equal 4", list.size(), 4);
    list = switch1.getSocialInformationsList(SocialInformationType.PUBLICATION, null,
        null, begin, end);
    assertEquals("must be equal 3", list.size(), 3);
    list = switch1.getSocialInformationsList(SocialInformationType.PHOTO, null,
        null, begin, end);
    assertNotNull("must be not null", list);
    list = switch1.getSocialInformationsList(SocialInformationType.ALL, null,
        null, begin, end);
    assertEquals("must be equal 3", list.size(), 5);
    when(switch1.getSocialGalleryInterface().getSocialInformationsList(null, begin, end)).thenReturn(null);
    list = switch1.getSocialInformationsList(SocialInformationType.ALL, null,
        null, begin, end);
    assertEquals("must be equal 3", list.size(), 3);
  }
}

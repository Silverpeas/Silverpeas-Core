/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.social.provider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.silverpeas.core.socialnetwork.provider.SocialCommentGalleryInterface;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.socialnetwork.provider.ProviderSwitch;
import org.silverpeas.core.socialnetwork.provider.SocialEventsInterface;
import org.silverpeas.core.socialnetwork.provider.SocialGalleryInterface;
import org.silverpeas.core.socialnetwork.provider.SocialPublicationsInterface;
import org.silverpeas.core.socialnetwork.provider.SocialCommentPublicationsInterface;
import org.silverpeas.core.socialnetwork.provider.SocialCommentQuickInfosInterface;
import org.silverpeas.core.socialnetwork.provider.SocialRelationShipsInterface;
import org.silverpeas.core.socialnetwork.provider.SocialStatusInterface;
import org.silverpeas.core.socialnetwork.status.SocialInformationStatus;
import org.silverpeas.core.socialnetwork.status.Status;

import org.silverpeas.core.util.DateUtil;
import org.silverpeas.util.exception.SilverpeasException;

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
    Status status = new Status(0, new Date(), "description");

    List<SocialInformation> listEvent = new ArrayList<SocialInformation>();
    for (int i = 0; i < 4; i++) {
      SocialInformationStatus event = new SocialInformationStatus(status);
      listEvent.add(event);
    }

    List<SocialInformation> listPhoto = new ArrayList<SocialInformation>();
    for (int i = 0; i < 2; i++) {
      SocialInformationStatus image = new SocialInformationStatus(status);
      listPhoto.add(image);
    }

    List<SocialInformation> listCommentPhoto = new ArrayList<SocialInformation>();
    for (int i = 0; i < 6; i++) {
      SocialInformationStatus commentImage = new SocialInformationStatus(status);
      listCommentPhoto.add(commentImage);
    }

    List<SocialInformation> listPub = new ArrayList<SocialInformation>();
    for (int i = 0; i < 3; i++) {
      SocialInformationStatus publi = new SocialInformationStatus(status);
      listPub.add(publi);
    }

    List<SocialInformation> listCommentPub = new ArrayList<SocialInformation>();
    for (int i = 0; i < 5; i++) {
      SocialInformationStatus commentPubli = new SocialInformationStatus(status);
      listCommentPub.add(commentPubli);
    }

    List<SocialInformation> listCommentNews = new ArrayList<SocialInformation>();
    for (int i = 0; i < 1; i++) {
      SocialInformationStatus commentNews = new SocialInformationStatus(status);
      listCommentNews.add(commentNews);
    }

    List<SocialInformation> listStatus = new ArrayList<SocialInformation>();
    for (int i = 0; i < 4; i++) {
      SocialInformationStatus eventStatus = new SocialInformationStatus(status);
      listStatus.add(eventStatus);
    }

    List<SocialInformation> listRelationShip = new ArrayList<SocialInformation>();
    for (int i = 0; i < 2; i++) {
      SocialInformationStatus relationShip = new SocialInformationStatus(status);
      listRelationShip.add(relationShip);
    }

    SocialEventsInterface eventsInterface = mock(SocialEventsInterface.class);
    SocialGalleryInterface galleryInterface = mock(SocialGalleryInterface.class);
    SocialCommentGalleryInterface commentGalleryInterface = mock(SocialCommentGalleryInterface.class);
    SocialPublicationsInterface publicationsInterface = mock(SocialPublicationsInterface.class);
    SocialCommentPublicationsInterface commentPublicationsInterface = mock(SocialCommentPublicationsInterface.class);
    SocialCommentQuickInfosInterface commentNewsInterface = mock(SocialCommentQuickInfosInterface.class);
    SocialStatusInterface statusInterface = mock(SocialStatusInterface.class);
    SocialRelationShipsInterface socialRelationShipsInterface = mock(
        SocialRelationShipsInterface.class);

    ProviderSwitch switch1 = new ProviderSwitch();
    switch1.setSocialEventsInterface(eventsInterface);
    switch1.setSocialGalleryInterface(galleryInterface);
    switch1.setSocialCommentGalleryInterface(commentGalleryInterface);
    switch1.setSocialPublicationsInterface(publicationsInterface);
    switch1.setSocialCommentPublicationsInterface(commentPublicationsInterface);
    switch1.setSocialCommentQuickInfosInterface(commentNewsInterface);
    switch1.setSocialStatusInterface(statusInterface);
    switch1.setSocialRelationShipsInterface(socialRelationShipsInterface);

    org.silverpeas.core.date.Date begin = null;
    org.silverpeas.core.date.Date end = null;
    try {
      begin = new org.silverpeas.core.date.Date(DateUtil.parse("2011/02/28"));
      end = new org.silverpeas.core.date.Date(DateUtil.parse("2011/02/01"));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    when(switch1.getSocialEventsInterface().getSocialInformationsList(null, null, begin, end)).thenReturn(
        listEvent);
    when(switch1.getSocialGalleryInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listPhoto);
    when(switch1.getSocialCommentGalleryInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listCommentPhoto);
    when(switch1.getSocialPublicationsInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listPub);
    when(switch1.getSocialCommentPublicationsInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listCommentPub);
    when(switch1.getSocialCommentQuickInfosInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listCommentNews);
    when(switch1.getSocialStatusInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listStatus);
    when(switch1.getSocialRelationShipsInterface().getSocialInformationsList(null, begin, end)).thenReturn(
        listRelationShip);

    List<SocialInformation>  list = switch1.getSocialInformationsList(SocialInformationType.ALL, null,
        null, begin, end);
    assertEquals("must be equal 23", list.size(), 23);

    list = switch1.getSocialInformationsList(SocialInformationType.EVENT, null,
        null, begin, end);
    assertEquals("must be equal 4", list.size(), 4);

    list = switch1.getSocialInformationsList(SocialInformationType.COMMENTMEDIA, null,
        null, begin, end);
    assertEquals("must be equal 6", list.size(), 6);

    list = switch1.getSocialInformationsList(SocialInformationType.COMMENTNEWS, null,
        null, begin, end);
    assertEquals("must be equal 1", list.size(), 1);

  }
}

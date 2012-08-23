/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.admin.registry;

/**
 * PortletRegistryTags defines Tags that are used in the Portlet Registry XML files.
 */

public interface PortletRegistryTags {
  // Tags
  public static final String PORTLET_APP_REGISTRY_TAG = "PortletAppRegistry";
  public static final String PORTLET_APP_TAG = "PortletApp";
  public static final String PROPERTIES_TAG = "Properties";
  public static final String COLLECTION_TAG = "Collection";
  public static final String STRING_TAG = "String";

  public static final String PORTLET_WINDOW_REGISTRY_TAG = "PortletWindowRegistry";
  public static final String PORTLET_WINDOW_TAG = "PortletWindow";

  public static final String PORTLET_WINDOW_PREFERENCE_REGISTRY_TAG =
      "PortletWindowPreferenceRegistry";
  public static final String PORTLET_WINDOW_PREFERENCE_TAG = "PortletWindowPreference";
  public static final String LANG = "lang";

  // Keys
  public static final String VERSION_KEY = "version";
  public static final String PORTLET_NAME_KEY = "portletName";
  public static final String USER_NAME_KEY = "userName";
  public static final String REMOTE_KEY = "remote";
  public static final String NAME_KEY = "name";
  public static final String VALUE_KEY = "value";

  // Attributes used for Collection Tag
  public static final String ARCHIVE_KEY = "archive";
  public static final String ARCHIVE_NAME_KEY = "archiveName";
  public static final String ARCHIVE_TYPE_KEY = "archiveType";
  public static final String KEYWORDS_KEY = "keywords";
  public static final String DESCRIPTION_KEY = "description";
  public static final String SUPPORTED_CONTENT_TYPES_KEY = "supportedContentTypes";
  public static final String SUPPORTS_MAP_KEY = "supportsMap";
  public static final String SUPPORTED_LOCALES_KEY = "supportedLocales";
  public static final String DISPLAY_NAME_MAP_KEY = "displayNameMap";
  public static final String DESCRIPTION_MAP_KEY = "descriptionMap";
  public static final String TRANSPORT_GUARANTEE_KEY = "transportGuarantee";

  public static final String PREFERENCE_PROPERTIES_KEY = "prefProperties";
  public static final String PREFERENCE_READ_ONLY_KEY = "prefReadOnly";

  // Attributes used for String Tag
  public static final String ROW_KEY = "row";
  public static final String WIDTH_KEY = "width";
  public static final String TITLE_KEY = "title";
  public static final String SHORT_TITLE_KEY = "shortTitle";
  public static final String VISIBLE_KEY = "visible";
  public static final String ENTITY_ID_PREFIX_KEY = "entityIDPrefix";

  public static String ROLE_MAP_KEY = "roleMap";
  public static final String ROLE_DESCRIPTIONS_KEY = "roleDescriptions";
  public static String USER_INFO_MAP_KEY = "userInfoMap";
  public static final String USER_INFO_DESCRIPTIONS_KEY = "userInfoDescriptions";

  // Added for WSRP
  public static final String CONSUMER_ID = "consumerId";
  public static final String PRODUCER_ENTITY_ID = "producerEntityID";
  public static final String PORTLET_HANDLE = "portletHandle";
  public static final String PORTLET_ID = "portletID";

}

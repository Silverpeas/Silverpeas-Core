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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.sun.portal.portletcontainer.admin.registry;

/**
 * PortletRegistryTags defines Tags that are used in the Portlet Registry XML
 * files.
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

  public static final String PORTLET_WINDOW_PREFERENCE_REGISTRY_TAG = "PortletWindowPreferenceRegistry";
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

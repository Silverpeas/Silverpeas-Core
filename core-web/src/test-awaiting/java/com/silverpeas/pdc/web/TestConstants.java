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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import com.silverpeas.web.TestResources;

/**
 * All the constants dedicated to be used in unit tests.
 */
public interface TestConstants {

  static final String WEB_CONTEXT = "http://localhost:9998/silverpeas/";
  static final String USER_ID = TestResources.USER_ID_IN_TEST;
  static final String COMPONENT_INSTANCE_ID = "kmelia1";
  static final String CONTENT_ID = "8";
  static final String NODE_ID = "5";
  static final String CONTENT_CLASSIFICATION_PATH = "pdc/classification/" + COMPONENT_INSTANCE_ID
      + "/" + CONTENT_ID;
  static final String PDC_PATH = "pdc";
  static final String CONTENT_PDC_PATH = "pdc/" + COMPONENT_INSTANCE_ID + "?contentId=" + CONTENT_ID;
  static final String USED_PDC_PATH = "pdc/filter/used";
  static final String PDC_PATH_WITH_NO_CONTENT = "pdc/" + COMPONENT_INSTANCE_ID;
  static final String NODE_DEFAULT_CLASSIFICATION_PATH = "pdc/classification/"
      + COMPONENT_INSTANCE_ID + "?nodeId=" + NODE_ID;
  static final String COMPONENT_DEFAULT_CLASSIFICATION_PATH = "pdc/classification/"
      + COMPONENT_INSTANCE_ID;
  static final String UNKNOWN_DEFAULT_CLASSIFICATION_PATH = "pdc/classification/kmelia1003";
  static final String UNKNOWN_CONTENT_PDC_PATH = "pdc/kmelia3?contentId=2";
  static final String UNKNOWN_CONTENT_CLASSIFICATION_PATH = "pdc/classification/kmelia3/2";
  static final String FRENCH = "fr";
  static final String CLASSIFICATION_URI = WEB_CONTEXT + CONTENT_CLASSIFICATION_PATH;
  static final String PDC_URI_WITH_NO_CONTENT = WEB_CONTEXT + PDC_PATH_WITH_NO_CONTENT;
  static final String PDC_URI = WEB_CONTEXT + PDC_PATH;
  static final String NODE_DEFAULT_CLASSIFICATION_URI = WEB_CONTEXT
      + NODE_DEFAULT_CLASSIFICATION_PATH;
  static final String COMPONENT_DEFAULT_CLASSIFICATION_URI = WEB_CONTEXT
      + COMPONENT_DEFAULT_CLASSIFICATION_PATH;
  static final String PDC_USED_IN_CLASSIFICATION_URI = WEB_CONTEXT + USED_PDC_PATH;
}

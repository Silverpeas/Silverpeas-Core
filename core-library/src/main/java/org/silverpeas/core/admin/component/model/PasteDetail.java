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

package org.silverpeas.core.admin.component.model;

import java.util.HashMap;
import java.util.Map;

public class PasteDetail {
  private String fromSpaceId;
  String fromComponentId;
  private String toSpaceId;
  String toComponentId;
  String userId;
  private Map<String, String> options;
  public final static String OPTION_PREFIX = "PasteOption_";

  public PasteDetail() {

  }

  public PasteDetail(String userId) {
    setUserId(userId);
  }

  public PasteDetail(String fromComponentId, String userId) {
    setFromComponentId(fromComponentId);
    setUserId(userId);
  }

  public PasteDetail(String fromComponentId, String toComponentId, String userId) {
    setFromComponentId(fromComponentId);
    setToComponentId(toComponentId);
    setUserId(userId);
  }

  public String getFromComponentId() {
    return fromComponentId;
  }

  public void setFromComponentId(String fromComponentId) {
    this.fromComponentId = fromComponentId;
  }

  public String getToComponentId() {
    return toComponentId;
  }

  public void setToComponentId(String toComponentId) {
    this.toComponentId = toComponentId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void addOption(String key, String value) {
    if (getOptions() == null) {
      setOptions(new HashMap<>());
    }
    getOptions().put(key, value);
  }

  public void setOptions(Map<String, String> options) {
    this.options = options;
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public void setToSpaceId(String toSpaceId) {
    this.toSpaceId = toSpaceId;
  }

  public String getToSpaceId() {
    return toSpaceId;
  }

  public void setFromSpaceId(String fromSpaceId) {
    this.fromSpaceId = fromSpaceId;
  }

  public String getFromSpaceId() {
    return fromSpaceId;
  }

}

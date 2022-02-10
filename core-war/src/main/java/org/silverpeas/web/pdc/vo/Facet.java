/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.vo;

import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facet {

  private String name;
  private String id;
  private Map<String, FacetEntryVO> entries = new HashMap<>();
  private List<FacetEntryVO> sortedEntries;

  public Facet(String id, String name) {
    super();
    this.name = name;
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<FacetEntryVO> getEntries() {
    return entries.values();
  }

  public List<FacetEntryVO> getSortedEntries() {
    if (sortedEntries == null) {
      sortedEntries = new ArrayList<>(entries.values());
    }
    return sortedEntries;
  }

  public void addEntry(FacetEntryVO entry) {
    if (entry != null && StringUtil.isDefined(entry.getName())) {
      FacetEntryVO registeredEntry = entries.computeIfAbsent(entry.getId(), k -> entry);
      registeredEntry.incrementEntry();
    }
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public FacetEntryVO getEntryById(String id) {
    return entries.get(id);
  }

}
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.vo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultGroupFilter {

  private Facet authorFacet = null;
  private Facet componentFacet = null;
  private Facet datatypeFacet = null;
  private Facet filetypeFacet = null;
  private Facet lastUpdateFacet = null;

  private List<Facet> formfieldFacets;

  /**
   * Default constructor
   */
  public ResultGroupFilter() {
    super();
  }

  public Facet getAuthorFacet() {
    return authorFacet;
  }

  public void setAuthorFacet(Facet authorFacet) {
    this.authorFacet = authorFacet;
  }

  public Facet getComponentFacet() {
    return componentFacet;
  }

  public void setComponentFacet(Facet componentFacet) {
    this.componentFacet = componentFacet;
  }

  public void setFormFieldFacets(List<Facet> formfields) {
    this.formfieldFacets = formfields;
  }

  public List<Facet> getFormFieldFacets() {
    return formfieldFacets;
  }

  public void sortFacetsEntries() {
    EntryComparator comparator = new EntryComparator();
    YearComparator yearComparator = new YearComparator();
    Collections.sort(authorFacet.getSortedEntries(), comparator);
    Collections.sort(componentFacet.getSortedEntries(), comparator);
    Collections.sort(datatypeFacet.getSortedEntries(), comparator);
    Collections.sort(filetypeFacet.getSortedEntries(), comparator);
    Collections.sort(lastUpdateFacet.getSortedEntries(), yearComparator);

    for (Facet formFieldFacet : formfieldFacets) {
      if (formFieldFacet instanceof FacetOnDates) {
        Collections.sort(formFieldFacet.getSortedEntries(), yearComparator);
      } else {
        Collections.sort(formFieldFacet.getSortedEntries(), comparator);
      }
    }
  }

  public void setDatatypeFacet(Facet datatypeFacet) {
    this.datatypeFacet = datatypeFacet;
  }

  public Facet getDatatypeFacet() {
    return datatypeFacet;
  }

  public Facet getFiletypeFacet() {
    return filetypeFacet;
  }

  public void setFiletypeFacet(Facet filetypeFacet) {
    this.filetypeFacet = filetypeFacet;
  }

  public Facet getLastUpdateFacet() {
    return lastUpdateFacet;
  }

  public void setLastUpdateFacet(Facet lastUpdateFacet) {
    this.lastUpdateFacet = lastUpdateFacet;
  }

  public void checkSelectedFacetsEntries(ResultFilterVO selectedFacetEntries) {
    if (selectedFacetEntries != null && !selectedFacetEntries.isEmpty()) {
      checkSelectedFacetEntries(authorFacet, selectedFacetEntries.getAuthorId());
      checkSelectedFacetEntries(lastUpdateFacet, selectedFacetEntries.getLastUpdate());
      checkSelectedFacetEntries(filetypeFacet, selectedFacetEntries.getFiletype());
      checkSelectedFacetEntries(datatypeFacet, selectedFacetEntries.getDatatype());
      checkSelectedFacetEntries(componentFacet, selectedFacetEntries.getComponentId());

      for (Facet facet : formfieldFacets) {
        checkSelectedFacetEntries(facet,
            selectedFacetEntries.getFormFieldSelectedFacetEntry(facet.getId()));
      }
    }
  }

  private void checkSelectedFacetEntries(Facet facet, String selectedFacetEntry) {
    for (FacetEntryVO entry : facet.getEntries()) {
      if (entry.getId().equals(selectedFacetEntry)) {
        entry.setSelected(true);
      }
    }
  }

  private class EntryComparator implements Comparator<FacetEntryVO>{
    @Override
    public int compare(FacetEntryVO o1, FacetEntryVO o2) {
      int comp = o2.getNbElt() - o1.getNbElt();
      if (comp != 0) {
        return comp;
      }
      // sort same weight entries according to alphabetical order
      return o1.getName().compareTo(o2.getName());
    }
  }

  private class YearComparator implements Comparator<FacetEntryVO>{
    @Override
    public int compare(FacetEntryVO o1, FacetEntryVO o2) {
      return o2.getName().compareTo(o1.getName());
    }
  }

}
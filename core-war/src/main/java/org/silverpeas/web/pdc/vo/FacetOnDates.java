package org.silverpeas.web.pdc.vo;

import org.silverpeas.core.util.DateUtil;

import java.time.LocalDate;

/**
 * Created by Nicolas on 11/01/2017.
 */
public class FacetOnDates extends Facet {

  public FacetOnDates(String id, String name) {
    super(id, name);
  }

  public FacetEntryVO addEntry(String d) {
    return addEntry(DateUtil.toLocalDate(d));
  }

  public FacetEntryVO addEntry(LocalDate date) {
    String year = String.valueOf(date.getYear());
    FacetEntryVO entry = new FacetEntryVO(year, year);
    super.addEntry(entry);
    return entry;
  }

}
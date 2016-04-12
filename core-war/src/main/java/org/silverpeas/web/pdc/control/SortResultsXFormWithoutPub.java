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
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.contribution.contentcontainer.content.XMLFormFieldComparator;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class sort the result using a form xml field and filter the results to display only the
 * publication
 *
 * @author david derigent
 */
@Named
public class SortResultsXFormWithoutPub implements SortResults {

  private PdcSearchSessionController pdcSearchSessionController;
  @Inject
  private IndexSearcher indexSearcher;

  private SortResultsXFormWithoutPub() {
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.web.pdc.control.SortResults#execute(java.util.List, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<GlobalSilverResult> execute(List<GlobalSilverResult> originalResults, String sortOrder,
      String sortValue, String language) {

    List<GlobalSilverResult> modifiedResults = new ArrayList<GlobalSilverResult>(originalResults
        .size());
    for (GlobalSilverResult originalResult : originalResults) {
      // Retrieve a matching index entry for the publication if
      // the original matching index entry was wrapping an attachment
      if (originalResult.getType() != null && originalResult.getType().startsWith("Attachment")) {
        MatchingIndexEntry mie = indexSearcher.search(originalResult.getInstanceId(), originalResult
            .getId(), "Publication");
        if (mie == null) {
          continue;
        }
        GlobalSilverResult newResult = pdcSearchSessionController.
            matchingIndexEntry2GlobalSilverResult(mie);
        modifiedResults.add(newResult);
      } // If not an attachment or a publication, skip
      else if ("Publication".equals(originalResult.getType())) {
        modifiedResults.add(originalResult);
      }
    }

    List<GlobalSilverResult> filteredResults = new ArrayList<GlobalSilverResult>(originalResults
        .size());
    for (GlobalSilverResult modifiedResult : modifiedResults) {
      IndexEntryKey entryToTest = modifiedResult.getIndexEntry().getPK();
      // Check to see if the corresponding publication is already in the list
      // as we don't want duplicates
      boolean toAdd = true;
      for (GlobalSilverResult filteredResult : filteredResults) {
        IndexEntryKey filteredPK = filteredResult.getIndexEntry().getPK();
        if (filteredPK.equals(entryToTest)) {
          toAdd = false;
          break;
        }
      }
      if (toAdd) {
        filteredResults.add(modifiedResult);
      }
    }

    // sorts the result on a XML form field
    XMLFormFieldComparator comparator = new XMLFormFieldComparator(sortValue, sortOrder);
    Collections.sort(filteredResults, comparator);
    return filteredResults;
  }

  @Override
  public void setPdcSearchSessionController(PdcSearchSessionController controller) {
    this.pdcSearchSessionController = controller;
  }
}
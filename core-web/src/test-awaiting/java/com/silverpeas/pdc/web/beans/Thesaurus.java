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

package com.silverpeas.pdc.web.beans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A thesaurus to use in tests.
 * The thesaurus manages the synonyms of terms in a classification plan (named PdC).
 */
public class Thesaurus {

  /**
   * Gets the synonym of the specified term.
   * @param term the term.
   * @return a collection of synonyms. If no synonyms exist for the term, then an empty collection
   * is returned.
   */
  public Collection<String> getSynonyms(String term) {
    Set<String> synonyms = new HashSet<String>();
    if ("religion".equalsIgnoreCase(term)) {
      synonyms.add("culte");
      synonyms.add("doctrine");
      synonyms.add("théologie");
    } else if ("période".equalsIgnoreCase(term)) {
      synonyms.add("âge");
      synonyms.add("ère");
      synonyms.add("époque");
    } else if ("pays".equalsIgnoreCase(term)) {
      synonyms.add("nation");
      synonyms.add("région");
    }
    return synonyms;
  }

}

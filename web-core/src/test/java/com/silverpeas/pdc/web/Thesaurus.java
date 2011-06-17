/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.pdc.web;

import java.util.List;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static com.silverpeas.pdc.web.TestConstants.*;

/**
 * A thesaurus to use in tests.
 * The thesaurus, in order to map synonyms with terms, maintains also a dictionary of terms with
 * their relationship (into an hierarchical semantic tree). From them, the PdC values can be
 * computed and used in unit tests on the classification of resources on the PdC.
 * The dictionary is loaded at class loading.
 */
public class Thesaurus {

  private static final Map<String, Term> terms = new HashMap<String, Term>();
  private static final Map<String, List<Term>> trees = new HashMap<String, List<Term>>();
  private static final String[] treeIds = new String[] {"1", "2", "3"};
  
  static {
    terms.put("1", new Term("1", treeIds[0], "Pays"));
    terms.put("2", new Term("2", treeIds[0], "France"));
    terms.put("3", new Term("3", treeIds[0], "Isère"));
    terms.put("4", new Term("4", treeIds[1], "Période"));
    terms.put("5", new Term("5", treeIds[1], "Moyen-Age"));
    terms.put("6", new Term("6", treeIds[2], "Religion"));
    terms.put("7", new Term("7", treeIds[2], "Christianisme"));
    
    List<Term> termsInATree = new ArrayList<Term>(3);
    termsInATree.add(terms.get("1"));
    termsInATree.add(terms.get("2"));
    termsInATree.add(terms.get("3"));
    trees.put("1", termsInATree);
    termsInATree = new ArrayList<Term>(2);
    termsInATree.add(terms.get("4"));
    termsInATree.add(terms.get("5"));
    trees.put("2", termsInATree);
    termsInATree = new ArrayList<Term>(2);
    termsInATree.add(terms.get("6"));
    termsInATree.add(terms.get("7"));
    trees.put("3", termsInATree);
  }

  /**
   * Gets all the synonyms of the specified term.
   * @param termId the identifier of the term.
   * @return a collection of synonyms. If no synonyms exist for the term, then an empty collection
   * is returned.
   */
  public Collection<String> getSynonyms(String termId) {
    Term term = terms.get(termId);
    if (term != null) {
      return getSynonyms(term);
    }
    return new HashSet<String>();
  }

  /**
   * Gets the synonym of the specified term.
   * @param term the term.
   * @return a collection of synonyms. If no synonyms exist for the term, then an empty collection
   * is returned.
   */
  private Collection<String> getSynonyms(Term term) {
    Set<String> synonyms = new HashSet<String>();
    if ("religion".equalsIgnoreCase(term.getName())) {
      synonyms.add("culte");
      synonyms.add("doctrine");
      synonyms.add("théologie");
    } else if ("période".equalsIgnoreCase(term.getName())) {
      synonyms.add("âge");
      synonyms.add("ère");
      synonyms.add("époque");
    } else if ("pays".equalsIgnoreCase(term.getName())) {
      synonyms.add("nation");
      synonyms.add("région");
    }
    return synonyms;
  }
  
  /**
   * Gets the identifiers of the semantic trees available in this thesaurus for the tests.
   * @return a list of tree identifiers.
   */
  public List<String> getTreeIds() {
    return Arrays.asList(treeIds);
  }

  /**
   * Gets the values that belong to the specified semantic tree.
   * @param treeId the identifier of the tree.
   * @return a collection of PdC value objects.
   */
  public Collection<Value> getValuesFromTree(String treeId) {
    Set<Value> values = new HashSet<Value>();
    List<Term> termsInTree = trees.get(treeId);
    if (termsInTree != null) {
      String fatherId = "-1";
      for (int i = 0; i < termsInTree.size(); i++) {
        Term term = termsInTree.get(i);
        values.add(anI18NValue(term.getId(), treeId, term.getName(), "2011/06/14", "0", 
                pathInTreeOfTerm(term.getId()), i, 1, fatherId));
        fatherId = String.valueOf(i);
      }
    }
    return values;
  }

  protected Value anI18NValue(String id, String treeId, String name, String creationDate,
          String creatorId, String path, int level, int order, String fatherId) {
    Value value = new Value(id, treeId, name, creationDate, creatorId, path, level, order, fatherId);
    value.setLanguage(FRENCH);
    TreeNodeI18N translation = new TreeNodeI18N(Integer.valueOf(id), FRENCH, name, "");
    value.addTranslation(translation);
    return value;
  }
  
  private String pathInTreeOfTerm(String termId) {
    String path = "";
    Term term = terms.get(termId);
    if (term != null) {
      List<Term> termsInTree = trees.get(term.getTreeId());
      for (Term aTermInTree : termsInTree) {
        path += "/" + aTermInTree.getName();
      }
    }
    return path;
  }
  
  private static class Term {
    
    private String id;
    private String treeId;
    private String name;
  
    protected Term(String id, String treeId, String name) {
      this.id = id;
      this.treeId = treeId;
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getTreeId() {
      return treeId;
    }
  }
}

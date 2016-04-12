/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.attachment.util;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.comparator.AbstractComplexComparator;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This list provides some additional useful behaviors around simple documents.
 * @author: Yohann Chastagnier
 * @param <SIMPLE_DOCUMENT> the type of simple document that the list contains.
 */
public class SimpleDocumentList<SIMPLE_DOCUMENT extends SimpleDocument>
    extends ArrayList<SIMPLE_DOCUMENT> {
  private static final long serialVersionUID = 4986827710138035170L;

  private static final String[] ALL_LANGUAGES_BY_PRIORITY =
      I18NHelper.getAllSupportedLanguages().toArray(new String[I18NHelper.getNumberOfLanguages()]);

  private String queryLanguage = null;

  public SimpleDocumentList(final int initialCapacity) {
    super(initialCapacity);
  }

  public SimpleDocumentList() {
    super();
  }

  public SimpleDocumentList(final Collection<? extends SIMPLE_DOCUMENT> c) {
    super(c);
  }

  /**
   * Gets the language used to perform the JCR query in order to load the current list.
   * @return the language is defined, null value otherwise.
   */
  public String getQueryLanguage() {
    return queryLanguage;
  }

  /**
   * Sets the language used to perform the JCR query in order to load the current list.
   * @param queryLanguage the language used to perform the JCR query in order to load the list.
   * @return itself.
   */
  public SimpleDocumentList<SIMPLE_DOCUMENT> setQueryLanguage(final String queryLanguage) {
    if (StringUtil.isDefined(queryLanguage)) {
      this.queryLanguage = queryLanguage;
    } else {
      this.queryLanguage = null;
    }
    return this;
  }

  /**
   * Removes from the current list all documents which content is in an other language than the one
   * returned by {@link #getQueryLanguage()}.
   * If {@link #getQueryLanguage()} returns null or an unknown languague, nothing is done.
   * @return itself.
   */
  public SimpleDocumentList<SIMPLE_DOCUMENT> removeLanguageFallbacks() {
    if (!isEmpty()) {
      String language = I18NHelper.checkLanguage(getQueryLanguage());
      if (language.equals(getQueryLanguage())) {
        Iterator<SIMPLE_DOCUMENT> it = iterator();
        while (it.hasNext()) {
          SIMPLE_DOCUMENT document = it.next();
          if (!language.equals(document.getLanguage())) {
            it.remove();
          }
        }
      }
    }
    return this;
  }

  /**
   * Orders the list by descending priority of the language and descending last update date.
   * By default, if no language priority is given, then the language priority of the platform is
   * taken into account. If a language priority is specified, then the language priorities of the
   * platform are overridden.
   * @param languageOrderedByPriority manual language priority definition ​​from the highest to the
   * lowest.
   * @return itself.
   */
  public SimpleDocumentList<SIMPLE_DOCUMENT> orderByLanguageAndLastUpdate(
      String... languageOrderedByPriority) {
    if (size() > 1) {
      Collections.sort(this, new LanguageAndLastUpdateComparator(languageOrderedByPriority,
          ORDER_BY.LANGUAGE_PRIORITY_DESC, ORDER_BY.LAST_UPDATE_DATE_DESC));
    }
    return this;
  }

  /**
   * Order by definitions.
   */
  private enum ORDER_BY {
    LANGUAGE_PRIORITY_DESC(false),
    LAST_UPDATE_DATE_DESC(false);

    private final boolean ascending;

    ORDER_BY(final boolean ascending) {
      this.ascending = ascending;
    }

    public boolean isAscending() {
      return ascending;
    }
  }

  /**
   * Comparator class that permits to order a list of simple documents by a language priority and
   * descending update date.
   */
  private class LanguageAndLastUpdateComparator extends AbstractComplexComparator<SIMPLE_DOCUMENT> {

    private Map<String, Integer> languagePriorityCache =
        new HashMap<String, Integer>(I18NHelper.getNumberOfLanguages());
    private final ORDER_BY[] orderBies;

    /**
     * Default constructor.
     * @param languageOrderedByPriority languages ​​from the highest to the lowest.
     * @param orderBies the order by directives.
     */
    private LanguageAndLastUpdateComparator(final String[] languageOrderedByPriority,
        ORDER_BY... orderBies) {
      this.orderBies = orderBies;
      if (ArrayUtil.contains(orderBies, ORDER_BY.LANGUAGE_PRIORITY_DESC)) {
        for (String language : ALL_LANGUAGES_BY_PRIORITY) {
          languagePriorityCache
              .put(language, (languagePriorityCache.size() + ALL_LANGUAGES_BY_PRIORITY.length + 1));
        }
        int i = 0;
        for (String language : languageOrderedByPriority) {
          languagePriorityCache.put(language, i++);
        }
        languagePriorityCache
            .put(null, (languagePriorityCache.size() + ALL_LANGUAGES_BY_PRIORITY.length + 1));
      }
    }

    @Override
    protected ValueBuffer getValuesToCompare(final SIMPLE_DOCUMENT simpleDocument) {
      ValueBuffer valueBuffer = new ValueBuffer();
      for (ORDER_BY orderBy : orderBies) {
        switch (orderBy) {
          case LANGUAGE_PRIORITY_DESC:
            valueBuffer.append(getLanguagePriorityIndex(simpleDocument), orderBy.isAscending());
            break;
          case LAST_UPDATE_DATE_DESC:
            valueBuffer.append((simpleDocument.getUpdated() != null ? simpleDocument.getUpdated() :
                simpleDocument.getCreated()), orderBy.isAscending());
            break;
          default:
            throw new UnsupportedOperationException();
        }
      }
      return valueBuffer;
    }

    /**
     * Gets the priority index of the language content of the document.
     * @param simpleDocument the document data.
     * @return the priority index of the language content, 0 is the highest priority and the less
     * the index decreases the less is the language priority
     */
    private int getLanguagePriorityIndex(final SIMPLE_DOCUMENT simpleDocument) {
      return languagePriorityCache.get(simpleDocument.getLanguage()) * -1;
    }
  }
}

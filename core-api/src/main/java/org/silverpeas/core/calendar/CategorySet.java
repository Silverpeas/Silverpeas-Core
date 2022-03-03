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
package org.silverpeas.core.calendar;

import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The categories in which a {@link Plannable} planned in a calendar is classified. The categories
 * are expected to be managed by the {@link Plannable} itself.
 */
@Embeddable
public class CategorySet implements Cloneable {

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "sb_cal_categories", joinColumns = {@JoinColumn(name = "id")})
  @Column(name = "category")
  private Set<String> categories = new HashSet<>();

  /**
   * Constructs an empty cateogries container. It is dedicated to the persistence engine.
   */
  public CategorySet() {
    // empty for JPA.
  }

  /**
   * Adds a category to a {@link Plannable}. The category is specified by its unique
   * identifier (its title for example). If the category is already added, then nothing is done.
   * @param categoryId the identifier of the category to add.
   */
  public void add(final String categoryId) {
    categories.add(categoryId);
  }

  /**
   * Adds several categories to a {@link Plannable}. The categories are specified by
   * their unique identifier (their title for example). If some of the categories to add are already
   * present, then they are not added.
   * @param categoryIds the identifiers of the categories to add.
   */
  public void addAll(final List<String> categoryIds) {
    categories.addAll(categoryIds);
  }

  /**
   * Adds one or several categories to a {@link Plannable}. The categories are
   * specified by their unique identifier (their title for example). If some of the categories to
   * add are already present, then they are not added.
   * @param categoryIds the identifiers of the categories to add.
   */
  public void addAll(final String... categoryIds) {
    addAll(Arrays.asList(categoryIds));
  }

  /**
   * Removes a category from the categories of a {@link Plannable}. The category is
   * specified by its unique identifier (its title for example). If the category isn't present, then
   * nothing is done.
   * @param categoryId the identifier of the category to remove.
   */
  public void remove(final String categoryId) {
    categories.remove(categoryId);
  }

  /**
   * Removes several categories from the categories of a {@link Plannable}. The
   * categories are specified by their unique identifier (their title for example). If some of the
   * categories to remove aren't present, then nothing is done with them.
   * @param categoryIds the identifiers of the categories to remove.
   */
  public void removeAll(final List<String> categoryIds) {
    categories.removeAll(categoryIds);
  }

  /**
   * Removes one or several categories from the categories of a {@link Plannable}. The
   * categories are specified by their unique identifier (their title for example). If some of the
   * categories to remove aren't present, then nothing is done with them.
   * @param categoryIds the identifiers of the categories to remove.
   */
  public void remove(final String... categoryIds) {
    removeAll(Arrays.asList(categoryIds));
  }

  /**
   * Converts this categories container to a list of category identifiers.
   * @return a list of category identifiers.
   */
  public List<String> asList() {
    return new ArrayList<>(categories);
  }

  /**
   * Converts this categories container to an array of category identifiers.
   * @return an array of category identifiers.
   */
  public String[] asArray() {
    List<String> categoryList = asList();
    return categoryList.toArray(new String[categoryList.size()]);
  }

  /**
   * Is the specified category is in the categories of a {@link Plannable}.
   * @param category a category identifier.
   * @return true if the specified category is among the categories of a {@link Plannable}, false
   * otherwise.
   */
  public boolean contains(final String category) {
    return categories.contains(category);
  }

  /**
   * Is there is no any categories set for a {@link Plannable}?
   * @return true if no categories are set, false otherwise.
   */
  public boolean isEmpty() {
    return categories.isEmpty();
  }

  public Stream<String> stream() {
    return categories.stream();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CategorySet)) {
      return false;
    }

    final CategorySet that = (CategorySet) o;
    return categories.equals(that.categories);
  }

  @Override
  public int hashCode() {
    return categories.hashCode();
  }

  /**
   * Adds to this categories all those from the specified ones.
   * @param categories the categories to add.
   */
  public void addAllFrom(final CategorySet categories) {
    this.categories.addAll(categories.categories);
  }

  @Override
  public CategorySet clone() {
    CategorySet clone = null;
    try {
      clone = (CategorySet) super.clone();
      clone.categories = new HashSet<>(categories);
    } catch (CloneNotSupportedException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return clone;
  }
}

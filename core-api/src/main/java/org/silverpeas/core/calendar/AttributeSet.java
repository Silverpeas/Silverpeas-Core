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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapsId;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A set of attributes of a {@link PlannableOnCalendar} object. An attribute is an additional information
 * carried by a {@link PlannableOnCalendar} object. This is a way to users to specify additional attributes
 * that weren't defined into a {@link PlannableOnCalendar} implementation. For example, for an event, the
 * location of the event can be set as an attribute.
 * @author mmoquillon
 */
@Embeddable
public class AttributeSet implements Serializable, Iterable<Map.Entry<String, String>> {

  @ElementCollection(fetch = FetchType.EAGER)
  @MapsId
  @CollectionTable(name = "sb_cal_attributes", joinColumns = {@JoinColumn(name = "id")})
  @MapKeyColumn(name = "name")
  @Column(name = "value")
  private Map<String, String> attributes = new HashMap<>();

  /**
   * Constructs an empty attribute container. It is dedicated to the persistence engine.
   */
  public AttributeSet() {
    // empty for JPA.
  }

  @Override
  public Iterator<Map.Entry<String, String>> iterator() {
    return attributes.entrySet().iterator();
  }

  @Override
  public void forEach(final Consumer<? super Map.Entry<String, String>> action) {
    attributes.entrySet().forEach(action);
  }

  @Override
  public Spliterator<Map.Entry<String, String>> spliterator() {
    return attributes.entrySet().spliterator();
  }

  /**
   * Streams the attendees in this calendar component.
   * @return a {@link Stream} with the attendees in this calendar component.
   */
  public Stream<Map.Entry<String, String>> stream() {
    return attributes.entrySet().stream();
  }

  /**
   * Sets the specified attributes.
   * @param name the attribute name.
   * @param value the attribute value.
   */
  public void set(String name, String value) {
    attributes.put(name, value);
  }

  /**
   * Remove the specified attribute.
   * @param name the name of the attribute to remove.
   */
  public void remove(String name) {
    attributes.remove(name);
  }

  /**
   * Removes all of the attributes that match the specified filter.
   * @param filter the predicate against which each attribute is filtered.
   * @return the updated attributes in this calendar component.
   */
  public AttributeSet removeIf(final Predicate<Map.Entry<String, String>> filter) {
    attributes.entrySet().removeIf(filter);
    return this;
  }

  /**
   * Is this set of attributes empty?
   * @return true if there is no attributes set, false otherwise.
   */
  public boolean isEmpty() {
    return attributes.isEmpty();
  }

  /**
   * Gets the value of the specified attribute or nothing is there is no a such attribute.
   * @param name the name of an attribute.
   * @return optionally the value of the specified attribute.
   */
  public Optional<String> get(String name) {
    return Optional.ofNullable(attributes.get(name));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeSet)) {
      return false;
    }

    final AttributeSet that = (AttributeSet) o;
    return attributes.equals(that.attributes);
  }

  @Override
  public int hashCode() {
    return attributes.hashCode();
  }

  /**
   * Adds to this attributes all those from the specified ones.
   * @param attributes the attributes to add.
   */
  public void addAllFrom(final AttributeSet attributes) {
    this.attributes.putAll(attributes.attributes);
  }

  /**
   * Sets to this attributes all those from the specified ones.
   * @param attributes the attributes to add.
   */
  public void setAllFrom(final AttributeSet attributes) {
    this.attributes.clear();
    addAllFrom(attributes);
  }

  /**
   * Copies this object into another one.
   * @return the copied object.
   */
  public AttributeSet copy() {
    AttributeSet copy = new AttributeSet();
    copy.attributes = new HashMap<>(attributes);
    return copy;
  }

  Map<String, String> getData() {
    return attributes;
  }
}

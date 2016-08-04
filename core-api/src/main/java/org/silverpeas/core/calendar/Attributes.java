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
package org.silverpeas.core.calendar;

import org.silverpeas.core.util.logging.SilverLogger;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of attributes of a {@link Plannable} object. An attribute is an additional information
 * carried by a {@link Plannable} object. This is a way to users to specify additional attributes
 * that weren't defined into a {@link Plannable} implementation. For example, for an event, the
 * location of the event can be set as an attribute. In order to be fully operational, id est to
 * be involved in the serialization of the entity that embeds an {@link Attributes} field, the
 * {@link SerializationListener} class requires to be declared as a {@link
 * javax.persistence.EntityListeners} for the entity that embeds it; otherwise the attributes won't
 * be neither serialized to nor serialized from the data source.
 * @author mmoquillon
 */
@Embeddable
public class Attributes {

  private static final Pattern ATTR_PATTERN =
      Pattern.compile("\"(\\p{Alnum}+)\":\"([\\p{javaLetterOrDigit}\\s\\p{Punct}&&[^\"]]+)\"");
  private static final MessageFormat ATTR_FORMAT = new MessageFormat("\"{0}\":\"{1}\"");

  @Transient
  private Map<String, String> attrs = new HashMap<>();
  @Column(name = "attributes", columnDefinition = "varchar(6000)")
  private String attributes = null;

  /**
   * Adds the specified attributes.
   * @param name the attribute name.
   * @param value the attribute value.
   */
  public void add(String name, String value) {
    attrs.put(name, value);
  }

  /**
   * Remove the specified attribute.
   * @param name the name of the attribute to remove.
   */
  public void remove(String name) {
    attrs.remove(name);
  }

  /**
   * Is this set of attributes empty?
   * @return true if there is no attributes set, false otherwise.
   */
  public boolean isEmpty() {
    return attrs.isEmpty();
  }

  /**
   * Gets the value of the specified attribute or nothing is there is no a such attribute.
   * @param name the name of an attribute.
   * @return optionally the value of the specified attribute.
   */
  public Optional<String> get(String name) {
    return Optional.ofNullable(attrs.get(name));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Attributes)) {
      return false;
    }

    final Attributes that = (Attributes) o;
    return attrs.equals(that.attrs);
  }

  @Override
  public int hashCode() {
    return attrs.hashCode();
  }

  protected Attributes() {

  }

  private void deserialize() {
    if (attributes != null) {
      Matcher matcher = ATTR_PATTERN.matcher(this.attributes);
      while (matcher.find()) {
        attrs.put(matcher.group(1), matcher.group(2).replaceAll("U\\+0022", "\""));
      }
    }
  }

  private void serialize() {
    if (attrs.isEmpty()) {
      attributes = null;
    } else {
      attrs.entrySet().forEach(e -> {
        String a = attributes == null || attributes.isEmpty() ? "" : attributes + ",";
        attributes = a + ATTR_FORMAT.format(new String[]{e.getKey(),
            e.getValue().replaceAll("\"", "U+0022")});
      });
    }
  }

  /**
   * A listener of events on the serialization of an {@link Attributes} field of an entity.
   */
  static class SerializationListener {

    @PostLoad
    public void deserializeAttributesOf(final Object entity) {
      Arrays.stream(entity.getClass().getDeclaredFields())
          .filter(f -> f.getType().isAssignableFrom(Attributes.class) &&
              f.isAnnotationPresent(Embedded.class))
          .findFirst()
          .ifPresent(f -> {
            try {
              f.setAccessible(true);
              ((Attributes) f.get(entity)).deserialize();
            } catch (IllegalAccessException e) {
              SilverLogger.getLogger(this).error(e.getMessage(), e);
            }
          });
    }

    @PrePersist
    @PreUpdate
    public void serializeAttributesOf(Object entity) {
      Arrays.stream(entity.getClass().getDeclaredFields())
          .filter(f -> f.getType().isAssignableFrom(Attributes.class) &&
              f.isAnnotationPresent(Embedded.class))
          .findFirst()
          .ifPresent(f -> {
            try {
              f.setAccessible(true);
              ((Attributes) f.get(entity)).serialize();
            } catch (IllegalAccessException e) {
              SilverLogger.getLogger(this).error(e.getMessage(), e);
            }
          });
    }
  }
}

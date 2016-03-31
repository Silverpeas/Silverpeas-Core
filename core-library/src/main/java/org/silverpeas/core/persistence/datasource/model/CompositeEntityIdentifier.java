package org.silverpeas.core.persistence.datasource.model;

/**
 * A composite entity identifier is a unique identifier that is made up of several identification
 * values. Usually, these values are the unique identifier of one or more external entities.
 * @author: ebonnet
 */
public interface CompositeEntityIdentifier extends ExternalEntityIdentifier {
  public static final String COMPOSITE_SEPARATOR = ":";

  @Override
  default EntityIdentifier fromString(String id) {
    String[] values = id.split(COMPOSITE_SEPARATOR);
    return fromString(values);
  }

  /**
   * Sets the value of this identifier from the specified values that will be part of this
   * composite identifier.
   * @return this entity identifier.
   */
  CompositeEntityIdentifier fromString(String... values);
}

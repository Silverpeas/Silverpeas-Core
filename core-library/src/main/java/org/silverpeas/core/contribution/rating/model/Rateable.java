package org.silverpeas.core.contribution.rating.model;


/**
 * A rateable is an object that represents a contribution which can be rated.
 * This interface defines all methods that must be implemented in order to obtain differents
 * contribution types that can be handled by a same rating mechanism.
 * @author: Yohann Chastagnier
 */
public interface Rateable {

  /**
   * Gets the contribution rating.
   * @return a contribution rating.
   * @see ContributionRating
   */
  public ContributionRating getRating();
}

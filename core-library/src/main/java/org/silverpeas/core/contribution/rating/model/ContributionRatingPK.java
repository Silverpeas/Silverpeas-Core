package org.silverpeas.core.contribution.rating.model;

import org.silverpeas.core.ForeignPK;

import java.io.Serializable;

/**
 * This class represents a technical primary key of a contribution rating.
 */
public class ContributionRatingPK extends ForeignPK implements Serializable {

  private static final long serialVersionUID = -4144961919465268637L;
  private String contributionType;

  public ContributionRatingPK(String id, String componentId, String type) {
    super(id, componentId);
    this.contributionType = type;
  }

  public String getContributionId() {
    return getId();
  }

  public String getContributionType() {
    return contributionType;
  }

  public void setContributionType(String contributionType) {
    this.contributionType = contributionType;
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  /**
   * Comparison between two notation primary key. Since various attributes of the both elements can
   * be null, using toString() method to compare the elements avoids to check null cases for each
   * attribute.
   * @param other
   */
  @Override
  public boolean equals(Object other) {
    return ((other instanceof ContributionRatingPK) && (toString().equals(other.toString())));
  }

  @Override
  public String toString() {
    return "id = " + getContributionId() + ", componentId = " + getComponentName() + ", type = " +
        getContributionType();
  }
}
package org.silverpeas.core.contribution.rating.model;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the global data about a rating on a contribution.
 */
public class ContributionRating implements Serializable {

  private static final long serialVersionUID = 5611801738147739307L;

  private ContributionRatingPK pk;
  private float ratingAverage = 0;
  private Map<String, Integer> raterRatings = new HashMap<>();

  /**
   * Default constructor.
   * @param pk the technical primary key of a rating related to a contribution.
   */
  public ContributionRating(ContributionRatingPK pk) {
    this.pk = pk;
  }

  /**
   * Gets the identifier of the component instance which the contribution aimed by the rating is
   * associated.
   * @return the identifier of a component instance.
   */
  public String getInstanceId() {
    return pk.getInstanceId();
  }

  /**
   * Gets the identifier of the contribution aimed by the rating.
   * @return the identifier of a contribution.
   */
  public String getContributionId() {
    return pk.getContributionId();
  }

  /**
   * Gets the type of the contribution aimed by the rating.
   * @return the type of a contribution.
   */
  public String getContributionType() {
    return pk.getContributionType();
  }

  /**
   * Gets the average of all rater ratings associated to the contribution aimed by the rating.
   * @return the average of all rater ratings.
   */
  public float getRatingAverage() {
    if (ratingAverage == 0 && raterRatings.size() > 0) {
      float sum = 0;
      for (Integer aRaterRating : raterRatings.values()) {
        sum += aRaterRating;
      }
      ratingAverage = sum / raterRatings.size();
    } else if (ratingAverage != 0 && raterRatings.isEmpty()) {
      ratingAverage = 0;
    }
    return ratingAverage;
  }

  /**
   * Adds a rating of a rater.
   * @param raterId the identifier of the user that is the rater.
   * @param ratingValue the value of the rater rating.
   */
  public void addRaterRating(String raterId, Integer ratingValue) {
    raterRatings.put(raterId, ratingValue);
    ratingAverage = 0;
  }

  /**
   * Gets an instance of a {@link RaterRating} according to the specified rater.
   * @param rater the user for whom the rating is requested.
   * @return the rater rating instance of the specified rater. Null if no rating has been done by
   * the specified user.
   */
  public RaterRating getRaterRating(UserDetail rater) {
    Integer raterRatingValue =
        (rater != null && StringUtil.isDefined(rater.getId())) ? raterRatings.get(rater.getId()) :
            null;
    if (raterRatingValue == null) {
      return new RaterRating(this, rater);
    }
    return new RaterRating(this, rater, raterRatingValue);
  }

  /**
   * Gets all rater ratings associated to the rating.
   * @return a mapping between user id of raters and their rating.
   */
  public Map<String, Integer> getRaterRatings() {
    return raterRatings;
  }
}
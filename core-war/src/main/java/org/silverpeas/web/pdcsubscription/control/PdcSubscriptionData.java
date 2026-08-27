package org.silverpeas.web.pdcsubscription.control;

import org.owasp.encoder.Encode;
import org.silverpeas.core.pdc.pdc.model.Value;

import java.util.List;

/**
 *
 * @author mmoquillon
 */
public class PdcSubscriptionData {

  private static final String SEPARATOR_PATH = ">";
  private static final int MAX_ELT_AUTHORIZED = 5;
  private static final String TRUNCATE_SEPARATOR = "...";
  private static final int SHOWED_ALLOWED_ELT_NB = 2;
  private String positions = "";
  private String name;
  private String id;
  private String nature = "";
  private boolean forced = false;

  /**
   * Gets a builder of the data about the position criteria on the PdC with the specified identifier.
   * @param id the unique identifier of position criteria on the PdC.
   * @param name the name of that position criteria.
   * @return a builder of {@link PdcSubscriptionData}.
   */
  public static Builder about(final String id, final String name) {
    return new Builder(id, name);
  }

  private PdcSubscriptionData() {
    // use the builder
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getPdCPositions() {
    return this.positions;
  }

  /**
   * Gets the nature of the subscription, already localized: is it a personal subscription or one
   * that has been forced, and, if it comes from a group of users, the name of that group.
   * @return a label or an empty string when no subscription in particular is rendered, as it is
   * the case in the window of the PdC where the position criteria are listed by themselves.
   */
  public String getNature() {
    return this.nature;
  }

  /**
   * Has the subscription been forced by a manager of the PdC? Such a subscription is managed from
   * the window of the PdC only and hence cannot be updated nor deleted by the subscriber himself.
   * @return true if the subscription has been forced, false if the user has subscribed himself.
   */
  public boolean isForced() {
    return this.forced;
  }

  /**
   * A builder of {@link PdcSubscriptionData} instances.
   */
  public static class Builder {

    private final PdcSubscriptionData data = new PdcSubscriptionData();

    private Builder(final String id, final String name) {
      data.id = id;
      data.name = name;
    }

    /**
     * Sets the positions on the axis of the PdC, each of them being the full path of a value of an
     * axis. They are rendered in the specified language.
     */
    public Builder withPositions(final List<List<Value>> positions, final String language) {
      data.positions = data.formatPdcPositions(positions, language);
      return this;
    }

    /**
     * Sets the already localized nature of the subscription.
     */
    public Builder withNature(final String nature) {
      data.nature = nature;
      return this;
    }

    /**
     * Indicates the subscription has been forced by a manager of the PdC.
     */
    public Builder asForced() {
      data.forced = true;
      return this;
    }

    public PdcSubscriptionData build() {
      return data;
    }
  }

  private void truncatePath(StringBuilder completPath, List<Value> list, boolean isLinked, int
          withLastValue,
      String language) {
    Value value;
    // prend les nbShowedEltAuthorized 1er elements
    for (int nb = 0; nb < SHOWED_ALLOWED_ELT_NB; nb++) {
      value = list.get(nb);
      completPath.append(linkedNode(value, isLinked, language))
          .append(SEPARATOR_PATH);
    }

    // colle ici les points de suspension
    completPath.append(TRUNCATE_SEPARATOR).append(SEPARATOR_PATH);

    // prend les nbShowedEltAuthorized derniers elements
    for (int nb = SHOWED_ALLOWED_ELT_NB + withLastValue; nb > withLastValue; nb--) {
      value = list.get(list.size() - nb);
      completPath.append(linkedNode(value, isLinked, language))
          .append(SEPARATOR_PATH);
    }
  }

  private String linkedNode(Value unit, boolean isLinked, String language) {
    String node;

    // Attention la partie hyperlink est à faire !!!!
    if (isLinked) {
      node = "<a href=" + unit.getPath() + ">" + Encode.forHtml(unit.getName(language)) +
          "</a>";
    } else {
      node = Encode.forHtml(unit.getName(language));
    }

    return node;
  }

  private String buildCompletPath(List<Value> list, String language) {
    boolean isLinked = false;
    int withLastValue = 0;
    StringBuilder completPath = new StringBuilder();

    // On regarde dans un 1er temps le nombre d'éléments de la liste que l'on reçoit.
    // Si ce nombre est strictement supérieur à maxEltAuthorized alors, on doit tronquer le chemin complet
    // et l'afficher comme suit : node1 / node2 / ... / node<n>.
    if (list.size() > MAX_ELT_AUTHORIZED) {
      truncatePath(completPath, list, isLinked, withLastValue, language);
    } else {
      for (int nb = 0; nb < list.size() - withLastValue; nb++) {
        Value value = list.get(nb);
        completPath.append(linkedNode(value, isLinked, language))
            .append(SEPARATOR_PATH);
      }
    }

    String path = completPath.toString().trim();
    if (path.isEmpty() || path.equals(">")) {
      path = null;
    } else {
      path = path.substring(0, completPath.length() - SEPARATOR_PATH.length());
      // retire le dernier séparateur
    }

    return path;
  }


  private String formatPdcPositions(List<List<Value>> positions, String language) {
    if (positions == null || positions.isEmpty()) {
      return "";
    }
    StringBuilder result = new StringBuilder();
    int size = positions.size();
    for (int i = 0; i < size; i++) {
      List<Value> list = positions.get(i);
      String fullPath = buildCompletPath(list, language);
      result.append(fullPath);
      if (i < size - 1) {
        result.append(" X ");
      }
    }

    return result.toString();
  }
}
  
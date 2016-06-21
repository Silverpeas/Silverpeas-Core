/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.core.persistence.datasource.repository;

import org.silverpeas.core.util.StringUtil;

/**
 * Criteria is a simplified representation of conditions the entities targeted by a query have to
 * satisfy and that constrains the query of such entities.
 * <p>
 * Instead of providing a full representation of a criteria API by which criterion objects can be
 * composing programmatically and that wraps each underlying persistence-specific criteria API we
 * use, we prefer to let the developers provide their own implementation of the criteria by
 * extendings this interface and by adapting it to their purpose.
 * @author mmoquillon
 */
public interface QueryCriteria {

  /**
   * Gets the pagination to apply to the query.
   * @return a criterion on a pagination. If no such criterion exists, then
   * {@link PaginationCriterion#NO_PAGINATION} is returned.
   */
  PaginationCriterion pagination();

  /**
   * Gets the clause the entities must match.
   * @return the clause of all conditions the entities have to match.
   */
  Clause clause();

  /**
   * A clause representing the criteria the entities have to match. The parameters used in this
   * clause are initialized by the caller and passed as such to the clause. By letting the caller
   * the charge of managing the parameters, they can be used accross several clauses and in
   * different process implied in the final query build.
   */
  public static class Clause {

    private final Parameters parameters;
    private final StringBuilder text = new StringBuilder();

    /**
     * Constructs a clause from the specified first criterion and with the specified parameters.
     * @param <T> the concrete type of the parameters to use in this clause.
     * @param criterionText a text representation of the criterion to add. The text representatipn
     * is in charge to the {@code QueryCriteria} implementation.
     * @param parameters the parameters initialized by the caller and that will be used in this
     * clause.
     */
    public <T extends Parameters> Clause(String criterionText, final T parameters) {
      this.text.append(criterionText).append(" ");
      this.parameters = parameters;
    }

    /**
     * Constructs a clause with the specified parameters.
     * @param <T> the concrete type of the parameters to use in this clause.
     * @param parameters the parameters initialized by the caller and that will be used in this
     * clause.
     */
    public <T extends Parameters> Clause(final T parameters) {
      this.parameters = parameters;
    }

    /**
     * Adds a criterion to this clause.
     * @param criterionText a text representation of the criterion to add. The text representation
     * is in charge to the {@code QueryCriteria} implementation. If the text is null or empty, then
     * nothing is added.
     * @return itself enriched with the new criterion.
     */
    public Clause add(String criterionText) {
      if (StringUtil.isDefined(criterionText)) {
        text.append(criterionText.trim()).append(" ");
      }
      return this;
    }

    /**
     * Replaces the last criterion by the specified one.
     * @param criterionText a text representation of the criterion to add. The text representatipn
     * is in charge to the {@code QueryCriteria} implementation.
     * @return itself with the new criterion replacing the last one.
     */
    public Clause replaceLast(String criterionText) {
      int lastCriterion = text.toString().trim().lastIndexOf(" ");
      text.replace(lastCriterion + 1, text.length(), criterionText.trim()).append(" ");
      return this;
    }

    /**
     * Gets the text of the clause. Each criterion in the clause is separated by a space.
     * @return a text representation of the clause.
     */
    public String text() {
      return text.toString().trim();
    }

    /**
     * Gets the parameters refered by the clause.
     * @param <T>
     * @return all the parameters to apply with the clause to a query.
     */
    public <T extends Parameters> T parameters() {
      return (T) parameters;
    }
  }

}

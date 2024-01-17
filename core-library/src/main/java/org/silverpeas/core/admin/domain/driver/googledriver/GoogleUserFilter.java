/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.json.GenericJson;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine;
import org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine.OperatorFunction;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleUserFilter.ERROR.*;
import static org.silverpeas.core.util.CollectionUtil.intersection;
import static org.silverpeas.core.util.CollectionUtil.union;
import static org.silverpeas.core.util.StringUtil.*;
import static org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine.from;

/**
 * This class handles the evaluation of a filter rule of Google user accounts.
 * <p>
 * A filter rule is represented by a {@link String} composed by:
 * <ul>
 * <li>
 * a simple rule, with <b>[resource attribute]</b> representing the full path in JSON structure,
 * <b>[resource attribute]</b> is case sensitive and <b>[string value]</b> is not:
 * <ul>
 * <li><b>[resource attribute] = <i>[string value]</i></b> targets all accounts which the
 * attribute value is equal to the specified value </li>
 * <li><b>[resource attribute] ^= <i>[string value]</i></b> targets all accounts which the
 * attribute value is equal to or begins with the specified value </li>
 * <li><b>[resource attribute] $= <i>[string value]</i></b> targets all accounts which the
 * attribute value is equal to or ends with the specified value </li>
 * <li><b>[resource attribute] *= <i>[string value]</i></b> targets all accounts which the
 * attribute value contains the specified value </li>
 * </ul>
 * </li>
 * <li>
 * a combination of simple rules. The language to write the combination is the one of
 * prefixed notation expression. Please take a look at documentation of
 * {@link PrefixedNotationExpressionEngine} class to get more information about this
 * language.<br>
 * Each operand, here, can be:
 * <ul>
 * <li><b>a simple rule</b> one of those defined above</li>
 * <li><b>an operation with</b> one or several operands</li>
 * </ul>
 * The possible operators here:
 * <ul>
 * <li><b>&</b>: apply an AND operation between each operand, so an intersection between the
 * users ids guessed from each operand</li>
 * <li><b>|</b>: apply an OR operation between each operand, so an intersection between the
 * users ids guessed from each operand</li>
 * <li><b>!</b>: apply a negation on one operand only, so all the users ids of the
 * platform unless those guessed from the operand. Not working if several one are defined</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * @author Yohann Chastagnier
 */
class GoogleUserFilter<T extends GenericJson> {

  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(?i)^\\s*[(].+[)]\\s*$");
  private static final Pattern CRITERION_ARRAY_PATTERN = Pattern
      .compile("(?i)^\\s*(\\S+)\\s*\\[\\s*(\\S+)\\s*]$");
  private static final String CRITERION_PART_DECODER = "(?i)^\\s*(\\S+)\\s*";
  private final List<T> allUsers;
  private final String combinationRule;

  /**
   * Hidden constructor
   * @param allUsers all the users before evaluation.
   * @param combinationRule the filter rules.
   */
  GoogleUserFilter(final List<T> allUsers, final String combinationRule) {
    this.allUsers = allUsers;
    this.combinationRule = combinationRule;
  }

  /**
   * Gets the list of user identifiers represented by the synchronization rule.
   * @return list of strings where each one represents a Silverpeas user identifier.
   */
  public List<T> apply() {
    if (isNotDefined(combinationRule)) {
      return new ArrayList<>(allUsers);
    }
    return evaluateCombinationRule();
  }

  /**
   * Gets the expression of the rule.<br>
   * If simple value, and if no escaped character detected, parentheses are escaped.
   * @return the expression.
   */
  private String getRuleExpression(PrefixedNotationExpressionEngine<List<T>> combinationEngine) {
    String rule = combinationRule != null ? combinationRule : "";
    Matcher matcher = EXPRESSION_PATTERN.matcher(rule);
    if (!matcher.matches() && !rule.contains("\\") && !combinationEngine.detectOperator(rule)) {
      rule = rule.replaceAll("[(]", "\\\\(").replaceAll("[)]", "\\\\)");
    } else {
      final StringBuilder newRule = new StringBuilder();
      int i = 0;
      int nbOpening = 0;
      while (i < rule.length()) {
        char currentChar = rule.charAt(i);
        if (currentChar == '[') {
          nbOpening++;
        } else if (currentChar == ']') {
          nbOpening--;
        } else if (nbOpening > 0 && (currentChar == '(' || currentChar == ')')) {
          newRule.append("\\");
        }
        newRule.append(currentChar);
        i++;
      }
      rule = newRule.toString();
    }
    return rule;
  }

  /**
   * Evaluates a combination of simple rules.
   * @return a list of user identifiers.
   */
  private List<T> evaluateCombinationRule() {
    final OperatorFunction<List<T>> negate = new OperatorFunction<>("!", (computed, users) -> {
      if (computed != EMPTY_LIST) {
        return new ArrayList<>();
      }
      final Set<String> idsToRemove = users.stream().map(u -> (String) u.get("id")).collect(Collectors.toSet());
      return allUsers.stream().filter(u -> {
        final String id = (String) u.get("id");
        return !idsToRemove.contains(id);
      })
      .collect(Collectors.toList());
    });
    final OperatorFunction<List<T>> and = new OperatorFunction<>("&", (computed, users) -> {
      List<T> safeComputed = computed == EMPTY_LIST ? users : computed;
      return intersection(safeComputed, users, u -> u.get("id"));
    });
    final OperatorFunction<List<T>> or = new OperatorFunction<>("|", (computed, users) -> {
      List<T> safeComputed = computed == EMPTY_LIST ? emptyList() : computed;
      return union(safeComputed, users);
    });
    final Function<String, List<T>> customMaskRuleToUsers = this::evaluateCriterion;
    @SuppressWarnings("unchecked")
    final PrefixedNotationExpressionEngine<List<T>> combinationEngine = from(
        customMaskRuleToUsers, negate, and, or);
    final String expression = getRuleExpression(combinationEngine);
    return combinationEngine.evaluate(expression);
  }

  /**
   * Evaluates a criterion.
   * @param criterion a criterion.
   * @return a list of matching users.
   */
  private List<T> evaluateCriterion(final String criterion) {
    if (criterion == null) {
      return Collections.emptyList();
    }
    CriterionDecoder criterionDecoder = new WithArrayCriterionDecoder(criterion);
    if (!criterionDecoder.isMatching()) {
      criterionDecoder = new SimpleCriterionDecoder(criterion);
    }
    if (!criterionDecoder.isMatching()) {
      final String message = "ground rule '" + criterion + "' is not correct !";
      SilverLogger.getLogger(this).error(message);
      throw new SilverpeasRuntimeException(message);
    }

    final CriterionDecoder decoder = criterionDecoder;
    return allUsers.stream().filter(u -> filterUser(decoder, u))
        .collect(Collectors.toList());
  }

  private boolean filterUser(final CriterionDecoder criterionDecoder, final T data) {
    final String criterion = criterionDecoder.getCriterion();
    final String path = criterionDecoder.getPath();
    final String[] explodedPath = criterionDecoder.getExplodedPath();
    final String subRule = criterionDecoder.getSubRule();
    final String expectedValue = criterionDecoder.getExpectedValue().toLowerCase();
    final GenericJson attributeValues;
    try {
      attributeValues = resolvePath(criterionDecoder, 0, data);
    } catch (UserFilterException e) {
      throw e.withCriterion(criterion).withPath(path);
    }
    final String attr = defaultStringIfNotDefined(explodedPath[explodedPath.length - 1]);
    final boolean filterResult;
    if (isNotDefined(attr)) {
      filterResult = false;
    } else if (isDefined(subRule)) {
      filterResult = applySubRule(subRule, data, attr);
    } else {
      filterResult = likeIgnoreCase(String.valueOf(attributeValues.get(attr)), expectedValue);
    }
    return filterResult;
  }

  @SuppressWarnings("unchecked")
  private boolean applySubRule(final String subRule, final T data, final String attr) {
    final boolean filterResult;
    final Object o = data.get(attr);
    if (o instanceof List) {
      final List<T> subData = ((List<Map<String, Object>>)o).stream().map(m -> {
        final T j = (T) new GenericJson();
        m.forEach(j::set);
        j.set("id", data.get("id"));
        return j;
      }).collect(Collectors.toList());
      filterResult = !new GoogleUserFilter<>(subData, subRule).apply().isEmpty();
    } else {
      filterResult = false;
    }
    return filterResult;
  }

  @SuppressWarnings("unchecked")
  private GenericJson resolvePath(final CriterionDecoder criterionDecoder, final int pathLevel,
      final GenericJson data) {
    final String[] path = criterionDecoder.getExplodedPath();
    if (path.length == (pathLevel + 1)) {
      return data;
    }
    final String pathPart = path[pathLevel];
    Object subData = data.get(pathPart);
    if (!(subData instanceof GenericJson) && subData instanceof Map) {
      final T temp = (T) new GenericJson();
      ((Map<String, Object>) subData).forEach(temp::set);
      temp.set("id", data.get("id"));
      subData = temp;
    } else if ("customSchemas".equals(path[0]) && subData == null) {
      subData = new GenericJson();
    }
    if (subData == null) {
      throw new UserFilterException(NOT_VALID_PATH_PART).withPathPart(pathPart);
    } else if (!(subData instanceof GenericJson)) {
      throw new UserFilterException(FINAL_VALUE_PATH_PART).withPathPart(pathPart);
    }
    return resolvePath(criterionDecoder, pathLevel + 1, (GenericJson) subData);
  }

  public enum ERROR {
    BAD_OPERATOR,
    NOT_VALID_PATH_PART,
    FINAL_VALUE_PATH_PART
  }

  private abstract static class CriterionDecoder {
    private final String criterion;
    String path = EMPTY;
    String[] explodedPath;
    String operator = EMPTY;
    String expectedValue = EMPTY;
    String subRule = EMPTY;
    boolean match = false;

    CriterionDecoder(final String criterion) {
      this.criterion = criterion;
      decode();
      this.explodedPath = path.split("[.]");
    }

    protected abstract void decode();

    public boolean isMatching() {
      return match;
    }

    public String getCriterion() {
      return criterion;
    }

    public String getPath() {
      return path;
    }

    String[] getExplodedPath() {
      return explodedPath;
    }

    public String getOperator() {
      return operator;
    }

    String getExpectedValue() {
      return expectedValue;
    }

    String getSubRule() {
      return subRule;
    }
  }

  private static class WithArrayCriterionDecoder extends CriterionDecoder {

    WithArrayCriterionDecoder(final String criterion) {
      super(criterion);
    }

    @Override
    protected void decode() {
      final Matcher matcher = CRITERION_ARRAY_PATTERN.matcher(getCriterion());
      if (matcher.find()) {
        this.path = matcher.group(1);
        explodedPath = path.split("[.]");
        subRule = matcher.group(2);
        match = true;
      }
    }
  }

  private static class SimpleCriterionDecoder extends CriterionDecoder {

    SimpleCriterionDecoder(final String criterion) {
      super(criterion);
    }

    @Override
    protected void decode() {
      final String[] explodedCriterion = getCriterion().split("[=]");
      match = explodedCriterion.length == 2;
      if (match) {
        path = explodedCriterion[0].replaceAll(CRITERION_PART_DECODER, "$1");
        explodedPath = path.split("[.]");
        expectedValue = explodedCriterion[1].replaceAll(CRITERION_PART_DECODER, "$1");
      }
    }
  }

  public static class UserFilterException extends IllegalArgumentException {
    private static final long serialVersionUID = 1541302731885140639L;
    private final ERROR type;
    private final String[] elements = new String[3];

    UserFilterException(final ERROR type) {
      super();
      this.type = type;
    }

    @Override
    public String getMessage() {
      final String message;
      if (type == BAD_OPERATOR) {
        message = format("clause ''{0}'' uses a bad operator", elements[0]);
      } else if (type == NOT_VALID_PATH_PART) {
        message = format("path part ''{2}'' of ''{1}'' in clause ''{0}'' is not valid",
            (Object[]) elements);
      } else if (type == FINAL_VALUE_PATH_PART) {
        message =  format("path part ''{2}'' of ''{1}'' in clause ''{0}'' represents a final value",
            (Object[]) elements);
      } else {
        message = super.getMessage();
      }
      return message;
    }

    public ERROR getType() {
      return type;
    }

    public String getCriterion() {
      return elements[0];
    }

    UserFilterException withCriterion(final String clause) {
      this.elements[0] = clause;
      return this;
    }

    public String getPath() {
      return elements[1];
    }

    UserFilterException withPath(final String path) {
      this.elements[1] = path;
      return this;
    }

    String getPathPart() {
      return elements[2];
    }

    UserFilterException withPathPart(final String pathPart) {
      this.elements[2] = pathPart;
      return this;
    }
  }
}

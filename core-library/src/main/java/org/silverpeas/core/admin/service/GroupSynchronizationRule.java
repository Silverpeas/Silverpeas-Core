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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.service;

import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPLocalException;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.DomainDriverManager;
import org.silverpeas.core.admin.domain.DomainDriverManagerProvider;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.synchro.SynchroGroupReport;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.WithNested;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine;
import org.silverpeas.core.util.expression.PrefixedNotationExpressionEngine.OperatorFunction;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.admin.service.AdministrationServiceProvider.getAdminService;
import static org.silverpeas.core.util.CollectionUtil.intersection;
import static org.silverpeas.core.util.CollectionUtil.union;

/**
 * This class handles the evaluation of a synchronization rule of group.
 * <p>
 * A synchronization rule is represented by a {@link String} composed by:
 * <ul>
 * <li>
 * a simple rule
 * <ul>
 * <li><b>DS_AccessLevel = [one of {@link UserAccessLevel#getCode()}]</b> gets
 * identifiers of users which has the specified access level</li>
 * <li><b>DS_Domains = [domain identifiers separated by comma]</b> gets identifiers of users which
 * are registered into domains represented by the specified identifiers</li>
 * <li><b>DC_[user extra property name] = [aimed value]</b> gets identifiers of users
 * which the specified value is the one registered for the specified extra property
 * name</li>
 * <li><b>DR_Groups = [group identifiers separated by comma]</b> gets the
 * identifiers of users which are directly registered into the groups (and not the sub
 * groups) represented by the list of identifiers</li>
 * <li><b>DR_GroupsWithSubGroups = [group identifiers separated by comma]</b>
 * gets the identifiers of users which are registered into the groups (and the sub
 * groups) represented by the list of identifiers</li>
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
class GroupSynchronizationRule {

  private static final Pattern ACCESSLEVEL_STANDARD_DATA_PATTERN =
      Pattern.compile("(?i)^\\s*ds_accesslevel\\s*=\\s*(\\S+)\\s*$");

  private static final Pattern DOMAIN_STANDARD_DATA_PATTERN =
      Pattern.compile("(?i)^\\s*ds_domain[s]?\\s*=\\s*(.+)\\s*$");

  private static final Pattern COMPLEMENTARY_DATA_PATTERN =
      Pattern.compile("(?i)^\\s*dc_(\\S+)\\s*=\\s*(.+)\\s*$");

  private static final Pattern GROUP_RULE_DATA_PATTERN =
      Pattern.compile("(?i)^\\s*dr_groups(withsubgroups)?\\s*=\\s*(.+)\\s*$");

  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(?i)^\\s*[(].+[)]\\s*$");
  private static final String ADMIN_GET_USER_IDS_BY_SPECIFIC_PROPERTY =
      "admin.getUserIdsBySpecificProperty";

  private final Group group;
  private final boolean isSharedDomain;

  private List<String> cacheOfAllUserIds = null;

  /**
   * Gets a new instance initialized with the context of the given group.
   * @param group group instance which contains mandatory group identifier, optional domain
   * identifier and the synchronization rule.
   * @return the initialized instance.
   */
  public static GroupSynchronizationRule from(final Group group) {
    return new GroupSynchronizationRule(group);
  }

  /**
   * Hidden constructor
   */
  private GroupSynchronizationRule(final Group group) {
    this.group = group;
    this.isSharedDomain = group.getDomainId() == null || "-1".equals(group.getDomainId());
  }

  /**
   * Gets the list of user identifiers represented by the synchronization rule.
   * @return list of strings where each one represents a Silverpeas user identifier.
   * @throws AdminException
   */
  public List<String> getUserIds() {
    List<String> userIds = evaluateCombinationRule(group.getRule());
    return userIds == null || userIds.isEmpty() ? Collections.emptyList() : userIds;
  }

  /**
   * Gets the expression of the rule.<br>
   * If simple value, and if no escaped character detected, parentheses are escaped.
   * @return the expression.
   */
  private static String getRuleExpression(
      PrefixedNotationExpressionEngine<Optional<List<String>>> combinationEngine,
      final String combinationRule) {
    String rule = combinationRule != null ? combinationRule : "";
    Matcher matcher = EXPRESSION_PATTERN.matcher(rule);
    if (!matcher.matches() && !rule.contains("\\") && !combinationEngine.detectOperator(rule)) {
      rule = rule.replaceAll("[(]", "\\\\(").replaceAll("[)]", "\\\\)");
    }
    return rule;
  }

  /**
   * Evaluates a combination of simple silverpeas rules.
   * @param combinationRule the combination rule to evaluate.
   * @return a list of user identifiers.
   * @throws RuleError
   */
  private List<String> evaluateCombinationRule(String combinationRule) {
    final OperatorFunction<Optional<List<String>>> negateFunction =
        new OperatorFunction<>("!", (computed, userIds) -> {
          try {
            List<String> safeComputed =
                computed.isPresent() ? new ArrayList<>() : getCacheOfAllUserIds();
            safeComputed.removeAll(userIds.orElse(Collections.emptyList()));
            return Optional.of(safeComputed);
          } catch (AdminException e) {
            throw new SilverpeasRuntimeException(e);
          }
        });

    final OperatorFunction<Optional<List<String>>> andFunction =
        new OperatorFunction<>("&", (computed, userIds) -> {
          final List<String> defaultUsersId = userIds.orElse(Collections.emptyList());
          List<String> safeComputed = computed.orElse(defaultUsersId);
          return Optional.of(intersection(safeComputed, defaultUsersId));
        });

    final OperatorFunction<Optional<List<String>>> orFunction =
        new OperatorFunction<>("|", (computed, userIds) -> {
          List<String> safeComputed = computed.orElse(Collections.emptyList());
          return Optional.of(union(safeComputed, userIds.orElse(Collections.emptyList())));
        });

    final Function<String, Optional<List<String>>> simpleSilverpeasRuleToUserIds =
        simpleSilverpeasRule -> {
      try {
        return evaluateSimpleSilverpeasRule(simpleSilverpeasRule);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    };

    @SuppressWarnings("unchecked") PrefixedNotationExpressionEngine<Optional<List<String>>>
        combinationEngine =
        PrefixedNotationExpressionEngine.from(simpleSilverpeasRuleToUserIds, negateFunction,
            andFunction, orFunction);

    try {
      String expression = getRuleExpression(combinationEngine, combinationRule);
      return combinationEngine.evaluate(expression).orElse(Collections.emptyList());
    } catch (RuleError e) {
      throw e;
    } catch (Exception e) {
      throw new RuleError(e);
    }
  }

  /**
   * Evaluates a simple Silverpeas rule.
   * @param simpleSilverpeasRule the simple Silverpeas rule to evaluate.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private Optional<List<String>> evaluateSimpleSilverpeasRule(final String simpleSilverpeasRule)
      throws AdminException {
    if (simpleSilverpeasRule == null) {
      return Optional.empty();
    }

    Matcher matcher = ACCESSLEVEL_STANDARD_DATA_PATTERN.matcher(simpleSilverpeasRule);
    if (matcher.find()) {
      return Optional.of(getUserIdsByAccessLevel(matcher.group(1)));
    }

    matcher = DOMAIN_STANDARD_DATA_PATTERN.matcher(simpleSilverpeasRule);
    if (matcher.find()) {
      // Split parameters as a list using comma separator and trimming spaces
      List<String> domainIds = asList(matcher.group(1).replaceAll("\\s", "").split(","));
      List<String> userIds = new ArrayList<>();
      for (String domainId : domainIds) {
        userIds.addAll(getUserIdsByDomain(domainId));
      }
      return Optional.of(userIds);
    }

    matcher = GROUP_RULE_DATA_PATTERN.matcher(simpleSilverpeasRule);
    if (matcher.find()) {
      boolean withSubGroups = StringUtil.isDefined(matcher.group(1));
      // Split parameters as a list using comma separator and trimming spaces
      String groupValues = matcher.group(2).replaceAll("\\s", "");
      List<String> groupIds = asList(groupValues.split(","));
      return Optional.of(getUserIdsByGroups(groupIds, withSubGroups));
    }

    matcher = COMPLEMENTARY_DATA_PATTERN.matcher(simpleSilverpeasRule);
    if (matcher.find()) {
      String propertyName = matcher.group(1);
      String propertyValue = matcher.group(2);
      Set<String> userIds = new HashSet<>();
      userIds.addAll(getUserIdsBySpecificProperty(propertyName, propertyValue));
      userIds.addAll(getUserIdsByExtraFormFieldValue(propertyName, propertyValue));
      return Optional.of(new ArrayList<>(userIds));
    }

    SilverLogger.getLogger(this)
        .error("ground rule '" + simpleSilverpeasRule + "' for groupId '" + group.getId() +
            "' is not correct !");

    throw new GroundRuleError(simpleSilverpeasRule);
  }

  /**
   * Gets the users of the given groups represented by their identifiers.
   * @param groupIds the group identifiers.
   * @param withSubGroups true in order to get the users of the sub groups of the given ones, false
   * otherwise.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getUserIdsByGroups(List<String> groupIds, boolean withSubGroups)
      throws AdminException {
    // Add each group passed as a parameter
    Set<String> allGroupIds = new HashSet<>();
    for (String currentGroupId : new HashSet<>(groupIds)) {
      allGroupIds.add(currentGroupId);
      // Add valid subgroups recursively if recursive option is selected
      if (withSubGroups) {
        allGroupIds.addAll(getGroupManager().getAllSubGroupIdsRecursively(currentGroupId));
      }
    }
    // Add all users belonging to any group in the list
    return getUserManager().getAllUserIdsInGroups(new ArrayList<>(allGroupIds));
  }

  /**
   * Gets the users of the domain represented by the given identifier.<br>
   * This method returns user identifiers only into the context of shared domain search.
   * @param domainId the identifier of the aimed domain.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getUserIdsByDomain(String domainId) throws AdminException {
    if (isSharedDomain) {
      return getUserManager().getAllUserIdsInDomain(domainId);
    }
    return Collections.emptyList();
  }

  /**
   * Gets the users which have their access level equals to the specified one.
   * @param accessLevel the access level the users must have.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getUserIdsByAccessLevel(String accessLevel) throws AdminException {
    List<String> userIds;
    if ("*".equalsIgnoreCase(accessLevel)) {
      // In case of "Shared domain" then retrieving all users of all domains
      // Otherwise getting only users of group's domain
      if (isSharedDomain) {
        userIds = getUserManager().getAllUsersIds();
      } else {
        userIds = getUserManager().getAllUserIdsInDomain(group.getDomainId());
      }
    } else {
      // All users by access level
      if (isSharedDomain) {
        userIds =
            asList(getUserManager().getUserIdsByAccessLevel(UserAccessLevel.fromCode(accessLevel)));
      } else {
        userIds = asList(getUserManager().getUserIdsByDomainAndByAccessLevel(group.getDomainId(),
                UserAccessLevel.fromCode(accessLevel)));
      }
    }
    return userIds;
  }

  /**
   * Gets the users which the value of the extra property name matches the given ones.
   * @param propertyName the name of the aimed extra property.
   * @param propertyValue the value the property must verify.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getUserIdsBySpecificProperty(String propertyName, String propertyValue)
      throws AdminException {
    List<String> userIds = new ArrayList<>();
    if (isSharedDomain) {
      // All users by extra information
      Domain[] domains = getAdminService().getAllDomains();
      for (Domain domain : domains) {
        userIds.addAll(getUserIdsBySpecificProperty(domain.getId(), propertyName, propertyValue));
      }
    } else {
      userIds = getUserIdsBySpecificProperty(group.getDomainId(), propertyName, propertyValue);
    }
    return userIds;
  }

  private List<String> getUserIdsByExtraFormFieldValue(String fieldName, String fieldValue) {
    final PublicationTemplate directoryTemplate =
        PublicationTemplateManager.getInstance().getDirectoryTemplate();
    if (directoryTemplate != null) {
      try {
        final List<DataRecord> records =
            directoryTemplate.getRecordSet().getRecords(fieldName, fieldValue);
        return records.stream()
            .map(r -> User.getById(r.getId()))
            .filter(u -> u != null && !u.isRemovedState())
            .filter(u -> isSharedDomain || u.getDomainId().equals(group.getDomainId()))
            .map(User::getId)
            .collect(Collectors.toList());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Gets the users which the value of the extra property name matches the given ones.
   * @param domainId the identifier of the aimed domain.
   * @param propertyName the name of the aimed extra property.
   * @param propertyValue the value the property must verify.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getUserIdsBySpecificProperty(String domainId, String propertyName,
      String propertyValue) throws AdminException {
    UserDetail[] users = new UserDetail[0];
    DomainDriverManager domainDriverManager =
        DomainDriverManagerProvider.getCurrentDomainDriverManager();
    DomainDriver domainDriver = null;
    try {
      domainDriver = domainDriverManager.getDomainDriver(domainId);
    } catch (Exception e) {
      reportInfo(ADMIN_GET_USER_IDS_BY_SPECIFIC_PROPERTY,
          "Erreur ! Domaine " + domainId + " inaccessible !");
    }

    if (domainDriver != null) {
      try {
        users = domainDriver.getUsersBySpecificProperty(propertyName, propertyValue);
        if (ArrayUtil.isEmpty(users)) {
          reportInfo(ADMIN_GET_USER_IDS_BY_SPECIFIC_PROPERTY,
              "La propriété '" + propertyName + "' n'est pas définie dans le domaine " +
                  domainId);
        }
      } catch (AdminException e) {
        Throwable cause = e.getCause();
        if (cause instanceof LDAPLocalException ||
            cause instanceof org.ietf.ldap.LDAPLocalException) {
          reportInfo(ADMIN_GET_USER_IDS_BY_SPECIFIC_PROPERTY,
              "Domain " + domainId + ": " + cause.toString());
        } else {
          throw e;
        }
      } catch (Exception e) {
        throw new AdminException(failureOnGetting("users by property", propertyName), e);
      }
    }

    // We have to find users according to theirs specificIds
    List<UserDetail> usersInDomain = Collections.emptyList();
    if (ArrayUtil.isNotEmpty(users)) {
      List<String> specificIds =
          Arrays.stream(users).map(UserDetail::getSpecificId).collect(Collectors.toList());
      usersInDomain = getUserManager().getUsersBySpecificIdsAndDomainId(specificIds, domainId);
    }

    return usersInDomain.stream()
        .filter(u -> !u.isRemovedState())
        .map(UserDetail::getId)
        .collect(Collectors.toList());
  }

  /**
   * Gets all user identifiers of the Silverpeas platform.<br>
   * The ids are cached.
   * @return a list of user identifiers.
   * @throws AdminException
   */
  private List<String> getCacheOfAllUserIds() throws AdminException {
    if (cacheOfAllUserIds == null) {
      // In case of "Shared domain", retrieving all users of all domains
      // Otherwise retrieving only users of group's domain
      if (isSharedDomain) {
        cacheOfAllUserIds = getUserManager().getAllUsersIds();
      } else {
        cacheOfAllUserIds = getUserManager().getAllUserIdsInDomain(group.getDomainId());
      }
    }
    return new ArrayList<>(cacheOfAllUserIds);
  }

  private void reportInfo(String clazz, String message) {
    SynchroGroupReport.info(clazz, message);
  }

  private UserManager getUserManager() {
    return ServiceProvider.getService(UserManager.class);
  }

  private GroupManager getGroupManager() {
    return ServiceProvider.getService(GroupManager.class);
  }

  /**
   * An error.
   */
  public static class RuleError extends RuntimeException {
    private static final long serialVersionUID = -3732193660967552614L;

    public RuleError(final Throwable cause) {
      super(cause);
    }

    /**
     * Gets a message handled by the synchronization rule process.<br>
     * If not handled, like a {@link NullPointerException}, empty message is returned.
     * @return handled message, empty otherwise.
     */
    String getHandledMessage() {
      String message = "";
      Throwable exceptionSource = getCause();
      Throwable previousExceptionSource = null;
      while (exceptionSource != null && previousExceptionSource != exceptionSource &&
          StringUtil.isNotDefined(message)) {
        previousExceptionSource = exceptionSource;
        if (StringUtil.isDefined(exceptionSource.getMessage()) &&
            exceptionSource.getMessage().startsWith("expression.")) {
          message = exceptionSource.getMessage();
        } else if (exceptionSource instanceof LDAPException ||
            exceptionSource instanceof org.ietf.ldap.LDAPLocalException) {
          message = exceptionSource.toString();
        }
        if (exceptionSource instanceof WithNested) {
          exceptionSource = ((WithNested) exceptionSource).getNested();
        } else {
          exceptionSource = exceptionSource.getCause();
        }
      }
      return message;
    }
  }

  /**
   * An error.
   */
  static class GroundRuleError extends RuleError {
    private static final long serialVersionUID = 4003102352897715610L;

    private final String baseRulePart;

    GroundRuleError(String baseRulePart) {
      super(new IllegalArgumentException("expression.groundrule.unknown"));
      this.baseRulePart = baseRulePart;
    }

    String getBaseRulePart() {
      return baseRulePart;
    }
  }
}

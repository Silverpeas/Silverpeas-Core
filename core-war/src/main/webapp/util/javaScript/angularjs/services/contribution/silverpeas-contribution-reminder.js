/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

(function() {

  var BASE_URI = webContext + '/services/reminder'

  /**
   * The module silverpeas within which the business objects are defined.
   * @type {Angular.Module} the silverpeas.services module.
   */
  var services = angular.module('silverpeas.services');

  services.factory('ContributionReminderService',
      ['RESTAdapter', function(RESTAdapter) {
        return new function() {

          // the type ContributionReminder
          var ContributionReminder = function() {
          };

          /**
           * Gets possible reminder duration about a contribution and a contribution property.
           * @param contributionId the identifier of a contribution.
           * @param contributionpropertyr the contribution property on which the reminder is based.
           * @returns {promise|a.fn.promise|*} a promise with the asked possible durations as
           *     callback parameter.
           */
          this.getContributionPossibleDurations = function(contributionId, contributionProperty) {
            var url = __getContributionBaseUri(contributionId) + '/possibledurations/' + contributionProperty;
            var adapter = RESTAdapter.get(url, ContributionReminder);
            return adapter.find();
          };

          /**
           * Gets reminders linked to a contribution represented by an identifier.
           * @param contributionId the identifier of a contribution.
           * @returns {promise|a.fn.promise|*} a promise with the asked reminders as callback
           *     parameter.
           */
          this.getByContributionId = function(contributionId) {
            var adapter = RESTAdapter.get(__getContributionBaseUri(contributionId), ContributionReminder);
            return adapter.find();
          };

          /**
           * Creates reminder linked to a contribution.
           * @param reminder a reminder about a contribution.
           * @returns {promise|a.fn.promise|*} a promise with the created reminder as callback
           *     parameter.
           */
          this.createReminder = function(reminder) {
            var adapter = RESTAdapter.get(__getContributionBaseUri(reminder.cId), ContributionReminder);
            return adapter.post(reminder);
          };

          /**
           * Updates into the persistence the given reminder.
           * @param reminder a reminder to update into persistence.
           * @returns {promise|a.fn.promise|*} a promise with the updated reminder as callback
           *     parameter.
           */
          this.updateReminder = function(reminder) {
            var adapter = RESTAdapter.get(reminder.uri, ContributionReminder);
            return adapter.put(reminder);
          };

          /**
           * Removes the given reminder from the persistence.
           * @param reminder a reminder to remove from the peristence.
           * @returns {promise|a.fn.promise|*} a promise.
           */
          this.deleteReminder = function(reminder) {
            var adapter = RESTAdapter.get(reminder.uri, ContributionReminder);
            return adapter.remove(reminder.uri);
          };
        };
      }]);

  /**
   * Gets the base URI of reminder services about a contribution.
   * @param contributionId the identifier of a contribution.
   * @returns {string}
   * @private
   */
  var __getContributionBaseUri = function(contributionId) {
    var cId = sp.contribution.id.from(contributionId);
    return BASE_URI + '/' + cId.getComponentInstanceId() + '/' + cId.getType() + '/' + cId.getLocalId();
  };
})();


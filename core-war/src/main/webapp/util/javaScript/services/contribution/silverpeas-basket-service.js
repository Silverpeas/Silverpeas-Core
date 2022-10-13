/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

//# sourceURL=/util/javaScript/vuejs/services/contribution/silverpeas-basket-service.js

(function() {

  sp.i18n.load({
    bundle : 'org.silverpeas.contribution.multilang.contribution',
    async : true
  });

  /**
   * Manager that permits to centralize some common code about the add of elements into basket
   * selection.
   */
  window.BasketManager = function() {

    /**
     * Puts a contribution into basket from its full identifier.
     * @param contributionId a contribution identifier.
     * @param reason an optional reason.
     */
    this.putContributionInBasket = function(contributionId, reason) {
      const r = reason ? reason : BasketService.Context.transfert;
      const cId = sp.contribution.id.from(contributionId);
      const basket = new BasketService();
      basket.putNewEntry({
        context: {
          reason: r
        },
        item: {
          id: cId.asString(),
          type : cId.getType()
        }
      });
    }

    /**
     * Handled selected and unselected contributions.
     * @param selectedContributionIds a list of selected contribution identifier.
     * @param unselectedContributionIds a list of unselected contribution identifier.
     * @param reason an optional reason.
     */
    this.putContributionsInBasket = function(selectedContributionIds, unselectedContributionIds, reason) {
      const r = reason ? reason : BasketService.Context.transfert;
      const filter = function(cId) {
        return cId && (typeof cId !== 'string' || StringUtil.isDefined(cId));
      };
      const selectedCId = selectedContributionIds.filter(filter).map(function(cId) {
        return sp.contribution.id.from(cId);
      });
      const unselectedCId = unselectedContributionIds.filter(filter).map(function(cId) {
        return sp.contribution.id.from(cId);
      });
      const basket = new BasketService();
      const isContributionToAdd = selectedCId.length > 0;
      let deletePromises = [];
      // remove from the basket the unselected contributions
      if (unselectedCId.length > 0) {
        deletePromises.push(basket.getBasketSelectionElements(r).then(function(elts) {
          const entriesToDelete = elts
              .filter(function(elt) {
                return unselectedCId.filter(function(id) {
                  return elt.getId() === id.asString();
                }).length > 0;
              })
              .map(function(elt) {
                return {
                  item : {
                    id : elt.getId()
                  }
                }
              });
          return basket.deleteEntries(entriesToDelete, isContributionToAdd);
        }));
      }

      // put into the basket the selected contributions
      if(isContributionToAdd) {
        const entriesToAdd = selectedCId.map(function(cId) {
          return {
            context : {
              reason : r
            }, item : {
              id : cId.asString(),
              type : cId.getType()
            }
          };
        });
        sp.promise.whenAllResolved(deletePromises).then(function() {
          basket.putNewEntries(entriesToAdd);
        })
      }
    }
  };

  window.BasketService = function() {
    const basketRepository = new BasketRepository();

    /**
     * Gets the API of the basket selection which permits to performs some UI refreshing from
     * services or components.
     * If the API is not available, then the callback is not executed.
     * @param callback the callback executed if API is available.
     */
    this.withBasketSelectionApi = function(callback) {
      if (typeof callback === 'function') {
        const spBasketSelectionApi = top.window.spBasketSelectionApi;
        if (spBasketSelectionApi) {
          callback(spBasketSelectionApi);
        }
      }
    };

    /**
     * Gets the elements from the basket selection.
     * @param filter an optional filter.
     * @see BasketService.Filters
     * @returns {*}
     */
    this.getBasketSelectionElements = function(filter) {
      let promise = basketRepository.getBasketSelectionElements();
      if (typeof filter === 'function') {
        promise = promise.then(function(elements) {
          return elements.filter(filter);
        });
      }
      return promise;
    };

    const __updateEntries = function(entries) {
      const safeEntries = entries || []
      this.withBasketSelectionApi(function(api) {
        api.updateWith(safeEntries);
      });
      return entries;
    }.bind(this);

    const __updateSuccessMsg = function(entries) {
      const safeEntries = entries || []
      this.withBasketSelectionApi(function(api) {
        if (api.length() !== safeEntries.length) {
          notySuccess(sp.i18n.get(safeEntries.length ? 'GML.basketCompleted' : 'GML.emptyBasket'));
        }
      });
      return entries;
    }.bind(this);

    /**
     * Puts a new entry into the Silverpeas's context.
     * @param entry a basket entry to add.
     * @returns {*}
     */
    this.putNewEntry = function(entry) {
      return this.putNewEntries([entry]);
    }

    /**
     * Puts a new entries into the Silverpeas's context.
     * @param entries an array of basket entry to add.
     * @returns {*}
     */
    this.putNewEntries = function(entries) {
      const promises = entries.map(function(entry) {
        return basketRepository.putNewEntry(entry);
      });
      return sp.promise.whenAllResolved(promises)
          .then(function(result) {
            if (!result.length) {
              return result;
            }
            return result.reduce(function(a, b) {
              return a.length >= b.length ? a : b;
            });
          })
          .then(__updateSuccessMsg)
          .then(__updateEntries);
    }

    /**
     * Puts a new entry into the Silverpeas's context.
     * @param entry a basket entry to delete.
     * @param skipUpdateApiEntries true to avoid update the entries with API of basket.
     * @returns {*}
     */
    this.deleteEntry = function(entry, skipUpdateApiEntries) {
      return this.deleteEntries([entry], skipUpdateApiEntries);
    }

    /**
     * Puts a new entry into the Silverpeas's context.
     * @param entries array of basket entries to delete.
     * @param skipUpdateApiEntries true to avoid update the entries with API of basket.
     * @returns {*}
     */
    this.deleteEntries = function(entries, skipUpdateApiEntries) {
      const promises = entries.map(function(entry) {
        return basketRepository.deleteEntry(entry);
      });
      let promise = sp.promise.whenAllResolved(promises).then(function(result) {
        if (!result.length) {
          return result;
        }
        return result.reduce(function(a, b) {
          return a.length > b.length ? b : a;
        });
      });
      if (!skipUpdateApiEntries) {
        promise = promise.then(__updateEntries);
      }
      return promise;
    }
  };

  window.BasketService.Context = {
    transfert: 'TRANSFER',
    copy: 'COPY',
    move: 'MOVE'
  }

  window.BasketService.Filters = {
    /**
     * Filters element with data transfer reason.
     * @param element an element of a basket selection.
     * @returns {*}
     */
    dataTransfer : function(element) {
      return element.isDataTransfer();
    },
    /**
     * Filters on event element.
     * @param element an element of a basket selection.
     * @returns {*}
     */
    eventItems : function(element) {
      return element.isEventOccurrence();
    }
  }

  const BasketElement = function() {
    this.type = 'BasketElement';
    this.$onInit = function() {
      this.isDataTransfer = function() {
        return this.context.reason === BasketService.Context.transfert;
      };
      this.isEventOccurrence = function() {
        return this.item.type === 'CalendarEventOccurrence';
      };
      this.getResourceType = function() {
        return sp.contribution.id.fromString(this.item.id).getType();
      };
      this.getId = function() {
        return this.item.id;
      };
      this.getImageSrc = function() {
        return this.item.thumbnailURI;
      };
      this.getTitle = function() {
        return StringUtil.defaultStringIfNotDefined(this.item.name);
      };
      this.getDescription = function() {
        return StringUtil.defaultStringIfNotDefined(this.item.description);
      };
      this.getLink = function() {
        return StringUtil.defaultStringIfNotDefined(this.item.permalink);
      };
      if (this.isEventOccurrence()) {
        const spPeriod = new SilverpeasPeriod(
            sp.moment.makeUtc(this.item.period.startDate),
            sp.moment.makeUtc(this.item.period.endDate),
            this.item.period.isInDays);
        this.getPeriod = function() {
          return spPeriod;
        }
      }
    };
  };

  const BasketRepository = function() {
    const baseUri = webContext + "/services/selection";
    const baseAdapter = RESTAdapter.get(baseUri, BasketElement);

    /**
     * Gets all elements from basket selection repository.
     */
    this.getBasketSelectionElements = function() {
      return baseAdapter.find();
    };

    this.putNewEntry = function(entry) {
      return baseAdapter.post(entry);
    }

    this.deleteEntry = function(entry) {
      return baseAdapter['delete'](baseAdapter.url + '/item/' + entry.item.id, {});
    }
  };
})();

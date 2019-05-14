/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
  /**
   * silverpeas-date-picker handles an input which represents a formatted date with a calendar to
   * pick a date (and little stuffs).
   *
   * It defines several attributes:
   * {dateId} - the identifier which will be given to the explicit input.
   * {name} - the name which will be given to the explicit input.
   * {date} - the date handled as two way binding. Only ISO String format is handled. The ISO date
   * as string is updated each time it is manually changed. If the displayed date is not well
   * formatted, then the date is empty.
   * {mandatory} - indicates if the date is mandatory or not.
   * {status} - indicates the status about the handled date :
   * . valid : the date is valid
   * . empty : the date is not filled
   * . unknown : the date is filled but cannot be interpreted
   *
   * The following example illustrates the only one possible use of the directive:
   * <div silverpeas-date-picker ...>...</div>
   */
  Vue.component('silverpeas-date-picker', function(resolve) {
    sp.ajaxRequest(webContext + '/util/javaScript/vuejs/components/silverpeas-date-picker.jsp').send().then(function(request) {
      resolve({
        mixins : [VuejsFormInputMixin],
        model : {
          prop : 'date',
          event : 'change'
        },
        props : {
          id: {
            'type': String,
            'required': true
          },
          name: {
            'type': String,
            'required': true
          },
          zoneId : {
            'type': String,
            'required': true
          },
          mandatory : {
            'type': Boolean,
            'default': false
          },
          disabled : {
            'type': Boolean,
            'default': false
          },
          date : {
            'type': String
          }
        },
        template : request.responseText,
        data : function() {
          return {
            $jqi : undefined,
            currentInput : undefined,
            status : {
              valid : false,
              empty : false,
              unknown : true
            }
          };
        },
        created : function() {
          this.extendApiWith({
            validateFormInput : function() {
              if (this.rootFormApi && !this.status.valid) {
                if (this.mandatory && this.status.empty) {
                  this.rootFormApi.errorMessage().add(this.formatMessage(this.rootFormMessages.mandatory,
                      this.getLabelByForAttribute(this.id)));
                } else if (this.status.unknown) {
                  this.rootFormApi.errorMessage().add(this.formatMessage(this.rootFormMessages.correctDate,
                      this.getLabelByForAttribute(this.id)));
                }
              }
              return (this.mandatory && this.status.valid) ||
                  (!this.mandatory && (this.status.valid || this.status.empty));
            }
          });
          this.valueChanged(this.formatDate(this.date));
          whenSilverpeasReady(function() {
            this.$jqi = jQuery(this.$refs.datePickerInput);
            this.$jqi.datepicker({
              showOn: (this.disabled ? '' : 'button'),
              buttonImage: webContext + '/util/icons/calendar_1.png',
              buttonImageOnly: true,
              showOtherMonths: true,
              selectOtherMonths: true,
              onSelect : function(formattedDate) {
                this.valueChanged(formattedDate);
                return true;
              }.bind(this)
            });
          }.bind(this));
        },
        methods : {
          formatDate : function(isoDate) {
            return (isoDate && /^[0-9].+/.exec(isoDate)) ? sp.moment.displayAsDate(isoDate) : '';
          },
          computeStatus : function(formattedDate) {
            var errors = isDateValid({
              date : formattedDate, isMandatory : this.mandatory
            });
            var status = {
              valid : StringUtil.isDefined(formattedDate) && errors.length === 0,
              empty : false,
              unknown : false
            };
            if (!status.valid) {
              if (!formattedDate) {
                status.empty = true;
              } else {
                status.unknown = true;
              }
            }
            return status;
          },
          updateStatus : function(status){
            if (!sp.object.areExistingValuesEqual(this.status, status)) {
              this.status = status;
            }
            this.$emit('status-change', this.status);
          },
          valueChanged : function(userInput) {
            this.currentInput = userInput;
            var status = this.computeStatus(userInput);
            if (status.valid) {
              var $dateToSet = sp.moment.make(userInput, 'L');
              var $date = sp.moment.adjustTimeMinutes(
                  sp.moment.atZoneIdSimilarLocal(moment(), this.zoneId).startOf('minutes'));
              if (this.date) {
                $date = sp.moment.make(this.date);
              }
              $date.year($dateToSet.year());
              $date.dayOfYear($dateToSet.dayOfYear());
              var newDate = sp.moment.atZoneIdSimilarLocal($date, this.zoneId).format();
              this.$emit('change', newDate);
            }
            this.updateStatus(status);
          }
        },
        watch : {
          date : function(date) {
            var formattedDate = this.formatDate(date);
            if (this.currentInput !== formattedDate) {
              if (formattedDate) {
                this.currentInput = formattedDate;
                this.$jqi.datepicker("setDate", formattedDate);
                this.$jqi.datepicker("refresh");
              }
              var status = this.computeStatus(this.currentInput);
              this.updateStatus(status);
            }
          },
          disabled : function(value) {
            if (this.$jqi) {
              this.$jqi.datepicker("option", "showOn", (value ? "" : "button"));
            }
          }
        },
        computed : {
          formattedDate : function() {
            return this.currentInput;
          },
          statusValid : function() {
            return this.status.valid;
          }
        }
      });
    });
  });
})();

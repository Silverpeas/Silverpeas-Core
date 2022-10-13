/**
 *    Copyright (C) 2000 - 2022 Silverpeas
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   As a special exception to the terms and conditions of version 3.0 of
 *   the GPL, you may redistribute this Program in connection with Free/Libre
 *   Open Source Software ("FLOSS") applications as described in Silverpeas's
 *   FLOSS exception.  You should have received a copy of the text describing
 *   the FLOSS exception, and it is also available here:
 *   "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

CKEDITOR.dialog.add( 'variables', function( editor ) {

	return {
		  title: editor.lang.variables.variables,
		  minWidth: 300,
		  minHeight: 100,
		  resizable: CKEDITOR.DIALOG_RESIZE_BOTH,
		  contents: [{
				    id: 'info',
				    elements: [
				      {
				        id : 'variablesList',
				        type : 'select',
                label : editor.lang.variables.title,
                items: [],
                onChange: function() {
				          var dialog = this.getDialog();
                  var selectList = this;
				          var variableId = selectList.getValue();
                  if (typeof variableId === 'string' && variableId.length > 0) {
                    var url = webContext + "/services/variables/" + variableId;
                    var ajaxRequest = sp.ajaxRequest(url).send().then(function(request) {
                      dialog.setValueOf('info', 'preview', request.responseAsJson().value);
                    });
                  }
                },
				        setup: function(widget) {
				          var selectList = this;

				          // remove previous loaded variables
                  selectList.clear();

                  var url= webContext+"/services/variables/currents";
                  var ajaxRequest = sp.ajaxRequest(url).send().then(function(request) {
                    // add all variables to list
                    request.responseAsJson().forEach(function(aVariable) {
                      selectList.add(aVariable.label, aVariable.id);
                    });
                    // set widget variable as selected variable in list
                    selectList.setValue(widget.data.variableId);
                  });
				        },
				        commit: function( widget ) {
                  var selectList = this;
				          var variableId = selectList.getValue();
                  if (typeof variableId === 'string') {
				            widget.setData('variableId', variableId);
                    var url= webContext+"/services/variables/"+variableId;
                    var ajaxRequest = sp.ajaxRequest(url).send().then(function(request) {
                      widget.setData('variableLabel', request.responseAsJson().label);
                    });
                  }
				        }
				      },
              {
                id : 'preview',
                label : editor.lang.variables.preview,
                type : 'textarea'
              }]
			    }		]
	  };
});
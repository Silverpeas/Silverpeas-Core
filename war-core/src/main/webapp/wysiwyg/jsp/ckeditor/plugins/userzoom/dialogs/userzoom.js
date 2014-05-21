/**
 *    Copyright (C) 2000 - 2014 Silverpeas
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
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

CKEDITOR.dialog.add( 'userzoom', function( editor ) {
	return {
		  title: editor.lang.userzoom.title,
		  minWidth: 300,
		  minHeight: 100,
		  resizable: CKEDITOR.DIALOG_RESIZE_NONE,
		  contents: [{
				    id: 'info',
				    elements: [
				      {
				        id: 'userId',
				        type: 'text',
				        label: editor.lang.userzoom.userId,
				        width: '50px',
				        setup: function( widget ) {
				          this.setValue( widget.data.userId );
				        },
				        commit: function( widget ) {
				          widget.setData( 'userId', this.getValue() );
				        }		
				      },
				      {
				        id: 'userName',
				        type: 'text',
				        label: editor.lang.userzoom.userName,
				        width: '50px',
				        setup: function( widget ) {
				          this.setValue( widget.data.userName );
				        },
				        commit: function( widget ) {
				          widget.setData( 'userName', this.getValue() );
				        }
				      },
				      {
				        type : 'html',
				        html : '<form name="myForm"><label title="user" class="txtlibform" for="user">'+editor.lang.userzoom.user+'</label><div class="fieldInput"><input type="hidden" value="" name="user" id="user"><input type="text" value="" name="user$$name" id="user_name" disabled="disabled"/>&nbsp;<a onclick="javascript:SP_openWindow(\'/silverpeas/RselectionPeasWrapper/jsp/open?formName=myForm&amp;elementId=user&amp;elementName=user_name&amp;selectedUser=\',\'selectUser\',800,600,\'\');" href="#"><img width="15" border="0" align="top" height="15" title="'+editor.lang.userzoom.select+'" alt="'+editor.lang.userzoom.select+'" src="/silverpeas/util/icons/user.gif"/></a></div></form>',
				        setup: function( widget ) {
				          const document = this.getElement().getDocument();
				          var element = document.getById('user');
				          element.setValue(widget.data.userId);
				          if (widget.data.userId) {
				            $.ajax({
				              type: "GET",
				              url: "/silverpeas/services/profile/users/" + widget.data.userId,
				              dataType: "json",
				              cache: false,
				              success: function (user, status, jqXHR) {
				                document.getById('user_name').setValue(user.firstName + " " + user.lastName);
				              },
				              error: function (jqXHR, status) {
				                // error handler
				                alert('error');
				              }
				            });
				          }
				        },
				        commit: function( widget ) {							
				          var document = this.getElement().getDocument();
				          var element = document.getById('user');
				          widget.setData( 'userId', element.getValue());
				          widget.setData( 'userName', document.getById('user_name').getValue());
				        }
				      }]
			    }		],
			    onLoad : function() {
			      this.getContentElement("info", "userId").getElement().setAttribute("hidden", true);
			      this.getContentElement("info", "userName").getElement().setAttribute("hidden", true);
			    }
	  };
});
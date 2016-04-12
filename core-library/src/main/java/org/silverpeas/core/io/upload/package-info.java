/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Provides an API to manage file uploads.
 * Uploaded files are retrieved from indicators contained in an HttpServletRequest.
 * <br/>
 * On user interface side, silverpeas-fileUpload.js has to be used via
 * <view:fileUpload [options] /> tag. When this Silverpeas jQuery plugin is used,
 * each files selected or dragged and dropped are uploaded by AJAX http request. User can't
 * validate form unless all files are uploaded.
 * <br/>
 * On server side, {@link org.silverpeas.core.io.upload.FileUploadManager} has to be used to
 * retrieve uploaded files from {@link javax.servlet.http.HttpServletRequest}.
 */
package org.silverpeas.core.io.upload;
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
package org.silverpeas.core.webapi.upload;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.File;
import java.util.Optional;

/**
 * It is a process implied within the manufacturing of a new file upload.
 * </p>
 * When a file upload is performed, it is first verified according to its name (the file is not
 * yet on the server), then it is verified a second time after than the upload is completed
 * successfully. In some circumstances, according to the application, some verifications have to be
 * performed in the behalf of the new file upload. The file upload process is unaware of these
 * circumstances and it cannot know what verification to perform; It is the responsibility of the
 * application to perform such verifications. This is why an implementation of this interface
 * qualified by a name that satisfies the following convention <code>[COMPONENT
 * NAME]InstanceFileUploadVerification</code> is looked for by the file upload process and then
 * invoked if it is has been found.
 * </p>
 * Any application that requires specific actions when a file upload is performed has to implement
 * this interface and the implementation has to be qualified with the @Named annotation by a name
 * satisfying the following convention <code>[COMPONENT NAME]FileUploadVerification</code>. For
 * example, for an application Kmelia, the implementation must be qualified with
 * <code>@Named("kmeliaInstanceFileUploadVerification") </code>
 * @author Yohann Chastagnier
 */
public interface ComponentInstanceFileUploadVerification {

  /**
   * The predefined suffix that must compound the name of each implementation of this interface.
   * An implementation of this interface by a Silverpeas application named Kmelia must be named
   * <code>kmelia[NAME_SUFFIX]</code> where NAME_SUFFIX is the predefined suffix as defined below.
   */
  String NAME_SUFFIX = "InstanceFileUploadVerification";

  /**
   * Gets the implementation of this interface with the qualified name guessed from the given
   * component instance identifier.
   * @param componentInstanceId the component instance identifier from which the qualified name of
   * the implementation is guessed.
   * @return either an implementation of this interface or nothing.
   */
  static Optional<ComponentInstanceFileUploadVerification> get(String componentInstanceId) {
    try {
      return Optional.of(ServiceProvider
          .getServiceByComponentInstanceAndNameSuffix(componentInstanceId, NAME_SUFFIX));
    } catch (IllegalStateException e) {
      SilverLogger.getLogger(ComponentInstanceFileUploadVerification.class).warn(e);
      return Optional.empty();
    }
  }

  /**
   * Performs verification tasks in the behalf of the specified component instance and specified
   * file data.<br>
   * Verification, here, are performed before the upload of the file (so only on the filename
   * which could not represent the real mime-type...).
   * @param componentInstanceId the unique identifier of the component instance.
   * @param fileUploadVerifyData date about the file which will be uploaded.
   */
  void verify(String componentInstanceId, FileUploadVerifyData fileUploadVerifyData);

  /**
   * Performs verification tasks in the behalf of the specified component instance and specified
   * file.<br>
   * Verification, here, are performed just after the file has been uploaded, but before the
   * component instance is registering the file.
   * @param componentInstanceId the unique identifier of the component instance.
   * @param uploadedFile the uploaded file.
   */
  void verify(String componentInstanceId, File uploadedFile);
}

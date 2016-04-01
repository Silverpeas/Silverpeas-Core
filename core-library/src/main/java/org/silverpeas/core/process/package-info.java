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
 * Provides an API for executing one or several processes that implement <code>SilverpeasProcess</code> @see {@link org.silverpeas.core.process.SilverpeasProcess}.
 * This API has been created for chaining within a single applicative transaction different processes (processes
 * dealing with database and file systems together for example) and applying global validations (@see {@link org.silverpeas.core.process.check.ProcessCheck})
 * on output (if any) of processes (Quota disk for example).
 * <code>SilverpeasProcess</code> is an interface which has to be implemented by each process (or task in other words)
 * that has to be taken in charge by this API. It offers methods whose the following three :
 * <ul>
 * <li>process : @see {@link org.silverpeas.core.process.SilverpeasProcess}</li>
 * <li>onSuccessful : @see {@link org.silverpeas.core.process.SilverpeasProcess}</li>
 * <li>onFailure : @see {@link org.silverpeas.core.process.SilverpeasProcess}</li>
 * </ul>
 * For now, it exists two types of <code>SilverpeasProcess</code> : @see {@link org.silverpeas.core.process.ProcessType}
 * The execution of processes is done by the 'execute' methods of <code>ProcessManagement</code> class. (@see {@link org.silverpeas.core.process.management.ProcessManagement})
 *
 * Two abstractions of <code>SilverpeasProcess</code> interface exists for now.
 * One is oriented on data manipulations such as database registring.
 * An other one is oriented on file system manipulations. This last abstraction implements definitively the 'process' method
 * and offers a new abstract method 'processFile' with FileHandler parameter (@see {@link org.silverpeas.core.process.io.file.FileHandler}).
 *
 * As concrete API using example, let's taken the photo creation functionnality of the Gallery application.
 * In a first time, create the transactional service which will contain all directives of photo data registering (<code>createMedia</code> as example).
 * In a second time, create the different Silverpeas processes :
 * <ul>
 * <li>one process to register the photo in database</li>
 * <li>one process to register the photo on file system</li>
 * <li>...</li>
 * <li>one process to handle photo indexes</li>
 * </ul>
 * Be sure all data that have to be persisted in database and all files that have to be saved on file system are handled by Silverpeas processes.
 * Finally, chaining the different processes previously created (@see {@link org.silverpeas.core.process.util.ProcessList})
 * and executing the resulting chain with services of <code>ProcessManagement</code> @see {@link org.silverpeas.core.process.management.ProcessManagement}.
 */
package org.silverpeas.core.process;
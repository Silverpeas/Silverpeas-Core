/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * @author miguel
 */
/**
 * The Silverpeas Logging Engine.
 * </p>
 * The Java ecosystem is rich in logging solutions as it was no standard solution provided by
 * Java until Java 1.5. In order to be agnostic to any logging solutions and to be able to change
 * in the time, Silverpeas has defined its own solution with the goal to wrap any possible logging
 * subsystem and to offer logging capabilities specific to Silverpeas.
 * </p>
 * The Silverpeas Logging Engine is based upon a set of well-defined loggers, each of them mapped
 * to a specific Silverpeas module (a business set of features) that can be a Silverpeas component
 * as well as a Silverpeas Core's engine/API. Each logger is defined by a namespace that defines
 * a dot-separated logger hierarchy and a level that filters the kind of messages to record into
 * the Silverpeas logs. If the level of a logger isn't set explicitly, then its level will be its
 * nearest ancestor with a non-null logging level. It will be the first ancestor with a non-null
 * logging level that will take in charge of the recording of the messages from its child loggers
 * to the logs.
 * </p>
 * Silverpeas defines by configuration at least two logs: one for all traces and another for only
 * errors. For those logs, the Logging Engine provides two handlers/adapters that are set to the
 * Silverpeas root logger, <code>Silverpeas</code>. This mechanism is set up by the Silverpeas
 * installer. It is the responsibility to the logging engine implementation to take care of the
 * handlers/adapters for each logger that have their level set (and hence not inherited).
 * @author mmoquillon
 */
package org.silverpeas.core.util.logging;
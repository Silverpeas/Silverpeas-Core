/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
 * Provides the different exporters of Silverpeas objects to files in a specific format (PDF, iCal,
 * and so on).
 *
 * An exporter is a processor dedicated to take a given type of Silverpeas resource and to serialize
 * it into a file in a dedicated format, so that the Silverpeas information can be shared with
 * outside persons and with other tools.
 * In order to open an access to the different exporters available in Silverpeas, an
 * ExporterFactory object is provided. This factory publishes for each availble exporter a method
 * to get it.
 * It is dedicated to to the exporter provider to enrich the interface of the ExporterFactory class
 * by adding a method to obtain an instance of its exporter (in the form of
 * <pre>Exporter<MySilverpeasResourcerType></pre>) and to exposes an implementation of this one by
 * CDI. To have an idea about that, please glance at the ICalExporter exemple.
 */
package com.silverpeas.export;

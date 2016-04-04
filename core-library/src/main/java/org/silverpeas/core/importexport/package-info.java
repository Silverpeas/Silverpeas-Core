/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * Provides the different exporters and importers of serializable Silverpeas resources in a specific
 * format (PDF, iCal, JSON, and so on).
 *
 * An exporter is a processor aimed to take a given type of a Silverpeas resource for serializing
 * it in a dedicated format into an output stream.
 * An importer is a processor aimed to take an input stream for deserializing from it a
 * Silverpeas resource.
 * Exporters and importers aren't for data source access purpose. Prefer the use of a dedicated ORM
 * for doing.
 *
 * Some core exporters and importers are provided for Silverpeas components. To access these core
 * exporters and importers, an ExporterFactory and an ImporterFactory is provided; they encapsulate
 * the concrete implementation of the core exporters and importers, so that their clients are
 * protected from implementation change.
 * So, for all new core exporters and importers, please enrich the interface of the ExporterFactory
 * and of the ImporterFactory classes to add an access to the implementation of your new core
 * exporters or importers. (Glance at the ICalExporter example to know how to do that).
 */
package org.silverpeas.core.importexport;

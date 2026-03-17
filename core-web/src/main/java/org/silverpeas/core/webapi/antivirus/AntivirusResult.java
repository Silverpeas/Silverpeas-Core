/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.webapi.antivirus;

/**
 * Represents the result of an antivirus scan.
 * <p>
 * Contains information about whether the scanned item is safe, whether an error occurred,
 * the name of any detected virus, and an optional error message.
 */
public class AntivirusResult {
    private boolean safe;
    private boolean error;
    private String virusName;
    private String errorMessage;

    /**
     * Creates a result indicating that the scanned item is safe and no error occurred.
     *
     * @return a safe {@link AntivirusResult} instance
     */
    public static AntivirusResult safeResult() {
        AntivirusResult safeResult = new AntivirusResult();
        safeResult.setError(false);
        safeResult.setSafe(true);
        return safeResult;
    }

    public boolean isSafe() {
        return safe;
    }

    public void setSafe(boolean safe) {
        this.safe = safe;
    }

    public String getVirusName() {
        return virusName;
    }

    public void setVirusName(String virusName) {
        this.virusName = virusName;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

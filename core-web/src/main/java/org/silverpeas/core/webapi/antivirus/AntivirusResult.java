package org.silverpeas.core.webapi.antivirus;

import org.apache.ecs.wml.A;

public class AntivirusResult {
    private boolean safe;
    private boolean error;
    private String virusName;
    private String errorMessage;

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

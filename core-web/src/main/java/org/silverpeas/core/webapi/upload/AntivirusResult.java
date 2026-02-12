package org.silverpeas.core.webapi.upload;

public class AntivirusResult {
    private boolean safe, error;
    private String virusName, errorMessage;

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

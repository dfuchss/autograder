package de.firemage.autograder.core.integrated;

import de.firemage.autograder.api.LinterException;

public class ModelBuildException extends LinterException {
    public ModelBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}

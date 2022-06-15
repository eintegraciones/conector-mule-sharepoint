package org.mule.modules.sharepoint.internal.exception;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Daniel Sánchez Fraile
 * @author dsanchfr
 *
 */
public class SharepointException extends ModuleException {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 8832634801806526602L;

	public <T extends Enum<T>> SharepointException(String message, ErrorTypeDefinition<T> errorTypeDefinition) {
        super(message, errorTypeDefinition);
    }

	

	
}

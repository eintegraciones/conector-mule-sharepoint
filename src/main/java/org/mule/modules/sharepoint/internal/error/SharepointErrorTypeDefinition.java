package org.mule.modules.sharepoint.internal.error;

import java.util.Optional;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

/**
 * Daniel Sánchez Fraile
 * @author dsanchfr
 *
 */
public enum SharepointErrorTypeDefinition implements ErrorTypeDefinition<SharepointErrorTypeDefinition> {

	

	PARSING,

	TIMEOUT,

	SECURITY(MuleErrors.SECURITY),

	CLIENT_SECURITY(MuleErrors.CLIENT_SECURITY),

	SERVER_SECURITY(MuleErrors.SERVER_SECURITY),

	TRANSFORMATION(MuleErrors.TRANSFORMATION),

	CONNECTIVITY(MuleErrors.CONNECTIVITY),

	BAD_REQUEST,

	BASIC_AUTHENTICATION(SERVER_SECURITY),

	UNAUTHORIZED(CLIENT_SECURITY),

	FORBIDDEN(CLIENT_SECURITY),

	NOT_FOUND,

	METHOD_NOT_ALLOWED,

	NOT_ACCEPTABLE,

	//UNSUPPORTED_MEDIA_TYPE(request -> "media type " + request.getHeaderValue(CONTENT_TYPE) + " not supported"),

	TOO_MANY_REQUESTS,

	INTERNAL_SERVER_ERROR,

	SERVICE_UNAVAILABLE,

	BAD_GATEWAY,

	GATEWAY_TIMEOUT;


	private ErrorTypeDefinition<? extends Enum<?>> parent;

    SharepointErrorTypeDefinition(ErrorTypeDefinition<? extends Enum<?>> parent) {
        this.parent = parent;
    }
    
    
    SharepointErrorTypeDefinition() {
    	
    }

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.ofNullable(parent);
    }
	
	
	 
	
}

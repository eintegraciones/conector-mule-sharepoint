package org.mule.modules.sharepoint.internal.error;

import java.util.HashSet;
import java.util.Set;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

public class SharepointErrorProvider implements ErrorTypeProvider {
	
	@SuppressWarnings("rawtypes")
	@Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
		Set<ErrorTypeDefinition> errors = new HashSet<>();
		errors.add(SharepointErrorTypeDefinition.PARSING);
		errors.add(SharepointErrorTypeDefinition.TIMEOUT);
		errors.add(SharepointErrorTypeDefinition.SECURITY);
		errors.add(SharepointErrorTypeDefinition.CLIENT_SECURITY);
		errors.add(SharepointErrorTypeDefinition.SERVER_SECURITY);
		errors.add(SharepointErrorTypeDefinition.TRANSFORMATION);
		errors.add(SharepointErrorTypeDefinition.CONNECTIVITY);
		errors.add(SharepointErrorTypeDefinition.BAD_REQUEST);
		errors.add(SharepointErrorTypeDefinition.BASIC_AUTHENTICATION);
		errors.add(SharepointErrorTypeDefinition.UNAUTHORIZED);
		errors.add(SharepointErrorTypeDefinition.FORBIDDEN);
		errors.add(SharepointErrorTypeDefinition.NOT_FOUND);
		errors.add(SharepointErrorTypeDefinition.METHOD_NOT_ALLOWED);
		errors.add(SharepointErrorTypeDefinition.NOT_ACCEPTABLE);
		errors.add(SharepointErrorTypeDefinition.TOO_MANY_REQUESTS);
		errors.add(SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
		errors.add(SharepointErrorTypeDefinition.SERVICE_UNAVAILABLE);
		errors.add(SharepointErrorTypeDefinition.BAD_GATEWAY);
		errors.add(SharepointErrorTypeDefinition.GATEWAY_TIMEOUT);
		
		return errors;
	}
}



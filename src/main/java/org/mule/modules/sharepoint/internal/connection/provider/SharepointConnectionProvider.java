package org.mule.modules.sharepoint.internal.connection.provider;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import org.mule.modules.sharepoint.internal.connection.SharepointSyncConnection;
import org.mule.modules.sharepoint.internal.error.SharepointErrorTypeDefinition;
import org.mule.modules.sharepoint.internal.exception.SharepointException;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daniel Sánchez Fraile
 * @author dsanchfr
 *
 */
public class SharepointConnectionProvider implements CachedConnectionProvider<SharepointSyncConnection>  {

	private static final Logger log = LoggerFactory.getLogger(SharepointConnectionProvider.class);

	@Parameter
	@Expression(SUPPORTED)
	@DisplayName("Url Token")
	@Summary("Url to get sharepoint token")
	@Example("https://login.microsoftonline.com/{{terantId}}/oauth2/v2.0/token")
	private String urlToken;
	
	@Parameter
	@Expression(SUPPORTED)
	@DisplayName("Client Id")
	@Summary("Client Id of the app from which the token is obtained")
	private String clientId;

	@Parameter
	@Expression(SUPPORTED)
	@DisplayName("Client Secret")
	@Summary("Client Secret of the app from which the token is obtained")
	private String clientSecret;
	
	
	@Parameter
	@Expression(SUPPORTED)
	@DisplayName("Scope")
	@Example("https://graph.microsoft.com/.default openid")
	private String scope;
	
	
	@Parameter
	@Optional
	@DisplayName("Timeout (ms)")
	@Expression(SUPPORTED) 
	@Summary("timeout for conection of api the dataverse") 
	@Placement(tab = ADVANCED_TAB)
	private Integer timeout;
	
	
	
	@Override
	public SharepointSyncConnection connect() throws SharepointException {
		SharepointSyncConnection sharepointConnection = null;
		log.debug("connect...........................");
		try {
			sharepointConnection = new SharepointSyncConnection(urlToken, clientId, clientSecret, scope, timeout);
			sharepointConnection.connect();
		} catch (Exception e) {
			String error = "Error [SharepointConnectionProvider::connect()::SharepointConnection.connect]";
			log.error(error, e);
			throw new SharepointException(error + " ex:" + e.getMessage(),
					SharepointErrorTypeDefinition.CONNECTIVITY);
		}

		return sharepointConnection;
	}

	@Override
	public void disconnect(SharepointSyncConnection sharepointConnection) {
		try {
			sharepointConnection.disconnect();
		} catch (Exception e) {
			String error = "Error [SharepointConnectionProvider::disconnect()::SharepointConnection.disconnectt()]";
			log.error(error, e);
			throw new SharepointException(error + " ex:" + e.getMessage(),
					SharepointErrorTypeDefinition.CONNECTIVITY);
		}
	}

	@Override
	public ConnectionValidationResult validate(SharepointSyncConnection connection) {
		return ConnectionValidationResult.success();
	}
}

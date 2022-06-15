package org.mule.modules.sharepoint.internal.operation;

import static org.mule.runtime.api.meta.ExpressionSupport.SUPPORTED;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.io.InputStream;

import javax.inject.Inject;

import org.mule.modules.sharepoint.internal.connection.SharepointSyncConnection;
import org.mule.modules.sharepoint.internal.error.SharepointErrorProvider;
import org.mule.modules.sharepoint.internal.exception.SharepointException;
import org.mule.modules.sharepoint.internal.operation.graph.rest.SharepointGraph;
import org.mule.runtime.api.meta.model.display.PathModel.Location;
import org.mule.runtime.api.meta.model.display.PathModel.Type;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 * 
 * Daniel Sánchez Fraile
 * @author dsanchfr
 *
 */
public class SharepointOperations {
	private static final Logger log = LoggerFactory.getLogger(SharepointOperations.class);

	@Inject
	ExpressionManager expressionManager;

	@Ignore
	public void setExpressionManager(ExpressionManager expressionManager) {
		this.expressionManager = expressionManager;
	}

	@MediaType(value = ANY, strict = false)
	@Throws(SharepointErrorProvider.class)
	public String findSites(@Connection SharepointSyncConnection connection,
			@Expression(SUPPORTED) @Summary("Name site of sharepoint") String name)
					throws SharepointException {

		log.debug("findSites: " + name);
		connection.connect();
		SharepointGraph sharepointGraph = new SharepointGraph(connection.getToken(), connection.getTimeout());
		String response = sharepointGraph.findSites(name, 0);
		return response;
	}
	
	@MediaType(value = ANY, strict = false)
	@Throws(SharepointErrorProvider.class)
	public String loadFilePath(@Connection SharepointSyncConnection connection,
			@Expression(SUPPORTED) @Summary("Name site of sharepoint") String nameSite, 
			@Expression(SUPPORTED) @Summary("Name drive of sharepoint") String nameDrive,
			@Expression(SUPPORTED) @Summary("Path file load of sharepoint") @Path(type = Type.FILE, acceptsUrls = false, location = Location.EMBEDDED) String file,
			@Expression(SUPPORTED) @Summary("Permissions file load of sharepoint") @Optional(defaultValue = "N/A") String permissions)
					throws SharepointException {

		log.debug("loadFilePath: " + file);
		connection.connect();
		SharepointGraph sharepointGraph = new SharepointGraph(connection.getToken(), connection.getTimeout());
		String response = sharepointGraph.loadFilePath(nameSite, nameDrive, file, permissions);
		return response;
	}
	
	@MediaType(value = ANY, strict = false)
	@DisplayName("LoadFile in Base64")
	@Throws(SharepointErrorProvider.class)
	public String loadFileByteBase64(@Connection SharepointSyncConnection connection,
			@Expression(SUPPORTED) @Summary("Name site of sharepoint") String nameSite, 
			@Expression(SUPPORTED) @Summary("Name drive of sharepoint") String nameDrive,
			@Expression(SUPPORTED) @Summary("Name file load of sharepoint") String nameFile,
			@Expression(SUPPORTED) @Summary("Path file load of sharepoint") String byteBase64,
			@Expression(SUPPORTED) @Summary("Permissions file load of sharepoint") @Optional(defaultValue = "N/A") String permissions)
					throws SharepointException {

		log.debug("loadFileByteBase64: " + nameFile);
		connection.connect();
		SharepointGraph sharepointGraph = new SharepointGraph(connection.getToken(), connection.getTimeout());
		String response = sharepointGraph.loadFileByteBase64(nameSite, nameDrive, byteBase64, nameFile, permissions);
		return response;
	}
	
	
	@MediaType(value = ANY, strict = false)
	@DisplayName("Download with Path")
	@Throws(SharepointErrorProvider.class)
	public InputStream downloadFile(@Connection SharepointSyncConnection connection,
			@Expression(SUPPORTED) @Summary("Name site of sharepoint") String nameSite, 
			@Expression(SUPPORTED) @Summary("Name drive of sharepoint") String nameDrive,
			@Expression(SUPPORTED) @Summary("Name file") String nameFile)
					throws SharepointException {

		connection.connect();
		SharepointGraph sharepointGraph = new SharepointGraph(connection.getToken(), connection.getTimeout());
		InputStream response = sharepointGraph.downloadFile(nameSite, nameDrive, nameFile);
		return response;
	}
}

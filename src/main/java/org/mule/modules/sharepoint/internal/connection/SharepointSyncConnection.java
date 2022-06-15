package org.mule.modules.sharepoint.internal.connection;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.mule.modules.sharepoint.internal.error.SharepointErrorTypeDefinition;
import org.mule.modules.sharepoint.internal.exception.SharepointException;
import org.mule.modules.sharepoint.util.SharepointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This class represents an extension connection just as example (there is no real connection).
 * 
 * Daniel Sánchez Fraile
 * @author dsanchfr
 *
 */
public final class SharepointSyncConnection {

	private static final Logger log = LoggerFactory.getLogger(SharepointSyncConnection.class);

	private String token;
	private String urlToken;
	private String clientId;
	private String clientSecret;
	private String scope;
	private Integer timeout = 0;
	private SharepointUtil sharepointUtil = new SharepointUtil();
	
	

	public SharepointSyncConnection(String urlToken, String clientId, String clientSecret, String scope, Integer timeout) {
		this.urlToken 		= urlToken;
		this.clientId 		= clientId;
		this.clientSecret 	= clientSecret;
		this.scope 		= scope;
	}
	
	/**
	 * Metodo al que hace la conexion con el sharepoint mediante el fichero client_id y client_secret.
	 * @throws SharepointException
	 */
	public void connect() throws SharepointException {
		log.debug("connect tokenClientCredentials validateTokenResource ...........................");
		tokenClientCredentials(0); 
		validateTokenResource(0);
	}

	private void tokenClientCredentials(int nExec) throws SharepointException {
		log.debug("connect tokenClientCredentials ..........................." + nExec);
		HttpResponse httpResponse = null;
		
		if (nExec < 3) {
			try {
				
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost httpPost = new HttpPost(this.urlToken);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpPost.setConfig(requestConfig);
				}
		
				
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
				
				List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
				nameValuePairList.add(new BasicNameValuePair("grant_type", "client_credentials"));
				nameValuePairList.add(new BasicNameValuePair("client_id", this.clientId));
				nameValuePairList.add(new BasicNameValuePair("client_secret", this.clientSecret));
				nameValuePairList.add(new BasicNameValuePair("scope", this.scope));
		        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, "utf-8");
				
				httpPost.setEntity(formEntity);
				
				
				httpResponse = httpclient.execute(httpPost);
			} catch (Exception e) {
				System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointConnection::connect()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.CONNECTIVITY);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("access_token")) {
						this.token = jResponse.getString("access_token");
					}
				} else if (statusCode == 429) {
					log.info("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.info("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    tokenClientCredentials(nExec + 1);
					}
	           
				} else {
					throw new SharepointException("Error [SharepointConnection:::connect()-tokenClientCredentials()::response code:["+ statusCode + "], Error: " + response,
							SharepointErrorTypeDefinition.CONNECTIVITY);
				}
			} catch (Exception e) {
				String error = "Error [SharepointConnection:::connect()-tokenClientCredentials()::read response formnat json]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.CONNECTIVITY);
			} 
		} else {
			String error = "Error [SharepointConnection::connect()-tokenClientCredentials()::failed: too many requests (429)]";
			log.info(error);
			throw new SharepointException(error,
					SharepointErrorTypeDefinition.CONNECTIVITY);
		}
	}
	
	
	private void validateTokenResource(int nExec) {
		log.debug("connect validateTokenResource ..........................." + nExec);
		HttpResponse httpResponse = null;
		if (nExec < 3) {
			try {
				
				String urlSharepoint = "https://graph.microsoft.com/v1.0/sites";
				
				HttpClient httpclient = HttpClientBuilder.create().build();
	
				HttpGet httpGet = new HttpGet(urlSharepoint);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
				
		
				httpGet.setHeader("Content-Type", "application/json");
				httpGet.setHeader("OData-MaxVersion", "4.0");
				httpGet.setHeader("OData-Version", "4.0");
				httpGet.setHeader("Authorization", "Bearer " + getToken());
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointConnection::connect()-validateTokenResource()::executeRequest]";
				log.error(error, e);
				if (e instanceof SocketTimeoutException || e instanceof ConnectTimeoutException) {
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.TIMEOUT);
				} else {
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					log.debug("Correct connection to sharepoint: graph.microsoft.com");
				}  else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    validateTokenResource(nExec + 1);
					}
	           
				} else {
					throw new SharepointException("Error [SharepointConnection::connect()-validateTokenResource()::response code:["+ statusCode + "], Error: " + response,
							SharepointErrorTypeDefinition.CONNECTIVITY);
				}
			} catch (Exception e) {
				String error = "Error [SharepointConnection::connect()-validateTokenResource()::read response formnat json]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.CONNECTIVITY);
			}
		} else {
			String error = "Error [SharepointConnection::connect()-validateTokenResource()::failed: too many requests (429)]";
			log.info(error);
			throw new SharepointException(error,
					SharepointErrorTypeDefinition.CONNECTIVITY);
		}
			
	}


	public void disconnect() {
		// do something to invalidate this connection!
	}

	public String getToken() {
		return token;
	}

	public Integer getTimeout() {
		return timeout;
	}

	
}

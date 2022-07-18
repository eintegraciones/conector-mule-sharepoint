package org.mule.modules.sharepoint.internal.operation.graph.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;



import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.modules.sharepoint.internal.error.SharepointErrorTypeDefinition;
import org.mule.modules.sharepoint.internal.exception.SharepointException;
import org.mule.modules.sharepoint.util.SharepointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SharepointGraph {
	private static final Logger log = LoggerFactory.getLogger(SharepointGraph.class);

	private String token;
	private Integer timeout;
	private SharepointUtil sharepointUtil = new SharepointUtil();
	
	public SharepointGraph(String token, Integer timeout) {
		this.token = token;
		this.timeout = timeout;
	}
	
	
	public String findSites(String nameSiteFind, int nExec) {
		String infoSite = "{\"response\":\"not found\"}";
		log.debug("findSites " + nameSiteFind +  " " + nExec);
		
		if (nExec < 3) {
			HttpResponse httpResponse = null;
			URL urlFindSites = null;
			try {
				urlFindSites = new URL("https://graph.microsoft.com/v1.0/sites?search=" + nameSiteFind);
				URI uriFindSites = new URI(urlFindSites.getProtocol(),
						urlFindSites.getUserInfo(),
	                    urlFindSites.getHost(),
	                    urlFindSites.getPort(),
	                    urlFindSites.getPath(),
	                    urlFindSites.getQuery(), urlFindSites.getRef());
	            String urlFindSitesEncode = uriFindSites.toASCIIString();
				
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(urlFindSitesEncode);
				httpGet.setHeader("Authorization", "Bearer " + this.token);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::findSites()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					log.debug("Response: " + response);
					log.debug("urlFindSites: " + urlFindSites + "##");
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("value")) {
						JSONArray jaValue = jResponse.getJSONArray("value");
						for (int i = 0; i < jaValue.length(); i++) {
							JSONObject jValue = jaValue.getJSONObject(i);
							if (jValue.has("displayName")) {
								String nameSite = jValue.getString("displayName");
								if (nameSiteFind.trim().equalsIgnoreCase(nameSite.trim())) {
									infoSite = jValue.toString();
								}
							}
						}
					}
					
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    infoSite = findSites(nameSiteFind, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::findSites()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::findSites()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		log.debug("response: " + infoSite);
		return infoSite;
	}
	
	
	public String findDriveId(String idSite, String nameDriveFind, int nExec) throws SharepointException {
		String infoDrive = "{\"response\":\"not found\"}";
		log.debug("findDriveId " + nameDriveFind +  " " + nExec);
		
		if (nExec < 3) {
			HttpResponse httpResponse = null;
			URL urlFindDrive = null;
			try {
				urlFindDrive = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives");
				URI uriFindDrive = new URI(urlFindDrive.getProtocol(),
						urlFindDrive.getUserInfo(),
	                    urlFindDrive.getHost(),
	                    urlFindDrive.getPort(),
	                    urlFindDrive.getPath(),
	                    urlFindDrive.getQuery(), urlFindDrive.getRef());
	            String urlFindDriveEncode = uriFindDrive.toASCIIString();
	            
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(urlFindDriveEncode);
				httpGet.setHeader("Authorization", "Bearer " + this.token);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::findDriveId()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("value")) {
						JSONArray jaValue = jResponse.getJSONArray("value");
						for (int i = 0; i < jaValue.length(); i++) {
							JSONObject jValue = jaValue.getJSONObject(i);
							if (jValue.has("name")) {
								String nameSite = jValue.getString("name");
								if (nameDriveFind.equalsIgnoreCase(nameSite)) {
									infoDrive = jValue.toString();
								}
							}
						}
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    infoDrive = findDriveId(idSite, nameDriveFind, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::findDriveId()::response code:["+ statusCode + "] url:" +  urlFindDrive;
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::findDriveId()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		log.debug("response: " + infoDrive);
		return infoDrive;
	}
	
	
	public String findItemId(String idSite, String idDrive, String nameItemFind, int nExec) {
		String infoItem = "{\"response\":\"not found\"}";
		log.debug("findItemId " + nameItemFind +  " " + nExec);
		
		if (nExec < 3) {
			
			HttpResponse httpResponse = null;
			try {
				URL urlFindItem = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives/" + idDrive + "/root");
				URI uriFindItem = new URI(urlFindItem.getProtocol(),
						urlFindItem.getUserInfo(),
	                    urlFindItem.getHost(),
	                    urlFindItem.getPort(),
	                    urlFindItem.getPath(),
	                    urlFindItem.getQuery(), urlFindItem.getRef());
	            String urlFindItemEncode = uriFindItem.toASCIIString();
	            
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(urlFindItemEncode);
				httpGet.setHeader("Authorization", "Bearer " + this.token);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::findItemId()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("name")) {
						String nameItem = jResponse.getString("name");
						if (nameItemFind.equalsIgnoreCase(nameItem)) {
							infoItem = jResponse.toString();
						}
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    infoItem = findItemId(idSite, idDrive, nameItemFind, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::findItemId()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::findItemId()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		log.debug("response: " + infoItem);
		return infoItem;
	}
	
	
	public String findFile(String idSite, String idDrive, String nameFile, int nExec) {
		String infoFile = "{\"response\":\"not found\"}";
		log.debug("findFile " + nameFile +  " " + nExec);
		
		if (nExec < 3) {
			HttpResponse httpResponse = null;
			try {
				URL urlFindFile = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives/" + idDrive + "/root/children");
				URI uriFindFile = new URI(urlFindFile.getProtocol(),
						urlFindFile.getUserInfo(),
	                    urlFindFile.getHost(),
	                    urlFindFile.getPort(),
	                    urlFindFile.getPath(),
	                    urlFindFile.getQuery(), urlFindFile.getRef());
	            String urlFindFileEncode = uriFindFile.toASCIIString();
				
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(urlFindFileEncode);
				httpGet.setHeader("Authorization", "Bearer " + this.token);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::findFile()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("value")) {
						JSONArray jaValue = jResponse.getJSONArray("value");
						for (int i = 0; i < jaValue.length(); i++) {
							JSONObject jValue = jaValue.getJSONObject(i);
							if (jValue.has("name")) {
								String name = jValue.getString("name");
								if (nameFile.equalsIgnoreCase(name)) {
									infoFile = jValue.toString();
								}
							}
						}
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    infoFile = findFile(idSite, idDrive, nameFile, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::findFile()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::findFile()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		log.debug("response: " + infoFile);
		return infoFile;
	}
	
	
	public String putFile(String idSite, String idDrive, InputStream fileContent, String nameFile, int nExec) {
		String infoFile = "{\"response\":\"not found\"}";
		log.debug("putFile " + nameFile +  " " + nExec);	
		if (nExec < 3) {
			
			
			HttpResponse httpResponse = null;
			try {
				URL urlFileLoad = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives/" + idDrive + "/root:/" + nameFile + ":/content");
				URI uriFileLoad = new URI(urlFileLoad.getProtocol(),
						urlFileLoad.getUserInfo(),
	                    urlFileLoad.getHost(),
	                    urlFileLoad.getPort(),
	                    urlFileLoad.getPath(),
	                    urlFileLoad.getQuery(), urlFileLoad.getRef());
	            String urlFileLoadEncode = uriFileLoad.toASCIIString();
				
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut httpPut = new HttpPut(urlFileLoadEncode);
				httpPut.setHeader("Authorization", "Bearer " + this.token);
				//httpPut.setHeader("Content-Type", "application/octet-stream");
	
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpPut.setConfig(requestConfig);
				}
	
				InputStreamEntity reqEntity = new InputStreamEntity(fileContent, -1);
				reqEntity.setContentType("binary/octet-stream");
				reqEntity.setChunked(true); // Send in multiple parts if needed
				httpPut.setEntity(reqEntity);
				
				httpResponse = httpclient.execute(httpPut);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::putFile()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("name")) {
						infoFile = jResponse.toString();
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    infoFile = putFile(idSite,idDrive, fileContent, nameFile, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::putFile()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::putFile()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		return infoFile;
	}
	
	
	
	private String putFileByte(String idSite, String idDrive, byte[] bytes, String nameFile) {
		String infoFile = "{\"response\":\"not found\"}";
		
		try {
			int sizeFile = bytes.length;
			String urlFileTemporl = postFileCreateUploadSession(idSite, idDrive, nameFile, String.valueOf(sizeFile), 0);
			
			byte[] bytesFile = null;
			String nextExpectedRanges = "0-";
			for (int size = 0; size < sizeFile; ) {
				int sizeMaxTemp = size + 1048576;
				String fileRange = "";
				if ((size + 1048576) > sizeFile) {
					bytesFile = Arrays.copyOfRange(bytes,size,sizeFile);
					fileRange = "bytes " + nextExpectedRanges + (sizeFile-1) + "/" + sizeFile;
				} else {
					bytesFile = Arrays.copyOfRange(bytes,size,sizeMaxTemp);
					fileRange = "bytes " + nextExpectedRanges + (size + (bytesFile.length-1)) + "/" + sizeFile;
				}
				nextExpectedRanges = postFileLoadByte(urlFileTemporl, String.valueOf(bytesFile.length), fileRange, bytesFile, 0);
				size = size + bytesFile.length;
				bytesFile = null;
			}
		
		} catch (Exception e) {
			if (e instanceof SharepointException) {
				throw (SharepointException) e;
			} else {
				String error = "Error [SharepointGraph::putFile()::read response formnat json]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			}
		} 
		
		return infoFile;
	}
	
	
	
	
	private String postFileCreateUploadSession(String idSite, String idDrive, String nameFile, String fileSize,  int nExec) {
		String uploadUrl = "";
		log.debug("postFileCreateUploadSession " + nameFile +  " " + nExec);
		
		if (nExec < 3) {
			
			HttpResponse httpResponse = null;
			try {
				URL urlFileTemporl = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives/" + idDrive + "/root:/" + nameFile + ":/createUploadSession");	
				URI uriFileTemporl = new URI(urlFileTemporl.getProtocol(),
						urlFileTemporl.getUserInfo(),
	                    urlFileTemporl.getHost(),
	                    urlFileTemporl.getPort(),
	                    urlFileTemporl.getPath(),
	                    urlFileTemporl.getQuery(), urlFileTemporl.getRef());
	            String urlFileTemporlEncode = uriFileTemporl.toASCIIString();
				
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost httpPost = new HttpPost(urlFileTemporlEncode);
				httpPost.setHeader("Authorization", "Bearer " + this.token);
				httpPost.setHeader("Content-Type", "application/json");
	
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpPost.setConfig(requestConfig);
				}
	
				String jBody = "{\"fileSize\":" + fileSize + ",\"name\":\"" + nameFile + "\"}";
				httpPost.setEntity(new StringEntity(jBody.toString()));
				
				httpResponse = httpclient.execute(httpPost);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::putFile()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_OK) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("uploadUrl")) {
						uploadUrl = jResponse.getString("uploadUrl");
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    uploadUrl = postFileCreateUploadSession(idSite, idDrive, nameFile, fileSize, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::putFile()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::putFile()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		return uploadUrl;
	}
	
	
	private String postFileLoadByte(String urlFileTemp, String fileSize, String fileRange, byte[] bytesFile,  int nExec) {
		String nextExpectedRanges = "";
		log.debug("postFileLoadByte " + fileRange +  " " + nExec);
		
		if (nExec < 3) {
			HttpResponse httpResponse = null;
			ByteArrayInputStream byteArrayInputStream = null;
			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut httpPut = new HttpPut(urlFileTemp);
				httpPut.setHeader("Authorization", "Bearer " + this.token);
				//httpPut.setHeader("Content-Length", fileSize);
				httpPut.setHeader("Content-Range", fileRange);
	
				byteArrayInputStream = new ByteArrayInputStream(bytesFile);
				InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(bytesFile), bytesFile.length);
				reqEntity.setContentType("binary/octet-stream");
				reqEntity.setChunked(false); // Send in multiple parts if needed
				httpPut.setEntity(reqEntity);
				
				httpResponse = httpclient.execute(httpPut);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::putFile()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} finally {
				if (byteArrayInputStream != null) {
					try {
						byteArrayInputStream.close();
					} catch (IOException e) {
						String error = "Error [SharepointGraph::putFile()::byteArrayInputStream.close()]";
						log.error(error, e);
						throw new SharepointException(error + " ex:" + e.getMessage(),
								SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
					}
				}
				System.gc();
				
			}
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
				if (statusCode == HttpURLConnection.HTTP_CREATED || statusCode == HttpURLConnection.HTTP_OK) {
					nextExpectedRanges = response;
				} else if (statusCode == HttpURLConnection.HTTP_ACCEPTED) {
					JSONObject jResponse = new JSONObject(response);
					if (jResponse.has("nextExpectedRanges")) {
						JSONArray jNextExpectedRanges = jResponse.getJSONArray("nextExpectedRanges");
						if (jNextExpectedRanges != null) { 
							for (int i = 0; i < jNextExpectedRanges.length(); i++) {
								nextExpectedRanges = jNextExpectedRanges.getString(i);
								nextExpectedRanges = nextExpectedRanges.split("-")[0] + "-";
							}
						}
					}
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    nextExpectedRanges = postFileLoadByte(urlFileTemp, fileSize, fileRange, bytesFile, nExec + 1);
					}
	           
				} else {
					String error = "Error [SharepointGraph::putFile()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::putFile()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} finally {
				System.gc();
			}
		}
		
		return nextExpectedRanges;
	}
	
	
	public InputStream getFile(String idSite, String idDrive, String idFile,  int nExec) {
		InputStream isResponse = null;
		log.debug("getFile " + idFile +  " " + nExec);
		
		if (nExec < 3) {		
			HttpResponse httpResponse = null;
			try {
				
				URL urlFileDownload = new URL("https://graph.microsoft.com/v1.0/sites/" + idSite + "/drives/" + idDrive + "/items/" + idFile + "/content");
				URI uriFileDownload = new URI(urlFileDownload.getProtocol(),
						urlFileDownload.getUserInfo(),
						urlFileDownload.getHost(),
						urlFileDownload.getPort(),
						urlFileDownload.getPath(),
						urlFileDownload.getQuery(), urlFileDownload.getRef());
	            String urlFileDownloadEncode = uriFileDownload.toASCIIString();

				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet httpGet = new HttpGet(urlFileDownloadEncode);
				httpGet.setHeader("Authorization", "Bearer " + this.token);
				
				if (this.timeout != null && this.timeout.compareTo(0) == 1) {
					RequestConfig requestConfig = RequestConfig.custom()
					    .setConnectionRequestTimeout(this.timeout)
					    .setConnectTimeout(this.timeout)
					    .setSocketTimeout(this.timeout)
					    .build();
					httpGet.setConfig(requestConfig);
				}
	
				
				httpResponse = httpclient.execute(httpGet);
			} catch (Exception e) {
				//System.out.println("ERROR " + e.getMessage());
				String error = "Error [SharepointGraph::getFile()::executeRequest]";
				log.error(error, e);
				throw new SharepointException(error + " ex:" + e.getMessage(),
						SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			} 
			
			//Leo la respuesta del post token
			try {
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode == HttpURLConnection.HTTP_OK) {
					isResponse = httpResponse.getEntity().getContent();
				} else if (statusCode == 429) {
					log.debug("ERROR 429");
					Integer retryAfter = sharepointUtil.getHeadersRetryAfter(httpResponse);
					log.debug("retryAfter " + retryAfter);
					if (retryAfter.compareTo(0) != 0) {
	                    Thread.sleep(retryAfter * 1000);
	                    isResponse = getFile(idSite, idDrive, idFile, nExec + 1);
					}
	           
				} else {
					String response = sharepointUtil.getBodyResponse(httpResponse.getEntity().getContent());
					String error = "Error [SharepointGraph::getFile()::response code:["+ statusCode + "]";
					sharepointUtil.thorwExceptionCode(statusCode, error, response);
				}
			} catch (Exception e) {
				if (e instanceof SharepointException) {
					throw (SharepointException) e;
				} else {
					String error = "Error [SharepointGraph::getFile()::read response formnat json]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			} 
		}
		
		return isResponse;
	}

	public String loadFilePath(String nameSite, String nameDrive, String pathFile, String permissions) {
		File fileLoad = new File(pathFile);
		String infoFile = "";
		String nameFileEncode = fileLoad.getName();
		InputStream fileContent = null;
		byte[] bytes = null;
		try {
			fileContent = new FileInputStream(fileLoad);
			//nameFileEncode = URLEncoder.encode(fileLoad.getName(), StandardCharsets.UTF_8.toString());
			if (4194304 > fileContent.available()) {
				infoFile = loadFile(nameSite, nameDrive, fileContent, nameFileEncode);
			} else {
				bytes = IOUtils.toByteArray(fileContent);
				infoFile = loadFile(nameSite, nameDrive, bytes, nameFileEncode);
			}
		} catch (IOException e) {
			String error = "Error [SharepointGraph::loadFilePath::FileInputStream]";
			log.error(error, e);
			throw new SharepointException(error + " ex:" + e.getMessage(),
					SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
		} finally { 
			if (fileContent != null) {
				try {
					fileContent.close();
				} catch (IOException e) {
					String error = "Error [SharepointGraph::loadFilePath::FileInputStream::close]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			}
			
			if (bytes != null) {
				bytes = null;
			}
			
			System.gc();
		}
		
		return infoFile;
	}
	
	
	public String loadFileByteBase64(String nameSite, String nameDrive, String byteBase64, String nameFile, String permissions) {
		String infoFile = "";
		String nameFileEncode = nameFile;
		InputStream fileContent = null;
		byte[] bytes = null;

		try {
			bytes = Base64.decodeBase64(byteBase64);
			//nameFileEncode = URLEncoder.encode(nameFile, StandardCharsets.UTF_8.toString());
			if (4194304 > bytes.length) {
				fileContent = new ByteArrayInputStream(bytes);
				infoFile = loadFile(nameSite, nameDrive, fileContent, nameFileEncode);
			} else {
				infoFile = loadFile(nameSite, nameDrive, bytes, nameFileEncode);
			}
		} catch (Exception e) {
			String error = "Error [SharepointGraph::loadFilePath::FileInputStream]";
			log.error(error, e);
			throw new SharepointException(error + " ex:" + e.getMessage(),
					SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
		} finally {
			if (fileContent != null) {
				try {
					fileContent.close();
				} catch (IOException e) {
					String error = "Error [SharepointGraph::loadFilePath::FileInputStream::close]";
					log.error(error, e);
					throw new SharepointException(error + " ex:" + e.getMessage(),
							SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
				}
			}
			
			if (bytes != null) {
				bytes = null;
			}
			
			System.gc();
		}
		
		return infoFile;
	}
	

	private String loadFile(String nameSite, String nameDrive, InputStream fileContent, String nameFile) {
		//Liberamos memoria
		System.gc();
		
		String idSite = "0";
		String infoSite = findSites(nameSite, 0);
		JSONObject jInfoSite = new JSONObject(infoSite);
		if (jInfoSite.has("id")) {
			idSite = jInfoSite.getString("id");
		}
		
		String idDrive = "0";
		String infoDrive = findDriveId(idSite, nameDrive, 0);
		
		JSONObject jInfoDrive = new JSONObject(infoDrive);
		if (jInfoDrive.has("id")) {
			idDrive = jInfoDrive.getString("id");
		}
		
		String infoFile = putFile(idSite, idDrive, fileContent, nameFile, 0);
		
		//Liberamos memoria
		System.gc();
		
		return infoFile;
	}
	
	
	private String loadFile(String nameSite, String nameDrive, byte[] bytes, String nameFile) {
		//Liberamos memoria
		System.gc();
		
		String idSite = "0";
		String infoSite = findSites(nameSite, 0);
		JSONObject jInfoSite = new JSONObject(infoSite);
		if (jInfoSite.has("id")) {
			idSite = jInfoSite.getString("id");
		}
		
		String idDrive = "0";
		String infoDrive = findDriveId(idSite, nameDrive, 0);
		
		JSONObject jInfoDrive = new JSONObject(infoDrive);
		if (jInfoDrive.has("id")) {
			idDrive = jInfoDrive.getString("id");
		}
		
		String infoFile = putFileByte(idSite, idDrive, bytes, nameFile);
		
		//Liberamos memoria
		System.gc();
		
		return infoFile;
	}


	public InputStream downloadFile(String nameSite, String nameDrive, String nameFile) {
		String idSite = "0";
		String infoSite = findSites(nameSite, 0);
		
		JSONObject jInfoSite = new JSONObject(infoSite);
		if (jInfoSite.has("id")) {
			idSite = jInfoSite.getString("id");
		}
		
		String idDrive = "0";
		String infoDrive = findDriveId(idSite, nameDrive, 0);
		
		JSONObject jInfoDrive = new JSONObject(infoDrive);
		if (jInfoDrive.has("id")) {
			idDrive = jInfoDrive.getString("id");
		}
		
		
		String idFile = "0";
		String infoFile= findFile(idSite, idDrive, nameFile, 0);
		
		JSONObject jInfoFile = new JSONObject(infoFile);
		if (jInfoFile.has("id")) {
			idFile = jInfoFile.getString("id");
		}
		
		InputStream response = getFile(idSite, idDrive, idFile, 0);
		
		return response;
	}

}

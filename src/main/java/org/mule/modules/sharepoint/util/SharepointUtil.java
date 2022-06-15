package org.mule.modules.sharepoint.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.mule.modules.sharepoint.internal.error.SharepointErrorTypeDefinition;
import org.mule.modules.sharepoint.internal.exception.SharepointException;

public class SharepointUtil {

	public String getBodyResponse(InputStream isResponse) throws IOException {
		StringBuilder sbResponse = new StringBuilder();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(isResponse))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sbResponse.append(line);
			}
		}
		return sbResponse.toString();
	}
	
	public void thorwExceptionCode(int statusCode, String error, String response) {
		switch (statusCode) {
			case 400:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.BAD_REQUEST);
			case 401:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.UNAUTHORIZED);
			case 403:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.FORBIDDEN);
			case 404:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.NOT_FOUND);
			case 405:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.METHOD_NOT_ALLOWED);
			case 408:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.TIMEOUT);
			//case 415:
			//	throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.MEDIa);
			//	break;
			case 429:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.TOO_MANY_REQUESTS);
			case 500:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
			//case 501:
			//	throw new DataverseException(error + " ex:" + response, SharepointErrorTypeDefinition.NOT_IMPE);
			//	break;
			case 503:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.SERVICE_UNAVAILABLE);
			default:
				throw new SharepointException(error + " ex:" + response, SharepointErrorTypeDefinition.INTERNAL_SERVER_ERROR);
		}
	}

	public Integer getHeadersRetryAfter(HttpResponse httpResponse) {
		Header[] headers = httpResponse.getHeaders("Retry-After");
		
		Integer retryAfter = 0;  
		for (Header header : headers) {
			String value = header.getValue();
			if (value != null && !value.trim().equals(""))  {
				String time = "0";
				if (value.contains(",")) {
					String[] data = value.split(",");
					time = data[0];
				} else {
					time = value;
				}
				retryAfter = Integer.valueOf(time);
			}
			//System.out.println("Key : " + header.getName() 
			//      + " ,Value : " + header.getValue());
		}
		return retryAfter;
	}
}

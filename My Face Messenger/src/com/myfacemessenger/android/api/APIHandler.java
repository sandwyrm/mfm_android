package com.myfacemessenger.android.api;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class APIHandler
{
	public static final String	API_URL			= "http://myface-messenger.heroku.com";
	public static final String	USERS_URL		= API_URL + "/users";
	public static final String	FACE_URL		= API_URL + "/numbers";
	// Connection Defaults
	public static final int		CLIENT_TIMEOUT	= 60000;

	private HttpClient			client			= null;
	private HttpResponse		lastResponse	= null;

	public APIHandler()
	{
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CLIENT_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, CLIENT_TIMEOUT);
		client = new DefaultHttpClient(httpParams);
	}

	public void register()
	{
		HttpPost request = new HttpPost(USERS_URL);
		try {
			client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
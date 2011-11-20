package com.myfacemessenger.android.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.myfacemessenger.android.MFMessenger;

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

	public void register(String number)
	{
		JSONObject requestData = new JSONObject();
		JSONObject requestUser = new JSONObject();
		try {
			requestUser.put("mobile_number", number);
			requestData.put("user", requestUser);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		HttpPost request = new HttpPost(USERS_URL);
		try {
			HttpEntity entity;
			StringEntity s = new StringEntity(requestData.toString());
			s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			entity = s;
			request.setEntity(s);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			lastResponse = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MFMessenger.log(lastResponse.toString());
	}
}
package com.myfacemessenger.android.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
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

	protected HttpResponse request(HttpUriRequest request)
	{
		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	protected HttpResponse post(String url, String data)
	{
		HttpResponse response = null;
		HttpPost request = new HttpPost(url);
		try {
			StringEntity s = new StringEntity(data);
			s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(s);
			request.addHeader("Content-Type", "application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		response = request(request);
		return response;
	}

	protected HttpResponse put(String url, String data)
	{
		HttpResponse response = null;
		HttpPut request = new HttpPut(url);
		try {
			StringEntity s = new StringEntity(data);
			s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			request.setEntity(s);
			request.addHeader("Content-Type", "application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		response = request(request);
		return response;
	}

	protected HttpResponse delete(String url)
	{
		HttpResponse response = null;
		HttpDelete request = new HttpDelete(url);
		request.addHeader("Content-Type", "application/json");
		response = request(request);
		return response;
	}

	protected HttpResponse get(String url)
	{
		HttpResponse response = null;
		HttpGet request = new HttpGet(url);
		request.addHeader("Content-Type", "application/json");
		response = request(request);
		return response;
	}

	protected JSONObject processResponse(HttpResponse response)
	{
		JSONObject responseData = null;
		try {
			responseData = new JSONObject(response.getEntity().getContent().toString());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseData;
	}

	public void registerUser(String number, String email)
	{
		JSONObject requestData = new JSONObject();
		JSONObject requestUser = new JSONObject();
		try {
			requestUser.put("mobile_number", number);
			if( email != null ) {
				requestUser.put("email", email);
			}
			requestData.put("user", requestUser);
			MFMessenger.log("Request to be sent: " + requestData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		lastResponse = post( USERS_URL, requestData.toString() );
		MFMessenger.log("Response received: " + lastResponse.toString());
	}

	public void updateUser(String number, String email)
	{
		JSONObject requestData = new JSONObject();
		JSONObject requestUser = new JSONObject();
		try {
			requestUser.put("mobile_number", number);
			if( email != null ) {
				requestUser.put("email", email);
			}
			requestData.put("user", requestUser);
			MFMessenger.log("Request to be sent: " + requestData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		lastResponse = put( FACE_URL + "/" + number, requestData.toString() );
		MFMessenger.log("Response received: " + lastResponse.toString());
	}

	public void deleteUser(String number)
	{
		lastResponse = delete( FACE_URL + "/" + number );
	}

	public JSONObject userInformation(String number)
	{
		lastResponse = get( FACE_URL + "/" + number );
		return processResponse(lastResponse);
	}

	public JSONObject getFace(String number, String emote)
	{
		lastResponse = get( FACE_URL + "/" + number + "/faces/" + emote );
		return processResponse(lastResponse);
	}

	public void updateFace(String number, String email)
	{
		JSONObject requestData = new JSONObject();
		JSONObject requestUser = new JSONObject();
		try {
			requestUser.put("mobile_number", number);
			if( email != null ) {
				requestUser.put("email", email);
			}
			requestData.put("user", requestUser);
			MFMessenger.log("Request to be sent: " + requestData.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		lastResponse = put(FACE_URL + "/" + number, requestData.toString());
		MFMessenger.log("Response received: " + lastResponse.toString());
	}
}
package com.feima;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import proxy.http.HttpRequest;
import proxy.http.HttpResponse;
import proxy.http.ReadFromStream;

public class Http {

	public static HttpResponse get(String urlString){
		byte[] datas=null;
		URL url=null; 
		try {
			url=new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		HttpURLConnection connection=null;
		//byte[] buffer=new byte[2048];
		try {
			connection=(HttpURLConnection)url.openConnection();
			   // Allow Inputs
			connection.setDoInput(true);
		    // Allow Outputs
			connection.setDoOutput(true);
		    // Don't use a cached copy.
			connection.setUseCaches(false);
		    // Use a post method.
			
			Map headerMap=connection.getHeaderFields();
			
			InputStream in=connection.getInputStream();
			datas=ReadFromStream.read(in, 10);
			in.close();
			
			HttpResponse httpResponse=HttpResponse.getInstance(datas, 
					"HTTP/1.1", 
					""+connection.getResponseCode(),
					connection.getResponseMessage(), 
					headerMap);
			connection.disconnect();

			return httpResponse;
		} catch (java.net.UnknownHostException uhe) {
			uhe.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
	public static String getContent(String urlString){
		HttpResponse response= get(urlString);
		byte [] data = response.getContent();
		try {
			return new String(data, "gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

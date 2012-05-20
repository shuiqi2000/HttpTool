package com.feima;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import proxy.http.HttpResponse;
import proxy.http.ReadFromStream;


public class HttpUpload {

	public static HttpResponse uploadArg(String urlString,Map argMap){
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
			connection.setRequestMethod("POST");
			
		    connection.setRequestProperty("Content-Type",
             "application/x-www-form-urlencoded");
			
			DataOutputStream dos = new DataOutputStream(connection
					.getOutputStream());
			StringBuffer argsb = new StringBuffer();
			if (argMap != null) {
				for (Object key : argMap.keySet()) {
					if (argMap.get(key) != null) {
						argsb.append(key);
						argsb.append("=");
						argsb.append(URLEncoder.encode(
								(String) argMap.get(key), "utf-8"));
						argsb.append("&");
					}
				}
				argsb=argsb.delete(argsb.length()-1,argsb.length());
				dos.writeBytes(argsb.toString());
			}
				
			dos.flush();
			dos.close();
			
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
	
	public static byte[] uploadFile(String urlString,File uploadFile){	
		byte[] datas=null;
		String boundary = "*****";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		FileInputStream fin=null;
		byte[] buffer=new byte[2048];
		try {
			fin = new FileInputStream(uploadFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		URL url=null;
		
		int pos=0;
		
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
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");

			connection.setRequestProperty("Content-Type",
		      "multipart/form-data;boundary=" + boundary);
			
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"upload\";"
			      + " filename=\"" + uploadFile.getName() + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			
			int len=1;
			while (len > 0) {
				try {
					len = fin.read(buffer, 0, 2048);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (len <= 0)
					break;
				dos.write(buffer, 0, len);
			}
			
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			fin.close();
			dos.flush();
			dos.close();
			
            datas=ReadFromStream.read(connection.getInputStream(), 2);
			
			/*
			StringBuffer sb=new StringBuffer();
			try {
				DataInputStream inStream = new DataInputStream(connection.getInputStream());
				String str;
				while ((str = inStream.readLine()) != null) {
					sb.append(str);
				}
				inStream.close();
			} catch (IOException ioex) {
				System.out.println("From (ServerResponse): " + ioex);
				return null;
			}
			*/
				 
			connection.disconnect();
			//return sb.toString();
			return datas;
		} catch (java.net.UnknownHostException uhe) {
			uhe.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
	public static void main(String args[]){
		//String urlString="http://192.168.8.130/apabibook/uploadMeta.do";
		String urlString="http://localhost/proxyserver/proxyrequest";
		Map argMap=new java.util.HashMap();
		argMap.put("user", "shuiqi2000");
		argMap.put("account", "shuiqi2000");
		argMap.put("email", "shuiqi2000@gmail.com");
		argMap.put("name", "无为");
		
		//File uploadFile=new File("d:\\津科数据.rar");
		HttpUpload upload=new HttpUpload();
		HttpResponse httpResponse=upload.uploadArg(urlString, argMap);
		try {
			System.out.println(new String(httpResponse.getData(),"utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//upload.uploadFile(urlString,uploadFile);
	}
	
}

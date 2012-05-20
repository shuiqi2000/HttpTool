package proxy.http;

import java.util.List;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class HttpUtl {
	public final static String endLine="\r\n";
	public final static int TotalUnAvailableCount=10;
	
	public static byte[] proxyRequest(byte[] content){
		
		ByteArrayInputStream bain=new ByteArrayInputStream(content);
	    BufferedReader br=new BufferedReader(new InputStreamReader(bain));
	    String requestLine=null;
		try {
			requestLine = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		String urlString=null;
		String urlArray[]=requestLine.split(" ");
		if(urlArray!=null&&urlArray.length>=2){
			urlString=urlArray[1];
		}else{
			return null;
		}
		String host=null;
		String pathAndQuery=null;
		int port=80;
		try{
		    URL url=new URL(urlString);
		    host=url.getHost();
		    pathAndQuery=url.getPath();
		    if(url.getQuery()!=null){
		    	pathAndQuery+="?"+url.getQuery();
		    }
		    if(url.getPort()!=-1) port=url.getPort();
		}catch (MalformedURLException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		
		
		List<String> headers=new ArrayList<String>();
		byte []datas=null;
		
		if((datas=parseRequest(content,headers))==null){
			System.out.println("request data is null");
		}
		
		StringBuffer newHeaderSB=new StringBuffer();	
		newHeaderSB.append(urlArray[0]);
		newHeaderSB.append(" ");
		newHeaderSB.append(pathAndQuery);
		newHeaderSB.append(" ");
		newHeaderSB.append(urlArray[2]);
		newHeaderSB.append(endLine);
		
	    for(String header: headers){
			if(header.indexOf("Referer")!=0){
				if(header.indexOf("Proxy-Connection")==0){
					newHeaderSB.append("Connection: Keep-Alive");
					newHeaderSB.append(endLine);
				}else{
			    	newHeaderSB.append(header);
			    	newHeaderSB.append(endLine);
				}
			}
		
		}
		newHeaderSB.append(endLine);
		byte headerBytes[]=newHeaderSB.toString().getBytes();
		byte requestBytes[]=null;
		if(datas!=null){
			requestBytes=new byte[headerBytes.length+datas.length];
			System.arraycopy(headerBytes, 0, requestBytes, 0, headerBytes.length);
			System.arraycopy(datas, 0, requestBytes, headerBytes.length, datas.length);
		}else{
			requestBytes=new byte[headerBytes.length];
			System.arraycopy(headerBytes, 0, requestBytes, 0, headerBytes.length);
		}
		
		try {
			System.out.println("requestBytes:"+new String(requestBytes,"utf-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		return request(host,port,requestBytes);
	}
	
	public static byte[] request(String host,int port,byte[] content){
	    try {
			Socket clientSocket=new Socket(host,port);
			OutputStream out=clientSocket.getOutputStream();
			out.write(content);
			out.flush();
			
			InputStream in=clientSocket.getInputStream();
			
			int len = -1;
			byte[] buffer = new byte[4096];
			int bufferSize = 4096;
			int size = 0;
			int onceSize=2046;
			Thread.sleep(100);
			int availableCount=0;
			do {
				if(in.available()!=0){
					availableCount=-1;
				    len = in.read(buffer,size,onceSize);
				}else{
					if(availableCount==-1||availableCount==TotalUnAvailableCount){
						System.out.println("in.available()==0 break");
						break;
					}else{
						availableCount++;
						System.out.println("availableCount="+availableCount);
						Thread.sleep(1000);
						continue;
					}	
				}
				
				if (len == -1){
					System.out.println("len==-1");
					//break;
				}
				size += len;
				if (size > bufferSize / 2) {
					byte[] tmp = new byte[bufferSize * 2];
					System.arraycopy(buffer, 0, tmp, 0, size);
					buffer = tmp;
					bufferSize *= 2;
				}
			} while(true);
			//while (len!=0&&len!= -1); 不判断in.read的返回值，判断in.available()

			System.out.println("server to client data size="+size);
			if (size > 0) {
				byte[] tmp = new byte[size];
				System.arraycopy(buffer, 0, tmp, 0, size);
				buffer = tmp;
			}else{
				buffer=null;
			}
			
			out.close();
			in.close();
			clientSocket.close();
		
			return buffer;
			
		} catch (IOException ioe){
			ioe.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] parseRequest(byte[] content, List<String> headers) {
		byte[] datas=null;
		try {
			int lastPos = 0;
			boolean isFirst=true;
			for (int i = 0; i < content.length; i++) {
				if (content[i] == '\r') {
					
					if(isFirst){
						isFirst=false;
						i++;
						lastPos=i+1;
						continue;
					}
					headers.add(new String(content, lastPos, i - lastPos));
					i++;
					lastPos = i + 1;
					if (content[i + 1] == '\r') {
						lastPos = i + 3;
						break;
					}
				} else if (content[i] == '\n') {
					if(isFirst){
						isFirst=false;
						lastPos=i+1;
						continue;
					}
					headers.add(new String(content, lastPos, i - lastPos));
					lastPos = i + 1;
					if (content[i + 1] == '\n') {
						lastPos = i + 2;
						break;
					}
				}
			}

			if (lastPos < content.length) {
				int dataLen = content.length - lastPos;
				datas=new byte[dataLen];
				System.arraycopy(content, lastPos, datas, 0, dataLen);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return datas;
	}

}

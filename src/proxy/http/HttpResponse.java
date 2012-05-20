package proxy.http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpResponse {
	public static String endLine="\r\n";
	private Map headerMap=new HashMap();
	private List<String> headerList=new ArrayList<String>();
	private byte data[];
	
	private String statusLine;
	private String statusCode;
	private String httpVersion;
	private String reasonPhrase;
	
	private byte content[];
	
	private HttpResponse(){
		
	}
	
	public final byte[] getContent(){
		return content;
	}
	
	public final byte[] getData(){
		return data;
	}
	
	public final Map getHeaderMap(){
		return headerMap;
	}
	
	public final List<String> getHeaderList(){
		return headerList;
	}
	
	public void removeHeader(String key){
		//ListIterator<String> it=headerList.listIterator();
		Iterator<String> it=headerList.iterator();
		while(it.hasNext()){
			String headerString=it.next();
			if(headerString.indexOf(key)==0){
				it.remove();
			}
		}
		
		headerMap.remove(key);
		generateData();
	}
	
	public void addHeader(String key,String value){
		//ListIterator<String> it=headerList.listIterator();
		headerList.add(key+": "+value);
		
		if(headerMap.containsKey(key)){
			((List)headerMap.get(key)).add(value);
		}else{
			List t=new ArrayList();
			t.add(value);
			headerMap.put(key, t);
		}
		
		generateData();
	}
	
	private void generateData(){
		Iterator it=headerMap.keySet().iterator();
		StringBuffer sb=new StringBuffer();
		
		sb.append(statusLine+endLine);	
		for(String headerLine:headerList){			
			sb.append(headerLine+endLine);
		}
		sb.append(endLine);
	
		byte headerBytes[]=sb.toString().getBytes();
		if(content!=null){
			data=new byte[headerBytes.length+content.length];
			System.arraycopy(headerBytes, 0,data, 0, headerBytes.length);
			System.arraycopy(content, 0, data, headerBytes.length, content.length);
		}else{
			data=new byte[headerBytes.length];
			System.arraycopy(headerBytes, 0, data, 0, headerBytes.length);
		}
		
	}
	
	public static HttpResponse getInstance(byte[] data){
		HttpResponse http=new HttpResponse();
		http.data=new byte[data.length];
		System.arraycopy(data, 0, http.data, 0, data.length);
		http.parse(data);
		return http;
	}
	
	public static HttpResponse getInstance(byte[]content,
			String httpVersion,
			String statusCode,
			String reasonPhrase,
			Map headerMap){
		HttpResponse http=new HttpResponse();
		http.content=new byte[content.length];
		System.arraycopy(content, 0, http.content, 0, content.length);
		http.statusCode=statusCode;
		http.httpVersion=httpVersion;
		http.reasonPhrase=reasonPhrase;
		http.statusLine=http.httpVersion+" "+http.statusCode+" "+http.reasonPhrase;
		
		http.headerMap=new HashMap(headerMap);
		
		Iterator it=headerMap.keySet().iterator();
		StringBuffer sb=new StringBuffer();
		sb.append(http.statusLine+endLine);
		while(it.hasNext()){
			String key=(String)it.next();
			List values=(List)headerMap.get(key);
			if(key==null)continue;
			for(Object valueO:values){
				String headerLine=key+": "+valueO;
				sb.append(headerLine+endLine);
				http.headerList.add(headerLine);
			}
		}
		sb.append(endLine);
	
		byte headerBytes[]=sb.toString().getBytes();
		if(content!=null){
			http.data=new byte[headerBytes.length+content.length];
			System.arraycopy(headerBytes, 0, http.data, 0, headerBytes.length);
			System.arraycopy(content, 0, http.data, headerBytes.length, content.length);
		}else{
			http.data=new byte[headerBytes.length];
			System.arraycopy(headerBytes, 0, http.data, 0, headerBytes.length);
		}
		
		return http;
	}

	public void parse(byte[] data){
		String dataString=null;
		try {
			dataString=new String(data,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<String> headers=new ArrayList<String>();	
		content=this.parseResponse(data, headers);
		
		if(headers.size()>1){
			statusLine=headers.get(0);
			String[] tmp=statusLine.split(" ");
			httpVersion=tmp[0];
			statusCode=tmp[1];
			reasonPhrase=tmp[2];
		}
		
		for(int i=1;i<headers.size();i++){
			headerList.add(headers.get(i));
			int mpos=headers.get(i).indexOf(':');
			if(mpos!=-1){
			    String key=headers.get(i).substring(0, mpos);
			    String value=headers.get(i).substring(mpos+1,headers.get(i).length() ).trim();
			    if(headerMap.containsKey(key)){
			    	((List)headerMap.get(key)).add(value);
			    }else{
			        List values=new ArrayList();
			        values.add(value);
			        headerMap.put(key,values);
			    }
			}
		}	
	}
	
	public byte[] parseResponse(byte[] content, List<String> headers) {
		byte[] datas=null;
		try {
			int lastPos = 0;
			for (int i = 0; i < content.length; i++) {
				if (content[i] == '\r') {	
					headers.add(new String(content, lastPos, i - lastPos));
					i++;
					lastPos = i + 1;
					if (content[i + 1] == '\r') {
						lastPos = i + 3;
						break;
					}
				} else if (content[i] == '\n') {	
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
	
	public void setContent(byte[] content){
		this.content=new byte[content.length];
		System.arraycopy(content,0,this.content,0,content.length);
		generateData();
	}

}

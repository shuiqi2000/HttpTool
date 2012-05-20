package proxy.http;

import java.io.IOException;
import java.io.InputStream;

public class ReadFromStream {
	public final static int TotalUnAvailableCount = 10;

	public static byte[] read(InputStream in,int TotalUnAvailableCount) {
		int len = -1;
		byte[] buffer = new byte[4096];
		int bufferSize = 4096;
		int size = 0;
		int onceSize = 2046;
		try {
			Thread.sleep(100);
			int availableCount = 0;
			do {
				if (in.available() != 0) {
					//availableCount = -1;
					len = in.read(buffer, size, onceSize);
				} else {
					if (availableCount == TotalUnAvailableCount) {
						System.out.println("in.available()==0 break");
						break;
					} else {
						availableCount++;
						System.out.println("availableCount=" + availableCount);
						Thread.sleep(1000);
						continue;
					}
				}

				if (len == -1) {
					System.out.println("len==-1");
					// break;
				}
				size += len;
				if (size > bufferSize / 2) {
					byte[] tmp = new byte[bufferSize * 2];
					System.arraycopy(buffer, 0, tmp, 0, size);
					buffer = tmp;
					bufferSize *= 2;
				}
			} while (true);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("server to client data size=" + size);
		if (size > 0) {
			byte[] tmp = new byte[size];
			System.arraycopy(buffer, 0, tmp, 0, size);
			buffer = tmp;
		} else {
			buffer = null;
		}
		return buffer;
	}

}

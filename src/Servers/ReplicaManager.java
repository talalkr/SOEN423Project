package Servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReplicaManager implements Runnable{
	private int countCA=0;
	private int countUK=0;
	private int countUS=0;
	
	private void Increment(String location) {
		if(location.startsWith("CA")) {
			countCA++;
		}if(location.startsWith("UK")) {
			countUK++;
		}if(location.startsWith("US")) {
			countUS++;
		}
	}
	
	private void RestartReplica(String location) {
		System.out.println("Should Restart Replica");
	}

	
	private void UDPServer(int serverPort) throws Exception{
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(serverPort);
			byte[] buffer = new byte[2046];
			while(true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				sock.receive(request);
				String data = new String(buffer,0,request.getLength());
				InetAddress host = InetAddress.getByName("localhost"); //GET HOST BY APPR. IP!
				//Record Count
				if(data.startsWith("N")) {
					String location = data.substring(1);
					Increment(location);
					RestartReplica(location); //Only if count == 3
					String res = "RM Value Incremented";
					byte[] m = res.getBytes();
					DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), 3000);
					sock.send(reply);
				}
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}finally {
			if(sock != null) sock.close();
		}
	}


	public void run() {
		// TODO Auto-generated method stub
		try {
				UDPServer(9000);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}


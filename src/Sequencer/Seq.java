package Sequencer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import Records.EmployeeRecord;
import Records.ManagerRecord;
import Records.Record;

public class Seq implements Runnable {
	
	public String location;
	
	public static void main(String[] args) {
		System.out.println("");
	}
	/*
	 * Receive from HRManager the request
	 * Identify request location
	 * Multicast request to appropriate servers in the replicas
	 */
	private void UDPServer(int udpPort) throws Exception{
		MulticastSocket sock = null;
		try {
			InetAddress host = InetAddress.getByName("localhost"); //GET HOST BY APPR. IP!
			sock = new MulticastSocket(udpPort);
			byte[] buffer = new byte[2000];
			while(true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				sock.receive(request);
				
				DatagramPacket reqReplica = new DatagramPacket(buffer, buffer.length , host, 7000);
				sock.send(reqReplica);

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
			UDPServer(6666);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

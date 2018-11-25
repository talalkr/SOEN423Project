package udp_bridge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class UDPState implements UDP{

private DatagramSocket aSocket;
private Process latestSender;
	
	protected UDPState(DatagramSocket aSocket) {
		this.aSocket = aSocket;
	}
	
	public UDPState(int port) throws SocketException {
		this.aSocket = new DatagramSocket(port);
	}
	
	public UDPState() throws SocketException {
		this.aSocket = new DatagramSocket();
	}
	

	protected void socketReceive(DatagramPacket request) throws IOException {
		this.aSocket.receive(request);
	}
	
	protected void socketSend(DatagramPacket request) throws IOException {
		this.aSocket.send(request);
	}
	
	public void closeSocket() {
		this.aSocket.close();
	}
	
	public int getLocalPort() {
		return aSocket.getLocalPort();
	}
	
	public InetAddress getLocalAddress() {
		
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			
			return aSocket.getLocalAddress();
		}
	}
	
	public Process getLocalProcess() {
		return new Process(getLocalAddress(), getLocalPort());
	}
	
	public Process getLatestSender() {
		return latestSender;
	}
	
	@Override
	public void send(String message) throws IOException {
		this.send(message.getBytes());
	}
	
	@Override
	public byte[] receive() throws IOException{
		byte[] buffer = new byte[1000];
		DatagramPacket request = new DatagramPacket(buffer,buffer.length);
		this.socketReceive(request);
		this.latestSender = new Process(request.getAddress(), request.getPort());
		return request.getData();
	}
	
	@Override
	public String listen() throws IOException{
		
		return new String(this.receive()).trim();
	}

	public void deltaLatestPort(int i) {
		this.latestSender = new Process(this.latestSender.address, this.latestSender.port + i);
		
	}
	
}

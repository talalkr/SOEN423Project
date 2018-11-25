package udp_bridge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Unicast extends UDPState {
	
	private Process remote;
	
	
	public Unicast(Process remote) throws SocketException {
		super();
		this.remote = remote;
	}
	
	public Unicast(String remoteAddress, int port) throws SocketException, UnknownHostException {
		this(new Process(remoteAddress, port));
	}
	
	public Unicast(int port) throws SocketException, UnknownHostException {
		this("localhost",port);
	}
	
	public Unicast(int localPort, int remotePort) throws SocketException, UnknownHostException {
		this(localPort, "localhost", remotePort);
	}
	
	
	public Unicast(int localPort, String remoteAddress, int remotePort) throws SocketException, UnknownHostException {
		this(localPort, new Process(remoteAddress, remotePort));
	}
	
	public Unicast(int localPort, Process process) throws SocketException {
		super(localPort);
		this.remote = process;
	}
	
	
	public void setRemote(Process process) {
		this.remote = process;
	}
	
	public void setAddress(byte[] address) throws UnknownHostException {
		InetAddress a = InetAddress.getByAddress(address);
		this.remote = new Process(a, this.remote.port);
	}
	
	public void setRemote(String address,int port) throws UnknownHostException {
		this.setRemote(new Process(address, port));
	}
	
	public void setAddress(String address) throws UnknownHostException {
		this.setRemote(new Process(address, this.remote.port));
	}
	
	public void setPort(int port) {
		this.setRemote(new Process(this.remote.address, port));
	}

	@Override
	public void send(byte[] message) throws IOException {

		DatagramPacket reply = new DatagramPacket(message, message.length, remote.address, remote.port);
		this.socketSend(reply);
	}

	@Override
	public void changeRemote(Process... processes) {
		this.remote = processes[0];
		
	}

}

package udp_bridge;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Multicast extends UDPState {
	

	private Process[] remotes;

	public Multicast(Process ...remotes) throws SocketException {
		super();
		this.remotes = remotes;
	}
	
	public Multicast(int port, Process ...remotes) throws SocketException {
		super(port);
		this.remotes = remotes;
	}
	
	
	public Multicast(int[] remotePorts) throws SocketException{
		super();
		
		initAddresses(remotePorts);
	}
	
	public Multicast(int localPort, int...remotePorts) throws SocketException {
		super(localPort);
		this.initAddresses(remotePorts);
	}
	//TODO support varying addresses
	
	private void initAddresses(int[] ports) {
		this.remotes = new Process[ports.length];
		for(int i = 0; i< remotes.length; i++) {
			try {
				remotes[i] = new Process("localhost", ports[i]);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private void initAdresses(String[] addresses, int[] ports) throws UnknownHostException {
		this.remotes = new Process[addresses.length];
		
		for(int i = 0; i < addresses.length || i< addresses.length; i++) {
			remotes[i] =new Process(addresses[i], ports[i]);
		}
	}
	
	@Override
	public void changeRemote(Process...processes) {
		this.remotes = processes;
	}
	
	public void setRemotes(int...ports) {
		this.initAddresses(ports);
	}
	
	
	@Override
	public void send(byte[] message) throws IOException {
		
		for(int i = 0; i < this.remotes.length; i++) {
		
			DatagramPacket request = new DatagramPacket(message, message.length, this.remotes[i].address, this.remotes[i].port);
			this.socketSend(request);
			
		}

	}

}

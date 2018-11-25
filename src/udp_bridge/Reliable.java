package udp_bridge;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;



public class Reliable implements UDP{
	
	private UDP udp;
	
	private Process process;
	private int s = 0;
	/**
	 * key is process# of members and value is their latest delivered sequence number
	 *  Types may change in the future
	 *  for now, we are using the port numbers as process id
	 */
	private HashMap<Process, Integer> r = new HashMap<Process,Integer>(10);
	/**
	 * holds history of sent messages, index should correspond to seq number
	 * it's important to note that the index of each messages will be
	 * s-1, but that's perfect for retrieving messages, because a Missed message 
	 * in another process will have R = s-1, which will point to the right message
	 */
	private ArrayList<Message> history = new ArrayList<Message>();
	
	/**
	 * holds "future" messages, waiting for current one
	 */
	private LinkedList<Message> holdback = new LinkedList<Message>();
	
	/**
	 * Missed state, for waiting current message in case of miss
	 */
	private boolean missed = false;
	
	/**
	 * state of UDP socket, so no more than a single thread is listening for a UDP request
	 */
	private boolean listening = false;
	
	private NegativeAckServer negativeAck;
	
	
	
	
	
	
	public Reliable(int localport, int ...remoteports) throws SocketException {
		this(new Multicast(localport, remoteports));
	}
	
	
	public Reliable(int localport, int remoteport) throws SocketException, UnknownHostException {
		this(new Unicast(localport,remoteport));
	}
	
	
	public Reliable(UDPState udp) {
		this.process = udp.getLocalProcess();
		
		this.udp = udp;
		
		try {
			this.negativeAck = new NegativeAckServer();
			negativeAck.start();
		} catch (SocketException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	/**
	 * Accepts an unstable UDP for testing purposes
	 * @param udp
	 */
	public Reliable(Unreliable udp) {
		this.process = udp.getLocalProcess();//TODO keep track of this here
		this.udp = udp;
		
		try {
			this.negativeAck = new NegativeAckServer();
			negativeAck.start();
		} catch (SocketException | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public int getSequenceNumber() {
		return s;
	}
	
	@Override
	public void send(String message) throws IOException {
		this.send(message.getBytes());
	}
	
	
	/**
	 * Sends message to all members of the group wit piggybacked value of sequence number
	 */
	@Override
	public void send(byte[] message) throws IOException {
		
		s++;//new sequence
		Message m = new Message(message, s, process);
		history.add(m);
		message = m.getBytes();
		
		this.udp.send(message);
	}

	@Override
	public String listen() throws IOException {
		
		return new String(receive());
	}

	/**
	 * Delivers a message received from the group
	 * If the piggybacked sequence number matches (S = Rq+1) with the latest sequence number received from that process
	 * the message is forwarded to the client
	 * 
	 * If the piggy backed sequence number is smaller than the latest sequence number, it waits for delivery of next message
	 * before forwarding the value to the client
	 * 
	 * If the piggy backed sequence number is larger than the latest sequence number (S > Rq +1), the the process has missed
	 * a message and has to request it from the sender. This request is handled via a NegativeAcknowledgement server, which promises
	 * a new reply, hence byte[] missedMessage = this.receiveData(). Upon reception of the new reply, the message is forwarded
	 * to the client and exists the 'MISSED' state
	 * 
	 */
	@Override
	public byte[] receive() throws IOException {
		Message m = deliver();
		
		this.r.putIfAbsent(m.process, 0);//init process sequence number
		int rq = this.r.get(m.process);
				
		if(m.s == (rq+1)) {
			this.missed = false;
			System.out.println(this.process.port + " SERVER: OKAY");
			this.r.put(m.process, rq+1);
		}else if(m.s <(rq +1)) {
			System.out.println(this.process.port + " SERVER: REJECTED");
			//if already received, wait for next message?
			return receive();
		}else {
			System.out.println(this.process.port + " SERVER: MISSED");
			holdback.add(m);
			//negative acknowledgement
			this.missed = true;
			negativeAck.negativeAck(m.process, rq);
			
			byte[] missedMessage = this.receive();
			
			this.missed = false;
			return missedMessage;
		}
		
		return m.message;
	}
	
	
	/**
	 * The delivery system.
	 * If the process in a 'MISSED' state, then it has to wait for a reply from the UDP socket. Then, that reply being an earlier
	 * request, it is placed in front of the queue.
	 * 
	 * If there are messages in the holdback queue, and the process is not in a missed state, the oldest message is delivered without
	 * waiting for the UDP socket message.
	 * 
	 * In all cases, the UDP socket will place the message it reveives in the holdback queue and the method will deliver the 
	 * oldest message (first in queue);
	 * @return
	 */
	private Message deliver(){
		
		/**
		 * If socket is already listening, there's no need for a new thread
		 */
		if(!this.listening) {
			this.listening = true;
			new Thread() {
				public void run() {
					try {
						
						Message m = Message.fromBytes(udp.receive());
						
						if(missed) {//successfully retrieved;
							holdback.addFirst(m);
							missed = false;
							((UDPState)udp).deltaLatestPort((-1)*NegativeAckServer.DELTA);
						}else {
							holdback.add(m);
						}
						
					}catch(IOException e) {
						e.printStackTrace();
						//TODO check this out
						System.exit(0);
					}
					
					listening = false;
					
				}
			}.start();
		}
		
		int size = this.holdback.size();
		while(size == 0 || missed) {
			//busy wait
			synchronized(this.holdback) {
				size = this.holdback.size();
			}
		}
		
		return holdback.remove();
	}
	
	public void changeRemote(UDPState udp) {
		this.udp = udp;
	}
	
	public void changeRemote(Process ...processes) {
		this.udp.changeRemote(processes);
	}
	
	public Process getLastSender() {
		return ((UDPState)udp).getLatestSender();
	}
	
	/**
	 * NegativeAckServer is used to send out and listen for negative acknowledgements
	 *  from members of the group 
	 * @author CharlesPhilippe
	 *
	 */
	private class NegativeAckServer extends Thread{
		private static final int DELTA = 100;
		
		private Unicast acknowledge;
		
		private NegativeAckServer() throws SocketException, UnknownHostException {
		
		
			this.acknowledge = new Unicast(process.port + DELTA,0);//needs to set port before
		}
		
		/**
		 * Listens for a negative acknowledgement
		 * if one is received and the requested message was sent by this process (process == p)
		 * then, the process will find the message in its history and send it to the requesting member of the group (q)
		 * @throws IOException
		 */
		private void retrieve() throws IOException {
			
			Message m = Message.fromBytes(this.acknowledge.receive());

			//is sender
			byte[] message = history.get(m.s).getBytes();
			Process sender = new Process(acknowledge.getLatestSender().address, m.process.port);
			this.acknowledge.setRemote(sender);
			this.acknowledge.send(message);
			
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					retrieve();
				} catch (IOException e) {
				
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Sends out negative acknowledgment to all members of the group
		 * TODO check if we need to send to all members of if we are able to know where to send the ack
		 * I know that for now, we could send directly to the sender, but I'm not sure if we'll always know that
		 * @param q
		 * @param seq
		 * @throws IOException
		 */
		private void negativeAck(Process q, int seq) throws IOException {
			//System.out.println(q.address.getHostAddress());
			byte[] message = (new Message("retrieve".getBytes(),seq, process)).getBytes();
			this.acknowledge.setRemote(new Process(q.address, q.port + DELTA));
			this.acknowledge.send(message);
			
		}
		
	}
	

}

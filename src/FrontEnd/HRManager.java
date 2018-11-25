package FrontEnd;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.HashMap;
import java.util.LinkedList;

import org.omg.CORBA.ORB;
import org.omg.CORBA.StringHolder;



import HRSystem.HRInterfacePOA;

import HRSystem.ProjectHolder;
import udp_bridge.Reliable;
import udp_bridge.UDP;


public class HRManager extends HRInterfacePOA implements Runnable{
	private String location;
	private int seqPort = 6666; //Sequencer's Port Number
	private ORB orb;
	private int replicas = 0; //check for the three replicas!
	private UDP udp;
	private UDP replicaManagers;
	
	HashMap<String, LinkedList<String>> Responses = new HashMap<String, LinkedList<String>>();
	
	public void setORB(ORB orb_val) {
		orb = orb_val; 
	 }
	
	public HRManager(String location, Reliable udp, Reliable rms) {
		this.location = location; 
		/**
		 * 1- Receives from the replicas
 		 * 2- Sends to sequencer
		 * can be Unicast
		 */
		this.udp = udp;
		/**
		 * Receives nothing
		 * Sends to RMS
		 * has to be multicast
		 */
		this.replicaManagers= rms;
	}

	public void run() {
		try {
			UDPServer();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void UDPServer() {
		
		try {
			while(true) {
				String data = new String(this.udp.listen());//receive string
				String[] spli = data.split(";");
				String id = spli[0];

				synchronized (Responses) {
					Responses.get(id).add(data);
					System.out.println("Received From Server: "+data);
				
				}
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}

	public String UDPClient(String message) throws Exception{
		
		try {
	        //Send
			this.udp.send(message);//send to sequencer

			//Add to Map
			String[] spli = message.split(";");
			String id = spli[1];
			synchronized (Responses){
				if(Responses.get(id) == null) {
					Responses.put(id, new LinkedList<String>());
				}
			}
			
			int size = 0;
			while(size < 3) {
				synchronized (Responses){
					size = Responses.get(id).size();
				}
			}

			System.out.println("Call CheckResponses");
			String result = CheckResponses(id);
			System.out.println("Result: "+ result);
			return result;
			
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		
		return "FE UDP Server Ended";
	}
	
	@Override
	public String createMRecord(String managerID, String firstName, String lastName, int employeeID, StringHolder mailID, ProjectHolder project, String location) {
		String message = "1"
				+";"+managerID
				+";"+firstName
				+";"+lastName
				+";"+employeeID
				+";"+mailID
				+";"+project.value.getProjectID()
				+";"+project.value.getClientName()
				+";"+project.value.getProjectName()
				+";"+location;
		
		String result = "";

		try {
			result = UDPClient(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Manager Created --> " +result;
	}
	
	@Override
	public String createERecord(String managerID, String firstName, String lastName, int employeeID,StringHolder mailID, StringHolder projectID) {
		String message = "2"+";"+managerID
				+";"+firstName
				+";"+lastName
				+";"+employeeID
				+";"+mailID
				+";"+projectID;
		String result="Arb Value";
		try {
			result = UDPClient(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	@Override
	public String getRecordCounts(String managerID) {
		String message = "3"+";"+managerID;
		String result="Arb Value";
		try {
			result = UDPClient(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String EditRecord(String managerID, String recordID, String fieldName, StringHolder newValue) {
		String message = "4"
				+";"+managerID
				+";"+recordID
				+";"+fieldName
				+";"+newValue;
	String result="Arb Value";
	try {
		result = UDPClient(message);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return result;
	}
	
	@Override
	public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
		String message = "5"
				+";"+managerID
				+";"+recordID
				+";"+remoteCenterServerName;
		String result="Arb Value";
		try {
			result = UDPClient(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Non-Byzantine:
		Check if Responses match Status "Success"
		Check if One response fails and notifies RM
	 * Crash:
	   	Check which Replica didn't reply to FE, Notifies RM
	 */
	public String CheckResponses(String id) {
		int size = Responses.get(id).size();
		//NON BYZANTINE CHECKER
		if(size == 3) {
			int fail = 0; //Find the reply that failed then return success b/c other replies succeeded
			int recCountFail = 0;//Find the record counts that doesn't match and Notify RMs
			String replicaFailedName  = ""; //Find the record counts that doesn't match get its name
			
			//For first reply
			String[] item1 = Responses.get(id).get(0).split(";");
			String status = item1[1];
			String replica1 = item1[2];
			
			//Check if first response has failed
			if(status.equals("Success")) {
				
			}else if(status.equals("Fail")){
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyIncorrectResult(replica1);
				System.out.println("RM Notified: " + replica1 + " Fail Response");
				fail++;
			}else {
				//Get Record Counts
				status = ReOrder(status);
//				System.out.println(replica1 + " " +status);
			}
				
			//Iterator that handles other 2 replies
			for (int j = 1; j < Responses.get(id).size(); j++) {
				int nextFails = 0;//Temporary Fail Counter, used to update the 'fail' counter appropriately
	
				String[] item2 = Responses.get(id).get(j).split(";");
				String nextStatus = item2[1];
				String nextReplica = item2[2];
				nextReplica = nextReplica.trim();
			
				//Only gets done if status starts with CA, US, or UK
				nextStatus = ReOrder(nextStatus);
//				System.out.println(nextReplica + " " +nextStatus);
				
				//Check Successes
				if(nextStatus.equals("Success")) {
					
				}
				//Failed
				else if(nextStatus.equals("Fail")){
					nextFails++;
				}else if(nextStatus.equals(status)){
					//For GetRecordCounts
				}else if(!nextStatus.equals(status)) {
					//Record Counts Failed
					//In the case where nextStatus is the issue
					//Save it in a variable
					replicaFailedName = nextReplica;
					recCountFail++;
				}else{
					//In the case where both 'nextStatus' are not equal to the original status variable
					recCountFail++;
				}
			
				if(nextFails > 0) {
					///Notify All RMs of NextReplica's Incorrect Result
//					NotifyIncorrectResult(nextReplica);
					System.out.println("RM Notified: " + nextReplica);
					fail++;
				}
				
			}
			
			if(recCountFail == 2) {
				//This means the variable 'replica1' has the wrong record count
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyIncorrectResult(replica1);
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replica1);
			}
			if(recCountFail == 1 && replicaFailedName.equals("Replica3")){
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyIncorrectResult(replicaFailedName);
			}
			if(recCountFail == 1 && replicaFailedName.equals("Replica2")){
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyIncorrectResult(replicaFailedName);
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
			}
			if(recCountFail == 1 && replicaFailedName.equals("Replica1")){
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyIncorrectResult(replicaFailedName);
			}
			
			if(fail >= 2) {
				return "Fail";
			}
			
			Responses.get(id).clear();
		}
		else if(size  == 2) {
			//CRASH
			
			String[] item1 = Responses.get(id).get(0).split(";");
			String replica1 = item1[3];
			
			String[] item2 = Responses.get(id).get(1).split(";");
			String nextReplica = item2[3];
		

			if((replica1.equals("Replica1") && nextReplica.equals("Replica2")) || 
					(replica1.equals("Replica2") && nextReplica.equals("Replica1"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyCrash("Replica3");
				return "Replica3 Failed";
			}
			if((replica1.equals("Replica1") && nextReplica.equals("Replica3")) || 
					(replica1.equals("Replica3") && nextReplica.equals("Replica1"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyCrash("Replica2");
				return "Replica2 Failed";
			}
			if((replica1.equals("Replica2") && nextReplica.equals("Replica3")) || 
					(replica1.equals("Replica3") && nextReplica.equals("Replica2"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyCrash("Replica1");
				return "Replica1 Failed";
			}
				
		}
		
		return "Success";
	}

	//Reorders GetRecordCount Strings after Received from Replicas.
	public static String ReOrder(String status) {
		if(status.startsWith("CA")) {
			//CA US UK order
		}
		if(status.startsWith("UK")) {
			//CA US UK order
			String uk = status.substring(0,4);
			String us = status.substring(4,10);
			String ca = status.substring(10);
			status = ca + us + uk;
		}
		if(status.startsWith("US")) {
			//CA US UK order
			String us = status.substring(0,4);
			String uk = status.substring(4,9);
			String ca = status.substring(10);
			status = ca + " " + us + uk;
		}
		System.out.println(status);
		return status;
	}
	
	//Multicast To RMs
	public void NotifyIncorrectResult(String replicaName) {	
		try {
			while(true) {
				String msg = "Incorrect Result "+replicaName;
				
		        //Send to 3 replicas
				this.replicaManagers.send(msg);
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	public void NotifyCrash(String replicaName) {
		
		try {
			while(true) {
				String msg = "Potential Crash "+replicaName;
		        //Send to 3 replicas
				this.replicaManagers.send(msg);
			}
		}catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	
}
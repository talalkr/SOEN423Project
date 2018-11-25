package Servers;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import udp_bridge.Reliable;
import udp_bridge.UDP;
import udp_bridge.Unicast;
import udp_bridge.Process;

public class Tester {
	static HashMap<String, LinkedList<String>> Responses = new HashMap<String, LinkedList<String>>();
	static boolean doneOnce = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
//			UDP p1 = new Reliable(new Unicast(3000, new Process("TUROK.ENCS.concordia.ca",1055)));
//			p1.send("MY NAME IS MY NAME");
				
//			System.out.println(p1.listen());

			String id = "CA1234";
			Responses.put(id, new LinkedList<String>()); //Add a new arraylist with that key
			
//			String reply = "CA1234;1;Success;Replica1";
//			String reply2 = "CA1234;1;Success;Replica2";
//			String reply3 = "CA1234;1;Success;Replica3";
			
//			String reply = "CA1234;1;Success;Replica1";
//			String reply2 = "CA1234;1;Success;Replica2";
//			String reply3 = "CA1234;1;Fail;Replica3";
			
			String reply = "CA1234;1;CA 1 US 2 UK 4;Replica1";
			String reply2 = "CA1234;1;UK 4 US 2 CA 1;Replica2";
			String reply3 = "CA1234;1;US 2 UK 4 CA 1;Replica3";
			
			Responses.get(id).add(reply);
			Responses.get(id).add(reply2);
			Responses.get(id).add(reply3);
			
			String result = CheckResponses(id);
			System.out.println(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String CheckResponses(String id) {
		int size = Responses.get(id).size();
		
		//NON BYZANTINE CHECKER
		if(size == 3) {
			int fail = 0; //Find the reply that failed then return success b/c other replies succeeded
			int recCountFail = 0;//Find the record counts that doesn't match and Notify RMs
			String replicaFailedName  = ""; //Find the record counts that doesn't match get its name
			
			//For first reply
			String[] item1 = Responses.get(id).get(0).split(";");
			String seqNum1 = item1[1];
			String status = item1[2];
			String replica1 = item1[3];
			
			//Check if first response has failed
			if(status.equals("Success")) {
				
			}else if(status.equals("Fail")){
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM(replica1);
				System.out.println("RM Notified: " + replica1 + " Fail Response");
			}else {
				//Get Record Counts
				status = ReOrder(status);
//				System.out.println(replica1 + " " +status);
			}
				
			//Iterator that handles other 2 replies
			for (int j = 1; j < Responses.get(id).size(); j++) {
				String[] item2 = Responses.get(id).get(j).split(";");
				String nextSeq = item2[1];
				String nextStatus = item2[2];
				String nextReplica = item2[3];
			
				//Only gets done if status starts with CA, US, or UK
				nextStatus = ReOrder(nextStatus);
//				System.out.println(nextReplica + " " +nextStatus);
				
				//Check Sequence Number
				if(seqNum1.equals(nextSeq)) {
					//Check Successes
					if(nextStatus.equals("Success")) {
						
					}
					//Failed
					else if(nextStatus.equals("Fail")){
						fail++;
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
				}
			
				if(fail > 0) {
					///Notify All RMs of NextReplica's Incorrect Result
//					NotifyRM(nextReplica);
					System.out.println("RM Notified: " + nextReplica + " " + nextStatus+ " Response");
				}
				
			}
			
			if(recCountFail == 2) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM(replica1);
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replica1);
			}else if(recCountFail == 1 && replicaFailedName.equals("Replica3")){
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM(replicaFailedName);
			}else if(recCountFail == 1 && replicaFailedName.equals("Replica2")){
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM(replicaFailedName);
			}else if(recCountFail == 1 && replicaFailedName.equals("Replica1")){
				System.out.println("DIFFERENT RECORD COUNT RESULT BY: " + replicaFailedName);
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM(replicaFailedName);
			}
			
			
			Responses.get(id).clear();
		}
		else if(size  == 2) {
			//For first reply
			String[] item1 = Responses.get(id).get(0).split(";");
			String replica1 = item1[3];
			
			String[] item2 = Responses.get(id).get(1).split(";");
			String nextReplica = item2[3];
		

			if((replica1.equals("Replica1") && nextReplica.equals("Replica2")) || 
					(replica1.equals("Replica2") && nextReplica.equals("Replica1"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM("Replica3");
				return "Replica3 Failed";
			}
			if((replica1.equals("Replica1") && nextReplica.equals("Replica3")) || 
					(replica1.equals("Replica3") && nextReplica.equals("Replica1"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM("Replica2");
				return "Replica2 Failed";
			}
			if((replica1.equals("Replica2") && nextReplica.equals("Replica3")) || 
					(replica1.equals("Replica3") && nextReplica.equals("Replica2"))) {
				//Notify All RMs of Replica1's Incorrect Result
//				NotifyRM("Replica2");
				return "Replica1 Failed";
			}
				
		}
		
		return "Success";
	}

	private static String ReOrder(String status) {
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
		return status;
	}
}

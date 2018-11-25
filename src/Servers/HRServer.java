package Servers;
import HRSystem.*;
import Sequencer.Seq;
import udp_bridge.Multicast;
import udp_bridge.Reliable;
import udp_bridge.UDP;

import org.omg.CosNaming.*; // will use for Naming Service 
import org.omg.CosNaming.NamingContextPackage.*; //contains special exceptions thrown by the naming service 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

import org.omg.CORBA.*;
import org.omg.PortableServer.*; // Classes required for the Portable server inheritance model
import org.omg.PortableServer.POA;

import FrontEnd.HRManager;

public class HRServer implements Runnable {
	
   public static void main(String args[]) {
	   try{	   
	//----------------------------------------FE---------------------------------------------
		  Reliable udp = new Reliable(3000, 1111); //3000 FE port, sequencer is 1111
		  Reliable rms = new Reliable(2000, 9000); //2000 To notify replicas
		   
	      // create and initialize the ORB
	      ORB orb = ORB.init(args, null);
	      // get reference to rootpoa & activate the POAManager (It allows an object implementation to function with different ORBs, hence the word portable)
	      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
	      rootpoa.the_POAManager().activate();
	      // create servant and register it with the ORB
	      HRManager exportedObj = new HRManager("FE", udp, rms);
	      exportedObj.setORB(orb);
	      // get object reference from the servant
	      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(exportedObj);
	     
	      HRInterface href = HRInterfaceHelper.narrow(ref);
	      // get the root naming context
	      // NameService invokes the name service
	      org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	      // Use NamingContextExt which is part of the Interoperable
	      // Naming Service (INS) specification.
	      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	      // bind the Object Reference in Naming
	      String name = "FE";
	      NameComponent path[] = ncRef.to_name( name );
	      ncRef.rebind(path, href);
	      System.out.println("FE Server ready and waiting ...");
	      
//-----------------------------------------------------------------------------------------------------
	      
	      Thread FE = new Thread(exportedObj);
		  FE.start();
		   
		  Sequencer Seq = new Sequencer(1111, 4000, 5000, 6000 ); //3600 and 3700 are fake replicas  
		  Seq.start();
		  
		  //REPLICA1
		  CenterServer csCA = new CenterServer("CA", 4001);
		  CenterServer csUK = new CenterServer("US", 4002);
		  CenterServer csUS = new CenterServer("UK", 4003);
		  Reliable CSR1 = new Reliable(4000, 3000); //3500 is CenterServer resolver port
		  ReplicaResolver rr1 = new ReplicaResolver("Replica1",CSR1,csCA, csUK, csUS);
		  Thread csr = new Thread(rr1);
		  csr.start();
		  
		  Thread ca = new Thread(csCA);
		  Thread uk = new Thread(csUK);
		  Thread us = new Thread(csUS);
		  ca.start();
		  uk.start();
		  us.start();
		  
		  //REPLICA2
		  CenterServer csCA2 = new CenterServer("CA", 5001);
		  CenterServer csUK2 = new CenterServer("US", 5002);
		  CenterServer csUS2 = new CenterServer("UK", 5003);
		  Reliable CSR2 = new Reliable(5000, 3000); //3600 is CenterServer resolver port
		  ReplicaResolver rr2 = new ReplicaResolver("Replica2",CSR2,csCA2, csUK2, csUS2);
		  Thread csr2 = new Thread(rr2);
		  csr2.start();
		  
		  Thread ca2 = new Thread(csCA2);
		  Thread uk2 = new Thread(csUK2);
		  Thread us2 = new Thread(csUS2);
		  ca2.start();
		  uk2.start();
		  us2.start();
		  
		  //REPLICA3
		  CenterServer csCA3 = new CenterServer("CA", 6001);
		  CenterServer csUK3 = new CenterServer("US", 6002);
		  CenterServer csUS3 = new CenterServer("UK", 6003);
		  Reliable CSR3 = new Reliable(6000, 3000); //3700 is CenterServer resolver port,
		  ReplicaResolver rr3 = new ReplicaResolver("Replica3",CSR3,csCA3, csUK3, csUS3);
		  Thread csr3 = new Thread(rr3);
		  csr3.start();
		  
		  Thread ca3 = new Thread(csCA3);
		  Thread uk3 = new Thread(csUK3);
		  Thread us3 = new Thread(csUS3);
		  ca3.start();
		  uk3.start();
		  us3.start();
		  
		
		  ReplicaManager rm = new ReplicaManager();
		  Thread ReplicaMan = new Thread(rm);
		  ReplicaMan.start();
		  
//		  Reliable udpCA = new Reliable(4000, 3000);
//		  CenterServer CS1 = new CenterServer(udpCA);
//		  Thread cs1 = new Thread(CS1);
//		  cs1.start();
	      // wait for invocations from clients
	      orb.run();
	    
	    } catch (Exception e) {
	    	System.err.println("ERROR: " + e);
	        e.printStackTrace(System.out);
	    }
}


	public void run() {	} 
 }
package Records;

import java.io.Serializable;

import org.omg.CORBA.StringHolder;

public abstract class Record implements Serializable{
	
	public String firstName;
	public String lastName;
	public int employeeID;
	public String mailID;
	public String recordID;
	public String id;
	
	public Record() {
		
	}
	
	public Record(String id, String firstName, String lastName, int employeeID, String mailID) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.employeeID = employeeID;
		this.mailID = mailID;
	}
	
	public String getFirstName() {
		return this.getFirstName();
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.getLastName();
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getEmployeeID() {
		return this.getEmployeeID();
	}

	public void setEmployeeID(int employeeID) {
		this.employeeID = employeeID;
	}

	public String getMailID() {
		return this.getMailID();
	}

	public void setMailID(String mailID) {
		this.mailID = mailID;
	}
	
}

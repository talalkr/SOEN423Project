package Records;

import java.io.Serializable;

public class Projects implements Serializable {
	private String projectID;
	private String clientName;
	private String projectName;
	
	public Projects() {
		
	}
	
	public Projects(String projectID, String clientName, String projectName) {
		this.projectID = projectID;
		this.clientName = clientName;
		this.projectName = projectName;
	}
	public String getProjectID() {
		return projectID;
	}
	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}
	public String getClientName() {
		return clientName;
	}
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	
}

package ee.adit.pojo;

import java.util.Date;
import java.util.List;

public class Activity {
	private Date time;
	private String type;
	private List<ActivityActor> actors;
	private List<ActivitySubject> subjects;
	private String description;
	private String application;
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public List<ActivityActor> getActors() {
		return actors;
	}
	public void setActors(List<ActivityActor> actors) {
		this.actors = actors;
	}
	
	public List<ActivitySubject> getSubjects() {
		return subjects;
	}
	public void setSubjects(List<ActivitySubject> subjects) {
		this.subjects = subjects;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
}

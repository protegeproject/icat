package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class NotesData implements Serializable {
	private String author = "(none)";
	private String body = "";
	private String date = "";
	private String type = "";
	private String subject = "";	
	private EntityData entity;	
	private ArrayList<NotesData> replies = new ArrayList<NotesData>();	
	private EntityData annotatedEntity;
	
	public NotesData() {
	}

	//TODO: add annotation
	public NotesData(String author, String body, String date, String type, String subject,
					EntityData entity, EntityData annotatedEntity, ArrayList<NotesData> replies) {
		this.author = author;
		this.body = body;
		this.date = date;
		this.type = type;
		this.entity = entity;
		this.replies = replies;
		this.subject = subject;
		this.annotatedEntity = annotatedEntity;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	//TODO: add annotation
	public ArrayList<NotesData> getReplies() {
		return replies;
	}

	//TODO: add annotation
	public void setReplies(ArrayList<NotesData> replies) {
		this.replies = replies;
	}
	
	public void addReply(NotesData reply) {
		this.replies.add(reply);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public EntityData getAnnotatedEntity() {
		return annotatedEntity;
	}

	public void setAnnotatedEntity(EntityData annotatedEntity) {
		this.annotatedEntity = annotatedEntity;
	}
		
	public EntityData getEntity() {
		return entity;
	}

	public void setEntity(EntityData entity) {
		this.entity = entity;
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(author);
		buffer.append(" (");
		buffer.append(date);
		buffer.append("), type: ");
		buffer.append(type);
		buffer.append(", body: ");
		buffer.append(body);
		return buffer.toString();
	}

}

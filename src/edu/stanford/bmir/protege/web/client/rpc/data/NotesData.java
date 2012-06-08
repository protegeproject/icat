package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class NotesData implements Serializable {
    private static final long serialVersionUID = 1499115919382949498L;

    private String author = null;
    private String body = null;
    private String creationDate = null;
    private Date latestUpdate = null;

    private String type = null;
    private String subject = null;
    private EntityData entity = null;
    private List<NotesData> replies = null;
    private EntityData annotatedEntity = null;
    private boolean isArchived = false;

    private int numOfReplies = 0;

    public NotesData() {
    }

    // TODO: add annotation
	public NotesData(String author, String body, String creationDate, String type,
	        String subject, EntityData entity, EntityData annotatedEntity,
	        ArrayList<NotesData> replies) {
        this.author = author;
        this.body = body;
        this.creationDate = creationDate;
        this.type = type;
        this.entity = entity;
        this.replies = replies;
        this.subject = subject;
        this.annotatedEntity = annotatedEntity;
        this.latestUpdate = null;
    }

    public int getNumOfReplies() {
        return numOfReplies;
    }

    public void setNumOfReplies(int numOfReplies) {
        this.numOfReplies = numOfReplies;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }

    public void setLatestUpdate(Date d){
	    if(d != null){
	        this.latestUpdate = d;
	    }
	}

    public Date getLatestUpdate() {
        return latestUpdate;
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

    public String getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(String date) {
        this.creationDate = date;
    }

    // TODO: add annotation
    public List<NotesData> getReplies() {
        return replies;
    }

    // TODO: add annotation
    public void setReplies(ArrayList<NotesData> replies) {
        this.replies = replies;
        if(replies != null){
            this.numOfReplies = replies.size();
        }
    }

    public void addReply(NotesData reply) {
        if(this.replies == null){
            this.replies = new ArrayList<NotesData>();
        }
        this.replies.add(reply);
        this.numOfReplies++;
    }

    public void addReply(int index, NotesData reply){
        if(this.replies == null){
            this.replies = new ArrayList<NotesData>();
        }
        if(index >=0 && index < this.replies.size()){
            this.replies.add(index, reply);
        } else {
            this.replies.add(0, reply);
        }
        this.numOfReplies++;
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

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(author);
        buffer.append(" (");
        buffer.append(getCreationDate());
        buffer.append("), type: ");
        buffer.append(type);
        buffer.append(", body: ");
        buffer.append(body);
        return buffer.toString();
    }
}

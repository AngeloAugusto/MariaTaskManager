package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

import java.util.Calendar;
import java.util.Date;

public class SubTask implements Comparable<SubTask> {
    private ObjectId id;
    private String title;
    private Date closeDate;
    private Date createdDate;
    private ObjectId parentTask;

    public SubTask() {
        this.createdDate = new Date();
    }

    public SubTask(String title, ObjectId parentTask) {
        this.title = title;
        this.parentTask = parentTask;
        this.createdDate = new Date();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ObjectId getParentTask() {
        return parentTask;
    }

    public void setParentTask(ObjectId parentTask) {
        this.parentTask = parentTask;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public void setClosed() {
        this.closeDate = Calendar.getInstance().getTime();
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int compareTo(SubTask arg0) {
        return getId().compareTo(arg0.getId());
    }
}

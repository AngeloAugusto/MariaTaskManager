package pt.axxiv.mariatasks.data;

import org.bson.types.ObjectId;

import java.time.MonthDay;

public class Birthday implements Comparable<Birthday>{

    private ObjectId id;
    private String name;
    private MonthDay monthDay;
    private ObjectId section;
    private ObjectId ownerId;

    public Birthday() { }

    public Birthday(String name, MonthDay monthDay, ObjectId section, ObjectId ownerId) {
        this.name = name;
        this.monthDay = monthDay;
        this.section = section;
        this.ownerId = ownerId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MonthDay getMonthDay() {
        return monthDay;
    }

    public String getDateLabel() {
        StringBuilder stringBuilder = new StringBuilder();
        if(this.getMonthDay().getDayOfMonth() < 10){
            stringBuilder.append("0");
        }
        stringBuilder.append(this.getMonthDay().getDayOfMonth());

        stringBuilder.append("/");

        if(this.getMonthDay().getMonthValue() < 10){
            stringBuilder.append("0");
        }
        stringBuilder.append(this.getMonthDay().getMonthValue());

        return stringBuilder.toString();
    }

    public void setMonthDay(MonthDay date) {
        this.monthDay = date;
    }

    public ObjectId getSection() {
        return section;
    }

    public void setSection(ObjectId section) {
        this.section = section;
    }

    public ObjectId getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(ObjectId ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isToday() {
        return this.monthDay != null && this.monthDay.equals(MonthDay.now());
    }

    @Override
    public int compareTo(Birthday arg0) {
        return getId().compareTo(arg0.getId());
    }
}

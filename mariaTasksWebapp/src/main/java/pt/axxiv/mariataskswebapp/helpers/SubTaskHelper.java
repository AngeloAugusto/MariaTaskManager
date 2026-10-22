package pt.axxiv.mariataskswebapp.helpers;

import pt.axxiv.mariatasks.connection.dao.SubTaskDAO;
import pt.axxiv.mariatasks.connection.labels.SubTaskFields;
import pt.axxiv.mariatasks.data.SubTask;

public class SubTaskHelper {
    public static void toggleSubTaskCloseDate(SubTask t) {
        if(t.getCloseDate() != null) {
            t.setCloseDate(null);
        }else {
            t.setClosed();
        }
        new SubTaskDAO().updateValue(t.getId(), SubTaskFields.CLOSE_DATE, t.getCloseDate());
    }
}

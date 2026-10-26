package pt.axxiv.mariataskswebapp.renderer;

import org.zkoss.zhtml.Hr;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.*;
import pt.axxiv.mariatasks.connection.dao.BirthdayDAO;
import pt.axxiv.mariatasks.connection.dao.SubTaskDAO;
import pt.axxiv.mariatasks.connection.labels.BirthdayFields;
import pt.axxiv.mariatasks.data.*;
import pt.axxiv.mariataskswebapp.AppViewModel;
import pt.axxiv.mariataskswebapp.controllers.fragment.FormBirthdayController;
import pt.axxiv.mariataskswebapp.controllers.fragment.FormContainerController;
import pt.axxiv.mariataskswebapp.controllers.fragment.FormTaskController;
import pt.axxiv.mariataskswebapp.controllers.fragment.MainContainerController;
import pt.axxiv.mariataskswebapp.helpers.SubTaskHelper;
import pt.axxiv.mariataskswebapp.helpers.TaskHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class BirthdayListRenderer {

    public static void generateList(Div birthDaySection, AppViewModel viewModel, List<Birthday> birthdays) {
        while (birthDaySection.getFirstChild() != null)
            birthDaySection.removeChild(birthDaySection.getFirstChild());

        if(birthdays == null || birthdays.isEmpty())
            return;

        for (Birthday b : birthdays) {
            // Card container
            Div birthdayCard = new Div();
            birthdayCard.setSclass("birthday-card");

            boolean isToday = b.isToday();
            if (isToday) {
                birthdayCard.setSclass("birthday-card birthday-today");
            }

            // Name
            Label name = new Label(b.getName());
            name.setSclass("birthday-name");
            birthdayCard.appendChild(name);

            // Date
            Label date = new Label(b.getDateLabel());
            date.setSclass("birthday-date");
            birthdayCard.appendChild(date);

            if (isToday) {
                birthdayCard.setSclass("birthday-card birthday-today");

                Div confetti = new Div();
                confetti.setSclass("confetti-container");
                confetti.setParent(birthdayCard);

                for (int i = 0; i < 15; i++) {
                    Div piece = new Div();
                    piece.setSclass("confetti-piece");
                    piece.setStyle(
                            "left: " + (int)(Math.random() * 100) + "%;" +
                                    "animation-delay: " + (Math.random() * 3) + "s;" +
                                    "animation-duration: " + (2 + Math.random() * 2) + "s;" +
                                    "background: " + randomConfettiColor() + ";" +
                                    "width: " + (4 + Math.random() * 4) + "px;" +
                                    "height: " + (6 + Math.random() * 6) + "px;"
                    );
                    piece.setParent(confetti);
                }
            }

            birthdayCard.addEventListener("onClick", e -> onClickOnRow(b, viewModel));

            birthDaySection.appendChild(birthdayCard);
        }
    }

    private static String randomConfettiColor() {
        String[] colors = {"#ffd700", "#ff6b9d", "#4caf50", "#42a5f5", "#ff5252", "#ff9800"};
        return colors[(int)(Math.random() * colors.length)];
    }

    public static void onClickOnRow(Birthday b, AppViewModel viewModel) {
        if(viewModel.getEditingBirthday() == null || viewModel.getEditingBirthday() != b) {
            viewModel.setEditingBirthday(b);

            EventQueue<Event> eqOpen = EventQueues.lookup(FormContainerController.OPEN_VIEW, EventQueues.DESKTOP, true);
            eqOpen.publish(new Event("onRefresh", null, null));

        } else if(b == viewModel.getEditingBirthday()) {
            EventQueue<Event> eq = EventQueues.lookup(FormContainerController.CLOSE_VIEW, EventQueues.DESKTOP, true);
            eq.publish(new Event("onRefresh", null, null));
            viewModel.setEditingBirthday(null);
        }else {
            System.out.println("NOW WHAT???");
        }
    }
}

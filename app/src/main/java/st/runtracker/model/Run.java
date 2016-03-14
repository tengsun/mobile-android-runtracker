package st.runtracker.model;

import java.util.Date;

/**
 * Created by tengsun on 3/11/16.
 */
public class Run {

    private long id;
    private Date startDate;

    public Run() {
        id = -1;
        startDate = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getDurationSeconds(long endMills) {
        return (int) ((endMills - startDate.getTime()) / 1000);
    }

    public static String formatDuration(int durationSeconds) {
        int seconds = durationSeconds % 60;
        int minutes = ((durationSeconds - seconds) / 60) % 60;
        int hours = (durationSeconds - seconds - (minutes * 60)) / 3600;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    }

}

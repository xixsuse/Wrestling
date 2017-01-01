package wrestling.model;

import java.io.Serializable;

/**
 *
 * for storing a completed event
 *
 * 
 */
public class EventArchive implements Serializable {

    private final int totalCost;

    private final int gate;

    private final String promotionName;

    private final int date;

    private final int attendance;

    private String summary;

    public EventArchive(String promotionName, final int totalCost, final int gate, final int attendance, int date, String summary) {
        this.gate = gate;
        this.totalCost = totalCost;
        this.promotionName = promotionName;
        this.date = date;
        this.attendance = attendance;
        this.summary = summary;
    }

    public String getSummary() {

        return this.summary;
    }

    @Override
    public String toString() {
        String string = new String();
        string += promotionName + " event, day " + getDate();
        return string;
    }

    /**
     * @return the totalCost
     */
    public int getTotalCost() {
        return totalCost;
    }

    /**
     * @return the gate
     */
    public int getGate() {
        return gate;
    }

    /**
     * @return the date
     */
    public int getDate() {
        return date;
    }
}

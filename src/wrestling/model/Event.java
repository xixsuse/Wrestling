package wrestling.model;

import java.time.LocalDate;
import wrestling.model.interfaces.iEvent;
import wrestling.model.segmentEnum.EventFrequency;

public class Event implements iEvent {

    private final Promotion promotion;

    private LocalDate date;
    private EventType eventType;
    private int cost;
    private int gate;
    private int attendance;
    private int defaultDuration;
    private String name;

//    private Television television;
//    private RecurringEvent eventName;
    public Event(Promotion promotion, LocalDate date, EventType eventType, int cost, int gate, int attendance) {
        this.promotion = promotion;
        this.date = date;
        this.eventType = eventType;
        this.cost = cost;
        this.gate = gate;
        this.attendance = attendance;
    }

    public Event(Promotion promotion, LocalDate date) {
        this.promotion = promotion;
        this.date = date;
    }

    @Override
    public String toString() {
//        String name = "";
//        if (getName() != null) {
//            name = promotion.getShortName() + " " + eventName.getName();
//        } else {
//            name = television == null
//                    ? promotion.getShortName() + " event"
//                    : television.getName();
//        }
//        String name = "";
//        if (name != null) {
//            return promotion.getShortName() + " " + name;
//        } 
//        else {
//            return = television == null
//                    ? promotion.getShortName() + " event"
//                    : television.getName();
//        }
        return promotion.getShortName() + " " + name;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public Promotion getPromotion() {
        return promotion;
    }

    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * @return the gate
     */
    public int getGate() {
        return gate;
    }

    /**
     * @return the attendance
     */
    public int getAttendance() {
        return attendance;
    }

//    /**
//     * @return the television
//     */
//    public Television getTelevision() {
//        return television;
//    }
//
//    /**
//     * @param television the television to set
//     */
//    public void setTelevision(Television television) {
//        this.television = television;
//    }
    /**
     * @param eventType the eventType to set
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * @param gate the gate to set
     */
    public void setGate(int gate) {
        this.gate = gate;
    }

    /**
     * @param attendance the attendance to set
     */
    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    /**
     * @return the eventName
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the eventFrequency
     */
//    public EventFrequency getEventFrequency() {
//        return eventFrequency;
//    }
//
//    /**
//     * @param eventFrequency the eventFrequency to set
//     */
//    public void setEventFrequency(EventFrequency eventFrequency) {
//        this.eventFrequency = eventFrequency;
//    }

    /**
     * @return the defaultDuration
     */
    public int getDefaultDuration() {
        return defaultDuration;
    }

    /**
     * @param defaultDuration the defaultDuration to set
     */
    public void setDefaultDuration(int defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

}

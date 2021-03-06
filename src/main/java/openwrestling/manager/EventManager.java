package openwrestling.manager;

import lombok.Getter;
import openwrestling.database.Database;
import openwrestling.model.gameObjects.Event;
import openwrestling.model.gameObjects.EventTemplate;
import openwrestling.model.gameObjects.Promotion;
import openwrestling.model.gameObjects.Segment;
import openwrestling.model.gameObjects.Worker;
import openwrestling.model.segmentEnum.SegmentType;
import openwrestling.model.utility.ModelUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class EventManager extends GameObjectManager implements Serializable {

    // private List<Event> events;
    // private List<EventTemplate> eventTemplates;
    private Map<Long, EventTemplate> eventTemplateMap;
    private Map<Long, Event> eventMap;

    private final DateManager dateManager;
    private final SegmentManager segmentManager;
    private final ContractManager contractManager;

    private final transient Logger logger = LogManager.getLogger(getClass());

    public EventManager(
            ContractManager contractManager,
            DateManager dateManager,
            SegmentManager segmentManager) {
        //   events = new ArrayList<>();
        //   eventTemplates = new ArrayList<>();
        eventTemplateMap = new HashMap<>();
        eventMap = new HashMap<>();
        this.segmentManager = segmentManager;
        this.contractManager = contractManager;
        this.dateManager = dateManager;
    }

    public List<Event> getEvents() {
        return new ArrayList<>(eventMap.values());
    }

    public List<EventTemplate> getEventTemplates() {
        return new ArrayList<>(eventTemplateMap.values());
    }

    @Override
    public void selectData() {
        List<Event> events = Database.selectAll(Event.class);
        events.forEach(event -> {
            eventMap.put(event.getEventID(), event);
        });
        List<EventTemplate> eventTemplates = Database.selectAll(EventTemplate.class);
        eventTemplates.forEach(eventTemplate -> {
            eventTemplateMap.put(eventTemplate.getEventTemplateID(), eventTemplate);
        });
    }

    public List<EventTemplate> createEventTemplates(List<EventTemplate> eventTemplates) {
        List<EventTemplate> saved = Database.insertList(eventTemplates);
        saved.forEach(savedEvent -> eventTemplateMap.put(savedEvent.getEventTemplateID(), savedEvent));
        return saved;
    }

    public void updateEventTemplates(List<EventTemplate> eventTemplates) {
        Database.updateList(eventTemplates);
        eventTemplates.forEach(updatedEvent -> eventTemplateMap.put(updatedEvent.getEventTemplateID(), updatedEvent));
    }

    public void createEvents(List<Event> events) {
        List<Event> eventsWithSegments = new ArrayList<>();
        List<Event> eventsWithoutSegments = new ArrayList<>();
        List<Segment> segmentsToSave = new ArrayList<>();

        events.forEach(event -> {
            if (CollectionUtils.isNotEmpty(event.getSegments())) {
                eventsWithSegments.add(event);
            } else {
                eventsWithoutSegments.add(event);
            }
        });

        List<Event> savedEventsWithoutSegments = Database.insertList(eventsWithoutSegments);
        savedEventsWithoutSegments.forEach(event -> {
            eventMap.put(event.getEventID(), event);
        });

        eventsWithSegments.forEach(event -> {
            List<Segment> segments = event.getSegments();
            Event savedEvent = Database.insertList(List.of(event)).get(0);
            segments.forEach(segment -> segment.setEvent(savedEvent));
            segmentsToSave.addAll(segments);
            eventMap.put(savedEvent.getEventID(), savedEvent);
        });

        segmentManager.createSegments(segmentsToSave);
        //selectData();
    }

    public void rescheduleEvent(Event event, LocalDate newDate) {
        EventTemplate template = event.getEventTemplate();
        event.setDate(newDate);
        updateFirstAndLastEvents(template);
    }

    public void updateEventName(EventTemplate eventTemplate) {
        for (Event event : getEvents()) {
            if (event.getEventTemplate().equals(eventTemplate)) {
                event.setName(eventTemplate.getName());
            }
        }
    }

    private void updateFirstAndLastEvents(EventTemplate template) {
        if (template != null) {
            Event event = getNextEvent(template,
                    dateManager.today());
            if (event != null) {
                template.setNextDate(getNextEvent(template,
                        dateManager.today()).getDate());
                template.setBookedUntil(getLastEvent(template).getDate());
            } else {
                template.setNextDate(LocalDate.MIN);
                template.setBookedUntil(LocalDate.MIN);
            }

        }
    }

    public void cancelEvent(Event eventToCancel) {
        EventTemplate template = eventToCancel.getEventTemplate();

        eventMap.remove(eventToCancel.getEventID());

        if (template != null) {
            updateFirstAndLastEvents(template);
        }

    }

    public Event getNextEvent(EventTemplate template) {
        for (Event event : getEvents()) {
            if (event.getEventTemplate().equals(template)
                    && event.getDate().equals(template.getNextDate())) {
                return event;
            }
        }

        return null;
    }

    public Event getNextEvent(EventTemplate template, LocalDate startDate) {
        List<Event> templateEvents = new ArrayList<>();
        for (Event event : getEventsForTemplate(template)) {
            if (event.getDate().isAfter(startDate)) {
                templateEvents.add(event);
            }
        }

        templateEvents.sort(
                Comparator.comparing(Event::getDate));
        return templateEvents.isEmpty()
                ? null : templateEvents.get(0);
    }

    public List<Event> getEventsForTemplate(EventTemplate template) {
        List<Event> templateEvents = new ArrayList<>();
        for (Event event : getEvents()) {
            if (event.getEventTemplate().equals(template)) {
                templateEvents.add(event);
            }
        }
        return templateEvents;
    }

    public Event getLastEvent(EventTemplate template) {
        List<Event> templateEvents = getEventsForTemplate(template);
        templateEvents.sort(
                Comparator.comparing(Event::getDate));
        return templateEvents.isEmpty()
                ? null : templateEvents.get(templateEvents.size() - 1);
    }

    public Event getNextEvent(Promotion promotion, LocalDate startDate) {
        Event event = null;
        int futureDaysToSearch = 180;
        LocalDate currentDate = startDate;
        for (int i = 0; i < futureDaysToSearch; i++) {
            event = getEventOnDate(promotion, currentDate);
            if (event != null) {
                break;
            }
            currentDate = LocalDate.from(currentDate).plusDays(1);
        }
        return event;
    }

    public Event getEventOnDate(Promotion promotion, LocalDate date) {
        for (Event event : getEvents()) {
            if (event.getDate().equals(date)
                    && event.getPromotion().equals(promotion)) {
                return event;
            }
        }
        return null;
    }

    public List<Event> getEventsOnDate(LocalDate date) {
        List<Event> eventsOnDate = new ArrayList<>();
        for (Event event : getEvents()) {
            if (event.getDate().equals(date)) {
                eventsOnDate.add(event);
            }
        }
        return eventsOnDate;
    }

    public int eventsAfterDate(Promotion promotion, LocalDate date) {
        int futureEvents = 0;
        futureEvents = getEvents().stream().filter((Event event) -> {
            return event.getPromotion().equals(promotion)
                    && event.getDate().isAfter(date);
        }).map((_item) -> 1).reduce(futureEvents, Integer::sum);
        return futureEvents;
    }

    //this will return a list of all workers currently booked
    //without any duplicates
    //so if a worker is in two different segments he is only on the list
    //one time. useful for cost calculation so we don't pay people
    //twice for the same show
    public List<Worker> allWorkers(List<Segment> segments) {

        List allWorkers = new ArrayList<>();
        for (Segment currentSegment : segments) {
            if (SegmentType.MATCH.equals(currentSegment.getSegmentType())) {
                allWorkers.addAll(segmentManager.getWorkers(currentSegment));
            }
        }

        //this should take the list of workers generated above
        //and convert it to a set, removing duplicates
        Set<Worker> allWorkersSet = new LinkedHashSet<>(allWorkers);
        //convert the set back to a list with no duplicates
        allWorkers = new ArrayList<>(allWorkersSet);

        return allWorkers;
    }

    //dynamic current cost calculation to be called while the player is booking
    public int calculateCost(Event event) {
        int currentCost = 0;

        List<Segment> matches = event.getSegments().stream()
                .filter(segment -> SegmentType.MATCH.equals(segment.getSegmentType()))
                .collect(Collectors.toList());

        for (Worker worker : allWorkers(matches)) {
            currentCost += contractManager.getContract(worker, event.getPromotion()).getAppearanceCost();
        }
        return currentCost;
    }

    //dynamic current cost calculation to be called while the player is booking
    public int calculateCost(List<Segment> segments, Promotion promotion) {
        int currentCost = 0;

        for (Worker worker : allWorkers(segments)) {
            currentCost += contractManager.getContract(worker, promotion).getAppearanceCost();
        }
        return currentCost;
    }

    //gross profit for the event
    public int calculateGate(Event event) {

        int ticketPrice = 0;

        switch (event.getPromotion().getLevel()) {
            case 1:
                ticketPrice += 5;
                break;
            case 2:
                ticketPrice += 10;
                break;
            case 3:
                ticketPrice += 15;
                break;
            case 4:
                ticketPrice += 20;
                break;
            case 5:
                ticketPrice += 35;
                break;
            default:
                break;
        }

        return calculateAttendance(event) * ticketPrice;
    }

    //gross profit for the event
    public int calculateGate(List<Segment> segments, Promotion promotion) {

        int ticketPrice = 0;

        switch (promotion.getLevel()) {
            case 1:
                ticketPrice += 5;
                break;
            case 2:
                ticketPrice += 10;
                break;
            case 3:
                ticketPrice += 15;
                break;
            case 4:
                ticketPrice += 20;
                break;
            case 5:
                ticketPrice += 35;
                break;
            default:
                break;
        }

        return calculateAttendance(segments, promotion) * ticketPrice;
    }

    public int calculateAttendance(Event event) {
        int attendance = 0;

        switch (event.getPromotion().getLevel()) {
            case 1:
                attendance += 20;
                break;
            case 2:
                attendance += 50;
                break;
            case 3:
                attendance += 100;
                break;
            case 4:
                attendance += 250;
                break;
            case 5:
                attendance += 4000;
                break;
            default:
                break;
        }

        //how many workers are draws?
        long draws = event.getSegments().stream()
                .flatMap(segment -> segment.getTeams().stream())
                .flatMap(segmentTeam -> segmentTeam.getWorkers().stream())
                .filter(worker -> worker.getPopularity() > ModelUtils.maxPopularity(event.getPromotion()) - 10)
                .count();

        attendance += RandomUtils.nextInt(event.getPromotion().getLevel(), event.getPromotion().getLevel() * 15) * draws;

        return attendance;
    }

    public int calculateAttendance(List<Segment> segments, Promotion promotion) {
        int attendance = 0;

        switch (promotion.getLevel()) {
            case 1:
                attendance += 20;
                break;
            case 2:
                attendance += 50;
                break;
            case 3:
                attendance += 100;
                break;
            case 4:
                attendance += 250;
                break;
            case 5:
                attendance += 4000;
                break;
            default:
                break;
        }

        //how many workers are draws?
        int draws = 0;
        for (Worker worker : allWorkers(segments)) {

            if (worker.getPopularity() > ModelUtils.maxPopularity(promotion) - 10) {
                draws++;
            }
        }

        attendance += RandomUtils.nextInt(promotion.getLevel(), promotion.getLevel() * 15) * draws;

        return attendance;
    }

    public String generateSummaryString(Event event) {

        StringBuilder sb = new StringBuilder();

        if (event.getDate().isAfter(dateManager.today())) {
            return sb.append("This event is in the future.\n").toString();
        }

        if (event.getDate().equals(dateManager.today())) {
            return sb.append("This event is scheduled for later today.\n").toString();
        }
        if (event != null) {
            List<Segment> segments = segmentManager.getSegments(event);
            for (Segment segment : segments) {
                if (!segment.getWorkers().isEmpty()) {
                    sb.append(segmentManager.getIsolatedSegmentString(segment));
                } else {
                    logger.log(Level.ERROR, "Encountered empty segment when constructing event summary string");
                }

                sb.append("\n");
            }

            sb.append("\n");

            sb.append("Total cost: $").append(event.getCost());
            sb.append("\n");
            sb.append("Attendance: ").append(event.getAttendance());
            sb.append("\n");
            sb.append("Gross profit: $").append(event.getGate());
        } else {
            sb.append("Event information not available.\n");
        }

        return sb.toString();
    }

    public boolean canReschedule(Event event) {
        //return event != null && event.getTelevision() == null;
        return true;
    }

    public List<EventTemplate> getEventTemplates(Promotion promotion) {
        return getEventTemplates().stream()
                .filter(eventTemplate -> promotion.getPromotionID() == eventTemplate.getPromotion().getPromotionID())
                .collect(Collectors.toList());
    }


}

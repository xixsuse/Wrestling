package openwrestling.model.manager;

import openwrestling.TestUtils;
import openwrestling.database.Database;
import openwrestling.manager.BankAccountManager;
import openwrestling.manager.ContractManager;
import openwrestling.manager.DateManager;
import openwrestling.manager.EventManager;
import openwrestling.manager.PromotionManager;
import openwrestling.manager.SegmentManager;
import openwrestling.manager.StableManager;
import openwrestling.manager.TagTeamManager;
import openwrestling.manager.WorkerManager;
import openwrestling.model.factory.PersonFactory;
import openwrestling.model.gameObjects.Event;
import openwrestling.model.gameObjects.EventTemplate;
import openwrestling.model.gameObjects.Promotion;
import openwrestling.model.gameObjects.Segment;
import openwrestling.model.gameObjects.SegmentTeam;
import openwrestling.model.gameObjects.SegmentTemplate;
import openwrestling.model.gameObjects.Worker;
import openwrestling.model.segmentEnum.AngleType;
import openwrestling.model.segmentEnum.MatchFinish;
import openwrestling.model.segmentEnum.MatchRule;
import openwrestling.model.segmentEnum.ResponseType;
import openwrestling.model.segmentEnum.SegmentType;
import openwrestling.model.segmentEnum.ShowType;
import openwrestling.model.segmentEnum.TeamType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SegmentManagerTest {

    private EventManager eventManager;
    private SegmentManager segmentManager;
    private WorkerManager workerManager;
    private PromotionManager promotionManager = new PromotionManager(new BankAccountManager());
    private Promotion promotion;

    @Before
    public void setUp() {
        Database.createNewDatabase("testdb");
        DateManager mockDateManager = mock(DateManager.class);
        when(mockDateManager.today()).thenReturn(LocalDate.now());
        workerManager = new WorkerManager(mock(ContractManager.class));
        segmentManager = new SegmentManager(mock(DateManager.class), mock(TagTeamManager.class), mock(StableManager.class));
        eventManager = new EventManager(mock(ContractManager.class), mockDateManager, segmentManager);
        promotion = promotionManager.createPromotions(List.of(TestUtils.randomPromotion())).get(0);
    }


    @Test
    public void createSegments() {
        Event event = new Event();
        event.setDate(LocalDate.now());
        EventTemplate eventTemplate = eventManager.createEventTemplates(List.of(EventTemplate.builder().build())).get(0);
        event.setEventTemplate(eventTemplate);

        Segment segment = Segment.builder()
                .segmentType(SegmentType.MATCH)
                .matchRule(MatchRule.DEFAULT)
                .matchFinish(MatchFinish.CLEAN)
                .build();
        Worker winnerWorker = workerManager.createWorker(PersonFactory.randomWorker());
        Worker loserWorker = workerManager.createWorker(PersonFactory.randomWorker());
        SegmentTeam winnerTeam = SegmentTeam.builder()
                .workers(List.of(winnerWorker))
                .type(TeamType.WINNER)
                .build();
        SegmentTeam loserTeam = SegmentTeam.builder()
                .workers(List.of(loserWorker))
                .type(TeamType.LOSER)
                .build();
        segment.setTeams(List.of(winnerTeam, loserTeam));

        event.setSegments(List.of(segment));
        event.setPromotion(promotion);
        eventManager.createEvents(List.of(event));
        List<Event> events = eventManager.getEvents();

       List<Segment> segments = segmentManager.getSegments();
        assertThat(segments).hasSize(1);

        Segment savedSegment = segments.get(0);

        assertThat(savedSegment.getSegmentID()).isNotNull().isPositive();
        assertThat(savedSegment.getSegmentType()).isEqualTo(SegmentType.MATCH);
        assertThat(savedSegment.getMatchFinish()).isEqualTo(MatchFinish.CLEAN);
        assertThat(savedSegment.getMatchRule()).isEqualTo(MatchRule.DEFAULT);
        assertThat(savedSegment.getEvent().getEventID()).isEqualTo(events.get(0).getEventID());
        assertThat(savedSegment.getTeams()).hasSize(2);
        SegmentTeam winner = savedSegment.getTeams().stream()
                .filter(segmentTeam -> segmentTeam.getType().equals(TeamType.WINNER))
                .findFirst()
                .orElse(null);
        assertThat(winner).isNotNull();
        assertThat(winner.getSegmentTeamID()).isNotNull().isPositive();
        assertThat(winner.getWorkers()).hasSize(1);
        assertThat(winner.getWorkers().get(0).getWorkerID()).isEqualTo(winnerWorker.getWorkerID());
        assertThat(winner.getWorkers().get(0).getName()).isEqualTo(winnerWorker.getName());
    }

    @Test
    public void createSegments_insertsTemplate() {
        Event event = new Event();
        event.setDate(LocalDate.now());
        event.setName(RandomStringUtils.random(20));
        EventTemplate eventTemplate = eventManager.createEventTemplates(List.of(EventTemplate.builder().build())).get(0);

        event.setEventTemplate(eventTemplate);

        Segment segment = Segment.builder()
                .segmentType(SegmentType.ANGLE)
                .angleType(AngleType.CHALLENGE)
                .showType(ShowType.NEXT_SHOW)
                .build();
        Worker winnerWorker = workerManager.createWorker(PersonFactory.randomWorker());
        Worker loserWorker = workerManager.createWorker(PersonFactory.randomWorker());
        SegmentTeam challengerTeam = SegmentTeam.builder()
                .workers(List.of(winnerWorker))
                .type(TeamType.CHALLENGER)
                .build();
        SegmentTeam challengedTeam = SegmentTeam.builder()
                .workers(List.of(loserWorker))
                .type(TeamType.CHALLENGED)
                .response(ResponseType.YES)
                .build();
        segment.setTeams(List.of(challengerTeam, challengedTeam));


        SegmentTeam winners = SegmentTeam.builder()
                .workers(List.of(winnerWorker))
                .type(TeamType.WINNER)
                .build();
        SegmentTeam losers = SegmentTeam.builder()
                .workers(List.of(loserWorker))
                .type(TeamType.LOSER)
                .build();
        SegmentTemplate segmentTemplate = SegmentTemplate.builder()
                .segmentTeams(List.of(winners, losers))
                .eventTemplate(eventTemplate)
                .sourceEventDate(event.getDate())
                .sourceEventName(event.getName())
                .build();

        segment.setChallengeSegment(segmentTemplate);

        event.setSegments(List.of(segment));
        event.setPromotion(promotion);
        eventManager.createEvents(List.of(event));

        List<EventTemplate> eventTemplates = eventManager.getEventTemplates();

        assertThat(eventTemplates).hasSize(1);

        EventTemplate updatedEventTemplate = eventTemplates.get(0);

        assertThat(segmentManager.getSegmentTemplates(updatedEventTemplate)).hasOnlyOneElementSatisfying(savedSegmentTemplate -> {
            assertThat(savedSegmentTemplate.getSegmentTeams()).hasSize(2);

            assertThat(
                    savedSegmentTemplate.getSegmentTeams().stream()
                            .flatMap(segmentTeam -> segmentTeam.getWorkers().stream())
                            .collect(Collectors.toList())
            ).extracting(Worker::getWorkerID, Worker::getName)
                    .containsOnly(
                            tuple(winners.getWorkers().get(0).getWorkerID(), winners.getWorkers().get(0).getName()),
                            tuple(losers.getWorkers().get(0).getWorkerID(), losers.getWorkers().get(0).getName())
                    );
        });

        assertThat(segmentManager.getSegmentTemplates()).hasOnlyOneElementSatisfying(savedSegmentTemplate -> {
            assertThat(savedSegmentTemplate.getSegmentTeams()).hasSize(2);
            assertThat(savedSegmentTemplate.getSourceEventDate()).isEqualTo(event.getDate());
            assertThat(savedSegmentTemplate.getSourceEventName()).isEqualTo(event.getName());

            assertThat(
                    savedSegmentTemplate.getSegmentTeams().stream()
                            .flatMap(segmentTeam -> segmentTeam.getWorkers().stream())
                            .collect(Collectors.toList())
            ).extracting(Worker::getWorkerID, Worker::getName)
                    .containsOnly(
                            tuple(winners.getWorkers().get(0).getWorkerID(), winners.getWorkers().get(0).getName()),
                            tuple(losers.getWorkers().get(0).getWorkerID(), losers.getWorkers().get(0).getName())
                    );
        });


    }

}
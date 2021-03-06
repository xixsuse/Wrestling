package openwrestling.model.factory;

import openwrestling.manager.StaffManager;
import openwrestling.manager.WorkerManager;
import openwrestling.model.gameObjects.BroadcastTeamMember;
import openwrestling.model.gameObjects.Injury;
import openwrestling.model.gameObjects.Promotion;
import openwrestling.model.gameObjects.Title;
import openwrestling.model.gameObjects.Worker;
import openwrestling.manager.DateManager;
import openwrestling.manager.InjuryManager;
import openwrestling.manager.SegmentManager;
import openwrestling.model.gameObjects.Segment;
import openwrestling.model.gameObjects.SegmentTeam;
import openwrestling.model.segmentEnum.AngleLength;
import openwrestling.model.segmentEnum.MatchLength;
import openwrestling.model.segmentEnum.MatchRule;
import openwrestling.model.segmentEnum.PresenceType;
import openwrestling.model.segmentEnum.SegmentType;
import openwrestling.model.segmentEnum.StaffType;
import openwrestling.model.utility.ModelUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static openwrestling.model.constants.GameConstants.*;

public class MatchFactory implements Serializable {

    private final SegmentManager matchManager;
    private final DateManager dateManager;
    private final InjuryManager injuryManager;
    private final WorkerManager workerManager;
    private final StaffManager staffManager;

    public MatchFactory(SegmentManager matchManager,
                        DateManager dateManager,
                        InjuryManager injuryManager,
                        WorkerManager workerManager,
                        StaffManager staffManager) {
        this.matchManager = matchManager;
        this.dateManager = dateManager;
        this.injuryManager = injuryManager;
        this.workerManager = workerManager;
        this.staffManager = staffManager;
    }

    public Segment saveSegment(Segment segment) {
        setSegmentRatings(segment);

        processInjuries(segment);

        return segment;
    }

    private int getSegmentWorkRating(Segment segment) {
        int workRatingTotal = 0;
        boolean isMatch = segment.getSegmentType().equals(SegmentType.MATCH);

        for (SegmentTeam team : segment.getTeams()) {
            workRatingTotal += getWorkRating(team, segment.getMatchRule());
        }

        int segmentRating = workRatingTotal / segment.getTeams().size();

        if (isMatch) {
            segmentRating = getSegmentRatingWithMatchModifiers(segment, segmentRating);
        } else {
            segmentRating = getSegmentRatingWithAngleModifiers(segment, segmentRating);
        }

        segmentRating = getSegmentRatingWithTimeModifier(segment, segmentRating);

        return segmentRating;
    }

    private int getSegmentRatingWithTimeModifier(Segment segment, int baseRating) {
        int maxTime = segment.getSegmentType().equals(SegmentType.MATCH) ? MatchLength.MAXIMUM.value() : AngleLength.MAXIMUM.value();
        int actual = segment.getSegmentLength() / maxTime * 100;

        int timeDiff = (int) actual - baseRating;

        int penalty = timeDiff > 0 ? TIME_OVERRUN_PENALTY_WEIGHT : TIME_UNDERRUN_PENALTY_WEIGHT;

        return baseRating - Math.abs(timeDiff) / penalty;
    }

    private int getSegmentRatingWithAngleModifiers(Segment segment, int baseRating) {
        baseRating = modifyRating(baseRating,
                staffManager.getStaffSkillAverage(StaffType.CREATIVE, segment.getPromotion()),
                CREATIVE_MODIFIER_WEIGHT);

        return baseRating;
    }

    private int getSegmentRatingWithMatchModifiers(Segment segment, int baseRating) {
        baseRating = modifyRating(baseRating,
                staffManager.getStaffSkillAverage(StaffType.REFEREE, segment.getPromotion()),
                REF_MODIFIER_WEIGHT);
        baseRating = modifyRating(baseRating,
                staffManager.getStaffSkillAverage(StaffType.ROAD_AGENT, segment.getPromotion()),
                ROAD_AGENT_MODIFIER_WEIGHT);

        return baseRating;
    }

    private int getMatchCrowdRating(Segment segment) {
        int teamCount = 0;
        int totalPop = 0;

        for (SegmentTeam team : segment.getTeams()) {
            int teamPopTotal = 0;
            for (Worker worker : team.getWorkers()) {
                teamPopTotal += worker.getPopularity();
            }

            int teamPop = team.getWorkers().isEmpty() ? 0 : teamPopTotal / team.getWorkers().size();

            if (!team.getEntourage().isEmpty()) {
                int entouragePopTotal = 0;
                for (Worker entourage : team.getEntourage()) {
                    entouragePopTotal += entourage.getPopularity();
                }
                int entourageAvg = entouragePopTotal / team.getEntourage().size();
                teamPop += modifyRating(teamPop, entourageAvg, ENTOURAGE_MODIFIER_WEIGHT);
            }

            totalPop += teamPop;
            teamCount++;
        }

        int crowdRating = totalPop / teamCount;

        if (!segment.getTitles().isEmpty()) {
            int titleTotal = 0;
            for (Title title : segment.getTitles()) {
                titleTotal += title.getPrestige();
            }

            int titleAvg = titleTotal / segment.getTitles().size();
            crowdRating = modifyRating(crowdRating, titleAvg, TITLE_MODIFIER_WEIGHT);
        }

        crowdRating = modifyRating(crowdRating,
                staffManager.getStaffSkillAverage(StaffType.CREATIVE, segment.getPromotion()),
                CREATIVE_MODIFIER_WEIGHT);
        crowdRating = modifyRating(crowdRating,
                staffManager.getStaffSkillAverage(StaffType.PRODUCTION, segment.getPromotion()),
                PRODUCTION_MODIFIER_WEIGHT);

        return crowdRating;
    }

    private int modifyRating(int base, int modifier, int ratio) {
        int diff = modifier - base;
        base += (diff / ratio);
        return base;
    }

    private void setSegmentRatings(Segment segment) {
        int workRating = getSegmentWorkRating(segment);
        segment.setWorkRating(workRating);

        int crowdRating = modifyRating(getMatchCrowdRating(segment), workRating, CROWD_RATING_MODIFIER_WEIGHT);

        if (CollectionUtils.isNotEmpty(segment.getBroadcastTeam())) {
            int broadCastTeamTotal = 0;
            for (BroadcastTeamMember broadcastTeamMember : segment.getBroadcastTeam()) {
                if (broadcastTeamMember.getStaffMember() != null) {
                    broadCastTeamTotal += broadcastTeamMember.getStaffMember().getSkill();
                } else if (broadcastTeamMember.getWorker() != null) {
                    broadCastTeamTotal += ModelUtils.getWeightedScore(new Integer[]{
                            broadcastTeamMember.getWorker().getCharisma(),
                            broadcastTeamMember.getWorker().getPopularity()
                    });
                }
            }

            crowdRating = modifyRating(crowdRating,
                    broadCastTeamTotal / segment.getBroadcastTeam().size(),
                    BROADCAST_TEAM_MODIFIER_WEIGHT);
        }

        segment.setCrowdRating(crowdRating);
    }

    private int getWorkRating(SegmentTeam team, MatchRule matchRule) {
        if (team.getWorkers().isEmpty()) {
            return 0;
        }

        int totalTeamScore = 0;
        int entourageTotalScore = 0;

        for (Worker worker : team.getWorkers()) {
            switch (team.getType()) {
                case OFFERER:
                case OFFEREE:
                case CHALLENGER:
                case CHALLENGED:
                case ANNOUNCER:
                case AUDIENCE:
                case PROMO: {
                    totalTeamScore += worker.getCharisma();
                }
                case PROMO_TARGET: {
                    if (team.getPresence().equals(PresenceType.PRESENT)) {
                        totalTeamScore += worker.getCharisma();
                    } else {
                        totalTeamScore += worker.getPopularity();
                    }
                }
                break;
                default:
                    totalTeamScore += ModelUtils.getMatchWorkRating(worker, matchRule);
                    break;
            }
        }

        for (Worker worker : team.getEntourage()) {
            switch (team.getType()) {
                case OFFERER:
                case OFFEREE:
                case CHALLENGER:
                case CHALLENGED:
                case ANNOUNCER:
                case AUDIENCE:
                case PROMO: {
                    entourageTotalScore += worker.getCharisma();
                }
                case PROMO_TARGET: {
                    if (team.getPresence().equals(PresenceType.PRESENT)) {
                        entourageTotalScore += worker.getCharisma();
                    } else {
                        entourageTotalScore += worker.getPopularity();
                    }
                }
                break;
                default:
                    break;
            }
        }

        if (!team.getEntourage().isEmpty() && entourageTotalScore > 0) {
            int entourageAvg = entourageTotalScore / team.getEntourage().size();
            totalTeamScore = modifyRating(totalTeamScore, entourageAvg, ENTOURAGE_MODIFIER_WEIGHT);
        }
        return totalTeamScore / team.getWorkers().size();
    }

    private void processInjuries(Segment segment) {
        List<Worker> matchWorkers = segment.getMatchParticipants();
        matchWorkers.stream().forEach((w) -> {
            Promotion promotion = segment.getPromotion();
            int medicModifier = staffManager.getStaffSkillModifier(StaffType.MEDICAL, promotion, workerManager.selectRoster(promotion));
            int injuryRate = BASE_INJURY_RATE - segment.getMatchRule().getInjuryModifier() * BASE_INJURY_RATE / 100;
            if (RandomUtils.nextInt(0, injuryRate) == 1 && RandomUtils.nextInt(0, injuryRate) > medicModifier) {
                int injuryDays = RandomUtils.nextInt(0, MAX_INJURY_DAYS);
                int duration = injuryDays - (medicModifier / 10);
                if (duration > 0) {
                    Injury injury = Injury.builder()
                            .startDate(dateManager.today())
                            .expiryDate(dateManager.today().plusDays(duration))
                            .promotion(segment.getPromotion())
                            .segment(segment)
                            .worker(w)
                            .build();
                    if (CollectionUtils.isEmpty(segment.getInjuries())) {
                        segment.setInjuries(new ArrayList<>(List.of(injury)));
                    } else {
                        segment.getInjuries().add(injury);
                    }
                }
            }
        });
    }

}

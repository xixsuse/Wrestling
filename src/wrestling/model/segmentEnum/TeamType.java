package wrestling.model.segmentEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import wrestling.model.SegmentItem;
import wrestling.model.interfaces.Description;
import wrestling.model.interfaces.iTeamType;
import wrestling.model.modelView.WorkerGroup;
import wrestling.model.modelView.StaffView;
import wrestling.model.modelView.TagTeamView;
import wrestling.model.modelView.TitleView;
import wrestling.model.modelView.WorkerView;

public enum TeamType implements Description, iTeamType {
    CHALLENGER {
        @Override
        public String description() {
            return "Challenger";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }

    },
    CHALLENGED {
        @Override
        public String description() {
            return "Challenged";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    ATTACKER {
        @Override
        public String description() {
            return "Attacker";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }

    },
    VICTIM {
        @Override
        public String description() {
            return "Victim";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    PROMO {
        @Override
        public String description() {
            return "Promo";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    BRAWLER_PRIME {
        @Override
        public String description() {
            return "Brawler";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }

    },
    BRAWLER {
        @Override
        public String description() {
            return "Brawler";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    PROMO_TARGET {
        @Override
        public String description() {
            return "Promo Target";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    OFFERER {
        @Override
        public String description() {
            return "Offerer";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    OFFEREE {
        @Override
        public String description() {
            return "Offeree";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    INTERFERENCE {
        @Override
        public String description() {
            return "Interference";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Arrays.asList(ENTOURAGE, BROADCAST);
        }
    },
    INTERVIEWER {
        @Override
        public String description() {
            return "Interviewer";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    ANNOUNCER {
        @Override
        public String description() {
            return "Announcer";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    AUDIENCE {
        @Override
        public String description() {
            return "Audience";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    DEFAULT {
        @Override
        public String description() {
            return "Default";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    WINNER {
        @Override
        public String description() {
            return "Winner";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    LOSER {
        @Override
        public String description() {
            return "Loser";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    DRAW {
        @Override
        public String description() {
            return "Draw";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    EVERYONE {
        @Override
        public String description() {
            return "Everyone";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return false;
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    TITLES {
        @Override
        public String description() {
            return "Titles";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return segmentItem instanceof TitleView;
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    REF {
        @Override
        public String description() {
            return "Referee";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return segmentItem instanceof StaffView && ((StaffView) segmentItem).getStaffType().equals(StaffType.REFEREE);
        }

        @Override
        public List<TeamType> getShared() {
            return Collections.emptyList();
        }
    },
    BROADCAST {
        @Override
        public String description() {
            return "Broadcast Team";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem) || (segmentItem instanceof StaffView && ((StaffView) segmentItem).getStaffType().equals(StaffType.BROADCAST));
        }

        @Override
        public List<TeamType> getShared() {
            return Arrays.asList(ENTOURAGE, INTERFERENCE);
        }
    },
    ENTOURAGE {
        @Override
        public String description() {
            return "with";
        }

        @Override
        public String result() {
            return "";
        }

        @Override
        public boolean droppable(SegmentItem segmentItem) {
            return workerDroppable(segmentItem);
        }

        @Override
        public List<TeamType> getShared() {
            return Arrays.asList(BROADCAST, INTERFERENCE);
        }
    };

    @Override
    public String toString() {
        return description();
    }

    public boolean isMatch() {
        return this.equals(WINNER) || this.equals(LOSER) || this.equals(DRAW);
    }

    private static boolean workerDroppable(SegmentItem segmentItem) {
        return segmentItem instanceof WorkerView || segmentItem instanceof TagTeamView || segmentItem instanceof WorkerGroup;
    }

}

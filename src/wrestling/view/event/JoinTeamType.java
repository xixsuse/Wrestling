package wrestling.view.event;

public enum JoinTeamType implements Description {

    TAG_TEAM {
        @Override
        public String description() {
            return "New Tag Team";
        }

        @Override
        public String result() {
            return "";
        }

    },
    NEW_STABLE {
        @Override
        public String description() {
            return "New Stable";
        }

        @Override
        public String result() {
            return "";
        }

    },
    STABLE {
        @Override
        public String description() {
            return "Existing Stable";
        }

        @Override
        public String result() {
            return "";
        }

    },
    ANNOUNCEMENT {
        @Override
        public String description() {
            return "Announcement";
        }

        @Override
        public String result() {
            return "";
        }

    };
    
    @Override
    public String toString() {
        return description();
    }
    
    public static String label() {
        return "Join: ";
    }

}

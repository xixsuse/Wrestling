package wrestling.model;

import wrestling.model.utility.UtilityFunctions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Match extends Segment implements Serializable {

    private List<List<Worker>> teams = new ArrayList<List<Worker>>();

    private List<Worker> teamA = new ArrayList<>();

    private List<Worker> winner;

    public List<Worker> teamA() {
        return teamA;
    }
    private List<Worker> teamB = new ArrayList<>();

    public List<Worker> teamB() {
        return teamB;
    }

    @Override
    public List<Worker> allWorkers() {
        List<Worker> allWorkersList = new ArrayList<>();

        for (List<Worker> team : teams) {
            allWorkersList.addAll(team);
        }

        return allWorkersList;
    }

    private boolean hasWinner;
    private boolean hasTeams;

    private int matchRating;

    @Override
    public int segmentRating() {
        return matchRating;
    }

    public Match(final Worker workerA, final Worker workerB) {
        teamA.add(workerA);
        teamB.add(workerB);
        teams.add(teamA);
        teams.add(teamB);
        this.hasWinner = true;
        this.hasTeams = true;
        this.winner = teamA;
        calculateMatchRating();

    }

    /*
    this constructor takes an arbitrary number of teams
     */
    public Match(final List<List<Worker>> teams) {

        this.hasWinner = false;
        this.hasTeams = false;

        if (teams.size() == 1) {
            this.winner = teams.get(0);
            this.hasWinner = true;
            this.hasTeams = false;

        } else if (teams.size() > 1) {
            this.winner = teams.get(0);
            this.hasWinner = true;
            this.teams.addAll(teams);
            this.hasTeams = true;
            calculateMatchRating();
        }

    }

    public void setWinner(int winnerIndex) {
        if (winnerIndex < teams.size()) {
            setWinner(teams.get(winnerIndex));
        }

    }

    public void setWinner(List<Worker> winningTeam) {
        if (teams.contains(winningTeam)) {
            this.winner = winningTeam;
            Collections.swap(teams, teams.indexOf(winningTeam), 0);
        }
    }

    @Override
    public boolean isComplete() {

        //consider a match completed if it has any workers (placeholder)
        return !allWorkers().isEmpty() && !getWinner().isEmpty();
    }

    @Override
    public String toString() {

        String string = new String();

        if (this.hasTeams) {
            for (int t = 0; t < teams.size(); t++) {
                List<Worker> team = teams.get(t);

                for (int i = 0; i < team.size(); i++) {
                    string += team.get(i).getShortName();
                    string += " (" + team.get(i).getPopularity() + ") ";
                    if (team.size() > 1 && i < team.size() - 1) {
                        string += "/";
                    }

                }

                if (t == 0 && !string.isEmpty()) {
                    string += " def. ";

                } else if (t < teams.size() - 1 && !string.isEmpty()) {
                    string += ", ";
                }

            }
        } else {
            string += getWinner();
        }
        if (string.isEmpty()) {

            string += "Empty Segment";
        }

        return string;
    }

    public List<Worker> getWinner() {

        return this.winner;
    }

    private void calculateMatchRating() {

        float ratingsTotal = 0;

        for (List<Worker> team : teams) {
            float rating = 0;
            for (Worker worker : team) {

                rating += (worker.getFlying() + worker.getStriking() + worker.getFlying()) / 3;

            }
            ratingsTotal += rating;

        }

        matchRating = Math.round(ratingsTotal / teams.size());

    }

    @Override
    public void processSegment() {

        int winnerPop = 0;

        for (Worker worker : getWinner()) {
            winnerPop += worker.getPopularity();
        }

        winnerPop = winnerPop / getWinner().size();

        for (List<Worker> team : teams) {

            if (!team.equals(getWinner())) {
                int teamPop = 0;

                for (Worker worker : team) {
                    teamPop += worker.getPopularity();
                }

                teamPop = teamPop / getWinner().size();

                if (teamPop > winnerPop) {
                    for (Worker worker : getWinner()) {
                        worker.gainPopularity();
                    }

                    for (Worker worker : team) {
                        if (UtilityFunctions.randRange(1, 3) == 1) {
                            worker.losePopularity();
                        }

                    }
                } else {
                    for (Worker worker : getWinner()) {
                        if (UtilityFunctions.randRange(1, 3) == 1) {
                            worker.gainPopularity();
                        }
                    }
                }

            }
        }
    }

}

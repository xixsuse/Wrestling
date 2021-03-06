package openwrestling.model.interfaces;

import openwrestling.model.segmentEnum.TeamType;

public interface iAngleType {

    public int minWorkers();

    public int defaultWorkers();

    public TeamType addTeamType();

    public TeamType mainTeamType();

    public String resultString();

}

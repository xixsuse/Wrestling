package openwrestling.model;

import java.io.Serializable;
import openwrestling.model.interfaces.SegmentParams;
import openwrestling.model.gameObjects.Stable;
import openwrestling.model.segmentEnum.AngleType;
import openwrestling.model.segmentEnum.JoinTeamType;
import openwrestling.model.segmentEnum.PresenceType;
import openwrestling.model.segmentEnum.PromoType;
import openwrestling.model.segmentEnum.ShowType;

public class AngleParams implements Serializable, SegmentParams {

    private AngleType angleType;
    private JoinTeamType joinTeamType;
    private PresenceType presenceType;
    private PromoType promoType;
    private ShowType showType;
    private Stable joinStable;
    private SegmentTemplate challengeSegment;

    public AngleParams() {
        angleType = AngleType.PROMO;
        joinTeamType = JoinTeamType.NEW_STABLE;
        presenceType = PresenceType.PRESENT;
        promoType = PromoType.PROMO;
        showType = ShowType.TONIGHT;

    }

    /**
     * @return the angleType
     */
    public AngleType getAngleType() {
        return angleType;
    }

    /**
     * @param angleType the angleType to set
     */
    public void setAngleType(AngleType angleType) {
        this.angleType = angleType;
    }

    /**
     * @return the joinTeamType
     */
    @Override
    public JoinTeamType getJoinTeamType() {
        return joinTeamType;
    }

    /**
     * @param joinTeamType the joinTeamType to set
     */
    public void setJoinTeamType(JoinTeamType joinTeamType) {
        this.joinTeamType = joinTeamType;
    }

    /**
     * @return the presenceType
     */
    public PresenceType getPresenceType() {
        return presenceType;
    }

    /**
     * @param presenceType the presenceType to set
     */
    public void setPresenceType(PresenceType presenceType) {
        this.presenceType = presenceType;
    }

    /**
     * @return the promoType
     */
    public PromoType getPromoType() {
        return promoType;
    }

    /**
     * @param promoType the promoType to set
     */
    public void setPromoType(PromoType promoType) {
        this.promoType = promoType;
    }

    /**
     * @return the showType
     */
    public ShowType getShowType() {
        return showType;
    }

    /**
     * @param showType the showType to set
     */
    public void setShowType(ShowType showType) {
        this.showType = showType;
    }

    /**
     * @return the joinStable
     */
    @Override
    public Stable getJoinStable() {
        return joinStable;
    }

    /**
     * @param joinStable the joinStable to set
     */
    public void setJoinStable(Stable joinStable) {
        this.joinStable = joinStable;
    }

    /**
     * @return the challengeSegment
     */
    public SegmentTemplate getChallengeSegment() {
        return challengeSegment;
    }

    /**
     * @param challengeSegment the challengeSegment to set
     */
    public void setChallengeSegment(SegmentTemplate challengeSegment) {
        this.challengeSegment = challengeSegment;
    }

}

package wrestling.model;

import wrestling.model.interfaces.Segment;
import wrestling.model.interfaces.SegmentParams;
import wrestling.model.segmentEnum.SegmentType;

public class Match implements Segment {

    private int rating;
    private MatchParams matchParams;

    public Match() {
        matchParams = new MatchParams();
    }

    /**
     * @return the rating
     */
    @Override
    public int getRating() {
        return rating;
    }

    /**
     * @param rating the rating to set
     */
    @Override
    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public MatchParams getSegmentParams() {
        return matchParams;
    }

    @Override
    public void setSegmentParams(SegmentParams segmentParams) {
        this.matchParams = (MatchParams) segmentParams;
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.MATCH;
    }

}

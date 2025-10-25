package market.trader;

import market.matching.MatchResult;

public interface MatchSubscriber {
    public void notifyOfMatch(MatchResult matchRes);
}

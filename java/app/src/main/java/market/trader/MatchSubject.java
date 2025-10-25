package market.trader;

import market.matching.MatchResult;

public interface MatchSubject {
    public void addSubscriber(MatchSubscriber sub);
    
    public void removeSubscriber(MatchSubscriber sub);
    
    public void notifySubscribers(MatchResult res);
}

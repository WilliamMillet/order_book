
from uuid import UUID
from datetime import datetime
from order_book.constants import NO_MATCH

class Trade:
    def __init__(
            self,
            offerer_id: UUID,
            bidder_id: UUID, 
            price: float,
            volume: int,
        ) -> None:
        self.offerer_id = offerer_id
        self.bidder_id = bidder_id
        self.price = price
        self.volume = volume
        self.timestamp = datetime.now()

class TradeAnalytics:
    @staticmethod
    def avg_trade_price(trades: list[Trade]) -> float:
        """
        Returns the mean trade price over a list of trades weighted
        by volume
        """

        total_volume = sum(t.volume for t in trades)
        total_cost = sum(t.price * t.volume for t in trades)

        return total_cost / total_volume if total_volume != 0 else NO_MATCH
        
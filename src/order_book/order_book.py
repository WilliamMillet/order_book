from uuid import UUID
from heapq import heapify, heappush
from order_book.order import Order, OrderSide
from order_book.trade import Trade

class OrderNotFoundError(Exception):
    pass

class OrderBook:
    """Price time order book"""
    def __init__(self) -> None:
        self.bids: list[Order] = [] # Max heap
        self.offers: list[Order] = []  # Min heap


    def best_bid(self) -> Order | None:
        return self.bids[0] if self.bids else None


    def best_offer(self) -> Order | None:
        return self.offers[0] if self.offers else None
    

    def best_order(self, side: OrderSide) -> Order | None:
        """
        Wrapper for best_bid and best_offer allowing you to route
        your call between these based on a dynamic argument.
        """
        if side == OrderSide.SELL:
            return self.best_bid()
        else:
            return self.best_offer()
    

    def _get_order_idx(self, order_id: UUID, side: OrderSide) -> int:
        """
        Get the index of an order in the bid or offer heap in O(n) time
        searching from the start to end to the end of that heap
        """
        relevant_heap = self.bids if side == OrderSide.BUY else self.offers
        for idx, bid in enumerate(relevant_heap):
            if bid.order_id == order_id:
                return idx   

                
        raise OrderNotFoundError(f"Order with id {order_id} not be found")


    def insert_resting_order(self, order: Order) -> None:
        """Insert an order into the order book in O(log(n)) time"""
        if order.price is None:
            raise ValueError("Cannot place a resting order with no price")
        
        if order.order_side == OrderSide.BUY:
            heappush(self.bids, order)
        else:
            heappush(self.offers, order)


    def cancel_order(self, order_id: UUID, side: OrderSide) -> None:
        """
        Cancel the top bid or offer in a given heap  
        """
        relevant_heap = self.bids if side == OrderSide.BUY else self.offers
        
        idx = self._get_order_idx(order_id, side)
        del relevant_heap[idx]
        heapify(relevant_heap)
            
    def amend_order(
            self,
            order_id: UUID,
            side: OrderSide,
            new_volume: int | None = None,
            new_price: float | None = None,
        ) -> None:
        """
        Change the quantity or price of an order. Note that we only need to
        re-heapify after a price change as volume is not a factor in heap
        comparisons
        """
        if new_volume:
            Order.validate_volume(new_volume)
        if new_price:
            Order.validate_price(new_price)

        relevant_heap = self.bids if side == OrderSide.BUY else self.offers
        idx = self._get_order_idx(order_id, side)

        if new_volume:
            relevant_heap[idx].volume = new_volume
        if new_price:
            relevant_heap[idx].price = new_price
            heapify(relevant_heap)

    def trade_top(
            self,
            order: Order,
            volume_to_trade: int
        ) -> Trade:
        """
        Accept part of all the whole top bid/offer and return the return the
        trade conducted
        """
        
        best = self.best_order(order.inverse_side)
        if not best:
            raise OrderNotFoundError(f"No orders to trade with found")

        if best.volume == volume_to_trade:
            self.cancel_order(best.order_id, best.order_side)
        else:
            self.amend_order(best.order_id, order.inverse_side, best.volume - volume_to_trade)

        if order.order_side == OrderSide.BUY:
            bidder, offerer = order.trader_id, best.trader_id
        else:
            offerer, bidder = order.trader_id, best.trader_id

        return Trade(offerer, bidder, best.price, volume_to_trade)

    
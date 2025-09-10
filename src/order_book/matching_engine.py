from order_book.order_book import OrderBook
from order_book.order import Order, OrderType, OrderSide

class MatchingEngine:
    def __init__(self, book: OrderBook):
        self.book = book
    
    def place_order(self, order: Order) -> None:
        match order.order_type:
            case OrderType.MARKET:
                self._process_market_order(order)
            case OrderType.LIMIT:
                self._process_market_order(order)
            case OrderType.FILL_OR_KILL:
                self._process_FOK_order(order)
            case OrderType.IMMEDIATE_OR_CANCEL:
                self._process_IOC_order(order)
            case OrderType.QUOTE:
                self._process_quote_order
    

    def _process_market_order(self, incoming: Order) -> None:
        """
        Immediately match an order to the best bid/offer available.
        If there are no orders to match with the order is placed
        in the order book.
        """
        # print("Processing")
        while incoming.volume > 0:
            # print(f"Current volume for {incoming.order_side} order: {incoming.volume}")
            best = self.book.best_order(incoming.order_side)
            if not best:
                # print("A")
                self.book.insert_resting_order(incoming)
                break
            elif best.volume <= incoming.volume:
                # print("B")
                print(f"best_volume: {best.volume}")
                incoming.volume -= best.volume
                self.book.cancel_order(best.order_id, incoming.inverse_side)
            elif best.volume > incoming.volume:
                # print("C")
                new_best_volume = best.volume - incoming.volume
                self.book.amend_order(
                    best.order_id,
                    incoming.inverse_side, 
                    new_volume=new_best_volume
                )
                incoming.volume = 0

    
    def _process_limit_order(self, incoming: Order) -> None:
        pass
    
    def _process_FOK_order(self, incoming: Order) -> None:
        pass
    
    def _process_IOC_order(self, incoming: Order) -> None:
        pass
    
    def _process_quote_order(self, incoming: Order) -> None:
        pass
    

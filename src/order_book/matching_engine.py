from order_book.order_book import OrderBook
from order_book.order import Order, OrderType, OrderSide


class LiquidityError(Exception):
    pass


class MatchingEngine:
    def __init__(self, book: OrderBook):
        self.book = book

    def place_order(self, order: Order) -> None:
        match order.order_type:
            case OrderType.MARKET:
                self._process_market_order(order)
            case OrderType.LIMIT:
                self._process_limit_order(order)
            case OrderType.FILL_OR_KILL:
                self._process_FOK_order(order)
            case OrderType.IMMEDIATE_OR_CANCEL:
                self._process_IOC_order(order)
            case OrderType.QUOTE:
                self._process_quote_order

    def _process_market_order(self, incoming: Order) -> None:
        """
        Immediately match an order to the best bid/offer available.
        If there are no orders to match with raise a liquidity
        error
        """
        # print("Processing")
        while incoming.volume > 0:
            # print(f"Current volume for {incoming.order_side} order: {incoming.volume}")
            best = self.book.best_order(incoming.order_side)
            if not best:
                # print("A")
                raise LiquidityError("No orders available to match with")
            elif best.volume <= incoming.volume:
                # print("B")
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
        """
        Continuously match with the best order that is within the limit. if no
        orders are available then place a resting order in the order book
        """
        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)

            if not best or not incoming.is_price_in_limit(best.price):
                self.book.insert_resting_order(incoming)
                break
            elif best.volume <= incoming.volume:
                # print("B")
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

    def _process_FOK_order(self, incoming: Order) -> None:
        pass

    def _process_IOC_order(self, incoming: Order) -> None:
        pass

    def _process_quote_order(self, incoming: Order) -> None:
        pass
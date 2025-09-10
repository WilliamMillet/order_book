from order_book.order_book import OrderBook
from order_book.order import Order, OrderType, Quote


class LiquidityError(Exception):
    pass


class MatchingEngine:
    def __init__(self, book: OrderBook):
        self.book = book

    def place_order(self, order: Order) -> None:
        """Place an order in the order book"""
        match order.order_type:
            case OrderType.MARKET:
                self._process_market_order(order)
            case OrderType.LIMIT:
                self._process_limit_order(order)
            case OrderType.FILL_OR_KILL:
                self._process_FOK_order(order)
            case OrderType.IMMEDIATE_OR_CANCEL:
                self._process_IOC_order(order)

    def place_quote(self, quote: Quote) -> None:
        self.place_order(quote.bid)
        self.place_order(quote.offer)


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
            else:
                self._handle_mismatched_volumes(incoming, best)

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
            else:
                self._handle_mismatched_volumes(incoming, best)

    def _process_FOK_order(self, incoming: Order) -> None:
        """
        Fill or kill order (A limit order that is cancelled if it can't be
        immediately met. O(k*log(n)) time complexity where k is the number
        of orders that the incoming order must be matched with.
        """
        # Strategy - Extract matched orders until the incoming order is
        # filled. If it can't be filled than we can reinsert these.
        pending_matches: list[Order] = []

        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)
            if not best or not incoming.is_price_in_limit(best.price):
                # 'Kill (reinsert pending)'
                self._insert_sorted_orders(pending_matches)
            else:
                self._handle_mismatched_volumes(incoming, best)

    def _process_IOC_order(self, incoming: Order) -> None:
        """
        Process an immediate or cancel (IOC) order. Similar to a FOK order
        but will fulfil some of the order if possible
        """
        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)
            if not best or not incoming.is_price_in_limit(best.price):
                break
            else:
                self._handle_mismatched_volumes(incoming, best)

    def _handle_mismatched_volumes(self, incoming: Order, best: Order) -> None:
        """
        Resolve a partial match between the incoming order current best order.

        If an order gets to this function it's assumed it's price limit is
        compatible with the best limit.
        """
        if best.volume <= incoming.volume:
            incoming.volume -= best.volume
            self.book.cancel_order(best.order_id, incoming.inverse_side)
        elif best.volume > incoming.volume:
            new_best_volume = best.volume - incoming.volume
            self.book.amend_order(
                best.order_id,
                incoming.inverse_side,
                new_volume=new_best_volume
            )
            incoming.volume = 0

    def _insert_sorted_orders(self, orders: list[Order]) -> None:
        """
        Takes a list of orders sorted from lowest to highest priority and
        inserts them back in the order book. Note that I tested this in
        reverse and normal sorted order. Iterating in reverse seems to have
        no effect or a negative effect.
        """
        for order in orders:
            self.book.insert_resting_order(order)
                
        
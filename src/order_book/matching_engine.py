from datetime import datetime
from enum import Enum
from order_book.order_book import OrderBook
from order_book.order import Order, OrderType, Quote
from order_book.trade import Trade, TradeAnalytics
from order_book.constants import NO_MATCH
from typing import Iterable


class OrderStatus(Enum):
    # Order status has not been decided yet
    PENDING = "PENDING"
    # All orders were rejected from the market for a reason such as a lack of
    # liquidity
    ALL_REJECTED = "ALL_REJECTED"
    # No orders could not be immediately met and these are all no resting in
    # the order book
    ALL_RESTING = "ALL_RESTING"
    # Some orders were rejected for a reason such as low liquidity and others
    # were able to find matches
    PARTIAL_REJECTION = "PARTIAL_REJECTION"
    # Some orders where able to find matches and orders are not resting in
    # the order book
    PARTIAL_RESTING = "PARTIAL_RESTING"
    # All orders found matches immediately
    FILLED = "FILLED"


class LiquidityError(Exception):
    pass


class MatchResult:
    def __init__(self, order: Order):
        """
        Certain properties of match result can be set before the order
        is completed using the original order
        """
        self.order_id = order.order_id
        self.side = order.order_side
        self.order_type = order.order_type
        self.note = ""
        self.filled_volume = 0
        self.remaining_volume = order.volume
        self.avg_match_price = NO_MATCH
        self.timestamp = datetime.now()
        self.status = OrderStatus.PENDING
        self.trades: list[Trade] = []

    def finalise(self, incoming: Order, trades: list[Trade]):
        """Finalise the status of an order with order and trade information"""
        self.status = MatchResult._order_status(incoming, trades)
        self.filled_volume = self.remaining_volume - incoming.volume
        self.remaining_volume = incoming.volume
        self.avg_match_price = TradeAnalytics.avg_trade_price(trades)
        self.trades.extend(trades)

    @staticmethod
    def _order_status(incoming: Order, trades: list[Trade]) -> OrderStatus:
        """
        Get the order status for a matching result from an incoming order and
        list of trades conducting. This incoming order should have had its
        volume reduced if applicable prior to this function call.
        """
        if incoming.volume == 0:
            return OrderStatus.FILLED

        # Only market orders have rejections and partial rejections
        match incoming.order_type:
            case OrderType.MARKET | OrderType.IMMEDIATE_OR_CANCEL:
                if trades:
                    return OrderStatus.PARTIAL_REJECTION
                else:
                    return OrderStatus.ALL_REJECTED
            case OrderType.FILL_OR_KILL:
                return OrderStatus.ALL_REJECTED
            case _:
                if trades:
                    return OrderStatus.PARTIAL_RESTING
                else:
                    return OrderStatus.ALL_RESTING

    def __str__(self) -> str:
        key_val_pairs = (
            ("Side", self.side),
            ("Type", self.order_type),
            ("Filled Volume", self.filled_volume),
            ("Remaining Volume", self.remaining_volume),
            ("Average Match Price", self.avg_match_price),
            ("Status", self.status),
        )
        formatted_pairs = ", ".join(f"{k}: {v}" for k, v in key_val_pairs)
        return f"MatchResult({formatted_pairs})"

class MatchingEngine:
    def __init__(self, book: OrderBook):
        self.book = book

    def place_order(self, order: Order) -> MatchResult:
        """Place an order in the order book"""
        match order.order_type:
            case OrderType.MARKET:
                return self._process_market_order(order)
            case OrderType.LIMIT:
                return self._process_limit_order(order)
            case OrderType.FILL_OR_KILL:
                return self._process_FOK_order(order)
            case OrderType.IMMEDIATE_OR_CANCEL:
                return self._process_IOC_order(order)

    def place_orders(self, orders: Iterable[Order]) -> list[MatchResult]:
        """
        Place multiple orders in the order book from an iterable. Insertion
        occurs in the same order as the iterable is ordered.
        """
        return [self.place_order(o) for o in orders]

    def place_quote(self, quote: Quote) -> None:
        self.place_order(quote.bid)
        self.place_order(quote.offer)

    def _process_market_order(self, incoming: Order) -> MatchResult:
        """
        Immediately match an order to the best bid/offer available.
        If there are no orders to match with raise a liquidity
        error
        """
        res = MatchResult(incoming)
        trades: list[Trade] = []

        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)
            if not best:
                res.note = "Insufficient liquidity to match order fully"
                break
            else:
                trade = self._handle_mismatched_volumes(incoming, best)
                trades.append(trade)

        res.finalise(incoming, trades)
        return res

    def _process_limit_order(self, incoming: Order) -> MatchResult:
        """
        Continuously match with the best order that is within the limit. if no
        orders are available then place a resting order in the order book
        """
        res = MatchResult(incoming)
        trades: list[Trade] = []

        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)

            if not best or not incoming.is_price_in_limit(best.price):
                self.book.insert_resting_order(incoming)
                break
            else:
                trade = self._handle_mismatched_volumes(incoming, best)
                trades.append(trade)

        res.finalise(incoming, trades)
        return res

    def _process_FOK_order(self, incoming: Order) -> MatchResult:
        """
        Fill or kill order (A limit order that is cancelled if it can't be
        immediately met. O(k*log(n)) time complexity where k is the number
        of orders that the incoming order must be matched with.
        """
        res = MatchResult(incoming)
        # Strategy - Extract matched orders until the incoming order is
        # filled. If it can't be filled than we can reinsert these.
        pending_orders_matched: list[Order] = []
        pending_trades: list[Trade] = []
        initial_volume = incoming.volume

        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)
            if not best or not incoming.is_price_in_limit(best.price):
                # 'Kill (reinsert pending)'
                res.note = "Insufficient liquidity to match order fully"
                self.insert_orders(pending_orders_matched)
                pending_trades.clear()
                incoming.volume = initial_volume
                break
            else:
                trade = self._handle_mismatched_volumes(incoming, best)
                pending_trades.append(trade)
                pending_orders_matched.append(best)
        
        res.finalise(incoming, pending_trades)
        return res

    def _process_IOC_order(self, incoming: Order) -> MatchResult:
        """
        Process an immediate or cancel (IOC) order. Similar to a FOK order
        but will fulfil some of the order if possible
        """
        res = MatchResult(incoming)
        trades: list[Trade] = []
        
        while incoming.volume > 0:
            best = self.book.best_order(incoming.order_side)
            if not best or not incoming.is_price_in_limit(best.price):
                break
            else:
                trade = self._handle_mismatched_volumes(incoming, best)
                trades.append(trade)

        res.finalise(incoming, trades)
        return res

    def _handle_mismatched_volumes(self, incoming: Order, best: Order) -> Trade:
        """
        Resolve a partial match between the incoming order current best order.

        If an order gets to this function it's assumed it's price limit is
        compatible with the best limit.
        """
        if best.volume <= incoming.volume:
            incoming.volume -= best.volume
            trade = self.book.trade_top(incoming, best.volume)
        elif best.volume > incoming.volume:
            vol_to_trade = min(best.volume, incoming.volume)
            trade = self.book.trade_top(incoming, vol_to_trade)
            incoming.volume -= vol_to_trade

        return trade

    def insert_orders(self, orders: list[Order]) -> None:
        """
        Takes a list of orders sorted from lowest to highest priority and
        inserts them back in the order book. Note that I tested this in
        reverse and normal sorted order. Iterating in reverse seems to have
        no effect or a negative effect on time efficiency. Also, inserting
        into the order book is a distinct action from making a trade, as we
        don't check if there is an order to match with. It is assumed this
        has been done already.
        """
        for o in orders:
            self.book.insert_resting_order(o)

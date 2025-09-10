from time import sleep
from order_book.matching_engine import MatchingEngine
from order_book.order import Order, OrderSide, OrderType
from order_book.order_book import OrderBook
from order_book.trader import Trader

def main() -> None:
    book = OrderBook()
    eng = MatchingEngine(book)

    john = Trader("john")
    jane = Trader("jane")

    o1 = Order(10.0, 10, OrderSide.BUY, OrderType.MARKET, john)
    eng.place_order(o1)
    sleep(0.01)
    # print(f"Initial bid volume: {book.bids[0].volume}")

    o2 = Order(10.0, 5, OrderSide.SELL, OrderType.MARKET, jane)
    eng.place_order(o2)
    sleep(0.01)
    # print(f"Offers: {book.bids[0].volume}")

    # print(book.bids)
    # print(book.offers)


if __name__ == "__main__":
    main()
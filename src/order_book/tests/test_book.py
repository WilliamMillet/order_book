from order_book.order_book import OrderBook

def test_book_is_initially_empty():
    book = OrderBook()
    assert book.best_bid() is None
    assert book.best_offer() is None

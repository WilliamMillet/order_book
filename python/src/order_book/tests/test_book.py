from order_book.order_book import OrderBook

def test_book_is_initially_empty():
    book = OrderBook()
    assert book.is_empty()

from uuid import UUID
from dataclasses import dataclass

@dataclass
class Trade:
    offerer_id: UUID
    bidder_id: UUID
    price: float
    volume: int

package tradingmaster.model

import java.time.Instant

class TimeRange {

    Instant from
    Instant to

    TimeRange(Instant from, Instant to) {
        this.from = from
        this.to = to
    }

}

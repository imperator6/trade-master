package tradingmaster.model

import java.util.concurrent.atomic.AtomicBoolean

class BacktestRun {

    BacktestRun(Integer botId) {
        this.botId = botId
    }

    Integer botId

    AtomicBoolean locked = new AtomicBoolean(false)

    Integer totalSignalCount = 0

    Integer signalCompleteCount = 0

    void setTotalSignalCount(Integer count) {
        this.signalCompleteCount = count
    }

    void increaseSignalCompleteCount() {
        this.totalSignalCount++
    }

    boolean allSignalseComplete() {
        return (totalSignalCount > 0 && signalCompleteCount == totalSignalCount)
    }

    synchronized void lock() {
        locked.set(true)
    }

    synchronized void unlock() {
        locked.set(false)
        this.signalCompleteCount = 0
        this.totalSignalCount = 0
    }

    void waitForUnlock() {
        while(locked.get()) {
            Thread.sleep(5)
        }
    }
}

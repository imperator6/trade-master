package tradingmaster.db.entity.json

class Alert {

    Alert() {
    }

    Alert(value) {
        this.value = value
    }

    Boolean enabled = false

    BigDecimal value = 10

    //String lastExecutionTime = null

    boolean once = true
}

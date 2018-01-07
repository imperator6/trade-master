package tradingmaster.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"exchange", "market"})
})
public class MarketWatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(nullable = false)
    String exchange;

    @Column(nullable = false)
    String market;

    @Column(nullable = false)
    boolean active = true;

    @Column(nullable = false)
    Long intervalMillis = new Long(10000);

    @Column(nullable = false)
    Date startDate  = new Date();

    @Column
    String integrationFlowId;


    public MarketWatcher() {
    }

    public MarketWatcher(String exchange, String market) {
        this.exchange = exchange;
        this.market = market;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getIntegrationFlowId() {
        return integrationFlowId;
    }


    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setIntegrationFlowId(String integrationFlowId) {
        this.integrationFlowId = integrationFlowId;
    }

    public Long getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(Long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }
}

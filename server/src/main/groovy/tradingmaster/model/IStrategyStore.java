package tradingmaster.model;


import java.util.List;

public interface IStrategyStore {


    List<IStrategy> loadStrategies();

    IStrategy saveStrategy(Strategy s);

    IStrategy loadStrategyById(Number id, Number version);


}

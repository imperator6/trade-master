package tradingmaster.model

import groovy.transform.TupleConstructor

@TupleConstructor
class TradeBatch {

    IMarket market
    String exchange
    List<ITrade> trades

}

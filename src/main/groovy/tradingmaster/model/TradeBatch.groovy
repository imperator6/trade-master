package tradingmaster.model

import groovy.transform.TupleConstructor

@TupleConstructor
class TradeBatch {

    IMarket market
    List<ITrade> trades

}

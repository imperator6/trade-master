package tradingmaster.rest

import groovy.util.logging.Commons
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tradingmaster.model.CryptoMarket
import tradingmaster.model.Exchange
import tradingmaster.model.IExchangeAdapter
import tradingmaster.model.RestResponse

@RestController
@RequestMapping("/api/exchange")
@Commons
class ExchangeController {

    @Autowired
    ApplicationContext ctx

    @RequestMapping(value = "/", method = RequestMethod.GET)
     RestResponse<List<Exchange>> list() {

        def result = []

        ctx.getBeanNamesForType(IExchangeAdapter.class).each {

            Exchange ex = new Exchange()
            ex.name = it

            IExchangeAdapter exchangeAdapter = (IExchangeAdapter) ctx.getBean(it)
            ex.markets = exchangeAdapter.getMakets()

            result << ex
        }

        return new RestResponse(result)
    }

    @RequestMapping(value = "/markets", method = RequestMethod.GET)
    RestResponse<List<CryptoMarket>> getMarkets(@RequestParam String exchange) {

        List<CryptoMarket> result = new ArrayList<CryptoMarket>();

        IExchangeAdapter exchangeAdapter = (IExchangeAdapter) ctx.getBean(exchange);

        if(exchangeAdapter != null)
            return new RestResponse(exchangeAdapter.getMakets());

        return new RestResponse(result);
    }
}

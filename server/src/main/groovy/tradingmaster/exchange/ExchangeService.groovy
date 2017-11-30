package tradingmaster.exchange

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import tradingmaster.model.IExchangeAdapter

@Service
class ExchangeService {

    @Autowired
    ApplicationContext ctx

    IExchangeAdapter getExchangyByName(String name) {
        return ctx.getBean(name, IExchangeAdapter.class)
    }
}

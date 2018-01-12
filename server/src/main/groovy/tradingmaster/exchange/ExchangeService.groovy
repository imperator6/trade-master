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

        name = name.toLowerCase()
        String upperCase = name.substring(0, 1).toUpperCase() + name.substring(1);

        return  ctx.getBean(upperCase, IExchangeAdapter.class)
    }
}

package tradingmaster.exchange

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ExchangeService {


    @Autowired
    ApplicationContext ctx

    IExchangeAdapter getExchangyByName(String name) {

        //name = name.toLowerCase()
        String upperCase = name.substring(0, 1).toUpperCase() + name.substring(1);

        return  ctx.getBean(upperCase, IExchangeAdapter.class)
    }

    List<String> getAvailableExchanges() {


        Set beanNames = ctx.getBeansOfType(IExchangeAdapter.class).keySet()

        List exchangeNames = beanNames.collect {
            IExchangeAdapter a = (IExchangeAdapter) ctx.getBean(it)
            return a.getExchangeName()
        }

        return  exchangeNames
    }
}

package tradingmaster.service

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.pushover.Pushover

@RunWith(value = SpringRunner.class)
@SpringBootTest
class PushoverTest {

    @Autowired
    Pushover pushover

    @Test
    void testSendMessage() {

       pushover.sendMessage("Title", "Hello" , null, null)

    }


}

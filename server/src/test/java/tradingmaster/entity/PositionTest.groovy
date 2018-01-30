package tradingmaster.entity

import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import tradingmaster.db.PositionRepository
import tradingmaster.db.entity.Position
import tradingmaster.db.entity.json.PositionSettings
import tradingmaster.db.entity.json.StopLoss

@RunWith(value = SpringRunner.class)
@SpringBootTest
class PositionTest extends TestCase {

    @Autowired
    PositionRepository repo


    @Test
    void testPositionSettings() {

        Position pos = repo.findOne(1)

        PositionSettings settings = new PositionSettings()
        settings.stopLoss = new StopLoss()

        pos.settings = settings

        repo.save(pos)

        assertNotNull(pos)

    }


}

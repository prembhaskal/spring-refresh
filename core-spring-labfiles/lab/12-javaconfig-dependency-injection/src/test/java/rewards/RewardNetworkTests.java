package rewards;

import config.RewardsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class RewardNetworkTests {

    private RewardNetwork rewardNetwork;

    @BeforeEach
    public void setUp() {
        ApplicationContext context = SpringApplication.run(TestInfrastructureConfig.class);
        rewardNetwork = context.getBean(RewardNetwork.class);
    }

    @Test
    public void testRewardForDining() {

    }
}

package com.aestheticsw.jobkeywords;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.aestheticsw.jobkeywords.config.ServiceTestBehavior;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = JobKeywordsApplication.class)
@WebAppConfiguration
@ServiceTestBehavior
public class JobKeywordsApplicationTest {

    @Test
    public void contextLoads() {
    }

}

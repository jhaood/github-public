package com.aestheticsw.jobkeywords.liquibase;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.exception.LiquibaseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@LiquibaseTestBehavior
public class LiquibaseDiffRunner {
    
    @Autowired
    private LiquibaseActuator liquibaseActuator;
    
    @Test
    public void recordSchemaDifferencesBetweenJavaAndLiquibase() throws LiquibaseException, SQLException, IOException, ParserConfigurationException {
        liquibaseActuator.recordSchemaDifferencesBetweenJavaAndLiquibase();
    }
}

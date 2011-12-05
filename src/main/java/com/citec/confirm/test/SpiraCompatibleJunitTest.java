package com.citec.confirm.test;

import com.citec.confirm.test.spring.SpiraJUnit44TestExecutionListener;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class for Spira compatible test that works with Junit 4.4 (the version Spring 2.5.x supports).
 * <p/>
 * We use the Spring's Test Execution listener to achieve this.
 * <p/>
 * This allows us to write Spring context tests that are compatible with Spring 2.5.x (and Junit 4.4) and
 * can be reported to Spira.
 * <p/>
 *
 * @see com.citec.confirm.test.spring.SpiraCompatibleSpringTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({SpiraJUnit44TestExecutionListener.class})
public abstract class SpiraCompatibleJunitTest {


}

package com.citec.confirm.test.spring;


import com.citec.confirm.test.SpiraCompatibleJunitTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Extends {@link SpiraCompatibleSpringTest} to support Spring aware context tests that can also report to Spira.
 */
@TestExecutionListeners({SpiraJUnit44TestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
public abstract class SpiraCompatibleSpringTest extends SpiraCompatibleJunitTest {
}

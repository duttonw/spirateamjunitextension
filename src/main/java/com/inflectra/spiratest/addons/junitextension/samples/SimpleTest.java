package com.inflectra.spiratest.addons.junitextension.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;
import com.inflectra.spiratest.addons.junitextension.SpiraTestWatchman;

/**
 * Some simple tests using the ability to return results back to SpiraTest
 * 
 * @author		Inflectra Corporation
 * @version		2.2.0
 *
 */
@SpiraTestConfiguration( testSetId=11 )
public class SimpleTest {
	@Rule 
	public MethodRule spiratestwatchmen = new SpiraTestWatchman();
		
	protected int fValue1;
	protected int fValue2;


	/**
	 * Sets up the unit test
	 */
	@Before
	public void setUp() {
		fValue1= 2;
		fValue2= 3;
	}

	/**
	 * Tests the addition of the two values
	 */
	@Test
	@SpiraTestCase(testCaseId=1433)
	public void testAdd() {
		double result = fValue1 + fValue2;

		// forced failure result == 5
		assertTrue (result == 6);
	}

	/**
	 * Tests division by zero
	 */
	@Test
	@SpiraTestCase(testCaseId=1440)
	public void testDivideByZero() {
		int zero = 0;
		int result = 8 / zero;
		result++; // avoid warning for not using result
	}

	/**
	 * Tests two equal values
	 */
	@Test
	@SpiraTestCase(testCaseId=1441)
	public void testEquals() {
		assertEquals(12, 12);
		assertEquals(12L, 12L);
		assertEquals(new Long(12), new Long(12));

		assertEquals("Size", 12, 13);
		assertEquals("Capacity", 12.0, 11.99, 0.0);
	}

	/**
	 * Tests success
	 */
	@Test
	@SpiraTestCase(testCaseId=1442)
	public void testSuccess() {
		//Successful test
		assertEquals(12, 12);
	}
}
package io.odysz.antson;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple main.
 */
public class CliTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CliTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( CliTest.class );
    }

    /**CLI */
    public void testApp() {
        Cli.main(null);;
    }
}

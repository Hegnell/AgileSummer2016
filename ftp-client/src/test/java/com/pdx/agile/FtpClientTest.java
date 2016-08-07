package com.pdx.agile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class FtpClientTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FtpClientTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( FtpClientTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testFilenameExtractor()
        {
            assertTrue(FtpClient.getFilenameFromPath("upload/lydia/A_Elbereth.txt").equals("A_Elbereth.txt"));
            assertTrue(FtpClient.getFilenameFromPath("/upload/lydia/A_Elbereth.txt").equals("A_Elbereth.txt"));
            assertTrue(FtpClient.getFilenameFromPath("/upload/lydia").equals("lydia"));
            assertTrue(FtpClient.getFilenameFromPath("upload/lydia").equals("lydia"));
        }
}

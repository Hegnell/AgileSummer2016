package com.pdx.agile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


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

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final FtpClient client = new FtpClient();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
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

    public void testConnectToServer() throws IOException {
        setUpStreams();
        setUpStreams();
        FtpClient f = new FtpClient();
        f.connectToServer("138.68.1.7", 8821);
        assertThat(outContent.toString().contains("Servers reply: 220"), equalTo(true));
    }

    public void testConnectToOtherServer() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        f.connectToServer("ftp.ed.ac.uk", 21);
        assertThat(outContent.toString().contains("Servers reply: 220"), equalTo(true));
    }

    public void testGoodLogin() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        loginToOurServer(f);
        assertThat(outContent.toString().contains("You have logged in successfully!"), equalTo(true));
    }

    public void testBadLogin() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        f.connectToServer("138.68.1.7", 8821);
        boolean b = f.loginToServer("wrong", "wrong");
        assertFalse(b);
        assertThat(outContent.toString().contains("Invalid Username and/or Password."), equalTo(true));
    }

    public void testAnotherBadLogin() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        f.connectToServer("ftp.ed.ac.uk", 21);
        boolean b = f.loginToServer("wrong", "wrong");
        assertFalse(b);
        assertThat(outContent.toString().contains("Invalid Username and/or Password."), equalTo(true));
    }

    public void testDisconnect() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        f.connectToServer("ftp.ed.ac.uk", 21);
        f.disconnectFromServer();
        assertThat(outContent.toString().contains("Disconnecting...\nDisconnected from server."), equalTo(true));
    }

    public void testMakeAndRemoveDirectory() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        loginToOurServer(f);
        f.chdirRemoteServer("upload");
        f.listFiles();
        assertThat(outContent.toString().contains("UnitTestDirectory"), equalTo(false));
        f.mkdirRemoteServer("UnitTestDirectory");
        f.listFiles();
        assertThat(outContent.toString().contains("Successfully created directory UnitTestDirectory"), equalTo(true));
        f.removeDirectory("UnitTestDirectory", "");
        f.listFiles();
        assertThat(outContent.toString().contains("Successfully removed directory: UnitTestDirectory"), equalTo(true));
    }

    public void testRemoveNonexistentDirectory() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        loginToOurServer(f);
        f.chdirRemoteServer("upload");
        f.listFiles();
        assertThat(outContent.toString().contains("NonexistentDirectory"), equalTo(false));
        f.removeDirectory("NonexistentDirectory", "");
        f.listFiles();
        assertThat(outContent.toString().contains("Unable to remove directory: NonexistentDirectory"), equalTo(true));
    }

    public void testHappyPathForFile() throws IOException {
        setUpStreams();
        FtpClient f = new FtpClient();
        loginToOurServer(f);
        f.listLocalFiles();
        assertThat(outContent.toString().contains("src"), equalTo(true));
        f.listFiles();
        assertThat(outContent.toString().contains("upload"), equalTo(true));
    }



    public void loginToOurServer(FtpClient f) {
        try {
            f.connectToServer("138.68.1.7", 8821);
        } catch (IOException e) {
        }
        f.loginToServer("ftptestuser", "2016AgileTeam2");
    }
}

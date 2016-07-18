package com.pdx.agile;

import java.io.IOException;
import java.util.Scanner;
import org.apache.commons.net.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP client.
 *
 */
public class FtpClient {
    private static FTPClient ftpClient;

    public static void main( String[] args ) {
        boolean debug = false;

        for (String arg : args) {
            if (arg.equals("-debug")) {
                debug = true;
            }
        }
//        String serverName = "ftp.ed.ac.uk";
        String username = "anonymous";
        String password = "anonymous";

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to our FTP client.\n");

        String serverName = readUserInput(scanner, "Enter the server name: ");
 //       username = readUserInput(scanner, "Enter your username: ");
 //       password = readUserInput(scanner, "Enter your password");

        ftpClient = new FTPClient();

        System.out.println("Attempting to connect to server...");
        try {
            boolean connect = connectToServer(serverName, 21);
        } catch (IOException e) {
            exitWithError("Unable to connect to the server.", e, debug);
        }

        System.out.println("Connected to server successfully!");

        boolean keepGoing = true;

        while(keepGoing) {

            System.out.print(">> ");
            String[] userInput = scanner.nextLine().trim().split(" ");
            String firstArg = userInput[0];
            if (firstArg.equals("pwd")) {
                try {
                    showPath();
                } catch (IOException e) {
                    exitWithError("Was unable to get the path on the server.", e, debug);
                }
            } else if (firstArg.equals("ls")) {
                try {
                    listFiles();
                } catch (IOException e) {
                    exitWithError("Was unable to list the contents of the directory on the server.", e, debug);
                }
            } else if (userInput[0].equals("quit")) {
                keepGoing = false;
            } else if (userInput[0].equals("help")) {
                printHelp();
            } else {
                System.out.println("Did not understand your command, type \"help\" for available commands.");
            }
        }

    }

    // Connect to the server.
    private static boolean connectToServer(String serverName, int port) throws IOException{
        ftpClient.connect(serverName, port);
        int reply = ftpClient.getReplyCode();
        System.out.println("Servers reply: "  + reply);

        return true;
    }

    private static void showPath() throws IOException {
        System.out.println(ftpClient.printWorkingDirectory());
    }

    // List files story.
    private static void listFiles() throws IOException {
       FTPFile[] files = ftpClient.listFiles("/.");

       for (FTPFile file : files) {
           System.out.println(file.getName());
       }
    }

    private static String readUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void printHelp() {
        System.out.println("This is a help section, this is where commands and usage info will go.");
        System.out.println("ls\t\t\t\t List files in current directory.");
        System.out.println("pwd\t\t\t Show current working directory.");
        System.out.println("help\t\t\t Get available commands.");
        System.out.println("quit\t\t\t Exit the program.");
    }

    private static void exitWithError(String error, Exception e, boolean debug) {
        System.out.println(error);
        if (debug) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}

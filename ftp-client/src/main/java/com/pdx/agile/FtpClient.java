package com.pdx.agile;

import java.io.IOException;
import java.util.Scanner;
import org.apache.commons.net.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP client.
 *
 */
public class FtpClient {
    private static FTPClient ftpClient;
    private static boolean debug = false;

    public static void main( String[] args ) {

        for (String arg : args) {
            if (arg.equals("-debug")) {
                debug = true;
            }
        }
//        String serverName = "138.68.1.7";
        String username = "ftptestuser";
        String password = "";

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to our FTP client.\n");

        String serverName = readUserInput(scanner, "Enter the server name: ");

        ftpClient = new FTPClient();

        System.out.println("Attempting to connect to server...");
        try {
            boolean connect = connectToServer(serverName, 8821);
        } catch (IOException e) {
            exitWithError("Unable to connect to the server.", e, debug);
        }

        System.out.println("Connected to server successfully!");

        //print server's welcome message
        System.out.println(ftpClient.getReplyString());


        boolean keepGoing = false;

        //if loginToServer returns false, allow the user to retry until the FTP server disconnects.
        do {
            username = readUserInput(scanner, "Enter your username: ");
            password = readUserInput(scanner, "Enter your password: ");

            keepGoing = loginToServer(username, password);
        } while (!keepGoing);


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
            } else if (firstArg.equals("mkdir")) {
                if(userInput[1] != null && !userInput[1].isEmpty()){
                    mkdirRemoteServer(userInput[1]);
                } else {
                    System.out.println("mkdir: an absolute or relative path is required argument\n");
                    printHelp();
                }
             } else if (firstArg.equals("cd")) {
                if(userInput[1] != null && !userInput[1].isEmpty()){
                    chdirRemoteServer(userInput[1]);
                } else {
                    System.out.println("cd: no path was specified\n");
                    printHelp();
                }
            } else if (userInput[0].equals("quit")) {
                keepGoing = false;
            } else if (userInput[0].equals("help")) {
                printHelp();
            } else {
                System.out.println("Did not understand your command, type \"help\" for available commands.");
            }
        }

        //disconnect if still connected
        try {
            ftpClient.disconnect();
        } catch (IOException exitErr) {
            if (debug) {
                exitErr.printStackTrace();
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

    private static boolean loginToServer(String username, String password) {
        boolean login = false;

        try {
            login = ftpClient.login(username, password);

            if (!login) {
                System.out.println("Invalid Username and/or Password.");
            } else {
                System.out.println("You have logged in successfully!");
            }

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection. Too many invalid login attempts.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to login.", e, debug);
        }

        return login;
    }

    private static boolean mkdirRemoteServer(String makePath) {
        boolean success = false;

        try {
            success = ftpClient.makeDirectory(makePath);

            if (!success) {
                System.out.println("Could not create directory " + makePath + " on remote server. Do you have the correct permissions?");
            } else {
                System.out.println("Successfully created directory" + makePath);
            }

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to create remote directory.", e, debug);
        }

        return success;
    }

    private static boolean chdirRemoteServer(String cdPath) {
        boolean success = false;

        try {
            success = ftpClient.changeWorkingDirectory(cdPath);

            //no need to show anything if we could change to the directory
            if (!success)
                System.out.println("Could not change to directory " + cdPath);

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to change to remote directory.", e, debug);
        }

        return success;
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
        System.out.println("mkdir <path>\t\t\t Create a directory on the remote server.");
        System.out.println("cd <path>\t\t\t Change the current working directory on the remote server.");
        System.out.println("help\t\t\t Get available commands.");
        System.out.println("quit\t\t\t Exit the program.");
    }

    private static void exitWithError(String error, Exception e, boolean debug) {
        System.out.println(error);
        if (debug) {
            e.printStackTrace();
        }

        //if still connected, gracefully disconnect
        try {
            ftpClient.disconnect();
        } catch (IOException exitErr) {
            if (debug) {
                exitErr.printStackTrace();
            }
        }
        System.exit(1);
    }
}

package com.pdx.agile;

import java.io.*;
import java.util.*;

//import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
//import com.sun.tools.javac.file.SymbolArchive;
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

        String username = null;
        String password = null;
        Scanner scanner = new Scanner(System.in);
        int port = 8821;

        System.out.println("Welcome to our FTP client.\n");

        /*
        String serverName = readUserInput(scanner, "Enter the server name: ");
        String portString = readUserInput(scanner, "Enter the port number: ");
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            exitWithError("The port number must be an integer.", e , debug);
        } */


        String serverName = "138.68.1.7";
        ftpClient = new FTPClient();

        System.out.println("Attempting to connect to server...");
        try {
            boolean connect = connectToServer(serverName, port);
        } catch (IOException e) {
            exitWithError("Unable to connect to the server.", e, debug);
        }

        System.out.println("Connected to server successfully!");

        //print server's welcome message
        System.out.println(ftpClient.getReplyString());

        // enter local passive mode to get files/directories to display
        ftpClient.enterLocalPassiveMode();


        boolean keepGoing = false;

        // Commented out for ease of testing, will be re-added at the end.
        //if loginToServer returns false, allow the user to retry until the FTP server disconnects.
    /*    do {
            username = readUserInput(scanner, "Enter your username: ");
            password = readUserInput(scanner, "Enter your password: ");

            keepGoing = loginToServer(username, password);
        } while (!keepGoing); */



        // Just for testing purposes so you don't actually have to type this in every time.
        username = "ftptestuser";
        password = "2016AgileTeam2";
        keepGoing = loginToServer(username, password);

        try {
            while (keepGoing) {

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
                    if (userInput[1] != null && !userInput[1].equals("")) {
                        mkdirRemoteServer(userInput[1]);
                    } else {
                        System.out.println("mkdir: and absolute or relative path is required argument\n");
                        printHelp();
                    }
                } else if (firstArg.equals("cd")) {
                    if (userInput[1] != null && !userInput[1].equals("")) {
                        chdirRemoteServer(userInput[1]);
                    } else {
                        System.out.println("cd: no path was specified\n");
                    }
                } else if (firstArg.equals("lls")) {
                	try {
                		listLocalFiles(userInput[1]);
                	} catch(IOException e) {
                		exitWithError("Unable to list local directories or files.", e, debug);
                	}
                } else if (firstArg.equals("get")) {
                    try {
                        String path = userInput[1];
                        try {
                            retrieveFile(path);
                        } catch (IOException e) {
                            exitWithError("Was unable to retrieve the file - " + e.getMessage(), e, debug);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("get requires a filepath");
                    }
                } else if (firstArg.equals("put")) {
                    if (userInput[1] != null && !userInput[1].equals("")) {
                        try {
                            sendFiles(userInput[1]);
                        } catch (IOException e) {
                            exitWithError("Unable to upload the file onto the server.", e, debug);
                        }
                    } else {
                        System.out.println("No file was specified.");
                    }
                } else if (userInput[0].equals("quit")) {
                    keepGoing = false;
                } else if (userInput[0].equals("help")) {
                    printHelp();
                } else {
                    System.out.println("Did not understand your command, type \"help\" for available commands.");
                }
            }
        } finally {
            disconnectFromServer();
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

    private static void disconnectFromServer() {
        if (ftpClient.isConnected()) {
            try {
                System.out.println("Disconnecting...");
                ftpClient.disconnect();
                System.out.println("Disconnected from server.");
            } catch (IOException e) {
                exitWithError("Problem disconnecting from server", e , debug);
            }
        }
    }

    private static boolean mkdirRemoteServer(String makePath) {
        boolean success = false;

        try {
            success = ftpClient.makeDirectory(makePath);

            if (!success) {
                System.out.println("Could not create directory " + makePath + " on remote server. Do you have the correct permission?");
            } else {
                System.out.println("Successfully created directory " + makePath);
            }
        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to create the remote directory.", e, debug);
        }

        return  success;
    }

    private static boolean chdirRemoteServer(String cdPath) {
        boolean success = false;

        try {
            success = ftpClient.changeWorkingDirectory(cdPath);

            if (!success) {
                System.out.println("Could not change to directory " + cdPath);
            }
        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to change to remote directory.", e, debug);
        }

        return  success;
    }

    private static void showPath() throws IOException {
        System.out.println(ftpClient.printWorkingDirectory());
    }

    // List files story.
    private static void listFiles() throws IOException {
       FTPFile[] files = ftpClient.listFiles(".");

       for (FTPFile file : files) {
           System.out.println(file.getName());
       }
    }
    
    // List local directories and files
    private static void listLocalFiles(String localDirectory) throws IOException {
    	File directory = new File(localDirectory);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
                System.out.println(file.getName());
        }
    }

    // Retrieve a file from the server and save it locally.
    private static void retrieveFile(String path) throws IOException {
        String localPath = path;
        File localFile = new File(localPath);
        OutputStream os = new FileOutputStream(localFile);
        if (!ftpClient.retrieveFile("./" + path, os)) {
            //retrieveFile returns false if file is not found on remote server.
            localFile.delete();
        }
        os.close();
    }

    // Upload files onto the server
    private static void sendFiles(String fileToFTP) throws IOException {
        Scanner scanner = new Scanner(System.in);
        // specify local directory
        String localDirectory = readUserInput(scanner, "Please enter your local directory: ");

        // specify remote directory
        String remoteDirectory = readUserInput(scanner, "Please enter the remote directory: ");

        // change working dirctory to the specifed remote directory
        ftpClient.changeWorkingDirectory(remoteDirectory);
        System.out.println("Current directory is " + ftpClient.printWorkingDirectory());
        InputStream input;
        input = new FileInputStream(localDirectory + "/" + fileToFTP);

        // store the file in the remote server
        if (ftpClient.storeFile(fileToFTP, input)) {
            // if successful, print the following line
            System.out.println(fileToFTP + " uploaded successfully");
        } else {
            // might be failed at this point
            System.out.println("Upload failed.");
        }

        // close the stream
        scanner.close();
        input.close();
    }
    private static String readUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void printHelp() {
        System.out.println("This is a help section, this is where commands and usage info will go.");
        System.out.println("ls\t\t\t\t List files in current directory.");
        System.out.println("lls <path>\t\t\t List local directories or files in the given path.");
        System.out.println("pwd\t\t\t\t Show current working directory.");
        System.out.println("lpwd\t\t\t\t Show current local directory.");
        System.out.println("mkdir <path>\t\t\t Create a directory on the remote server.");
        System.out.println("cd <path>\t\t\t Change the current working directory on the remote server.");
        System.out.println("lcd <path>\t\t\t Change the current local directory.");
        System.out.println("get <path>\t\t\t Download the file at the given path on the server.");
        System.out.println("put <file>\t\t\t Upload the file to the remote server.");
        System.out.println("help\t\t\t\t Get available commands.");
        System.out.println("quit\t\t\t\t Exit the program.");
    }

    private static void exitWithError(String error, Exception e, boolean debug) {
        System.out.println(error);
        if (debug) {
            e.printStackTrace();
        }
        disconnectFromServer();
        System.exit(1);
    }

    // Confirms the action specified by the string passed. Returns a boolean value representing the user's decision.
    private static boolean confirm(String action, Scanner scanner) {
        String input;
        System.out.println("Are you sure youw ant to " + action + "?(y/n)");

        while (true) {
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("y")) {
                return true;
            } else if (input.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out.println("Please enter a valid response, 'y' or 'n'.");
            }
        }
    }
}

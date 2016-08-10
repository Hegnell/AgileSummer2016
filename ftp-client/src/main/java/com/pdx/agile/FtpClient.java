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
        Scanner scanner = new Scanner(System.in);
        ftpClient = new FTPClient();
        if (args.length > 1) {//if there is one arg or less, run normally and check for debug option
            String serverName = null;
            int port = -1;
            String username = null;
            boolean cdFlag = false;
            String cdPath = null;
            boolean mkdirFlag = false;
            String mkdirPath = null;
            boolean rmFlag = false;
            String rmPath = null;
            boolean lsFlag = false;
            String putFile = null;
            boolean putFlag = false;
            for (String arg : args) {
                if (serverName == null) {
                    //check for second arg of options with two args first
                    if(cdFlag && cdPath == null) {
                        cdPath = arg;
                    } else if (mkdirFlag && mkdirPath == null) {
                        mkdirPath = arg;
                     }else if (rmFlag && rmPath == null) {
                        rmPath = arg;
                    } else if (putFlag && putFile == null) {
                        putFile = arg;
                    //check for options
                    } else if (arg.equals("-cd")) {
                        if (cdFlag) {
                            exitWithError("Duplicate option: " + arg, null, debug);
                        }
                        cdFlag = true;
                    } else if (arg.equals("-mkdir")) {
                        if (mkdirFlag) {
                            exitWithError("Duplicate option: " + arg, null, debug);
                        }
                        mkdirFlag = true;
                    } else if (arg.equals("-rm")) {
                        if (rmFlag) {
                            exitWithError("Duplicate option: " + arg, null, debug);
                        }
                        rmFlag = true;
                    } else if (arg.equals("-ls")) {
                        if (lsFlag) {
                            exitWithError("Duplicate option: " + arg, null, debug);
                        }
                        lsFlag = true;
                    } else if (arg.equals("-put")) {
                        if (putFlag) {
                            exitWithError("Duplicate option: " + arg, null, debug);
                        }
                        putFlag = true;
                    } else if (arg.equals("-README")) {
                        printCMDHelp();
                        return;
                    } else {
                        //no options left, set server name
                        serverName = arg;
                    }
                } else if (port == -1) {
                    port = Integer.parseInt(arg);
                } else if (username == null) {
                    username = arg;
                } else {
                    exitWithError("Too many arguments" + arg, null, debug);
                }
            }
            //check for null host/port/username
            if(serverName == null){
                exitWithError("Missing server name", null, debug);
            } else if (port == -1) {
                exitWithError("Missing port number", null, debug);
            }  else if (username == null) {
                exitWithError("Missing username", null, debug);
            }

            System.out.println("Attempting to connect to server...");
            try {
                boolean connect = connectToServer(serverName, port);
            } catch (IOException e) {
                exitWithError("Unable to connect to the server.", e, debug);
            }

            try {
                System.out.println("Connected to server successfully!");

                //print server's welcome message
                System.out.println(ftpClient.getReplyString());

                // enter local passive mode to get files/directories to display
                ftpClient.enterLocalPassiveMode();

                boolean loggedIn = false;
                String password;
                //Get password from user and attempt to log in
                while (!loggedIn) {
                    password = readUserInput(scanner, "Enter the password: ");
                    loggedIn = loginToServer(username, password);
                }

                //execute options
                if (cdFlag) {
                    chdirRemoteServer(cdPath);
                }
                if (mkdirFlag) {
                    mkdirRemoteServer(mkdirPath);
                }
                if (rmFlag) {
                    rmRemoteServer(rmPath);
                }
                if (putFlag) {
                    try {
                        sendFiles(putFile);
                    } catch (IOException e) {
                        exitWithError("Unable to upload the file onto the server.", e, debug);
                    }
                }
                if (lsFlag) {
                    try {
                        listFiles();
                    } catch (IOException e) {
                        exitWithError("Was unable to list the contents of the directory on the server.", e, debug);
                    }
                }
            }finally {
                disconnectFromServer();
            }



        } else {

            for (String arg : args) {
                if (arg.equals("-debug")) {
                    debug = true;
                }
            }

            String username = null;
            String password = null;

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
                        if (userInput.length != 2) {
                            System.out.println("mkdir: and absolute or relative path is required argument\n");
                            printHelp();
                        } else {
                            mkdirRemoteServer(userInput[1]);
                        }
                    } else if (firstArg.equals("rm")) {
                        if (userInput.length != 2) {
                            System.out.println("rm: and absolute or relative path is required argument\n");
                            printHelp();
                        } else {
                            rmRemoteServer(userInput[1]);
                        }
                    } else if (firstArg.equals("cd")) {
                        if (userInput.length != 2) {
                            System.out.println("Incorrect arguments specified to cd. Type \"help\" for usage.\n");
                        } else {
                            chdirRemoteServer(userInput[1]);
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
                        if (userInput.length != 2) {
                            System.out.println("No file was specified.");
                        } else {
                            try {
                                sendFiles(userInput[1]);
                            } catch (IOException e) {
                                exitWithError("Unable to upload the file onto the server.", e, debug);
                            }
                        }
                    } else if (firstArg.equals("chmod")) {
                        if (userInput.length != 3) {
                            System.out.println("Incorrect number of arguments provided to chmod.");
                        } else {
                            changeFilepermissions(userInput[1], userInput[2]);
                        }

                    } else if (firstArg.equals("quit")) {
                        if (confirm("disconnect and quit the application", scanner)) {
                            keepGoing = false;
                        }
                    } else if (firstArg.equals("help")) {
                        printHelp();
                    } else {
                        System.out.println("Did not understand your command, type \"help\" for available commands.");
                    }
                }
            } finally {
                disconnectFromServer();
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

    private static boolean rmRemoteServer(String rmPath) {
        boolean success = false;

        try {
            success = ftpClient.deleteFile(rmPath);

            if (!success) {
                System.out.println("Could not delete file " + rmPath + " on remote server. Do you have the correct permission?");
            } else {
                System.out.println("Successfully removed file " + rmPath);
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
           System.out.println(file.getRawListing());
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

    //Change the permissions of a specified file
    private static void changeFilepermissions(String permissions, String file) {
        if (validPermissions(permissions)) {
            try {
                ftpClient.sendSiteCommand("chmod " + permissions + " " + file);
            } catch (IOException e) {
                System.out.println("Failed to change permissions on file, double check the file name.");
            }
        } else {
            System.out.println("Could not understand those permissions, check your chmod values.");
        }

    }

    //Check that the string passed represents a valid permissions arg
    private static boolean validPermissions(String permissions) {
        return permissions.matches("[0-7][0-7][0-7]");
    }
    private static String readUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    //Print the usage of this program running normally
    private static void printHelp() {
        System.out.println("This is a help section, this is where commands and usage info will go.");
        System.out.println("ls\t\t\t\t\t List files in current directory.");
        System.out.println("pwd\t\t\t\t\t Show current working directory.");
        System.out.println("mkdir <path>\t\t Create a directory on the remote server.");
        System.out.println("rm <path>\t\t Remove a file from the remote server.");
        System.out.println("cd <path>\t\t\t Change the current working directory on the remote server.");
        System.out.println("get <path>\t\t\t Download the file at the given path on the server.");
        System.out.println("put <file>\t\t\t Upload the file to the remote server.");
        System.out.println("chmod <perm> <file> \t\t Change permissions on specified file.");
        System.out.println("help\t\t\t\t Get available commands.");
        System.out.println("quit\t\t\t\t Exit the program.");
    }

    //Print the usage for running this application from the command line
    private static void printCMDHelp() {
        System.out.println("usage: java <packagename> [options] <args>");
        System.out.println(" args are (in this order):");
        System.out.println("   serverName\t\tThe name of the server");
        System.out.println("   username\t\t\tThe user name in the server");
        System.out.println("   port\t\t\t\tThe port number to use when connecting");
        System.out.println("\t\t(user is prompted for password on program execution)");
        System.out.println(" options are (options may appear in any order):");
        System.out.println("-ls\t\t\t\t\t List files in current directory.");
        System.out.println("-mkdir <path>\t\t Create a directory on the remote server.");
        System.out.println("-rm <path>\t\t\t Remove a file from the remote server.");
        System.out.println("-cd <path>\t\t\t Change the current working directory on the remote server.");
        System.out.println("-put <file>\t\t\t Upload the file to the remote server.");
        System.out.println("-README\t\t\t\t Print this readme and do nothing else.");
    }

    //Print the error message passed and the stack trace of e if debug is true, then exit with error message 1
    private static void exitWithError(String error, Exception e, boolean debug) {
        System.out.println(error);
        if (debug) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        disconnectFromServer();
        System.exit(1);
    }

    // Confirms the action specified by the string passed. Returns a boolean value representing the user's decision.
    private static boolean confirm(String action, Scanner scanner) {
        String input;
        System.out.println("Are you sure you want to " + action + "?(y/n)");

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

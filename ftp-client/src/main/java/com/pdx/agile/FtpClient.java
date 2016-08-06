package com.pdx.agile;

import java.io.*;
import java.util.*;

import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
import com.sun.tools.javac.file.SymbolArchive;
import org.apache.commons.net.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.Util;
import sun.net.ftp.FtpDirEntry;

/**
 * FTP client.
 *
 */
public class FtpClient {
    private static FTPClient ftpClient;
    private static boolean debug = false;

    private static String userName;
    private static int port;
    private static String serverName;
    private static String password;

    public static void main( String[] args ) {

        for (String arg : args) {
            if (arg.equals("-debug")) {
                debug = true;
            }
        }

        Scanner scanner = new Scanner(System.in);
        port = 8821;

        System.out.println("Welcome to our FTP client.\n");

        /*
        String serverName = readUserInput(scanner, "Enter the server name: ");
        String portString = readUserInput(scanner, "Enter the port number: ");
        try {
            port = Integer.parseInt(portString);
        } catch (Exception e) {
            exitWithError("The port number must be an integer.", e , debug);
        } */


        serverName = "138.68.1.7";
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
        userName = "ftptestuser";
        password = "2016AgileTeam2";
        keepGoing = loginToServer(userName, password);

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
                } else if (firstArg.equals("cp")) {
                    if (userInput.length != 3) {
                        System.out.println("Incorrect number of arguments provided to cp.");
                    } else {

                        try {
                            String path = ftpClient.printWorkingDirectory();
                            if (copyDirectories(userInput[1], userInput[2])) {
                                System.out.println("Copied directory successfully.");
                                // Having tons of issue here, only way to get it working smoothly was to disconnect and
                                // reconnect.
                                ftpClient.disconnect();
                                ftpClient.connect(serverName, port);
                                ftpClient.login(userName, password);
                                ftpClient.enterLocalPassiveMode();
                                ftpClient.changeWorkingDirectory(path);
                            } else {
                                System.out.println("Unable to copy the directories.");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else if (firstArg.equals("rm")) {
                    if (userInput.length != 2) {
                         System.out.println("Incorrect number of arguments provided to rm.");
                    } else {
                        try {
                            if (removeDirectory(userInput[1], "")) {
                                System.out.println("Removed directory successfully.");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (firstArg.equals("quit")) {
                    keepGoing = false;
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
       FTPFile[] files = ftpClient.listFiles();

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

    // Helper function that returns the absolute path of a given file, taking the current working directory of the
    // client into context.
    private static String getAbsolutePath(String path) {
        String ret = "";
        try {
            String old = ftpClient.printWorkingDirectory();
            String[] pieces = path.split("/");

            String newPath = "/";
            for (int i = 1; i < pieces.length; i++) {
                String p = pieces[i];
                newPath += p + "/";
            }
            if (!newPath.equals("/")) {
                ftpClient.changeWorkingDirectory(newPath);
            }
            ret = ftpClient.printWorkingDirectory() + "/" + pieces[pieces.length - 1];
            ftpClient.changeWorkingDirectory(old);
        } catch (IOException e) {
            exitWithError("Failed to get absolute file path for: " + path, e, debug);
        }
        return ret;
    }

    private static boolean copyDirectories(String source, String dest) {
        boolean success = false;

        dest = getAbsolutePath(dest);
        try {
            success = ftpClient.makeDirectory(dest);

            if (!success) {
                System.out.println("Could not create the new directory.");
                return success;
            }

            // Fire up a helper ftp client, useful for navigating through the directories smoothly.
            FTPClient temp = new FTPClient();
            temp.connect(serverName, port);
            temp.login(userName, password);
            ftpClient.changeWorkingDirectory(source);
            temp.changeWorkingDirectory(dest);
            temp.enterLocalPassiveMode();

            String[] paths = dest.split("/");
            String newDirectory = paths[paths.length-1];
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Loop to go through the top most directory and make calls to copy each of those directories recursively.
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (!file.isDirectory()) {
                    copyFile(file.getName(), temp.printWorkingDirectory());
                } else {
                    if (!file.getName().equals(newDirectory)) {

                        success = copyDirectory(temp, dest + "/" + file.getName());
                        ftpClient.changeWorkingDirectory("../");
                        temp.changeWorkingDirectory("../");
                    }
                }
            }
            temp.logout();
            temp.disconnect();

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to copy the remote directory.", e, debug);
        }

        return success;
    }


    private static boolean copyDirectory(FTPClient temp, String path) {
        boolean success = false;
        try {
            success = ftpClient.makeDirectory(path);

            if (!success) {
                System.out.println("Could not create the new directory.");
                return success;
            }

            //
            String[] paths = path.split("/");
            String newDirectory = paths[paths.length-1];
            ftpClient.changeWorkingDirectory(newDirectory);
            temp.changeWorkingDirectory(newDirectory);

            // Go through the files in the directory and either copy them if they are a file, or recursively call this
            // function again if it's a directory.
            FTPFile[] files = ftpClient.listFiles();
            for (FTPFile file : files) {
                if (!file.isDirectory()) {
                    copyFile(file.getName(), temp.printWorkingDirectory());
                } else {
                    if (!file.getName().equals(newDirectory)) {
                        copyDirectory(temp, path + "/" + file.getName());
                        ftpClient.changeWorkingDirectory("../");
                        temp.changeWorkingDirectory("../");
                    }
                }
            }

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to copy the remote directory.", e, debug);
        }

        return success;
    }

    // Function to copy a single file on the server to another destination on the server.
    private static boolean copyFile(String file, String destPath) throws IOException {
        // Create new FTPClient for transfer.
        FTPClient dest = new FTPClient();
        dest.connect(serverName, port);
        dest.login(userName, password);
        dest.enterLocalPassiveMode();

        // Set up input and output streams on clients.
        dest.changeWorkingDirectory(destPath);
        dest.setFileType(FTP.BINARY_FILE_TYPE);
        InputStream inputStream = ftpClient.retrieveFileStream(file);
        OutputStream outputStream = dest.storeFileStream(file);

        // Performs the copy, and shut down temporary client.
        outputStream.flush();
        inputStream.close();
        outputStream.close();

        boolean success = ftpClient.completePendingCommand();

        dest.logout();
        dest.disconnect();
        return success;
    }

    // Function to remove an entire directory recursively on the server.
    private static boolean removeDirectory(String currentPath, String directory) throws IOException {
        boolean success = false;

        String path;
        // Determine the path of the directory to be listed.
        if (!directory.equals("")) {
            path = currentPath + "/" + directory;
        } else {
            path = currentPath;
        }

        FTPFile[] files = ftpClient.listFiles(path);

        for (FTPFile file : files) {
            String fileName = file.getName();

            String filePath;
            // Append current directory before the filename, unless we are at the top directory.
            if (directory.equals("")) {
                filePath = currentPath + "/" + fileName;
            } else {
                filePath = currentPath + "/" + directory + "/" + fileName;
            }

            // If it's a directory, call function again with new directory.
            if (file.isDirectory()) {
                removeDirectory(path, fileName);
            } else {
                // Otherwise it's a file, so delte it.
                boolean deleted = ftpClient.deleteFile(filePath);
                if (deleted) {
                    System.out.println("Successfully removed the file: " + filePath);
                } else {
                    System.out.println("Unable to remove the file: " + filePath);
                }
            }
        }

        // After all the files have been deleted, try and delete the directory itself.
        try {
            success = ftpClient.removeDirectory(path);
            if (success) {
                System.out.println("Successfully removed directory: " + path);
            } else {
                System.out.println("Unable to remove directory: " + path);
            }

        } catch (FTPConnectionClosedException e) {
            exitWithError("The FTP server has closed the connection.", e, debug);
        } catch (IOException e) {
            exitWithError("An I/O error occurred when attempting to change to remote directory.", e, debug);
        }

        return success;
    }


    private static boolean validPermissions(String permissions) {
        return permissions.matches("[0-7][0-7][0-7]");
    }
    private static String readUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private static void printHelp() {
        System.out.println("This is a help section, this is where commands and usage info will go.");
        System.out.println("ls\t\t\t\t\t List files in current directory.");
        System.out.println("pwd\t\t\t\t\t Show current working directory.");
        System.out.println("mkdir <path>\t\t Create a directory on the remote server.");
        System.out.println("cd <path>\t\t\t Change the current working directory on the remote server.");
        System.out.println("get <path>\t\t\t Download the file at the given path on the server.");
        System.out.println("put <file>\t\t\t Upload the file to the remote server.");
        System.out.println("chmod <perm> <file> \t\t Change permissions on specified file.");
        System.out.println("cp <source> <dest>\t\t\t Copy source directory or file to destination");
        System.out.println("rm <path>\t\t\t Remove a directory on the server.");
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

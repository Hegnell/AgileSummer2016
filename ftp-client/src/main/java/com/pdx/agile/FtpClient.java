package com.pdx.agile;

import java.io.*;
import java.util.*;

//import com.sun.tools.doclets.internal.toolkit.util.DocFinder;
//import com.sun.tools.javac.file.SymbolArchive;
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
                } else if (firstArg.equals("lpwd")) {
                    try{
                        showLocalPath();
                    } catch (IOException e){
                        exitWithError("Unable to get the local path.", e, debug);
                    }
                } else if (firstArg.equals("ls")) {
                    try {
                        listFiles();
                    } catch (IOException e) {
                        exitWithError("Was unable to list the contents of the directory on the server.", e, debug);
                    }
                } else if (firstArg.equals("lls")) {
                    try{
                        listLocalFiles();
                    } catch(IOException e){
                        exitWithError("Unable to list local directories or files.", e, debug);
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
                } else if (firstArg.equals("lcd")) {
                    try{
                        if(userInput[1] != null && !userInput[1].equals("")){
                            changeLocalDir(userInput[1]);
                        }
                    } catch (IOException e){
                        exitWithError("Unable to change local directory", e, debug);
                    }
                } else if (firstArg.equals("get")) {
                    try {
                        String remotePath = userInput[1];
                        String localPath = userInput[2];
                        try {
                            retrieveFile(remotePath, localPath);
                        } catch (IOException e) {
			    System.out.println("Unable to retrieve the specified file or directory to remote server.");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Incorrect arguments specified to get. Type \"help\" for usage.\n");
                    }
                } else if (firstArg.equals("put")) {
                    try {
                        String localPath = userInput[1];
                        String remotePath = userInput[2];
                        try {
                            sendFiles(localPath, remotePath);
                        } catch (IOException e) {
			    System.out.println("Unable to send the specified file or directory to remote server.");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("Incorrect arguments specified to put. Type \"help\" for usage.\n");
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
                } else if (firstArg.equals("rmdir")) {
                    if (userInput.length != 2) {
                         System.out.println("Incorrect number of arguments provided to rmdir.");
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
            exitWithError("An I/O error occurred when attempting to remove file from the remote directory.", e, debug);
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

    // display current working local directory
    private static void showLocalPath() throws IOException {
        String currentDir = System.getProperty("user.dir");
        System.out.println(currentDir);
    }

    // List files story.
    private static void listFiles() throws IOException {
       FTPFile[] files = ftpClient.listFiles();

       for (FTPFile file : files) {
           System.out.println(file.getRawListing());
       }
    }

    private static void retrieveFile(String remotePath, String localPath) throws IOException {
        String previousWorkingDirectory = ftpClient.printWorkingDirectory();
        String root = getFilenameFromPath(remotePath); //the name - NOT full path - of the directory or file requested
        if (ftpClient.printWorkingDirectory().equals(remotePath) ||
                goToPath(remotePath) == true) {
            //we were in the requested directory or have successfully changed to it, so remotePath must be a directory
            if(localPath.endsWith("/") == false) {
                //ensure localPath ends with a "/"
                localPath = localPath + "/";
            }
            new File(localPath + root).mkdir();
            retrieveFileRecursive(".", localPath + root);
        } else {
            //remotePath is a file
            File localFile = new File(localPath + "/" + root);
            OutputStream os = new FileOutputStream(localFile);
            if(remotePath.startsWith("/") == false) {
                //handle relative path properly
                remotePath = "./" + remotePath;
            }
            if (ftpClient.retrieveFile(remotePath, os) == false) {
                System.out.println("File \"" + ftpClient.printWorkingDirectory() + "/" + remotePath + "\" not found on remote server");
                localFile.delete();
            }
            os.close();
        }
        ftpClient.changeWorkingDirectory(previousWorkingDirectory);
    }

    //helper function to strip off the file name from a relative path
    public static String getFilenameFromPath(String path) {
        int lastSlash = path.lastIndexOf("/");
        if(lastSlash == path.length()) {
            path = path.substring(0, path.length() - 1);
            lastSlash = path.lastIndexOf("/");
        }
        return path.substring(lastSlash + 1, path.length());
    }


    //helper function to strip the file name off of a path if supplied
    public static String getPathOnly(String path) {
        int lastSlash = path.lastIndexOf("/");
        if (lastSlash == -1) {
            //there is no path!
            return "";
        } else {
            return path.substring(0, lastSlash);
        }

    }


    private static boolean goToPath(String remotePath) throws IOException {
        if(remotePath.startsWith("/")){
            return ftpClient.changeWorkingDirectory(remotePath);
        } else {
            return ftpClient.changeWorkingDirectory("./" + remotePath);
        }
    }

    //Retrieve a directory structure recursively.
    private static void retrieveFileRecursive(String remotePath, String localPath) throws IOException {
        //remotePath is what to get, localPath is the directory to put it in
        if (ftpClient.changeWorkingDirectory("./" + remotePath) == true) {
            //directory change worked - remotePath is a directory, so we get multiple (recurse)
            new File(localPath + "/" + remotePath).mkdirs();
            for( FTPFile file : ftpClient.listFiles() ) {
                retrieveFileRecursive(file.getName(), localPath + "/" + remotePath);
            }
            ftpClient.changeWorkingDirectory("..");
        } else {
            File localFile = new File(localPath + "/" + remotePath);
            OutputStream os = new FileOutputStream(localFile);
            if (ftpClient.retrieveFile("./" + remotePath, os) == false) {
                //retrieveFileRecursive returns false if file is not found on remote server
                System.out.println("File \"" + ftpClient.printWorkingDirectory() + "/" + remotePath + "\" not found on remote server");
                localFile.delete();
            }
            os.close();
        }
    }


    // List local directories and files
    private static void listLocalFiles() throws IOException {
        // set the current working directory
        String currentDir = System.getProperty("user.dir");

        File directory = new File(currentDir);
        File[] fList = directory.listFiles();

        for (File file : fList){
            System.out.println(file.getName());
        }
    }

    // Change local directory
    private static boolean changeLocalDir(String localDirectory) throws IOException {
        boolean success = false;
        File directory;

        directory = new File(localDirectory).getAbsoluteFile();
        if(directory.exists() || directory.mkdir()){
            success = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return success;
    }

    //Create and store local files recursively on remote server
    private static void sendFileRecursive(File localFileObj) throws IOException {
        //localPath is what to get, remotePath is the directory to put it in
        if (localFileObj.isDirectory()) {
            if (ftpClient.changeWorkingDirectory( "./" + localFileObj.getPath())) {
                //directory change worked - localPath was already created on remote server
                File[] fList = localFileObj.listFiles();
                for( File localFileList : localFileObj.listFiles() ) {
                    sendFileRecursive(localFileList);
                }
            } else {
                // Directory hasn't been created
                if(ftpClient.makeDirectory(localFileObj.getName())) {
			System.out.println("Created remote directory: " + ftpClient.printWorkingDirectory() + "/" +  localFileObj.getName());
		} else {
			System.out.println("Could not create remote directory: " + ftpClient.printWorkingDirectory() + "/" + localFileObj.getName());
		}
                ftpClient.changeWorkingDirectory("./" + localFileObj.getName());
                File[] fList = localFileObj.listFiles();
                for( File localFileList : localFileObj.listFiles() ) {
                    sendFileRecursive(localFileList);
                }

            }
            ftpClient.changeWorkingDirectory("..");
        } else if(localFileObj.isFile()) {
            InputStream input;
            input = new FileInputStream(localFileObj.getAbsoluteFile());
            // store the file in the remote server
            if (ftpClient.storeFile(localFileObj.getName(), input)) {
                System.out.println(localFileObj.getName() + " uploaded successfully to: " + ftpClient.printWorkingDirectory() + localFileObj.getName());
            } else {
                // might have failed but unlikely
                System.out.println("could not create " + localFileObj.getName() + "Are you able to write to the remote location?");
            }
            input.close();
        } else {
            System.out.println("Could not access (permissions?): " + localFileObj.getAbsoluteFile());
        }

    }


    // Upload file or directory onto the server
    private static void sendFiles(String localPath, String remotePath) throws IOException {
        File localFileObj;
        //Save the current PWD for remote and restore it when we are done
        String previousRemoteWorkingDirectory = ftpClient.printWorkingDirectory();
        //determine if localPath is a file or directory
        try {
            localFileObj = new File(localPath);
            //let's check to see if their path is legit
            if(!localFileObj.exists()) {
                //they might have tried to use a relative path
                localFileObj = new File(System.getProperty("user.dir") + localPath);
                 if(!localFileObj.exists()) {
                     System.out.println("The specified file or directory does not exist: " + localPath);
                     return;
                 }
            }

        } catch(NullPointerException e) {
            System.out.println("Could not find a file or directory for specified local path: " + localPath);
            return;
        }
        //check if local is a directory
        if(localFileObj.isDirectory()) {
            // try to change working directory to the specified remote directory
            if(!chdirRemoteServer(remotePath)) {
                System.out.println("Could not find specified remote path: " + remotePath);
                return;
            }
            
            // if "./" is used for current local directory, this needs to be sanitized a bit
            if(localFileObj.getName().equals(".")) {
               localFileObj = new File(System.getProperty("user.dir"));
            }

	    //local and remote directories are good, start copying.
            sendFileRecursive(localFileObj);

        } else if(localFileObj.isFile()) {
            //If they supplied a path with a filename, strip the file name, regardless try to change to working directory
            chdirRemoteServer(getPathOnly(remotePath));
            //ensure the remote path is a file and write able
            String remoteFileName = getFilenameFromPath(remotePath);
            //Check to make sure it's actually a file name for remote. Otherwise try just creating
            if(remoteFileName.length() == 0) {
                System.out.println("A directory path was supplied. Using local file name for remote file name.");
                remoteFileName = localFileObj.getName();
            }
            InputStream input;
            input = new FileInputStream(localFileObj.getAbsoluteFile());
            System.out.println(localFileObj.getAbsoluteFile());
            // store the file in the remote server
            if (ftpClient.storeFile(remoteFileName, input)) {
                // if successful, print the following line
                System.out.println(localPath + " uploaded successfully to: " + remoteFileName);
            } else {
                // might be failed at this point
                System.out.println("Upload failed. Are you able to write to the remote location?");
            }
            // close the stream
            input.close();
        }
        // return to the previous working directory
        chdirRemoteServer(previousRemoteWorkingDirectory);
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
        System.out.println("Need some help? Here are the supported FTP client commands:");

        System.out.println("\nNavigating and getting around:");
        System.out.println("\tls\t\t\t\t\t List directories or files in current remote directory.");
        System.out.println("\tlls\t\t\t\t\t List local directories or files in the current local path.");
        System.out.println("\tpwd\t\t\t\t\t Show current working remote directory.");
        System.out.println("\tlpwd\t\t\t\t Show current working local directory.");
        System.out.println("\tcd <path>\t\t\t Change the current remote working directory.");
        System.out.println("\tlcd <path>\t\t\t Change the current local working directory.");

        System.out.println("\nSending, receiving, and changing files on the remote server:");
        System.out.println("\tget <rpath> <lpath>\t Download a remote file or directory at <rpath> to local folder <lpath>.");
        System.out.println("\tput <lpath> <rpath>\t Upload a local file or directory at <lpath> to the remote server at <rpath>.");
        System.out.println("\tcp <source> <dest>\t Copy source directory or file to destination another location on the remote server");
        System.out.println("\tchmod <perm> <file>\t Change permissions on specified file.");
        System.out.println("\trm <path>\t\t\t Remove a single file on the remote server.");
        System.out.println("\tmkdir <path>\t\t Create a directory on the remote server.");
        System.out.println("\trmdir <path>\t\t Remove a directory on the remote server.");

        System.out.println("\nOther commands:");
        System.out.println("\thelp\t\t\t\t Show this list of commands.");
        System.out.println("\tquit\t\t\t\t Disconnect and exit the program.");
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

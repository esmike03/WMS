package com.zebra.waltermartmobilecollector.services;

import android.database.sqlite.SQLiteStatement;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.Globals;
import com.zebra.waltermartmobilecollector.Helper;
import com.zebra.waltermartmobilecollector.interfaces.FTPFileLoopListener;
import com.zebra.waltermartmobilecollector.interfaces.FTPFileRowListener;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public final class FTP {
    private final static FTPClient ftp = new FTPClient();
    private final static FTPClient mmsFtp = new FTPClient();

    public static FTPClient getFtp() {
        return ftp;
    }

    public static FTPClient getMmsFtp() {
        return mmsFtp;
    }

    public static void login() throws Exception {
        try {
            // ✅ Disconnect first to avoid duplicate sessions
            disconnect();

            ftp.connect(Globals.getIpAddress());
            ftp.login(Globals.getFtpUser(), Globals.getFtpPassword());
            ftp.enterLocalPassiveMode();
            ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);

            if (!isConnected())
                throw new Exception("Failed to connect to FTP!!!");
        } catch (Exception e) {
            throw new Exception("No connection through FTP in " + Globals.getIpAddress() + "!!!");
        }
    }

    public static boolean isConnected() {
        return ftp.isConnected();
    }

    public static void checkConnection() throws Exception {
        try {
            // ✅ Disconnect first to avoid duplicate sessions
            disconnect();

            ftp.connect(Globals.getIpAddress());
            ftp.login(Globals.getFtpUser(), Globals.getFtpPassword());
            ftp.enterLocalPassiveMode();
            ftp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);

            if (!isConnected())
                throw new Exception("Failed to connect to FTP!!!");
        } catch (Exception e) {
            throw new Exception("No connection through FTP in " + Globals.getIpAddress() + "!!!");
        }
    }

    public static void disconnect() {
        try {
            if (!ftp.isConnected()) return;
            ftp.logout();
            ftp.disconnect();
        } catch (Exception e) {
        }
    }

    public static void loginMMS() throws Exception {
        try {
            // ✅ Disconnect first to avoid duplicate sessions
            disconnectMMS();

            mmsFtp.connect(Globals.getMmsIpAddress());
            mmsFtp.login(Globals.getMmsFtpUser(), Globals.getMmsFtpPassword());
            mmsFtp.enterLocalPassiveMode();
            mmsFtp.setFileType(org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE);

            if (!mmsFtp.isConnected())
                throw new Exception("Failed to connect to MMS FTP!!!");
        } catch (Exception e) {
            throw new Exception("No connection through MMS FTP in " + Globals.getMmsIpAddress() + "!!!");
        }
    }

    public static void disconnectMMS() {
        try {
            if (!mmsFtp.isConnected()) return;
            mmsFtp.logout();
            mmsFtp.disconnect();
        } catch (Exception e) {
        }
    }

    public static void uploadToMMS(String filepath, String content) throws Exception {
        File tempFile = File.createTempFile("ftpTemp", null);
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(content);
        writer.close();

        int lastSlash = filepath.lastIndexOf('/');
        String directory = lastSlash >= 0 ? filepath.substring(0, lastSlash) : "";
        String filename = lastSlash >= 0 ? filepath.substring(lastSlash + 1) : filepath;

        FileInputStream is = null;
        try {
            if (!directory.isEmpty())
                mmsFtp.changeWorkingDirectory(directory);
            is = new FileInputStream(tempFile);
            mmsFtp.storeFile(filename, is);
        } finally {
            try { tempFile.delete(); is.close(); } catch (Exception e) {}
        }
    }

    public static void makeMmsDirectory(String path) {
        try {
            mmsFtp.makeDirectory(path);
        } catch (Exception e) {
        }
    }

    public static ArrayList<String> getFilenames(String directory) throws Exception {
        if (directory != null)
            ftp.changeWorkingDirectory(directory);

        ArrayList<String> filenames = new ArrayList<>();

        for (FTPFile file : ftp.listFiles()) {
            if (file.isFile() && !file.getName().equalsIgnoreCase("STORE_CODE.TXT"))
                filenames.add(file.getName());
        }
        return filenames;
    }

    public static void downloadAsArraylist(String directory, String filename, FTPFileLoopListener listener) throws Exception {
        if (directory != null)
            ftp.changeWorkingDirectory(directory);

        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = ftp.retrieveFileStream(filename);
            if (is == null) throw new Exception("Unable to get or find file from FTP!!!");

            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null)
                listener.onRow(null, Arrays.asList(line.split(",")));

            ftp.completePendingCommand();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public static void loopThroughData(String filename, FTPFileRowListener listener) throws Exception {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = ftp.retrieveFileStream(filename);
            if (is == null) throw new Exception("Unable to get or find file from FTP!!!");

            reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null)
                listener.onRow(line);

            ftp.completePendingCommand();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public static FTPFile[] getFiles(String directory) throws Exception {
        if (directory != null)
            ftp.changeWorkingDirectory(directory);

        return ftp.listFiles();
    }

    public static void download(String directory, String filename, String sql, FTPFileLoopListener listener) throws Exception {
        if (directory != null)
            ftp.changeWorkingDirectory(directory);

        InputStream is = null;
        BufferedReader reader = null;
        SQLiteStatement statement = null;
        try {
            is = ftp.retrieveFileStream(filename);
            if (is == null) throw new Exception("Unable to get or find file from FTP!!!");

            reader = new BufferedReader(new InputStreamReader(is));
            statement = Globals.db.compileStatement(sql);

            String line;
            Globals.db.beginTransaction();
            while ((line = reader.readLine()) != null) {
                if (listener.onRow(
                        statement,
                        Helper.split(line)
                )) {
                    statement.executeInsert();
                    statement.clearBindings();
                }
            }

            Globals.db.setTransactionSuccessful();
            Globals.db.endTransaction();
            ftp.completePendingCommand();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                statement.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public static void upload(String filepath, String content) throws Exception {
        File tempFile = File.createTempFile("ftpTemp", null);

        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(content);
        writer.close();

        FileInputStream is = null;
        try {
            is = new FileInputStream(tempFile);
            ftp.storeFile(filepath, is);
        } finally {
            try {
                tempFile.delete();
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static void uploadFile(String fromFilepath, String toFilepath) throws Exception {
        FileInputStream is = null;
        try {
            is = new FileInputStream(FileService.mainFolder + fromFilepath);
            ftp.storeFile(toFilepath, is);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static void fetchStoreCode() throws Exception {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            ftp.changeWorkingDirectory(Folders.MASTERFILES);
            is = ftp.retrieveFileStream("STORE_CODE.TXT");
            if (is == null)
                throw new Exception("Unable to get or find store code file from FTP!!!");
            reader = new BufferedReader(new InputStreamReader(is));

            Globals.ftpStoreCode = reader.readLine();

            ftp.completePendingCommand();

            if (Globals.ftpStoreCode == null) throw new Exception("Empty store code file");
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public static void move(String from, String to) throws Exception {
        ftp.rename(from, to);
    }

    public static void copy(String oldFilePath, String newFilePath) throws Exception {
        File tempFile = File.createTempFile("ftpTemp", null);
        try {
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            boolean success = ftp.retrieveFile(oldFilePath, outputStream);
            outputStream.close();

            if (!success)
                throw new Exception("Error creating new file!!!");

            FileInputStream inputStream = new FileInputStream(tempFile);
            boolean uploadSuccess = ftp.storeFile(newFilePath, inputStream);
            inputStream.close();

            if (!uploadSuccess)
                throw new Exception("Failed to upload the file!!!");
        } finally {
            tempFile.delete();
        }
    }
}
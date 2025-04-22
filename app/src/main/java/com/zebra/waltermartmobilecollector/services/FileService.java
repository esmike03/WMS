package com.zebra.waltermartmobilecollector.services;

import android.os.Environment;

import com.zebra.waltermartmobilecollector.Folders;
import com.zebra.waltermartmobilecollector.activities.syncfile.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public final class FileService {

    public static final File mainFolder = new File(Environment.getExternalStorageDirectory() + "/AIOS");

    public static ArrayList<Model> getFilenames(String folder) {
        File directory = new File(mainFolder + folder);

        ArrayList<Model> list = new ArrayList<>();

        if (!directory.exists() || !directory.isDirectory()) return list;

        File[] files = directory.listFiles();
        if (files == null) return list;

        for (File file : files)
            if (file.isFile())
                list.add(new Model(file.getName(), folder));

        return list;
    }

//    public static void downloadDWProfile(InputStream inputStream) throws Exception {
//            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File outputFile = new File(storageDir, Globals.DW_PROFILE_FILENAME);
//
//            OutputStream outputStream = new FileOutputStream(outputFile);
//
//            // Write the file from inputStream to outputStream
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//
//            // Close streams
//            outputStream.close();
//            inputStream.close();
//    }

    public static void copy(String from, String to) throws Exception {
        File fromFile = new File(Environment.getExternalStorageDirectory() + from);
        File toFile = new File(Environment.getExternalStorageDirectory()+ to);
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(fromFile);
            fos = new FileOutputStream(toFile);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0)
                fos.write(buffer, 0, length);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String archive(Model model) {
        try {
            File file = new File(mainFolder + model.getFolder() + model.getFilename());

            if (!file.exists())
                return "File doesnt exists!!!";

            File to = new File(mainFolder + Folders.ARCHIVE + model.getFolder());
            if (!to.exists())
                to.mkdirs();

            File newFile = new File(to + "/" + model.getNewFilename());

            if (!file.renameTo(newFile))
                return "Error moving file to archive!!!";

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String move(String from, String to) {
        try {
            File file = new File(from);

            if (!file.exists())
                return "File doesnt exists!!!";

            if (!file.renameTo(new File(Environment.getExternalStorageDirectory() + to)))
                return "Error moving file to archive!!!";

            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static void download(String filename, String folder, String fileData) throws Exception {
        FileOutputStream fos = null;
        try {
            File directory = folder == null
                    ? mainFolder
                    : new File(mainFolder + folder);

            if (!directory.exists())
                directory.mkdirs();

            File file = new File(directory + "/" + filename);

            file.createNewFile();

            fos = new FileOutputStream(file);
            fos.write(fileData.getBytes());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void download(String filePath, String content) throws Exception {
        FileOutputStream fos = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + filePath);

            file.createNewFile();

            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

}

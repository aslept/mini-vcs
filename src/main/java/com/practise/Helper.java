package com.practise;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Helper {
    public static void objectWrite(Object obj, String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object objectRead(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha1(Path path) {
        byte[] b;
        MessageDigest messageDigest;

        try {
            b = Files.readAllBytes(path);
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] hash = messageDigest.digest(b);

        HexFormat hexFormat = HexFormat.of();
        String hex = hexFormat.formatHex(hash);

        return hex;
    }

    public static boolean sha1Checker (Path p, Path q) {
        String hex1 = sha1(p);
        String hex2 = sha1(q);

        return hex1.equalsIgnoreCase(hex2);
    }
}

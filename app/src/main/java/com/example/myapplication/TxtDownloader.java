package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TxtDownloader {

    public interface Callback {
        void onSuccess(List<Pair<String, Integer>> data);
        void onError(Exception e);
    }

    public static void descargarYLeerTxt(String urlString, File appFilesDir, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    File file = new File(appFilesDir, "datos.txt");
                    try (InputStream input = connection.getInputStream();
                         FileOutputStream output = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }

                    List<Pair<String, Integer>> tabla = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] partes = line.split("\t");
                            if (partes.length == 2) {
                                String nombre = partes[0].trim().replace("'", "");
                                int valor = Integer.parseInt(partes[1].trim().replace("'", ""));
                                tabla.add(new Pair<>(nombre, valor));
                            }
                        }
                    }

                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(tabla));

                } else {
                    throw new IOException("Error HTTP: " + connection.getResponseCode());
                }

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }
}

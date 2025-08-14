package com.example.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvDownloader {

    public interface Callback {
        void onSuccess(List<Pair<String, Integer>> data);
        void onError(Exception e);
    }

    private static final String FILE_NAME = "datos.csv";

    /** Conveniencia: descarga y luego lee */
    public static void descargarYLeerCsv(String urlString, File appFilesDir, Callback callback) {
        new Thread(() -> {
            try {
                File file = downloadCsv(urlString, appFilesDir);
                List<Pair<String, Integer>> tabla = parseCsv(file);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(tabla));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

    /** Lee el CSV local si existe, reutilizando el mismo parser */
    public static void leerLocalCsv(File appFilesDir, Callback callback) {
        new Thread(() -> {
            try {
                File file = new File(appFilesDir, FILE_NAME);
                if (!file.exists() || file.length() == 0) {
                    throw new FileNotFoundException("No existe " + file.getAbsolutePath());
                }
                List<Pair<String, Integer>> tabla = parseCsv(file);
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(tabla));
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

    /** Descarga el CSV y lo guarda como datos.csv */
    private static File downloadCsv(String urlString, File appFilesDir) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error HTTP: " + connection.getResponseCode());
        }

        Log.d("Tabla", "Descargando archivo CSV...");
        File file = new File(appFilesDir, FILE_NAME);
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        Log.d("Tabla", "Archivo guardado en: " + file.getAbsolutePath() + " | Tamaño: " + file.length() + " bytes");
        return file;
    }

    /** Parser reutilizable del CSV (salta encabezado, limpia miles, soporta negativos) */
    private static List<Pair<String, Integer>> parseCsv(File file) throws IOException {
        List<Pair<String, Integer>> tabla = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean primeraLinea = true;
            while ((line = reader.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; } // encabezado

                String[] partes = line.split(",");
                if (partes.length != 2) continue;

                String fecha = partes[0].trim().replace("'", "").replace("\"", "");
                String numeroStr = partes[1].trim().replace("'", "").replace("\"", "");

                try {
                    // quitar separador de miles (.) y mantener signo
                    String limpio = numeroStr.replace(".", "").trim();
                    int valor = Integer.parseInt(limpio);
                    tabla.add(new Pair<>(fecha, valor));
                    Log.d("Tabla", "Leído -> " + fecha + " | " + valor);
                } catch (NumberFormatException e) {
                    Log.w("Tabla", "Valor no numérico en línea: " + line);
                }
            }
        }
        return tabla;
    }
    public static void leerCsvLocal(File appFilesDir, Callback callback) {
        new Thread(() -> {
            try {
                File file = new File(appFilesDir, "datos.csv");
                if (!file.exists()) {
                    throw new FileNotFoundException("Archivo local no encontrado");
                }

                List<Pair<String, Integer>> tabla = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean primeraLinea = true;
                    while ((line = reader.readLine()) != null) {
                        if (primeraLinea) {
                            primeraLinea = false;
                            continue;
                        }
                        String[] partes = line.split(",");
                        if (partes.length == 2) {
                            String nombre = partes[0]
                                    .trim()
                                    .replace("'", "")
                                    .replace("\"", "");
                            String numeroStr = partes[1]
                                    .trim()
                                    .replace("'", "")
                                    .replace("\"", "");

                            try {
                                String numeroLimpio = numeroStr.replace(".", "").trim();
                                int valor = Integer.parseInt(numeroLimpio);
                                tabla.add(new Pair<>(nombre, valor));
                                Log.d("Tabla", "Leído local -> " + nombre + " | " + valor);
                            } catch (NumberFormatException e) {
                                Log.w("Tabla", "Valor no numérico en: " + line);
                            }
                        }
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(tabla));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        }).start();
    }

}
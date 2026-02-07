
package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private static final double INTERES_MENSUAL = 0.015;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
// Cargar último URL al iniciar
        binding.editTextString.setText(cargarUltimoUrl());

        binding.buttonUpdate.setOnClickListener(v -> {
            String url = binding.editTextString.getText().toString().trim();

            if (!url.isEmpty()) {
                // Si hay URL -> descargar y leer
                guardarUltimoUrl(url);
                CsvDownloader.descargarYLeerCsv(url, requireContext().getFilesDir(), callbackProcesarDatos());
            } else {
                // Si no hay URL -> intentar leer el CSV local
                File file = new File(requireContext().getFilesDir(), "datos.csv");
                if (file.exists()) {
                    CsvDownloader.leerCsvLocal(requireContext().getFilesDir(), callbackProcesarDatos());
                } else {
                    Toast.makeText(getContext(), "No hay URL ni archivo local guardado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    private double idFromIm(double im) {
        return Math.pow(1 + im, 1.0 / 30.0) - 1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private CsvDownloader.Callback callbackProcesarDatos() {
        return new CsvDownloader.Callback() {
            @Override
            public void onSuccess(List<Pair<String, Integer>> data) {
                Log.d("Tabla", "Datos obtenidos: " + data.size());

                double interesDiario = idFromIm(INTERES_MENSUAL);
                double valor = 0.0, valorPos = 0.0, pagado = 0.0, mensual = 0.0;

                if (!data.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                    LocalDate fechaAnterior = LocalDate.parse(data.get(0).first, formatter);
                    double ingresoInicial = data.get(0).second;
                    valorPos += ingresoInicial;
                    valor += ingresoInicial;

                    for (int i = 1; i < data.size(); i++) {
                        Pair<String, Integer> par = data.get(i);
                        LocalDate fechaActual = LocalDate.parse(par.first, formatter);
                        double ingreso = par.second;

                        if (ingreso < 0) pagado += -ingreso;
                        else valorPos += ingreso;

                        long dias = ChronoUnit.DAYS.between(fechaAnterior, fechaActual);
                        valor = valor * Math.pow(1 + interesDiario, dias);
                        valor += ingreso;
                        fechaAnterior = fechaActual;
                    }

                    mensual = valor * INTERES_MENSUAL;
                }

                NumberFormat formatoCOP = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
                formatoCOP.setMaximumFractionDigits(0);
                binding.textAcumuladoValor.setText(formatoCOP.format(valor));
                binding.textMensualValor.setText(formatoCOP.format(mensual));

                List<ResultadoItem> listaResultado = new ArrayList<>();
                for (Pair<String, Integer> par : data) {
                    listaResultado.add(new ResultadoItem(par.first, par.second));
                }
                binding.recyclerResultados.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerResultados.setAdapter(new ResultadoAdapter(listaResultado));
            }

            @Override
            public void onError(Exception e) {
                Log.e("Tabla", "Error: " + e.getMessage());
            }
        };
    }
    private void guardarUltimoUrl(String url) {
        SharedPreferences prefs = requireContext().getSharedPreferences("mis_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("ultimo_url", url).apply();
    }
    private String cargarUltimoUrl() {
        SharedPreferences prefs = requireContext().getSharedPreferences("mis_prefs", Context.MODE_PRIVATE);
        return prefs.getString("ultimo_url", "");
    }

}
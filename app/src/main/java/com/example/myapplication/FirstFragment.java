
package com.example.myapplication;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private String strValue;
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Log.d("Tabla", "hola_aqui_estoy_-1");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        Log.d("Tabla", "hola_aqui_estoy_0");
        binding.buttonUpdate.setOnClickListener(v ->{
            Log.d("Tabla","hola_aqui_estoy");
            strValue = binding.editTextString.getText().toString();
            TxtDownloader.descargarYLeerTxt(strValue, requireContext().getFilesDir(), new TxtDownloader.Callback() {
                @Override
                public void onSuccess(List<Pair<String, Integer>> data) {
                    for (Pair<String, Integer> par : data) {
                        Log.d("Tabla", par.first + " -> " + par.second);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Tabla", "Error al descargar o leer archivo: " + e.getMessage());
                }
            });
            //buscar en la api de google sheets el link de strValue y si es una sheet publica buscar unos datos
        });
        binding.buttonCalculate.setOnClickListener(v -> {
            // Revisamos si el archivo existe
            File file = new File(requireContext().getFilesDir(), "datos.txt");
            if (file.exists() && file.length() > 0) {
                // Si el archivo existe y tiene datos, proceder
                int searchValue;
                try {
                    searchValue = Integer.parseInt(binding.editTextInt.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e("Tabla", "El campo de número no tiene un valor válido");
                    return;
                }

                // Leer el archivo y buscar el valor correspondiente
                calculateProportions(file, searchValue);
            } else {
                Log.e("Tabla", "El archivo no existe o está vacío");
            }
        });

        return binding.getRoot();

    }

//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
////        binding.buttonFirst.setOnClickListener(v ->
////                NavHostFragment.findNavController(FirstFragment.this)
////                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
////        );
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void calculateProportions(File file, int inputValue) {
        List<Pair<String, Integer>> data = new ArrayList<>();
        List<ResultadoItem> listaResultado = new ArrayList<>();
        int total = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    String name = parts[0].trim().replace("'", "");
                    int value = Integer.parseInt(parts[1].trim().replace("'", ""));
                    data.add(new Pair<>(name, value));
                    total += value;
                }
            }

            if (total > 0) {
                for (Pair<String, Integer> pair : data) {
                    String name = pair.first;
                    int value = pair.second;

                    double percentage = (double) value / total;
                    double result = percentage * inputValue;
                    int roundedResult = Math.round((float)(result / 50)) * 50;

                    listaResultado.add(new ResultadoItem(name, roundedResult));

                    Log.d("Tabla", "Nombre: " + name +
                            " -> Valor original: " + value +
                            " | Porcentaje: " + String.format("%.2f", percentage * 100) + "%" +
                            " | Asignado: $" + roundedResult);
                }

                // Mostrar resultados en el RecyclerView
                binding.recyclerResultados.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.recyclerResultados.setAdapter(new ResultadoAdapter(listaResultado));

            } else {
                Log.e("Tabla", "El total de los valores es cero");
            }

        } catch (IOException e) {
            Log.e("Tabla", "Error al leer el archivo: " + e.getMessage());
        }
    }

}
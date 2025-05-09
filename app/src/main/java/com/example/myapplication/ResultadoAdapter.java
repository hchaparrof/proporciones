package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResultadoAdapter extends RecyclerView.Adapter<ResultadoAdapter.ViewHolder> {
    private final List<ResultadoItem> resultados;

    public ResultadoAdapter(List<ResultadoItem> resultados) {
        this.resultados = resultados;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textValor;

        public ViewHolder(View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.text_nombre);
            textValor = itemView.findViewById(R.id.text_valor);
        }
    }

    @NonNull
    @Override
    public ResultadoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resultado, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResultadoItem item = resultados.get(position);
        holder.textNombre.setText(item.nombre);
        holder.textValor.setText("$" + item.valorCalculado);
    }

    @Override
    public int getItemCount() {
        return resultados.size();
    }
}

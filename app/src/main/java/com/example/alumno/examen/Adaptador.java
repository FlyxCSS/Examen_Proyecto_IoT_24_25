package com.example.alumno.examen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

// 5. RECYCLERVIEW
public class Adaptador extends FirestoreRecyclerAdapter<Eleccion, Adaptador.ViewHolder> {

    public Adaptador(@NonNull FirestoreRecyclerOptions<Eleccion> options) {
        super(options);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView partido, año, presidente;

        public ViewHolder(View itemView) {
            super(itemView);
            partido = itemView.findViewById(R.id.partido);
            año = itemView.findViewById(R.id.anyo);
            presidente = itemView.findViewById(R.id.presidente);
        }
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.elementos_lista, parent, false);
        return new ViewHolder(view);
    }

    @Override protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Eleccion lectura){
        holder.año.setText(""+lectura.getAnyo());
        holder.partido.setText(""+lectura.getPartido());
        holder.presidente.setText(""+lectura.getPresidente());
    }
}
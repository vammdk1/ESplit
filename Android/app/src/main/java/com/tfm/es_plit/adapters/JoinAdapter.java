package com.tfm.es_plit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tfm.es_plit.R;
import com.tfm.es_plit.data.fakeUsers;
import com.tfm.es_plit.models.Participant;

import java.util.List;

public class JoinAdapter extends RecyclerView.Adapter<JoinAdapter.ViewHolder> {

    private fakeUsers fakeRepository;

    /**
     * Intefaz para procesar lógico dentro del adaptador
     * */
    public interface OnParticipantActionListener {
        void onRemove(Participant participant);
        void onConfirm(Participant participant);
    }

    //Lista de participantes a dibujar
    private final List<Participant> participants;

    //Listener para realziar acciones en el código

    public JoinAdapter(List<Participant> participants, fakeUsers fakeRepository) {
        this.participants = participants;
        this.fakeRepository=fakeRepository;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant_join, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Participant p = participants.get(position);
        holder.tvName.setText(p.getName());
        holder.tvAmount.setText(String.format("€%.2f", p.getAmount()));
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    //declaración de elementos visuales
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        Button btConfirm, btRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvParticipantName);
            tvAmount = itemView.findViewById(R.id.tvParticipantAmount);
            btConfirm = itemView.findViewById(R.id.btnConfirm);
            btRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
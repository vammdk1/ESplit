package com.tfm.es_plit.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.tfm.es_plit.R;
import com.tfm.es_plit.network.PaymentSocket;
import com.tfm.es_plit.models.Participant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private PaymentSocket socket;

    public interface OnParticipantActionListener {
        void onRemove(Participant participant);
        void onConfirm(Participant participant);
    }

    private final List<Participant> participants;
    private final OnParticipantActionListener listener;

    public ParticipantAdapter(List<Participant> participants, OnParticipantActionListener listener, PaymentSocket socket) {
        this.participants = participants;
        this.listener = listener;
        this.socket = socket;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Participant p = participants.get(position);
        holder.tvName.setText(p.getName());
        holder.tvAmount.setText(String.format("€%.2f", p.getAmount()));

        // refleja el estado actual (por si llega un confirm_response y se redibuja la lista)
        if (p.getConfirmationStatus()) {
            holder.tvName.setTextColor(Color.parseColor("#05d61a"));
        } else {
            holder.tvName.setTextColor(Color.BLACK); // o el color por defecto que uses
        }

        holder.btRemove.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                participants.remove(pos);
                notifyItemRemoved(pos);
                listener.onRemove(p);
            }
        });

        holder.btConfirm.setOnClickListener(v -> {
            try {
                JSONObject request = new JSONObject();
                request.put("type", "confirm_request");
                request.put("user_id", p.getid());
                request.put("amount", p.getAmount());
                socket.send(request);
            } catch (JSONException e) {
                Log.e("WS", "Error armando mensaje: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

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
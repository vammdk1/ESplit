package com.tfm.es_plit.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.tfm.es_plit.R;
import com.tfm.es_plit.models.User;
import com.tfm.es_plit.network.UserRepository;
import com.tfm.es_plit.models.Participant;

import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private UserRepository userRepository;

    public interface OnParticipantActionListener {
        void onRemove(Participant participant);
        void onConfirm(Participant participant);
    }

    private final List<Participant> participants;
    private final OnParticipantActionListener listener;

    public ParticipantAdapter(List<Participant> participants, OnParticipantActionListener listener, UserRepository userRepository) {
        this.participants = participants;
        this.listener = listener;
        this.userRepository = userRepository;
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

        holder.btRemove.setOnClickListener(v -> {
            int pos = holder.getAbsoluteAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                participants.remove(pos);
                notifyItemRemoved(pos);
                listener.onRemove(p);
            }
        });

        holder.btConfirm.setOnClickListener(v -> {
            userRepository.getUserById(p.getid(), new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user.getFunds() >= p.getAmount()) {
                        holder.tvName.setTextColor(Color.parseColor("#05d61a"));
                        p.setConfirmationStatus(true);
                    } else {
                        holder.tvName.setTextColor(Color.parseColor("#f8352a"));
                    }
                    listener.onConfirm(p);
                }

                @Override
                public void onError(String message) {
                    holder.tvName.setTextColor(Color.parseColor("#f8352a"));
                }
            });
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
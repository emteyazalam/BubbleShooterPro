package com.redcodersgroup.bubbleshooter.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.redcodersgroup.bubbleshooter.databinding.ItemAvatarChoiceBinding;
import com.redcodersgroup.bubbleshooter.profile.AvatarManager;
import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    public interface OnAvatarSelectedListener {
        void onAvatarSelected(AvatarManager.AvatarItem avatar);
    }

    private final List<AvatarManager.AvatarItem> avatars;
    private final OnAvatarSelectedListener listener;
    private String selectedAvatarId;

    public AvatarAdapter(List<AvatarManager.AvatarItem> avatars, String initialSelectedId, OnAvatarSelectedListener listener) {
        this.avatars = avatars;
        this.selectedAvatarId = (initialSelectedId != null) ? initialSelectedId : AvatarManager.DEFAULT_AVATAR_ID;
        this.listener = listener;
    }

    public void setSelectedAvatarId(String avatarId) {
        this.selectedAvatarId = avatarId;
        notifyDataSetChanged();
    }

    public String getSelectedAvatarId() {
        return selectedAvatarId;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAvatarChoiceBinding binding = ItemAvatarChoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AvatarViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        AvatarManager.AvatarItem item = avatars.get(position);
        boolean isSelected = item.id.equalsIgnoreCase(selectedAvatarId);

        holder.binding.ivAvatarIcon.setImageResource(item.drawableResId);
        holder.binding.viewSelectionRing.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.binding.ivCheckmarkBadge.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.binding.viewUnselectedBase.setVisibility(isSelected ? View.GONE : View.VISIBLE);

        holder.binding.layoutAvatarItemContainer.setScaleX(isSelected ? 1.05f : 1.0f);
        holder.binding.layoutAvatarItemContainer.setScaleY(isSelected ? 1.05f : 1.0f);

        holder.binding.layoutAvatarItemContainer.setOnClickListener(v -> {
            selectedAvatarId = item.id;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onAvatarSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        final ItemAvatarChoiceBinding binding;

        AvatarViewHolder(ItemAvatarChoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

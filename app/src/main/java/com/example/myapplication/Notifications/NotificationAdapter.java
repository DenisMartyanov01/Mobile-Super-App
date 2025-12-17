package com.example.myapplication.Notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationData> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationData notification);
        void onNotificationLongClick(NotificationData notification);
    }

    public NotificationAdapter(List<NotificationData> notifications,
                               OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationData notification = notifications.get(position);

        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getFormattedTime());

        // Простой статус
        updateStatus(holder, notification);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotificationLongClick(notification);
                return true;
            }
            return false;
        });
    }

    private void updateStatus(ViewHolder holder, NotificationData notification) {
        long currentTime = System.currentTimeMillis();
        long notificationTime = notification.getTimeInMillis();

        if (currentTime >= notificationTime) {
            // Уведомление просрочено - будет удалено через секунду
            holder.statusTextView.setText("ПРОСРОЧЕНО");
            holder.statusTextView.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(android.R.color.holo_red_dark)
            );
        } else {
            // Уведомление еще активно
            long remaining = notificationTime - currentTime;
            long seconds = remaining / 1000;
            long minutes = seconds / 60;

            if (seconds < 60) {
                holder.statusTextView.setText("Через " + seconds + " сек");
                holder.statusTextView.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(android.R.color.holo_orange_dark)
                );
            } else {
                holder.statusTextView.setText("Через " + minutes + " мин");
                holder.statusTextView.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(android.R.color.holo_green_dark)
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateNotifications(List<NotificationData> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;
        public TextView statusTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
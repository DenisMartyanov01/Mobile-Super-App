package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

        // Устанавливаем данные в элементы View
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());
        holder.timeTextView.setText(notification.getFormattedTime());

        // Обновляем статус времени
        updateRemainingTime(holder, notification);

        // Обработчики кликов
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

    // Метод для обновления оставшегося времени (может вызываться извне)
    public void updateRemainingTime(ViewHolder holder, NotificationData notification) {
        holder.statusTextView.setText(notification.getRemainingTime());

        // Меняем цвет статуса в зависимости от времени
        if (notification.isTimePassed()) {
            holder.statusTextView.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(android.R.color.holo_red_dark)
            );
        } else {
            // Определяем цвет по оставшемуся времени
            long remaining = notification.getTimeInMillis() - System.currentTimeMillis();
            long minutes = remaining / (60 * 1000);

            if (minutes < 5) {
                // Менее 5 минут - красный
                holder.statusTextView.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(android.R.color.holo_red_dark)
                );
            } else if (minutes < 60) {
                // Менее часа - оранжевый
                holder.statusTextView.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(android.R.color.holo_orange_dark)
                );
            } else {
                // Более часа - зеленый
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

    // Метод для обновления списка
    public void updateNotifications(List<NotificationData> newNotifications) {
        this.notifications = newNotifications;
        notifyDataSetChanged();
    }

    // Метод для удаления элемента
    public void removeNotification(int position) {
        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Внутренний класс ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;
        TextView statusTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.NotificationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationResponse> notifications = new ArrayList<>();
    private OnNotificationClickListener clickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationResponse notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addNotifications(List<NotificationResponse> newNotifications) {
        if (newNotifications != null && !newNotifications.isEmpty()) {
            int startPosition = this.notifications.size();
            this.notifications.addAll(newNotifications);
            notifyItemRangeInserted(startPosition, newNotifications.size());
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponse notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final View unreadIndicator;
        private final ImageView iconImageView;
        private final TextView titleTextView;
        private final TextView contentTextView;
        private final TextView timeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            unreadIndicator = itemView.findViewById(R.id.v_unread_indicator);
            iconImageView = itemView.findViewById(R.id.iv_notification_icon);
            titleTextView = itemView.findViewById(R.id.tv_notification_title);
            contentTextView = itemView.findViewById(R.id.tv_notification_content);
            timeTextView = itemView.findViewById(R.id.tv_notification_time);
        }

        public void bind(NotificationResponse notification) {
            titleTextView.setText(notification.getTitle());
            contentTextView.setText(notification.getContent());
            timeTextView.setText(formatTime(notification.getCreatedAt()));

            // Show/hide unread indicator
            unreadIndicator.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });

            // Set icon based on notification type
            setNotificationIcon(notification.getType());
        }

        private void setNotificationIcon(String type) {
            // Set icon based on notification type using existing app icons
            if (type == null) {
                iconImageView.setImageResource(R.drawable.ic_notification);
                return;
            }

            switch (type.toUpperCase()) {
                case "SOCIAL":
                    // Backend sends "SOCIAL" for all community interactions:
                    // post likes, comments, and comment likes
                    iconImageView.setImageResource(R.drawable.ic_community);
                    break;
                case "WORKOUT_REMINDER":
                    // Use dumbbell/exercise icon for workout reminders
                    iconImageView.setImageResource(R.drawable.ic_exercises_24);
                    break;
                case "SYSTEM":
                case "ADMIN":
                case "ANNOUNCEMENT":
                    // Use notification icon for system messages
                    iconImageView.setImageResource(R.drawable.ic_notification);
                    break;
                default:
                    iconImageView.setImageResource(R.drawable.ic_notification);
                    break;
            }
        }

        private String formatTime(String createdAt) {
            if (createdAt == null || createdAt.isEmpty()) {
                return "";
            }

            try {
                // Parse the date from backend format: "dd/MM/yyyy HH:mm:ss"
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(createdAt);

                if (date == null) {
                    return createdAt;
                }

                long timeMillis = date.getTime();
                long now = System.currentTimeMillis();
                long diff = now - timeMillis;

                // Convert to relative time in Vietnamese
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (seconds < 60) {
                    return "Vừa xong";
                } else if (minutes < 60) {
                    return minutes + " phút trước";
                } else if (hours < 24) {
                    return hours + " giờ trước";
                } else if (days < 7) {
                    return days + " ngày trước";
                } else {
                    // Show actual date for older notifications
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                return createdAt;
            }
        }
    }
}

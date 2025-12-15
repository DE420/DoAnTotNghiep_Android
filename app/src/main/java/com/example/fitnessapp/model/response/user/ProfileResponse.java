package com.example.fitnessapp.model.response.user;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.common.ChartDataResponse;

import java.util.List;

public class ProfileResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String avatar;
    private String sex;
    private FitnessGoal fitnessGoal;
    private ActivityLevel activityLevel;

    private String dateOfBirth;

    private Double weight;
    private Double height;

    private String memberSince;

    private int totalWorkouts; // Tổng số buổi tập (All time)
    private double totalHours; // Tổng số giờ tập (All time)
    private double totalCalories; // Tổng calo (All time)

    private MonthlyStats monthlyStats;

    public static class MonthlyStats {
        private String monthName;
        private int totalWorkouts;
        private int activeDays;
        private int currentStreak;
        private double totalDurationMin;
        private double avgDurationMin;
        private double totalCalories;

        // Dành cho biểu đồ (Graph)
        private List<ChartDataResponse> chartData;

        public String getMonthName() {
            return monthName;
        }

        public int getTotalWorkouts() {
            return totalWorkouts;
        }

        public int getActiveDays() {
            return activeDays;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public double getTotalDurationMin() {
            return totalDurationMin;
        }

        public double getAvgDurationMin() {
            return avgDurationMin;
        }

        public double getTotalCalories() {
            return totalCalories;
        }

        public List<ChartDataResponse> getChartData() {
            return chartData;
        }

        @Override
        public String toString() {
            return "MonthlyStats{" +
                    "monthName='" + monthName + '\'' +
                    ", totalWorkouts=" + totalWorkouts +
                    ", activeDays=" + activeDays +
                    ", currentStreak=" + currentStreak +
                    ", totalDurationMin=" + totalDurationMin +
                    ", avgDurationMin=" + avgDurationMin +
                    ", totalCalories=" + totalCalories +
                    ", chartData=" + chartData +
                    '}';
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getSex() {
        return sex;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public Double getWeight() {
        return weight;
    }

    public Double getHeight() {
        return height;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public int getTotalWorkouts() {
        return totalWorkouts;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public MonthlyStats getMonthlyStats() {
        return monthlyStats;
    }

    @Override
    public String toString() {
        return "ProfileResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", sex='" + sex + '\'' +
                ", fitnessGoal=" + fitnessGoal +
                ", activityLevel=" + activityLevel +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", memberSince='" + memberSince + '\'' +
                ", totalWorkouts=" + totalWorkouts +
                ", totalHours=" + totalHours +
                ", totalCalories=" + totalCalories +
                ", monthlyStats=" + monthlyStats +
                '}';
    }
}

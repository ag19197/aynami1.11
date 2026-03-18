package com.gs.ayanami.model;

import java.util.List;

public class TripPlan {
    private String trip_name;
    private String overview;
    private List<Day> days;
    private List<Hotel> hotels;
    private String budget_advice;

    // getters and setters (或使用Lombok @Data)
    public String getTrip_name() { return trip_name; }
    public void setTrip_name(String trip_name) { this.trip_name = trip_name; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public List<Day> getDays() { return days; }
    public void setDays(List<Day> days) { this.days = days; }
    public List<Hotel> getHotels() { return hotels; }
    public void setHotels(List<Hotel> hotels) { this.hotels = hotels; }
    public String getBudget_advice() { return budget_advice; }
    public void setBudget_advice(String budget_advice) { this.budget_advice = budget_advice; }

    public static class Day {
        private int day;
        private List<Location> locations;

        // getters/setters
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
        public List<Location> getLocations() { return locations; }
        public void setLocations(List<Location> locations) { this.locations = locations; }
    }

    public static class Location {
        private String name;
        private double latitude;
        private double longitude;
        private String description;
        private String real_photo;
        private String anime_photo;

        // getters/setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getReal_photo() { return real_photo; }
        public void setReal_photo(String real_photo) { this.real_photo = real_photo; }
        public String getAnime_photo() { return anime_photo; }
        public void setAnime_photo(String anime_photo) { this.anime_photo = anime_photo; }
    }

    public static class Hotel {
        private String name;
        private String description;
        private String price;

        // getters/setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
    }
}
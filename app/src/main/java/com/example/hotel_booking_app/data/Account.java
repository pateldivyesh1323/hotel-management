package com.example.hotel_booking_app.data;

public final class Account {
    public final String email;
    public final String name;
    public final String phone;
    public final String passwordHash;
    public final boolean admin;

    public Account(String email, String name, String phone, String passwordHash, boolean admin) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.admin = admin;
    }

    public Account withProfile(String name, String phone) {
        return new Account(email, name, phone, passwordHash, admin);
    }
}

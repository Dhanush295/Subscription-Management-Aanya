package com.netflixsub.user;

import com.netflixsub.common.Json;
import java.time.Instant;

public class Profile {
    public final String userId;
    public String firstName;
    public String lastName;
    public int age;
    public String email;
    public Instant createdAt = Instant.now();

    public Profile(String userId, String firstName, String lastName, int age, String email) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
    }

    public Profile(String userId, String firstName, String lastName, int age, String email, Instant createdAt) {
        this(userId, firstName, lastName, age, email);
        if (createdAt != null) this.createdAt = createdAt;
    }

    public String fullName() { return (firstName + " " + lastName).trim(); }

    @Override
    public String toString() {
        return "{\"userId\":\"" + Json.esc(userId) + "\","
             + "\"firstName\":\"" + Json.esc(firstName) + "\","
             + "\"lastName\":\"" + Json.esc(lastName) + "\","
             + "\"fullName\":\"" + Json.esc(fullName()) + "\","
             + "\"age\":" + age + ","
             + "\"email\":\"" + Json.esc(email) + "\","
             + "\"createdAt\":\"" + createdAt + "\"}";
    }
}

package org.keycloak.gateway.isendpro;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public class SmsRequest {
    @JsonProperty("emetteur")
    private String from;

    @JsonProperty("sms")
    private String message;

    @JsonProperty("num")
    private String phone;

    @JsonProperty("keyid")
    private String key;

    @JsonProperty("nostop")
    private long noStop;

    private UUID tracker;

    public String getFrom() {
        return from;
    }

    public SmsRequest setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SmsRequest setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public SmsRequest setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getKey() {
        return key;
    }

    public SmsRequest setKey(String key) {
        this.key = key;
        return this;
    }

    public long getNoStop() {
        return noStop;
    }

    public SmsRequest setNoStop(long noStop) {
        this.noStop = noStop;
        return this;
    }

    public UUID getTracker() {
        return tracker;
    }

    public SmsRequest setTracker(UUID tracker) {
        this.tracker = tracker;
        return this;
    }

    @Override
    public String toString() {
        return "SmsRequest{" +
                "from='" + from + '\'' +
                ", message='" + message + '\'' +
                ", phone='" + phone + '\'' +
                ", key='" + key + '\'' +
                ", smslong=" + noStop +
                ", tracker=" + tracker +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        SmsRequest that = (SmsRequest) object;
        return noStop == that.noStop &&
                Objects.equals(from, that.from) &&
                Objects.equals(message, that.message) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(key, that.key) &&
                Objects.equals(tracker, that.tracker);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, message, phone, key, noStop, tracker);
    }
}

package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;


@Getter
public enum State {

    IN_ACTIVATION("in-activation", "In Activation"),
    ACTIVE("active", "Active"),
    IN_UPDATE("in-update", "In Update"),
    IN_BLOCKING("in-blocking", "In Blocking"),
    BLOCKED("blocked", "Blocked"),
    IN_DELETION("in-deletion", "In Deletion"),
    IN_SELF_RECOVERABLE_ERROR("in-self-recoverable-error", "In Self-Recoverable Error"),
    IN_RECOVERABLE_ERROR("in-recoverable-error", "In Recoverable Error"),
    FINAL_ERROR("final-error", "Final Error"),
    UNKNOWN("Unknown", "unknown");

    private final String state;
    private final String readableState;

    State(String state, String readableState) {
        this.state = state;
        this.readableState = readableState;
    }

    @JsonCreator
    public static State fromString(String state) {
        if (state == null) {
            return null; // Or throw IllegalArgumentException if needed
        }
        state = state.replaceAll("_", "-");
        return switch (state.toLowerCase()) {
            case "in-activation" -> IN_ACTIVATION;
            case "active" -> ACTIVE;
            case "in-update" -> IN_UPDATE;
            case "in-deletion" -> IN_DELETION;
            case "in-self-recoverable-error" -> IN_SELF_RECOVERABLE_ERROR;
            case "in-recoverable-error" -> IN_RECOVERABLE_ERROR;
            case "final-error" -> FINAL_ERROR;
            default -> throw new IllegalArgumentException("Unknown state: " + state);
        };
    }

    // Static method to find ResourceState by its readableState (case insensitive)
    public static State fromReadableState(String readableState) {
        if (readableState != null) {
            for (State resourceState : State.values()) {
                if (resourceState.getReadableState().equalsIgnoreCase(readableState)) {
                    return resourceState;
                }
            }
        }
        return UNKNOWN;  // Default to UNKNOWN if no match found
    }

    // This method is optional, for serializing the enum back to a string
    //@JsonValue
    public String getState() {
        return state;
    }

    @JsonValue  //for serializing the enum back to a string
    public String getReadableState() {
        return readableState;
    }
}

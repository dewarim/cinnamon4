package com.dewarim.cinnamon.api.lifecycle;

import java.util.List;

public class StateChangeResult {

    private final boolean      successful;
    private       List<String> messages;

    public StateChangeResult(boolean successful) {
        this.successful = successful;
    }

    public StateChangeResult(boolean successful, List<String> messages) {
        this.successful = successful;
        this.messages = messages;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getCombinedMessages(){
        if (messages == null) {
            return "";
        } else {
            return String.join("\n",messages);
        }
    }

    @Override
    public String toString() {
        return "StateChangeResult{" +
                "successful=" + successful +
                ", messages=" + String.join("\n",messages) +
                '}';
    }
}

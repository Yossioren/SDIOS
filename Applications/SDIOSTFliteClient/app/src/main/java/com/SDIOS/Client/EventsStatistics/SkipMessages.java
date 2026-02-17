package com.SDIOS.Client.EventsStatistics;

public class SkipMessages {
    private final int skip_amount;
    private int count = 0;

    public SkipMessages(int skip_amount) {
        this.skip_amount = skip_amount;
    }

    public boolean should_skip() {
        if (count > skip_amount) {
            count = 0;
            return false;
        }
        count += 1;
        return true;
    }
}

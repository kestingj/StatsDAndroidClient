package com.amazon.statsdclient;

/**
 * Created by kestingj on 7/15/15.
 */
public class StatsDClientKey {

    public final String prefix;

    public final String domain;

    public final int port;

    public final boolean operational;

    public StatsDClientKey(final String prefix, final String domain, final int port, final boolean operational) {
        // Validate.notEmpty(prefix, "Prefix can't be empty");
        // Validate.notEmpty(prefix, "Prefix can't be empty");
        // Validate.notEmpty(prefix, "Prefix can't be empty");
        this.prefix = prefix;
        this.domain = domain;
        this.port = port;
        this.operational = operational;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatsDClientKey that = (StatsDClientKey) o;

        if (port != that.port) return false;
        if (prefix != null ? !prefix.equals(that.prefix) : that.prefix != null) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        return this.operational == that.operational;

    }

    @Override
    public int hashCode() {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + port;
        if (operational) result++;
        return result;
    }
}

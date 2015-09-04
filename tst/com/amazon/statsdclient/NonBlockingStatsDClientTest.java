package com.amazon.statsdclient;

import android.provider.Settings;

import com.amazon.statsdclient.NonBlockingStatsDClient;
import com.amazon.statsdclient.StatsDClientContainer;

import org.junit.*;

import java.io.IOException;
import java.lang.Exception;
import java.lang.Override;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.sound.midi.SysexMessage;

import static java.lang.Long.valueOf;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;


public final class NonBlockingStatsDClientTest {

    private static final int STATSD_SERVER_PORT = 8125;

    private StatsDClient client;
    private DummyStatsDServer server;


    @Before
    public void initialize () {
        StatsDClientKey key = new StatsDClientKey("my.prefix", "localhost", STATSD_SERVER_PORT, true);
        client = StatsDClientContainer.INSTANCE.getClient(key);
        server = new DummyStatsDServer(STATSD_SERVER_PORT);
    }


    @After
    public void stop() throws Exception {
        StatsDClientContainer.INSTANCE.closeAll();
        server.stop();

    }



    @Test(timeout=5000L) public void
    sends_counter_value_with_rate_to_statsd() throws Exception {
        client.count("mycount", Long.MAX_VALUE, 0.00024);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mycount:9223372036854775807|c|@0.00024"));
    }

    @Test(timeout=5000L) public void
    sends_counter_value_to_statsd() throws Exception {
        client.count("mycount", Long.MAX_VALUE);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mycount:9223372036854775807|c"));

    }

    @Test(timeout=5000L) public void
    sends_counter_increment_to_statsd() throws Exception {
        client.incrementCounter("myinc");
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.myinc:1|c"));
    }

    @Test(timeout=5000L) public void
    sends_counter_decrement_to_statsd() throws Exception {
        client.decrementCounter("mydec");
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mydec:-1|c"));
    }

    @Test(timeout=5000L) public void
    sends_gauge_to_statsd() throws Exception {
        client.recordGaugeValue("mygauge", Long.MAX_VALUE);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:9223372036854775807|g"));
    }

    @Test(timeout=5000L) public void
    sends_fractional_gauge_to_statsd() throws Exception {
        client.recordGaugeValue("mygauge", 423.123456789d);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:423.123456789|g"));
    }

    @Test(timeout=5000L) public void
    sends_large_fractional_gauge_to_statsd() throws Exception {
        client.recordGaugeValue("mygauge", 423423423.9d);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:423423423.9|g"));
    }

    @Test(timeout=5000L) public void
    sends_zero_gauge_to_statsd() throws Exception {
        client.recordGaugeValue("mygauge", 0L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:0|g"));
    }

    @Test(timeout=5000L) public void
    sends_negagive_gauge_to_statsd_by_resetting_to_zero_first() throws Exception {
        client.recordGaugeValue("mygauge", -423L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:0|g\nmy.prefix.mygauge:-423|g"));
    }

    @Test(timeout=5000L) public void
    sends_gauge_positive_delta_to_statsd() throws Exception {
        client.recordGaugeDelta("mygauge", 423L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:+423|g"));
    }

    @Test(timeout=5000L) public void
    sends_gauge_negative_delta_to_statsd() throws Exception {
        client.recordGaugeDelta("mygauge", -423L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:-423|g"));
    }

    @Test(timeout=5000L) public void
    sends_gauge_zero_delta_to_statsd() throws Exception {
        client.recordGaugeDelta("mygauge", 0L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mygauge:+0|g"));
    }

    @Test(timeout=5000L) public void
    sends_set_to_statsd() throws Exception {
        client.recordSetEvent("myset", "test");
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.myset:test|s"));
    }

    @Test(timeout=5000L) public void
    sends_timer_to_statsd() throws Exception {
        client.recordExecutionTime("mytime", 123L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mytime:123|ms"));
    }

    @Test(timeout=5000L) public void
    sends_timer_with_rate_to_statsd() throws Exception {
        client.recordExecutionTime("mytime", 123L, 0.000123);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mytime:123|ms|@0.000123"));
    }

    @Test(timeout=5000L) public void
    sends_timer_of_zero_to_statsd_based_on_specified_start_time_in_the_future() throws Exception {
        client.recordExecutionTimeToNow("mytime", System.currentTimeMillis() + 100000L);
        server.waitForMessage();

        assertTrue(server.messagesReceived().contains("my.prefix.mytime:0|ms"));
    }

    @Test(timeout=5000L) public void
    allows_empty_prefix() {
        final NonBlockingStatsDClient emptyPrefixClient = new NonBlockingStatsDClient(" ", "localhost", STATSD_SERVER_PORT);
        try {
            emptyPrefixClient.count("mycount", 24L);
            server.waitForMessage();
        } finally {
            emptyPrefixClient.stop();
        }

        for (int i = 0; i < server.messagesReceived().size(); i ++) {
            assertTrue(server.messagesReceived().get(i).startsWith("mycount:"));
        }
    }

    @Test(timeout=5000L) public void
    allows_null_prefix() {
        final NonBlockingStatsDClient nullPrefixClient = new NonBlockingStatsDClient(null, "localhost", STATSD_SERVER_PORT);
        try {
            nullPrefixClient.count("mycount", 24L);
            server.waitForMessage();
        } finally {
            nullPrefixClient.stop();
        }

        for (int i = 0; i < server.messagesReceived().size(); i ++) {
            assertTrue(server.messagesReceived().get(i).startsWith("mycount:"));
        }
    }

    private static final class DummyStatsDServer {
        private final List<String> messagesReceived = new ArrayList<String>();
        private final DatagramSocket server;

        public DummyStatsDServer(int port) {
            try {
                server = new DatagramSocket(port);
            } catch (SocketException e) {
                throw new IllegalStateException(e);
            }
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        final DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                        server.receive(packet);
                        messagesReceived.add(new String(packet.getData(), Charset.forName("UTF-8")).trim());
                    } catch (Exception e) { }
                }
            }).start();
        }

        public void stop() {
            server.close();
        }

        public void waitForMessage() {
            while (messagesReceived.isEmpty()) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {}
            }
        }

        public List<String> messagesReceived() {
            return new ArrayList<String>(messagesReceived);
        }
    }
}


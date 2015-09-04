package com.amazon.statsdclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class NonBlockingUdpSender {
    private final Charset encoding;
    private DatagramChannel [] clientSockets;
    private final ExecutorService executor;
    private StatsDClientErrorHandler handler;

    private int count;

    public NonBlockingUdpSender(final String hostname,
                                final int port,
                                Charset encoding,
                                final StatsDClientErrorHandler handler) throws IOException {
        this.encoding = encoding;
        this.handler = handler;
        clientSockets = new DatagramChannel[10];
        count = 0;

        this.executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            final ThreadFactory delegate = Executors.defaultThreadFactory();
            @Override
            public Thread newThread(Runnable r) {
                Thread result = delegate.newThread(r);
                result.setName("StatsD-" + result.getName());
                result.setDaemon(true);
                return result;
            }
        });

        executor.execute(new Runnable() {
            @Override public void run() {
                try {

                    for (int i = 0; i < 10; i++) {
                        clientSockets[i] = DatagramChannel.open();
                        clientSockets[i].connect(new InetSocketAddress(hostname, port));
                    }
                } catch (IOException e) {
                    handler.handle(e);
                }
            }
        });

    }

    public void stop() {
        try {
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            handler.handle(e);
        }
        finally {
            if (clientSockets != null) {
                try {
                    for (int i = 0; i < 10; i++) {
                        clientSockets[i].close();
                    }
                }
                catch (Exception e) {
                    handler.handle(e);
                }
            }
        }
    }

    public void send(final String message) {
        try {
            executor.execute(new Runnable() {
                @Override public void run() {
                    blockingSend(message);
                }
            });
        }
        catch (Exception e) {
            handler.handle(e);
        }
    }

    private void blockingSend(String message) {
        try {
            final byte[] sendData = message.getBytes(encoding);
            DatagramChannel clientSocket = clientSockets[(count % 10)];
            clientSocket.write(ByteBuffer.wrap(sendData));
            count++;
        } catch (Exception e) {
            handler.handle(e);
        }
    }
}

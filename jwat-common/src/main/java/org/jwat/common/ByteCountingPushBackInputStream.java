package org.jwat.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Basic <code>PushBackInputStream</code> that also keeps track of the number
 * of consumed bytes at any given time.
 *
 * @author nicl
 */
public class ByteCountingPushBackInputStream extends PushbackInputStream {

    /** Read line initial size. */
    public static final int READLINE_INITIAL_SIZE = 128;

    /** Pushback buffer size. */
    protected int pushback_size;

    /** Offset relative to beginning of stream. */
    protected long consumed = 0;

    /** Byte counter which can also be changed. */
    protected long counter = 0;

    /**
     * Given an <code>InputStream</code> and a push back buffer size returns
     * a wrapped input stream with push back capabilities.
     * @param in <code>InputStream</code> to wrap
     * @param size push back buffer size
     */
    public ByteCountingPushBackInputStream(InputStream in, int size) {
        super(in, size);
        pushback_size = size;
    }

    /**
     * Get the pushback buffer size.
     * @return pushback buffer size
     */
    public int getPushbackSize() {
        return pushback_size;
    }

    /**
     * Retrieve the number of bytes consumed by this stream.
     * @return current byte offset in this stream
     */
    public long getConsumed() {
        return consumed;
    }

    /**
     * Change the counter value.
     * Useful for reading zero indexed relative data.
     * @param bytes new counter value
     */
    public void setCounter(long bytes) {
        counter = bytes;
    }

    /**
     * Retrieve the current counter value.
     * @return current counter value
     */
    public long getCounter() {
        return counter;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            ++consumed;
            ++counter;
        }
        return b;
    }

    /*
     * The super method did this anyway causing a double amount of
     * consumed bytes.
     * @see java.io.FilterInputStream#read(byte[])
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            consumed += n;
            counter += n;
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        n = super.skip(n);
        consumed += n;
        counter += n;
        return n;
    }

    @Override
    public void unread(int b) throws IOException {
        super.unread(b);
        --consumed;
        --counter;
    }

    /*
     * The super method did this anyway causing a double amount of
     * un-consumed bytes.
     * @see java.io.PushbackInputStream#unread(byte[])
     */
    @Override
    public void unread(byte[] b) throws IOException {
        unread(b, 0, b.length);
    }

    @Override
    public void unread(byte[] b, int off, int len) throws IOException {
        super.unread(b, off, len);
        consumed -= len;
        counter -= len;
    }

    /**
     * Read a single line into a string.
     * @return single string line
     * @throws IOException io exception while reading line
     */
    public String readLine() throws IOException {
        ByteArrayOutputStream bos =
                new ByteArrayOutputStream(READLINE_INITIAL_SIZE);
        int b;
        while (true) {
            b = read();
            if (b == -1) {
                return null;    //Unexpected EOF
            }
            if (b == '\n'){
                break;
            }
            if (b != '\r') {
                bos.write(b);
            }
        }
        return bos.toString("US-ASCII");
    }

    /**
     * Guaranteed to read the exact number of bytes that are in the array,
     * if not, the bytes are pushed back into the stream before returning.
     * @param buffer byte buffer to read bytes into
     * @return the number of bytes read into array
     * @throws IOException io exception while reading array
     */
    public int readFully(byte[] buffer) throws IOException {
        int readOffset = 0;
        int readRemaining = buffer.length;
        int readLast = 0;
        while (readRemaining > 0 && readLast != -1) {
            readRemaining -= readLast;
            readOffset += readLast;
            readLast = read(buffer, readOffset, readRemaining);
        }
        if (readRemaining > 0) {
            unread(buffer, 0, readOffset);
            readOffset = 0;
        }
        return readOffset;
    }

}
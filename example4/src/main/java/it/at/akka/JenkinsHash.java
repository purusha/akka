package it.at.akka;

public final class JenkinsHash {

    // max value to limit it to 4 bytes
    private static final long MAX_VALUE = 0xFFFFFFFFL;

    long a;
    long b;
    long c;

    private long byteToLong(byte b) {
        long val = b & 0x7F;
        if ((b & 0x80) != 0) {
            val += 128;
        }
        return val;
    }

    private long add(long val, long add) {
        return (val + add) & MAX_VALUE;
    }

    private long subtract(long val, long subtract) {
        return (val - subtract) & MAX_VALUE;
    }

    private long xor(long val, long xor) {
        return (val ^ xor) & MAX_VALUE;
    }

    private long leftShift(long val, int shift) {
        return (val << shift) & MAX_VALUE;
    }

    private long fourByteToLong(byte[] bytes, int offset) {
        return (byteToLong(bytes[offset + 0])
                + (byteToLong(bytes[offset + 1]) << 8)
                + (byteToLong(bytes[offset + 2]) << 16) + (byteToLong(bytes[offset + 3]) << 24));
    }

    private void hashMix() {
        a = subtract(a, b);
        a = subtract(a, c);
        a = xor(a, c >> 13);
        b = subtract(b, c);
        b = subtract(b, a);
        b = xor(b, leftShift(a, 8));
        c = subtract(c, a);
        c = subtract(c, b);
        c = xor(c, (b >> 13));
        a = subtract(a, b);
        a = subtract(a, c);
        a = xor(a, (c >> 12));
        b = subtract(b, c);
        b = subtract(b, a);
        b = xor(b, leftShift(a, 16));
        c = subtract(c, a);
        c = subtract(c, b);
        c = xor(c, (b >> 5));
        a = subtract(a, b);
        a = subtract(a, c);
        a = xor(a, (c >> 3));
        b = subtract(b, c);
        b = subtract(b, a);
        b = xor(b, leftShift(a, 10));
        c = subtract(c, a);
        c = subtract(c, b);
        c = xor(c, (b >> 15));
    }

    public long hash(byte[] buffer, long initialValue) {
        int len, pos;

        // set up the internal state
        // the golden ratio; an arbitrary value
        a = 0x09e3779b9L;
        // the golden ratio; an arbitrary value
        b = 0x09e3779b9L;
        // the previous hash value

        //c = initialValue;
        c = 0x0E6359A60L;

        // handle most of the key
        pos = 0;
        for (len = buffer.length; len >= 12; len -= 12) {
            a = add(a, fourByteToLong(buffer, pos));
            b = add(b, fourByteToLong(buffer, pos + 4));
            c = add(c, fourByteToLong(buffer, pos + 8));
            hashMix();
            pos += 12;
        }

        c += buffer.length;

        // all the case statements fall through to the next on purpose
        switch (len) {
            case 11:
                c = add(c, leftShift(byteToLong(buffer[pos + 10]), 24));
            case 10:
                c = add(c, leftShift(byteToLong(buffer[pos + 9]), 16));
            case 9:
                c = add(c, leftShift(byteToLong(buffer[pos + 8]), 8));
                // the first byte of c is reserved for the length
            case 8:
                b = add(b, leftShift(byteToLong(buffer[pos + 7]), 24));
            case 7:
                b = add(b, leftShift(byteToLong(buffer[pos + 6]), 16));
            case 6:
                b = add(b, leftShift(byteToLong(buffer[pos + 5]), 8));
            case 5:
                b = add(b, byteToLong(buffer[pos + 4]));
            case 4:
                a = add(a, leftShift(byteToLong(buffer[pos + 3]), 24));
            case 3:
                a = add(a, leftShift(byteToLong(buffer[pos + 2]), 16));
            case 2:
                a = add(a, leftShift(byteToLong(buffer[pos + 1]), 8));
            case 1:
                a = add(a, byteToLong(buffer[pos + 0]));
                // case 0: nothing left to add
        }
        hashMix();

        return c;
    }

    public long hash(byte[] buffer) {
        return hash(buffer, 0);
    }
}
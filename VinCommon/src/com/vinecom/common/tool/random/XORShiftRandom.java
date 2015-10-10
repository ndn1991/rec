package com.vinecom.common.tool.random;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by ndn on 7/23/2015.
 */
public class XORShiftRandom  extends Random {
    private int arraySeed = 0x3c074a61;
    private long seed;

    public XORShiftRandom(long init) {
        this.seed = hashSeed(init);
    }

    public XORShiftRandom() {
        this(System.nanoTime());
    }

    @Override
    protected int next(int bits) {
        long nextSeed = seed ^ (seed << 21);
        nextSeed ^= (nextSeed >>> 35);
        nextSeed ^= (nextSeed << 4);
        seed = nextSeed;
        return (int) (nextSeed & ((1L << bits) - 1));
    }

    @Override
    public void setSeed(long s) {
        seed = hashSeed(s);
    }

    private int bytesHash(byte[] data, int seed) {
        int len = data.length;
        int h = seed;

        // Body
        int i = 0;
        while(len >= 4) {
            int k = data[i + 0] & 0xFF;
            k |= (data[i + 1] & 0xFF) << 8;
            k |= (data[i + 2] & 0xFF) << 16;
            k |= (data[i + 3] & 0xFF) << 24;

            h = mix(h, k);

            i += 4;
            len -= 4;
        }

        // Tail
        int k = 0;
        if(len == 3) k ^= (data[i + 2] & 0xFF) << 16;
        if(len >= 2) k ^= (data[i + 1] & 0xFF) << 8;
        if(len >= 1) {
            k ^= (data[i + 0] & 0xFF);
            h = mixLast(h, k);
        }

        // Finalization
        return avalanche(h^data.length);
    }

    private int mix(int hash, int data) {
        int h = mixLast(hash, data);
        h = Integer.rotateLeft(h, 13);
        return h * 5 + 0xe6546b64;
    }

    private int mixLast(int hash, int data) {
        int k = data;

        k *= 0xcc9e2d51;
        k = Integer.rotateLeft(k, 15);
        k *= 0x1b873593;

        return hash ^ k;
    }

    private int avalanche(int hash) {
        int h = hash;

        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;

        return h;
    }

    private long hashSeed(long seed) {
        byte[] bytes = ByteBuffer.allocate(java.lang.Long.SIZE).putLong(seed).array();
        return bytesHash(bytes, arraySeed);
    }
}

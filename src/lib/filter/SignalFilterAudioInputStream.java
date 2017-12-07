package lib.filter;

import biz.source_code.dsp.sound.AudioIo;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SignalFilterAudioInputStream {
    private SignalFilterAudioInputStream() {
    }

    public static AudioInputStream getAudioInputStream(AudioInputStream var0, SignalFilter[] var1) {
        FilterStream var2 = new FilterStream(var0, var1);
        return new AudioInputStream(var2, var0.getFormat(), var0.getFrameLength());
    }

    public static class FilterStream extends InputStream {
        private static final int inBufFrames = 4096;
        private AudioInputStream in;
        private SignalFilter[] signalFilters;
        private AudioFormat format;
        private int channels;
        private int frameSize;
        private byte[] inBuf;
        private float[][] floatBufs;

        public FilterStream(AudioInputStream var1, SignalFilter[] var2) {
            this.in = var1;
            this.signalFilters = var2;
            this.format = var1.getFormat();
            this.channels = this.format.getChannels();
            if (this.channels != var2.length) {
                throw new IllegalArgumentException();
            } else {
                this.frameSize = this.format.getFrameSize();
                this.inBuf = new byte[4096 * this.frameSize];
                this.floatBufs = new float[this.channels][];

                for(int var3 = 0; var3 < this.channels; ++var3) {
                    this.floatBufs[var3] = new float[4096];
                }

            }
        }

        public int read(byte[] var1, int var2, int var3) throws IOException {
            int var4 = Math.min(var3, this.inBuf.length);
            int var5 = var4 / this.frameSize * this.frameSize;
            int var6 = this.in.read(this.inBuf, 0, var5);
            if (var6 <= 0) {
                return var6;
            } else if (var6 % this.frameSize != 0) {
                throw new AssertionError();
            } else {
                int var7 = var6 / this.frameSize;
                AudioIo.unpackAudioStreamBytes(this.format, this.inBuf, 0, this.floatBufs, 0, var7);

                for(int var8 = 0; var8 < this.channels; ++var8) {
                    SignalFilter var9 = this.signalFilters[var8];
                    float[] var10 = this.floatBufs[var8];

                    for(int var11 = 0; var11 < var7; ++var11) {
                        var10[var11] = (float)var9.step((double)var10[var11]);
                    }
                }

                AudioIo.packAudioStreamBytes(this.format, this.floatBufs, 0, var1, var2, var7);
                return var6;
            }
        }

        public int read() throws IOException {
            throw new AssertionError();
        }
    }
}


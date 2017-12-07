package lib.wave;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class AudioIo {
    private AudioIo() {
    }

    public static void saveWavFile(String var0, AudioIo.AudioSignal var1, int var2, int var3) throws Exception {
        AudioFormat var4 = new AudioFormat((float)var1.samplingRate, 16, var1.getChannels(), true, false);
        AudioIo.AudioBytesPackerStream var5 = new AudioIo.AudioBytesPackerStream(var4, var1.data, var2, var3);
        AudioInputStream var6 = new AudioInputStream(var5, var4, (long)var3);
        AudioSystem.write(var6, AudioFileFormat.Type.WAVE, new File(var0));
    }

    public static void saveWavFile(String var0, AudioIo.AudioSignal var1) throws Exception {
        saveWavFile(var0, var1, 0, var1.getLength());
    }

    public static void saveWavFile(String var0, float[] var1, int var2) throws Exception {
        AudioIo.AudioSignal var3 = new AudioIo.AudioSignal();
        var3.samplingRate = var2;
        var3.data = new float[][]{var1};
        saveWavFile(var0, var3);
    }

    public static AudioIo.AudioSignal loadWavFile(String var0) throws Exception {
        AudioInputStream var1 = null;

        AudioIo.AudioSignal var18;
        try {
            AudioIo.AudioSignal var2 = new AudioIo.AudioSignal();
            var1 = AudioSystem.getAudioInputStream(new File(var0));
            AudioFormat var3 = var1.getFormat();
            var2.samplingRate = Math.round(var3.getSampleRate());
            int var4 = var3.getFrameSize();
            int var5 = var3.getChannels();
            long var6 = var1.getFrameLength();
            if (var6 > 2147483647L) {
                throw new IOException("Sound file too long.");
            }

            int var8 = (int)var6;
            var2.data = new float[var5][];

            for(int var9 = 0; var9 < var5; ++var9) {
                var2.data[var9] = new float[var8];
            }

            byte[] var10 = new byte[var4 * 16384];

            int var14;
            for(int var11 = 0; var11 < var8; var11 += var14) {
                int var12 = Math.min(var8 - var11, 16384);
                int var13 = var1.read(var10, 0, var12 * var4);
                if (var13 <= 0) {
                    if (var3.getEncoding() == AudioFormat.Encoding.PCM_FLOAT && var11 * var4 == var8) {
                        truncateSignal(var2, var11);
                        break;
                    }

                    throw new IOException("Unexpected EOF while reading WAV file. totalFrames=" + var8 + " pos=" + var11 + " frameSize=" + var4 + ".");
                }

                if (var13 % var4 != 0) {
                    throw new IOException("Length of transmitted data is not a multiple of frame size. reqFrames=" + var12 + " trBytes=" + var13 + " frameSize=" + var4 + ".");
                }

                var14 = var13 / var4;
                unpackAudioStreamBytes(var3, var10, 0, var2.data, var11, var14);
            }

            var18 = var2;
        } finally {
            if (var1 != null) {
                var1.close();
            }

        }

        return var18;
    }

    private static void truncateSignal(AudioIo.AudioSignal var0, int var1) {
        for(int var2 = 0; var2 < var0.getChannels(); ++var2) {
            var0.data[var2] = Arrays.copyOf(var0.data[var2], var1);
        }

    }

    public static void play(AudioIo.AudioSignal var0) throws Exception {
        int var1 = var0.getChannels();
        AudioFormat var2 = new AudioFormat((float)var0.samplingRate, 16, var1, true, false);
        int var3 = var2.getFrameSize();
        SourceDataLine var4 = AudioSystem.getSourceDataLine(var2);
        var4.open(var2, var0.samplingRate * var3);
        var4.start();
        byte[] var6 = new byte[var3 * 16384];

        int var8;
        for(int var7 = 0; var7 < var0.getLength(); var7 += var8) {
            var8 = Math.min(var0.getLength() - var7, 16384);
            packAudioStreamBytes(var2, var0.data, var7, var6, 0, var8);
            int var9 = var8 * var3;
            int var10 = var4.write(var6, 0, var9);
            if (var10 != var9) {
                throw new AssertionError();
            }
        }

        var4.drain();
        var4.stop();
        var4.close();
    }

    public static void play(float[] var0, int var1) throws Exception {
        AudioIo.AudioSignal var2 = new AudioIo.AudioSignal();
        var2.data = new float[][]{var0};
        var2.samplingRate = var1;
        play(var2);
    }

    public static void unpackAudioStreamBytes(AudioFormat var0, byte[] var1, int var2, float[][] var3, int var4, int var5) {
        AudioFormat.Encoding var6 = var0.getEncoding();
        if (var6 == AudioFormat.Encoding.PCM_SIGNED) {
            unpackAudioStreamBytesPcmSigned(var0, var1, var2, var3, var4, var5);
        } else {
            if (var6 != AudioFormat.Encoding.PCM_FLOAT) {
                throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).");
            }

            unpackAudioStreamBytesPcmFloat(var0, var1, var2, var3, var4, var5);
        }

    }

    private static void unpackAudioStreamBytesPcmSigned(AudioFormat var0, byte[] var1, int var2, float[][] var3, int var4, int var5) {
        int var6 = var0.getChannels();
        boolean var7 = var0.isBigEndian();
        int var8 = var0.getSampleSizeInBits();
        int var9 = var0.getFrameSize();
        if (var3.length != var6) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        } else if (var8 != 16 && var8 != 24 && var8 != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + var8 + " bits per sample for signed PCM).");
        } else {
            int var10 = (var8 + 7) / 8;
            if (var10 * var6 != var9) {
                throw new AssertionError();
            } else {
                float var11 = (float)((1 << var8 - 1) - 1);

                for(int var12 = 0; var12 < var6; ++var12) {
                    float[] var13 = var3[var12];
                    int var14 = var2 + var12 * var10;

                    for(int var15 = 0; var15 < var5; ++var15) {
                        int var16 = unpackSignedInt(var1, var14 + var15 * var9, var8, var7);
                        var13[var4 + var15] = (float)var16 / var11;
                    }
                }

            }
        }
    }

    private static void unpackAudioStreamBytesPcmFloat(AudioFormat var0, byte[] var1, int var2, float[][] var3, int var4, int var5) {
        int var6 = var0.getChannels();
        boolean var7 = var0.isBigEndian();
        int var8 = var0.getSampleSizeInBits();
        int var9 = var0.getFrameSize();
        if (var3.length != var6) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        } else if (var8 != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + var8 + " bits per sample for floating-point PCM).");
        } else {
            int var10 = (var8 + 7) / 8;
            if (var10 * var6 != var9) {
                throw new AssertionError();
            } else {
                for(int var11 = 0; var11 < var6; ++var11) {
                    float[] var12 = var3[var11];
                    int var13 = var2 + var11 * var10;

                    for(int var14 = 0; var14 < var5; ++var14) {
                        var12[var4 + var14] = unpackFloat(var1, var13 + var14 * var9, var7);
                    }
                }

            }
        }
    }

    public static void packAudioStreamBytes(AudioFormat var0, float[][] var1, int var2, byte[] var3, int var4, int var5) {
        AudioFormat.Encoding var6 = var0.getEncoding();
        if (var6 == AudioFormat.Encoding.PCM_SIGNED) {
            packAudioStreamBytesPcmSigned(var0, var1, var2, var3, var4, var5);
        } else {
            if (var6 != AudioFormat.Encoding.PCM_FLOAT) {
                throw new UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).");
            }

            packAudioStreamBytesPcmFloat(var0, var1, var2, var3, var4, var5);
        }

    }

    private static void packAudioStreamBytesPcmSigned(AudioFormat var0, float[][] var1, int var2, byte[] var3, int var4, int var5) {
        int var6 = var0.getChannels();
        boolean var7 = var0.isBigEndian();
        int var8 = var0.getSampleSizeInBits();
        int var9 = var0.getFrameSize();
        if (var1.length != var6) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        } else if (var8 != 16 && var8 != 24 && var8 != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + var8 + " bits per sample for signed PCM).");
        } else {
            int var10 = (var8 + 7) / 8;
            if (var10 * var6 != var9) {
                throw new AssertionError();
            } else {
                int var11 = (1 << var8 - 1) - 1;

                for(int var12 = 0; var12 < var6; ++var12) {
                    float[] var13 = var1[var12];
                    int var14 = var4 + var12 * var10;

                    for(int var15 = 0; var15 < var5; ++var15) {
                        float var16 = Math.max(-1.0F, Math.min(1.0F, var13[var2 + var15]));
                        int var17 = Math.round(var16 * (float)var11);
                        packSignedInt(var17, var3, var14 + var15 * var9, var8, var7);
                    }
                }

            }
        }
    }

    private static void packAudioStreamBytesPcmFloat(AudioFormat var0, float[][] var1, int var2, byte[] var3, int var4, int var5) {
        int var6 = var0.getChannels();
        boolean var7 = var0.isBigEndian();
        int var8 = var0.getSampleSizeInBits();
        int var9 = var0.getFrameSize();
        if (var1.length != var6) {
            throw new IllegalArgumentException("Number of channels not equal to number of buffers.");
        } else if (var8 != 32) {
            throw new UnsupportedOperationException("Audio stream format not supported (" + var8 + " bits per sample for floating-point PCM).");
        } else {
            int var10 = (var8 + 7) / 8;
            if (var10 * var6 != var9) {
                throw new AssertionError();
            } else {
                for(int var11 = 0; var11 < var6; ++var11) {
                    float[] var12 = var1[var11];
                    int var13 = var4 + var11 * var10;

                    for(int var14 = 0; var14 < var5; ++var14) {
                        float var15 = Math.max(-1.0F, Math.min(1.0F, var12[var2 + var14]));
                        packFloat(var15, var3, var13 + var14 * var9, var7);
                    }
                }

            }
        }
    }

    private static int unpackSignedInt(byte[] var0, int var1, int var2, boolean var3) {
        switch(var2) {
            case 16:
                if (var3) {
                    return var0[var1] << 8 | var0[var1 + 1] & 255;
                }

                return var0[var1 + 1] << 8 | var0[var1] & 255;
            case 24:
                if (var3) {
                    return var0[var1] << 16 | (var0[var1 + 1] & 255) << 8 | var0[var1 + 2] & 255;
                }

                return var0[var1 + 2] << 16 | (var0[var1 + 1] & 255) << 8 | var0[var1] & 255;
            case 32:
                return unpackInt(var0, var1, var3);
            default:
                throw new AssertionError();
        }
    }

    private static void packSignedInt(int var0, byte[] var1, int var2, int var3, boolean var4) {
        switch(var3) {
            case 16:
                if (var4) {
                    var1[var2] = (byte)(var0 >>> 8 & 255);
                    var1[var2 + 1] = (byte)(var0 & 255);
                } else {
                    var1[var2] = (byte)(var0 & 255);
                    var1[var2 + 1] = (byte)(var0 >>> 8 & 255);
                }
                break;
            case 24:
                if (var4) {
                    var1[var2] = (byte)(var0 >>> 16 & 255);
                    var1[var2 + 1] = (byte)(var0 >>> 8 & 255);
                    var1[var2 + 2] = (byte)(var0 & 255);
                } else {
                    var1[var2] = (byte)(var0 & 255);
                    var1[var2 + 1] = (byte)(var0 >>> 8 & 255);
                    var1[var2 + 2] = (byte)(var0 >>> 16 & 255);
                }
                break;
            case 32:
                packInt(var0, var1, var2, var4);
                break;
            default:
                throw new AssertionError();
        }

    }

    private static int unpackInt(byte[] var0, int var1, boolean var2) {
        return var2 ? var0[var1] << 24 | (var0[var1 + 1] & 255) << 16 | (var0[var1 + 2] & 255) << 8 | var0[var1 + 3] & 255 : var0[var1 + 3] << 24 | (var0[var1 + 2] & 255) << 16 | (var0[var1 + 1] & 255) << 8 | var0[var1] & 255;
    }

    private static void packInt(int var0, byte[] var1, int var2, boolean var3) {
        if (var3) {
            var1[var2] = (byte)(var0 >>> 24 & 255);
            var1[var2 + 1] = (byte)(var0 >>> 16 & 255);
            var1[var2 + 2] = (byte)(var0 >>> 8 & 255);
            var1[var2 + 3] = (byte)(var0 & 255);
        } else {
            var1[var2] = (byte)(var0 & 255);
            var1[var2 + 1] = (byte)(var0 >>> 8 & 255);
            var1[var2 + 2] = (byte)(var0 >>> 16 & 255);
            var1[var2 + 3] = (byte)(var0 >>> 24 & 255);
        }

    }

    private static float unpackFloat(byte[] var0, int var1, boolean var2) {
        int var3 = unpackInt(var0, var1, var2);
        return Float.intBitsToFloat(var3);
    }

    private static void packFloat(float var0, byte[] var1, int var2, boolean var3) {
        int var4 = Float.floatToIntBits(var0);
        packInt(var4, var1, var2, var3);
    }

    private static class AudioBytesPackerStream extends InputStream {
        AudioFormat format;
        float[][] inBufs;
        int inOffs;
        int inLen;
        int pos;

        public AudioBytesPackerStream(AudioFormat var1, float[][] var2, int var3, int var4) {
            this.format = var1;
            this.inBufs = var2;
            this.inOffs = var3;
            this.inLen = var4;
        }

        public int read() throws IOException {
            throw new AssertionError("Not implemented.");
        }

        public int read(byte[] var1, int var2, int var3) throws IOException {
            int var4 = this.inLen - this.pos;
            if (var4 <= 0) {
                return -1;
            } else {
                int var5 = var3 / this.format.getFrameSize();
                int var6 = Math.min(var4, var5);
                AudioIo.packAudioStreamBytes(this.format, this.inBufs, this.inOffs + this.pos, var1, var2, var6);
                this.pos += var6;
                return var6 * this.format.getFrameSize();
            }
        }
    }

    public static class AudioSignal {
        public int samplingRate;
        public float[][] data;

        public AudioSignal() {
        }

        public int getLength() {
            return this.data[0].length;
        }

        public int getChannels() {
            return this.data.length;
        }
    }
}


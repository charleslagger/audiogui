package lib.filter;

import javax.sound.sampled.AudioInputStream;

public class IirFilterAudioInputStream {
    private IirFilterAudioInputStream() {
    }

    public static AudioInputStream getAudioInputStream(AudioInputStream var0, IirFilterCoefficients var1) {
        int var2 = var0.getFormat().getChannels();
        IirFilter[] var3 = new IirFilter[var2];

        for(int var4 = 0; var4 < var2; ++var4) {
            var3[var4] = new IirFilter(var1);
        }

        return SignalFilterAudioInputStream.getAudioInputStream(var0, var3);
    }
}


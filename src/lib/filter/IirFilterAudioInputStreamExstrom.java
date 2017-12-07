package lib.filter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class IirFilterAudioInputStreamExstrom {
    private IirFilterAudioInputStreamExstrom() {
    }

    public static AudioInputStream getAudioInputStream(AudioInputStream var0, FilterPassType var1, int var2, double var3, double var5) {
        AudioFormat var7 = var0.getFormat();
        double var8 = (double)var7.getSampleRate();
        double var10 = var3 / var8;
        double var12 = var5 / var8;
        IirFilterCoefficients var14 = IirFilterDesignExstrom.design(var1, var2, var10, var12);
        return IirFilterAudioInputStream.getAudioInputStream(var0, var14);
    }
}

package lib.filter;

import biz.source_code.dsp.util.ArrayUtils;

public class IirFilterDesignExstrom {
    private IirFilterDesignExstrom() {
    }

    public static IirFilterCoefficients design(FilterPassType var0, int var1, double var2, double var4) {
        if (var1 < 1) {
            throw new IllegalArgumentException("Invalid filterOrder.");
        } else if (var2 > 0.0D && var2 < 0.5D) {
            if (var0 != FilterPassType.bandpass && var0 != FilterPassType.bandstop || var4 > 0.0D && var4 < 0.5D) {
                IirFilterCoefficients var6 = new IirFilterCoefficients();
                var6.a = calculateACoefficients(var0, var1, var2, var4);
                double[] var7 = calculateBCoefficients(var0, var1, var2, var4);
                double var8 = calculateScalingFactor(var0, var1, var2, var4);
                var6.b = ArrayUtils.multiply(var7, var8);
                return var6;
            } else {
                throw new IllegalArgumentException("Invalid fcf2.");
            }
        } else {
            throw new IllegalArgumentException("Invalid fcf1.");
        }
    }

    private static double[] calculateACoefficients(FilterPassType var0, int var1, double var2, double var4) {
        switch(var0) {
            case lowpass:
            case highpass:
                return calculateACoefficients_lp_hp(var1, var2);
            case bandpass:
            case bandstop:
                return calculateACoefficients_bp_bs(var0, var1, var2, var4);
            default:
                throw new AssertionError();
        }
    }

    private static double[] calculateACoefficients_lp_hp(int var0, double var1) {
        double[] var3 = new double[2 * var0];
        double var4 = 6.283185307179586D * var1;
        double var6 = Math.sin(var4);
        double var8 = Math.cos(var4);

        for(int var10 = 0; var10 < var0; ++var10) {
            double var11 = 3.141592653589793D * (double)(2 * var10 + 1) / (double)(2 * var0);
            double var13 = Math.sin(var11);
            double var15 = Math.cos(var11);
            double var17 = 1.0D + var6 * var13;
            var3[2 * var10] = -var8 / var17;
            var3[2 * var10 + 1] = -var6 * var15 / var17;
        }

        double[] var19 = binomial_mult(var0, var3);
        double[] var20 = new double[var0 + 1];
        var20[0] = 1.0D;

        for(int var12 = 1; var12 <= var0; ++var12) {
            var20[var12] = var19[2 * var12 - 2];
        }

        return var20;
    }

    private static double[] calculateACoefficients_bp_bs(FilterPassType var0, int var1, double var2, double var4) {
        double var6 = Math.cos(6.283185307179586D * (var4 + var2) / 2.0D);
        double var8 = 6.283185307179586D * (var4 - var2) / 2.0D;
        double var10 = Math.sin(var8);
        double var12 = Math.cos(var8);
        double var14 = 2.0D * var10 * var12;
        double var16 = 2.0D * var12 * var12 - 1.0D;
        double[] var18 = new double[2 * var1];
        double[] var19 = new double[2 * var1];
        double var20 = var0 == FilterPassType.bandstop ? -1.0D : 1.0D;

        for(int var22 = 0; var22 < var1; ++var22) {
            double var23 = 3.141592653589793D * (double)(2 * var22 + 1) / (double)(2 * var1);
            double var25 = Math.sin(var23);
            double var27 = Math.cos(var23);
            double var29 = 1.0D + var14 * var25;
            var18[2 * var22] = var16 / var29;
            var18[2 * var22 + 1] = var14 * var27 / var29 * var20;
            var19[2 * var22] = -2.0D * var6 * (var12 + var10 * var25) / var29;
            var19[2 * var22 + 1] = -2.0D * var6 * var10 * var27 / var29 * var20;
        }

        double[] var31 = trinomial_mult(var1, var19, var18);
        double[] var32 = new double[2 * var1 + 1];
        var32[0] = 1.0D;

        for(int var24 = 1; var24 <= 2 * var1; ++var24) {
            var32[var24] = var31[2 * var24 - 2];
        }

        return var32;
    }

    private static double[] calculateBCoefficients(FilterPassType var0, int var1, double var2, double var4) {
        int[] var6;
        switch(var0) {
            case lowpass:
                var6 = calculateBCoefficients_lp(var1);
                return ArrayUtils.toDouble(var6);
            case highpass:
                var6 = calculateBCoefficients_hp(var1);
                return ArrayUtils.toDouble(var6);
            case bandpass:
                var6 = calculateBCoefficients_bp(var1);
                return ArrayUtils.toDouble(var6);
            case bandstop:
                return calculateBCoefficients_bs(var1, var2, var4);
            default:
                throw new AssertionError();
        }
    }

    private static int[] calculateBCoefficients_lp(int var0) {
        int[] var1 = new int[var0 + 1];
        var1[0] = 1;
        var1[1] = var0;
        int var2 = var0 / 2;

        for(int var3 = 2; var3 <= var2; ++var3) {
            var1[var3] = (var0 - var3 + 1) * var1[var3 - 1] / var3;
            var1[var0 - var3] = var1[var3];
        }

        var1[var0 - 1] = var0;
        var1[var0] = 1;
        return var1;
    }

    private static int[] calculateBCoefficients_hp(int var0) {
        int[] var1 = calculateBCoefficients_lp(var0);

        for(int var2 = 1; var2 <= var0; var2 += 2) {
            var1[var2] = -var1[var2];
        }

        return var1;
    }

    private static int[] calculateBCoefficients_bp(int var0) {
        int[] var1 = calculateBCoefficients_hp(var0);
        int[] var2 = new int[2 * var0 + 1];

        for(int var3 = 0; var3 < var0; ++var3) {
            var2[2 * var3] = var1[var3];
            var2[2 * var3 + 1] = 0;
        }

        var2[2 * var0] = var1[var0];
        return var2;
    }

    private static double[] calculateBCoefficients_bs(int var0, double var1, double var3) {
        double var5 = -2.0D * Math.cos(6.283185307179586D * (var3 + var1) / 2.0D) / Math.cos(6.283185307179586D * (var3 - var1) / 2.0D);
        double[] var7 = new double[2 * var0 + 1];
        var7[0] = 1.0D;
        var7[1] = var5;
        var7[2] = 1.0D;

        for(int var8 = 1; var8 < var0; ++var8) {
            var7[2 * var8 + 2] += var7[2 * var8];

            for(int var9 = 2 * var8; var9 > 1; --var9) {
                var7[var9 + 1] += var5 * var7[var9] + var7[var9 - 1];
            }

            var7[2] += var5 * var7[1] + 1.0D;
            var7[1] += var5;
        }

        return var7;
    }

    private static double calculateScalingFactor(FilterPassType var0, int var1, double var2, double var4) {
        switch(var0) {
            case lowpass:
            case highpass:
                return calculateScalingFactor_lp_hp(var0, var1, var2);
            case bandpass:
            case bandstop:
                return calculateScalingFactor_bp_bs(var0, var1, var2, var4);
            default:
                throw new AssertionError();
        }
    }

    private static double calculateScalingFactor_lp_hp(FilterPassType var0, int var1, double var2) {
        double var4 = 6.283185307179586D * var2;
        double var6 = Math.sin(var4);
        double var8 = 3.141592653589793D / (double)(2 * var1);
        int var10 = var1 / 2;
        double var11 = 1.0D;

        for(int var13 = 0; var13 < var1 / 2; ++var13) {
            var11 *= 1.0D + var6 * Math.sin((double)(2 * var13 + 1) * var8);
        }

        double var15;
        switch(var0) {
            case lowpass:
                var15 = Math.sin(var4 / 2.0D);
                if (var1 % 2 != 0) {
                    var11 *= var15 + Math.cos(var4 / 2.0D);
                }
                break;
            case highpass:
                var15 = Math.cos(var4 / 2.0D);
                if (var1 % 2 != 0) {
                    var11 *= var15 + Math.sin(var4 / 2.0D);
                }
                break;
            default:
                throw new AssertionError();
        }

        var11 = Math.pow(var15, (double)var1) / var11;
        return var11;
    }

    private static double calculateScalingFactor_bp_bs(FilterPassType var0, int var1, double var2, double var4) {
        double var6 = Math.tan(6.283185307179586D * (var4 - var2) / 2.0D);
        double var8 = var0 == FilterPassType.bandpass ? 1.0D / var6 : var6;
        double var10 = 1.0D;
        double var12 = 0.0D;

        for(int var14 = 0; var14 < var1; ++var14) {
            double var15 = 3.141592653589793D * (double)(2 * var14 + 1) / (double)(2 * var1);
            double var17 = var8 + Math.sin(var15);
            double var19 = Math.cos(var15);
            double var21 = (var10 + var12) * (var17 - var19);
            double var23 = var10 * var17;
            double var25 = -var12 * var19;
            var10 = var23 - var25;
            var12 = var21 - var23 - var25;
        }

        return 1.0D / var10;
    }

    private static double[] binomial_mult(int var0, double[] var1) {
        double[] var2 = new double[2 * var0];

        for(int var3 = 0; var3 < var0; ++var3) {
            for(int var4 = var3; var4 > 0; --var4) {
                var2[2 * var4] += var1[2 * var3] * var2[2 * (var4 - 1)] - var1[2 * var3 + 1] * var2[2 * (var4 - 1) + 1];
                var2[2 * var4 + 1] += var1[2 * var3] * var2[2 * (var4 - 1) + 1] + var1[2 * var3 + 1] * var2[2 * (var4 - 1)];
            }

            var2[0] += var1[2 * var3];
            var2[1] += var1[2 * var3 + 1];
        }

        return var2;
    }

    private static double[] trinomial_mult(int var0, double[] var1, double[] var2) {
        double[] var3 = new double[4 * var0];
        var3[2] = var2[0];
        var3[3] = var2[1];
        var3[0] = var1[0];
        var3[1] = var1[1];

        for(int var4 = 1; var4 < var0; ++var4) {
            var3[2 * (2 * var4 + 1)] += var2[2 * var4] * var3[2 * (2 * var4 - 1)] - var2[2 * var4 + 1] * var3[2 * (2 * var4 - 1) + 1];
            var3[2 * (2 * var4 + 1) + 1] += var2[2 * var4] * var3[2 * (2 * var4 - 1) + 1] + var2[2 * var4 + 1] * var3[2 * (2 * var4 - 1)];

            for(int var5 = 2 * var4; var5 > 1; --var5) {
                var3[2 * var5] += var1[2 * var4] * var3[2 * (var5 - 1)] - var1[2 * var4 + 1] * var3[2 * (var5 - 1) + 1] + var2[2 * var4] * var3[2 * (var5 - 2)] - var2[2 * var4 + 1] * var3[2 * (var5 - 2) + 1];
                var3[2 * var5 + 1] += var1[2 * var4] * var3[2 * (var5 - 1) + 1] + var1[2 * var4 + 1] * var3[2 * (var5 - 1)] + var2[2 * var4] * var3[2 * (var5 - 2) + 1] + var2[2 * var4 + 1] * var3[2 * (var5 - 2)];
            }

            var3[2] += var1[2 * var4] * var3[0] - var1[2 * var4 + 1] * var3[1] + var2[2 * var4];
            var3[3] += var1[2 * var4] * var3[1] + var1[2 * var4 + 1] * var3[0] + var2[2 * var4 + 1];
            var3[0] += var1[2 * var4];
            var3[1] += var1[2 * var4 + 1];
        }

        return var3;
    }
}


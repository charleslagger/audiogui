package lib.wave;

import biz.source_code.dsp.swing.FunctionPlot;

import java.awt.*;

public class SignalPlot extends FunctionPlot {
    private static final long serialVersionUID = 1L;
    private static final Color gridLineColor = new Color(12632256);

    public SignalPlot(float[] var1, double var2, double var4) {
        this(var1, 0, 0.0D, (double)var1.length, var2, var4);
    }

    public SignalPlot(float[] var1, int var2, double var3, double var5, double var7, double var9) {
        super(new SignalPlot.SignalPlotFunction(var1, var2), var3, var5, var7, var9);
    }

    private static class SignalPlotFunction implements PlotFunction {
        private static double sampleOffset = 0.5D;
        private float[] signal;
        private int signalOffset;
        private int signalEnd;

        public SignalPlotFunction(float[] var1, int var2) {
            this.signal = var1;
            this.signalOffset = var2;
            this.signalEnd = var1.length - var2;
        }

        public double[] getMinMaxY(double var1, double var3) {
            if (var1 >= var3) {
                throw new AssertionError();
            } else {
                double var5 = var1 + (double)this.signalOffset;
                double var7 = var3 + (double)this.signalOffset;
                if ((var5 >= 0.0D || var7 >= 0.0D) && (var5 <= (double)this.signal.length || var7 <= (double)this.signal.length) && this.signal.length != 0) {
                    double var9 = Math.max(0.0D, var5 - sampleOffset);
                    double var11 = Math.max(0.0D, Math.min((double)(this.signal.length - 1), var7 - sampleOffset));
                    double var13 = var11 - var9;
                    if (var13 > 5.0D) {
                        return this.findMinMaxNearestNeighbour(var9, var11);
                    } else {
                        return var13 > 0.3D ? this.findMinMaxLinearInterpolated(var9, var11) : this.findMinMaxCubicInterpolated(var9, var11);
                    }
                } else {
                    return null;
                }
            }
        }

        private double[] findMinMaxNearestNeighbour(double var1, double var3) {
            return this.findMinMaxSamples(this.nearestSamplePos(var1), this.nearestSamplePos(var3));
        }

        private double[] findMinMaxLinearInterpolated(double var1, double var3) {
            double var5 = this.linearInterpolation(var1);
            double var7 = this.linearInterpolation(var3);
            return this.findMinMaxSamples(var1, var3, var5, var7);
        }

        private double[] findMinMaxCubicInterpolated(double var1, double var3) {
            double var5 = this.cubicHermiteInterpolation(var1);
            double var7 = this.cubicHermiteInterpolation(var3);
            return this.findMinMaxSamples(var1, var3, var5, var7);
        }

        private int nearestSamplePos(double var1) {
            return (int)Math.round(Math.max(0.0D, Math.min((double)(this.signal.length - 1), var1)));
        }

        private double linearInterpolation(double var1) {
            int var3 = (int)Math.floor(var1);
            if (var3 < 0) {
                return (double)this.signal[0];
            } else if (var3 >= this.signal.length - 1) {
                return (double)this.signal[this.signal.length - 1];
            } else {
                double var4 = var1 - (double)var3;
                return var4 == 0.0D ? (double)this.signal[var3] : (double)this.signal[var3] * (1.0D - var4) + (double)this.signal[var3 + 1] * var4;
            }
        }

        private double cubicHermiteInterpolation(double var1) {
            int var3 = (int)Math.floor(var1);
            if (var3 >= 1 && var3 + 2 <= this.signal.length - 1) {
                double var4 = var1 - (double)var3;
                if (var4 == 0.0D) {
                    return (double)this.signal[var3];
                } else {
                    double var6 = (double)this.signal[var3 - 1];
                    double var8 = (double)this.signal[var3];
                    double var10 = (double)this.signal[var3 + 1];
                    double var12 = (double)this.signal[var3 + 2];
                    double var14 = (3.0D * (var8 - var10) - var6 + var12) / 2.0D;
                    double var16 = 2.0D * var10 + var6 - (5.0D * var8 + var12) / 2.0D;
                    double var18 = (var10 - var6) / 2.0D;
                    return ((var14 * var4 + var16) * var4 + var18) * var4 + var8;
                }
            } else {
                return this.linearInterpolation(var1);
            }
        }

        private double[] findMinMaxSamples(double var1, double var3, double var5, double var7) {
            double[] var9 = this.findMinMaxSamples((int)Math.ceil(var1), (int)Math.floor(var3));
            double var10 = Math.min(var5, Math.min(var7, var9[0]));
            double var12 = Math.max(var5, Math.max(var7, var9[1]));
            return new double[]{var10, var12};
        }

        private double[] findMinMaxSamples(int var1, int var2) {
            double var3 = 1.7976931348623157E308D;
            double var5 = -1.7976931348623157E308D;

            for(int var7 = var1; var7 <= var2; ++var7) {
                float var8 = this.signal[var7];
                var3 = Math.min(var3, (double)var8);
                var5 = Math.max(var5, (double)var8);
            }

            return new double[]{var3, var5};
        }

        public GridLine[] getHorizontalGridLines(double var1, double var3) {
            return var1 >= -1.0D && var3 <= 1.0D ? new GridLine[]{new GridLine(0.0D, SignalPlot.gridLineColor)} : new GridLine[]{new GridLine(-1.0D, SignalPlot.gridLineColor), new GridLine(0.0D, SignalPlot.gridLineColor), new GridLine(1.0D, SignalPlot.gridLineColor)};
        }

        public GridLine[] getVerticalGridLines(double var1, double var3) {
            if (var3 - var1 <= 50.499D) {
                return this.genSampleGridLines(var1, var3);
            } else {
                return var1 >= 0.0D && var3 <= (double)this.signalEnd ? null : new GridLine[]{new GridLine(0.0D, SignalPlot.gridLineColor), new GridLine((double)this.signalEnd, SignalPlot.gridLineColor)};
            }
        }

        private GridLine[] genSampleGridLines(double var1, double var3) {
            int var5 = (int)Math.floor(var1);
            int var6 = (int)Math.ceil(var3);
            GridLine[] var7 = new GridLine[var6 - var5 + 1];

            for(int var8 = var5; var8 <= var6; ++var8) {
                var7[var8 - var5] = new GridLine((double)var8, SignalPlot.gridLineColor);
            }

            return var7;
        }
    }
}


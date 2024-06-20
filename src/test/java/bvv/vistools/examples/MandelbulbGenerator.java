package bvv.vistools.examples;

import bdv.util.BdvFunctions;
import bvv.vistools.BvvFunctions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import net.imglib2.util.Util;

public class MandelbulbGenerator {

    public static void main(String[] args) {
        int gridSize = 256; // Adjust grid size as needed
        int maxIter = 255;
        int order = 8; // Mandelbulb order, can be adjusted

        RandomAccessibleInterval<UnsignedByteType> mandelbulb = generateMandelbulb(gridSize, maxIter, order);

        // Display using ImgLib2 (additional code for visualization)
        //BdvFunctions.show(mandelbulb, "Mandelbulb");
        BvvFunctions.show(mandelbulb, "Mandelbulb");
    }

    public static RandomAccessibleInterval<UnsignedByteType> generateMandelbulb(int gridSize, int maxIter, int order) {
        final long[] dimensions = new long[]{gridSize, gridSize, gridSize};
        RandomAccessibleInterval<UnsignedByteType> img = ArrayImgs.unsignedBytes(dimensions);

        for (long z = 0; z < gridSize; z++) {
            for (long y = 0; y < gridSize; y++) {
                for (long x = 0; x < gridSize; x++) {
                    double[] coordinates = new double[]{x, y, z};
                    int iterations = mandelbulbIter(coordinates, maxIter, order);
                    img.getAt(x, y, z).set((int) iterations);
                }
            }
        }
        return img;
    }

    private static int mandelbulbIter(double[] coord, int maxIter, int order) {
        double x = coord[0];
        double y = coord[1];
        double z = coord[2];
        double cx = (x / 128.0 - 1.0) * 2;
        double cy = (y / 128.0 - 1.0) * 2;
        double cz = (z / 128.0 - 1.0) * 2;

        double xn = 0, yn = 0, zn = 0;
        int iter = 0;
        while (iter < maxIter && xn * xn + yn * yn + zn * zn < 4) {
            double r = Math.sqrt(xn * xn + yn * yn + zn * zn);
            double theta = Math.atan2(Math.sqrt(xn * xn + yn * yn), zn);
            double phi = Math.atan2(yn, xn);

            double newR = Math.pow(r, order);
            double newTheta = theta * order;
            double newPhi = phi * order;

            xn = newR * Math.sin(newTheta) * Math.cos(newPhi) + cx;
            yn = newR * Math.sin(newTheta) * Math.sin(newPhi) + cy;
            zn = newR * Math.cos(newTheta) + cz;

            iter++;
        }
        return iter;
    }
}

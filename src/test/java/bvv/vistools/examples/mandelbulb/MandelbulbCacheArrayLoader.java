package bvv.vistools.examples.mandelbulb;

import bdv.img.cache.CacheArrayLoader;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class MandelbulbCacheArrayLoader implements CacheArrayLoader<VolatileShortArray>
{
    private final int maxIter;
    private final int order;

    public MandelbulbCacheArrayLoader(int maxIter, int order)
    {
        this.maxIter = maxIter;
        this.order = order;
    }

    @Override
    public VolatileShortArray loadArray(final int timepoint, final int setup, final int level, final int[] dimensions, final long[] min) throws InterruptedException
    {
        // Generate Mandelbulb for the specific cell region
        final RandomAccessibleInterval<UnsignedShortType> img = generateMandelbulbForCell(dimensions, min, maxIter, order);

        // Create a VolatileShortArray to hold the generated data
        final VolatileShortArray shortArray = new VolatileShortArray(dimensions[0] * dimensions[1] * dimensions[2], true);

        // Extract the data into the short array
        final short[] data = shortArray.getCurrentStorageArray();
        Views.flatIterable(img).forEach(pixel -> data[(int) pixel.index().get()] = (short) pixel.get());

        return shortArray;
    }

    @Override
    public int getBytesPerElement()
    {
        return 2; // Each element is 2 bytes (16 bits)
    }

    public static RandomAccessibleInterval<UnsignedShortType> generateMandelbulbForCell(int[] dimensions, long[] min, int maxIter, int order)
    {
        final RandomAccessibleInterval<UnsignedShortType> img = ArrayImgs.unsignedShorts(new long[]{dimensions[0], dimensions[1], dimensions[2]});

        for (long z = 0; z < dimensions[2]; z++)
        {
            for (long y = 0; y < dimensions[1]; y++)
            {
                for (long x = 0; x < dimensions[0]; x++)
                {
                    double[] coordinates = new double[]{
                            x + min[0],
                            y + min[1],
                            z + min[2]
                    };
                    int iterations = mandelbulbIter(coordinates, maxIter, order);
                    img.getAt(x, y, z).set((int) (iterations * 65535.0 / maxIter)); // Scale to 16-bit range
                }
            }
        }
        return img;
    }

    private static int mandelbulbIter(double[] coord, int maxIter, int order)
    {
        double x = coord[0];
        double y = coord[1];
        double z = coord[2];
        double cx = (x / 128.0 - 1.0) * 2;
        double cy = (y / 128.0 - 1.0) * 2;
        double cz = (z / 128.0 - 1.0) * 2;

        double xn = 0, yn = 0, zn = 0;
        int iter = 0;
        while (iter < maxIter && xn * xn + yn * yn + zn * zn < 4)
        {
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

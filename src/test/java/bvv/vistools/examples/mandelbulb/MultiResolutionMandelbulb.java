package bvv.vistools.examples.mandelbulb;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bvv.core.VolumeViewerPanel;
import bvv.vistools.BvvFunctions;
import bvv.vistools.BvvOptions;
import bvv.vistools.BvvStackSource;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.DefaultVoxelDimensions;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiResolutionMandelbulb {

    public static void main(String[] args) throws Exception {
        // Define max scale level
        int maxScale = 18; // Example maxScale value

        // Desired grid size at the finest resolution level
        final int desiredFinestGridSize = 2; // Define as per your requirement

        // Compute the base grid size
        final int baseGridSize = desiredFinestGridSize * (int) Math.pow(2, maxScale - 1);

        // Generate resolutions and corresponding grid sizes
        final double[][] resolutions = new double[maxScale][3];
        final int[] gridSizes = new int[maxScale];
        for (int i = 0; i < maxScale; i++) {
            double scaleFactor = Math.pow(2, i);
            resolutions[i][0] = scaleFactor;
            resolutions[i][1] = scaleFactor;
            resolutions[i][2] = scaleFactor;
            gridSizes[i] = baseGridSize / (int) scaleFactor;
            System.out.println("Grid size for " + i + " grid size: " + gridSizes[i]);
        }

        MandelbulbCacheArrayLoader.gridSizes = gridSizes;
        MandelbulbCacheArrayLoader.baseGridSize = baseGridSize;
        MandelbulbCacheArrayLoader.desiredFinestGridSize = desiredFinestGridSize;

        // Mandelbulb parameters
        final int maxIter = 255;
        final int order = 8;

        // Create Mandelbulb ImgLoader
        MandelbulbImgLoader imgLoader = new MandelbulbImgLoader(gridSizes, maxIter, order);

        // Create a list of TimePoints (assuming single timepoint)
        final List<TimePoint> timepoints = Collections.singletonList(new TimePoint(0));

        // Create BasicViewSetup
        final BasicViewSetup viewSetup = new BasicViewSetup(0, "setup0", imgLoader.dimensions(), new DefaultVoxelDimensions(3));

        // Create SequenceDescriptionMinimal
        final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal(new TimePoints(timepoints), Collections.singletonMap(viewSetup.getId(), viewSetup), imgLoader, null);

        // Define voxel size
        final double[] voxelSize = {1.0, 1.0, 1.0};

        // Create ViewRegistrations
        final HashMap<ViewId, ViewRegistration> registrations = new HashMap<>();
        for (final BasicViewSetup setup : seq.getViewSetupsOrdered()) {
            final int setupId = setup.getId();
            for (final TimePoint timepoint : seq.getTimePoints().getTimePointsOrdered()) {
                final int timepointId = timepoint.getId();
                for (int level = 0; level < resolutions.length; level++) {
                    AffineTransform3D transform = new AffineTransform3D();
                    transform.set(
                            voxelSize[0] * resolutions[level][0], 0, 0, 0,
                            0, voxelSize[1] * resolutions[level][1], 0, 0,
                            0, 0, voxelSize[2] * resolutions[level][2], 0);
                    registrations.put(new ViewId(timepointId, setupId), new ViewRegistration(timepointId, setupId, transform));
                }
            }
        }

        // Create SpimDataMinimal
        final SpimDataMinimal spimData = new SpimDataMinimal(null, seq, new ViewRegistrations(registrations));

        // Display the SpimData using BVV
        List<BvvStackSource<?>> sources = BvvFunctions.show(spimData, BvvOptions.options().frameTitle("Multiresolution Mandelbulb"));

        for (int k = 0; k < sources.size(); k++) {
            sources.get(k).setDisplayRange(0, 60000);
        }

        // Center the view on the data
        VolumeViewerPanel viewerPanel = sources.get(0).getBvvHandle().getViewerPanel();
        viewerPanel.getState().setViewerTransform(new AffineTransform3D());
    }
}

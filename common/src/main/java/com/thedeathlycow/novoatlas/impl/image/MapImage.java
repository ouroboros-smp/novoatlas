package com.thedeathlycow.novoatlas.impl.image;

import net.minecraft.util.Mth;
import org.joml.Vector2fc;

public abstract class MapImage {
    private final int width;
    private final int height;

    protected MapImage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /// Returns the signed distance, in blocks, from the sampled block to the nearest image edge.
    /// Positive when the block is inside the image, negative when it is outside.
    ///
    /// The index math is in pixels. Each pixel covers {@code horizontalScale} blocks, so the pixel
    /// distance is scaled before it is compared with block-based blend distances.
    public final double getDistanceToEdge(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        double xR = getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width);
        double zR = getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height);

        boolean insideX = xR >= 0 && xR <= this.width;
        boolean insideZ = zR >= 0 && zR <= this.height;

        if (insideX && insideZ) {
            double distX = Math.min(xR, this.width - xR);
            double distZ = Math.min(zR, this.height - zR);
            return Math.min(distX, distZ) * horizontalScale;
        }

        double dx = Math.max(0, Math.max(-xR, xR - this.width));
        double dz = Math.max(0, Math.max(-zR, zR - this.height));
        return -Math.sqrt(dx * dx + dz * dz) * horizontalScale;
    }

    public final boolean isBlockInsideImage(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        double xR = getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width);
        double zR = getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height);

        return xR >= 0 && xR <= this.width && zR >= 0 && zR <= this.height;
    }

    public final int sample(int x, int z, MapInfo info) {
        double horizontalScale = info.horizontalScale().value();
        Vector2fc centerOffset = info.centerOffset();

        double xR = getIndexWithAlpha(x, horizontalScale, centerOffset.x(), this.width);
        double zR = getIndexWithAlpha(z, horizontalScale, centerOffset.y(), this.height);

        return this.sampleInterpolated(xR, zR, info);
    }

    private static double getIndexWithAlpha(int i, double horizontalScale, double centerOffset, double size) {
        double iR = i - (centerOffset * size);
        return (iR / horizontalScale) + size * 0.5;
    }

    public final int width() {
        return width;
    }

    public final int height() {
        return height;
    }

    public final int getTruncated(double x, double z, ImageWrapping imageWrapping) {
        int truncatedX = Mth.floor(x);
        int truncatedZ = Mth.floor(z);
        return this.getPixelValue(truncatedX, truncatedZ, imageWrapping);
    }

    public final int getPixelValue(int x, int z, ImageWrapping imageWrapping) {
        return imageWrapping.transform(x, z, this.width, this.height, this::getPixelValue);
    }

    protected abstract int getPixelValue(int x, int z);

    protected abstract int sampleInterpolated(double x, double z, MapInfo info);
}
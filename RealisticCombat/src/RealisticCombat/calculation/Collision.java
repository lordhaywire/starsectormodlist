package RealisticCombat.calculation;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.BoundsAPI.SegmentAPI;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public final class Collision {

    /**
     * Returns from a line the point nearest a {@link Vector2f}
     * <p></p>
     * @param source {@link Vector2f} to test distance from
     * @param lineStart {@link Vector2f} start of the line to check
     * @param lineEnd {@link Vector2f} end of the line to check
     * <p></p>
     * @return {@link Vector2f} nearest source on the line between lineStart
     *         and lineEnd
     */
    private static Vector2f getNearestPointOnLine(final Vector2f source,
                                                  final Vector2f lineStart,
                                                  final Vector2f lineEnd)
    {
        float u = (source.x - lineStart.x) * (lineEnd.x - lineStart.x)
                + (source.y - lineStart.y) * (lineEnd.y - lineStart.y);
        float denom = Vector2f.sub(lineEnd, lineStart, new Vector2f()).length();

        u /= denom * denom;

        // if closest point on line is outside the segment, clamp to on the segment
        if (u < 0) u = 0; if (u > 1) u = 1;

        Vector2f i = new Vector2f();
        i.x = lineStart.x + u * (lineEnd.x - lineStart.x);
        i.y = lineStart.y + u * (lineEnd.y - lineStart.y);
        return i;
    }

    /**
     * Returns whether a point is along a {@link SegmentAPI}.
     * <p></p>
     * @param point {@link Vector2f} to check
     * @param segment {@link SegmentAPI} to check for collision with
     * <p></p>
     * @return {@code boolean} true if the point is along the segment,
     *         false otherwise.
     */
    private static boolean isPointOnSegment(final Vector2f point, final SegmentAPI segment) {
        return Line2D.Float.ptSegDistSq(
                segment.getP1().getX(), segment.getP1().getY(),
                segment.getP2().getX(), segment.getP2().getY(),
                point.getX(), point.getY()) <= 0.11111f;
    }

    /**
     * Returns whether a {@link Vector2f} is inside or on the {@link BoundsAPI}
     * of a {@link CombatEntityAPI}
     * <p></p>
     * @param point {@link Vector2f} to check
     * @param entity {@link CombatEntityAPI} the {@link BoundsAPI} of which
     *               to check against
     * <p></p>
     * @return {@code boolean} true if point is within or on the bounds of
     *         entity, else false
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPointWithinBounds(final Vector2f point, final CombatEntityAPI entity) {
        // Grab the ship bounds and update them to reflect the ship's position
        final BoundsAPI bounds = entity.getExactBounds();
        if (bounds == null)
            try {
                return Misc.getDistance(point, entity.getLocation()) < entity.getCollisionRadius();
            } catch (final Throwable t) { return false; }
        bounds.update(entity.getLocation(), entity.getFacing());

        // Transform the bounds into a series of points
        List<SegmentAPI> segments = bounds.getSegments();
        List<Vector2f> points = new ArrayList<>(segments.size() + 1);
        SegmentAPI seg;
        for (int x = 0; x < segments.size(); x++) {
            seg = segments.get(x);
            if (isPointOnSegment(point, seg)) return true; // Is the point is exactly on the bounds?
            points.add(seg.getP1());
            if (x == (segments.size() - 1)) points.add(seg.getP2()); // Add the final point
        }

        // Check if the point is inside the bounds polygon
        // This code uses the extremely efficient PNPOLY solution taken from:
        // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        int i, j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).y > point.y) != (points.get(j).y > point.y)
                 && (point.x < (points.get(j).x - points.get(i).x)
                 * (point.y - points.get(i).y)
                 / (points.get(j).y - points.get(i).y) + points.get(i).x)) {
                result = !result;
            }
        }

        return result;
    }

    private static float getBeamFacing(final BeamAPI beam) {
        return Misc.getAngleInDegrees(beam.getFrom(), beam.getTo());
    }

    private static float getFacing(final Object object) {
        return (object instanceof BeamAPI) ? getBeamFacing((BeamAPI) object)
                : ((DamagingProjectileAPI) object).getFacing();
    }

    public static Vector2f getLocation(final Object object) {
        return (object instanceof BeamAPI) ? ((BeamAPI) object).getTo()
                : ((DamagingProjectileAPI) object).getLocation();
    }

    /**
     * Returns the {@link SegmentAPI} nearest a {@link Vector2f}.
     * <p></p>
     * @param point {@link Vector2f} whence to check distance
     * @param ship {@link ShipAPI} whereof to check bounds
     * <p></p>
     * @return {@link SegmentAPI} nearest a {@link Vector2f}
     */
    public static SegmentAPI getClosestSegment(final Vector2f point, final ShipAPI ship) {
        if (point == null || ship == null) return null;
        final BoundsAPI bounds = ship.getExactBounds();
        if (bounds == null) return null;
        final List<SegmentAPI> segments = bounds.getSegments();
        if (segments == null) return null;
        SegmentAPI hitSegment = null; float closestDistanceSquared = Float.MAX_VALUE; Vector2f tmp;
        for (final SegmentAPI segment : segments) {
            tmp = getNearestPointOnLine(point, segment.getP1(), segment.getP2());
            final float distanceSquared = Misc.getDistanceSq(point, tmp);
            if (distanceSquared < closestDistanceSquared) {
                hitSegment = segment; closestDistanceSquared = distanceSquared;
            }
        } return hitSegment;
    }

    /**
     * @return {@code float} oblique angle, in degrees, at which an {@link DamagingProjectileAPI},
     * {@link MissileAPI}, or {@link BeamAPI} hits a {@link ShipAPI}.
     */
    public static float getObliqueAngle(final Object object, final ShipAPI ship) {
        final SegmentAPI hitSegment = getClosestSegment(getLocation(object), ship);
        if (hitSegment == null) return 0;
        final float segmentFacing = Misc.getAngleInDegrees(hitSegment.getP1(), hitSegment.getP2());
        final float collisionAngle = Math.abs(getFacing(object) - segmentFacing) % 180;
        return collisionAngle > 90 ? 180 - collisionAngle : collisionAngle;
    }
}

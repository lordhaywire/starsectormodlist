package RealisticCombat.renderers;

import RealisticCombat.calculation.Vector;
import RealisticCombat.scripts.Categorization;
import RealisticCombat.settings.Colors;
import RealisticCombat.settings.Status.QuantityName;
import RealisticCombat.util.OpenGLUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Status implements Indication {

    private static final int[][] SIGNS = { {-1, -1}, {1, -1}, {1, 1}, {-1, 1} };

    private static final float COS = (float) Math.cos(Math.PI / 4),
                               SIN = (float) Math.sin(Math.PI / 4),
                               SQRT2 = (float) Math.sqrt(2),
                               THICKENING_THRESHOLD = 6,
                               THINNING_THRESHOLD = 2,
                               THINNING_THRESHOLD_SQUARED = 4,
                               MINIMUM_THICKNESS = 0.25f;

    private float viewMult, alpha, offsetLength, offsetRadius;

    private Vector2f[] offsetsThickness;

    private static boolean isStatusToBeIndicated(final ShipAPI ship) {
        return Categorization.isFlying(ship)
                && Global.getCombatEngine().getFogOfWar(0).isVisible(ship)
                && !(Categorization.isStrikeCraft(ship) || Categorization.isStation(ship));
    }

    private static int getEdgeIndex(final float level) { return (int) Math.min(4f * level, 3); }

    private static float getOffsetThickness(final float edgeThickness) {
        return SQRT2 * edgeThickness;
    }

    private static float getOffsetLength(final float edgeThickness) {
        return 2 * edgeThickness;
    }

    private static float getOffsetRadius(final float offsetThickness) {
        return 2 * offsetThickness;
    }

    private static float getEdgeThickness(final float viewMult) {
        float edgeThickness = RealisticCombat.settings.Status.getEdgeThickness();
        if (THICKENING_THRESHOLD <= viewMult)
            return edgeThickness + 0.5f * (viewMult - THICKENING_THRESHOLD);
        else if (viewMult < THINNING_THRESHOLD)
            return edgeThickness
                   * Math.max(viewMult * viewMult / THINNING_THRESHOLD_SQUARED, MINIMUM_THICKNESS);
        return edgeThickness;
    }

    private static float getEdgeFraction(final float level, final int edgeIndex) {
        return 4 * Math.min(level - 0.25f * edgeIndex, 0.25f);
    }

    private static float getDiamondFraction(final ShipAPI ship, final QuantityName quantityName) {
        final float level;
        switch (quantityName) {
            case HULL: level = ship.getHullLevel(); break;
            case FLUX: level = ship.getFluxLevel(); break;
            default: return 0;
        }
        return Math.min(1, Math.max(0, level));
    }

    private static float getInnermostRadius(final ShipAPI ship) {
        return ship.getShieldRadiusEvenIfNoShield()
                * RealisticCombat.settings.Status.getInnermostDiamondRadiusFactor();
    }

    private static Vector2f[] getThicknessOffsets(final float offsetThickness) {
        return new Vector2f[]{
                new Vector2f(-offsetThickness, offsetThickness),
                new Vector2f(-offsetThickness, -offsetThickness),
                new Vector2f(offsetThickness, -offsetThickness),
                new Vector2f(offsetThickness, offsetThickness)
        };
    }

    /**
     * Return the vertices of a diamond indicating quantity level.
     * <p></p>
     * Begin the diamond from the top and draw counter-clockwise, each edge
     * beginning where the last one ends, until the proportion of the total
     * edge lengths to the possible perimeter equals the level.
     * <p></p>
     * @param center {@link Vector2f} of the diamond
     * @param level {@code float} proportion from 0 to 1
     * @param radius {@code float} of the diamond from center to corner
     * <p></p>
     * @return {@code Vector2f[]} of the diamond edges
     */
    private static Vector2f[][] getDiamondEdges(final Vector2f center,
                                                final float level,
                                                final float radius,
                                                final float offsetLength)
    {
        final float maximumLength = SQRT2 * radius + offsetLength,
                    x0 = center.getX(), y0 = center.getY();
        final float[] X = { x0, x0 - radius, x0, x0 + radius },
                      Y = { y0 + radius, y0, y0 - radius, y0 };
        final Vector2f[][] edges = new Vector2f[getEdgeIndex(level)+1][2];
        for (int i = 0; i < edges.length; i++) {
            float length = maximumLength * getEdgeFraction(level, i),
                  x = SIGNS[i][0] * COS * length + X[i], y = SIGNS[i][1] * SIN * length + Y[i];
            edges[i] = new Vector2f[] { new Vector2f(X[i], Y[i]), edges[i][1] = new Vector2f(x, y)};
        } return edges;
    }

    private static Vector2f[] getEdgeTriangleOne(final Vector2f center,
                                                 final Vector2f corner,
                                                 final Vector2f offsetThickness) {
        return new Vector2f[] {
                new Vector2f(center), Vector.sum(center, offsetThickness), new Vector2f(corner)
        };
    }

    private static Vector2f[] getEdgeTriangleTwo(final Vector2f center,
                                                 final Vector2f corner,
                                                 final Vector2f offsetThickness) {
        return new Vector2f[]{
                new Vector2f(corner),
                Vector.sum(corner, offsetThickness),
                Vector.sum(center, offsetThickness)
        };
    }

    private static void renderTriangle(final Vector2f[] triangle) {
        GL11.glBegin(GL11.GL_POLYGON);
        for (Vector2f vertex : triangle) GL11.glVertex2f(vertex.getX(), vertex.getY());
        GL11.glEnd();
    }

    private static void renderRectangle(final Vector2f innerEdgeStart,
                                        final Vector2f innerEdgeEnd,
                                        final Vector2f offsetThickness)
    {
        renderTriangle(getEdgeTriangleOne(innerEdgeStart, innerEdgeEnd, offsetThickness));
        renderTriangle(getEdgeTriangleTwo(innerEdgeStart, innerEdgeEnd, offsetThickness));
    }

    private static void renderDiamond(final Vector2f[][] edges, final Vector2f[] offsetsThickness) {
        for (int i = 0; i < edges.length; i++)
            renderRectangle(edges[i][0], edges[i][1], offsetsThickness[i]);
    }


    /**
     * Render a small rectangle across the flux indication diamond to show hard
     * flux level.
     */
    private static void renderInlineIndicator(final float hardFlux,
                                              final Vector2f[][] softFluxDiamondEdges,
                                              final float radius,
                                              final Vector2f[] offsetsThickness,
                                              final float offsetLength)
    {
        final int edge = getEdgeIndex(hardFlux);
        final int[] sign = SIGNS[edge];
        final Vector2f corner = softFluxDiamondEdges[edge][0];
        final float offsetCorner = SQRT2 * radius * getEdgeFraction(hardFlux, edge),
                    x0 = corner.getX() + sign[0] * COS * offsetCorner,
                    y0 = corner.getY() + sign[1] * SIN * offsetCorner,
                    x = x0 - sign[0] * SIN * offsetLength,
                    y = y0 - sign[1] * COS * offsetLength;
        final Vector2f innerEdgeStart = Vector.sum(new Vector2f(x0, y0), offsetsThickness[edge]),
                       innerEdgeEnd = Vector.sum(new Vector2f(x, y), offsetsThickness[edge]),
                       offsetThickness = Vector.scalarProduct(2, offsetsThickness[edge]);
        renderRectangle(innerEdgeStart, innerEdgeEnd, offsetThickness);
    }

    private static void indicateHull(final ShipAPI ship,
                                     final float radius,
                                     final Vector2f[] offsetsThickness,
                                     final float offsetLength) {
        final float hull = getDiamondFraction(ship, QuantityName.HULL);
        if (hull == 0) return;
        final Vector2f[][] edges = getDiamondEdges(ship.getShieldCenterEvenIfNoShield(), hull,
                radius, offsetLength);
        renderDiamond(edges, offsetsThickness);
    }

    private static void indicateFlux(final ShipAPI ship,
                                     final float radius,
                                     final Vector2f[] offsetsThickness,
                                     final float offsetLength)
    {
        final float softFlux = getDiamondFraction(ship, QuantityName.FLUX);
        if (softFlux == 0) return;
        final Vector2f[][] edges = getDiamondEdges(ship.getShieldCenterEvenIfNoShield(), softFlux,
                radius, offsetLength);
        renderDiamond(edges, offsetsThickness);

        final float hardFlux = Math.min(1, Math.max(0, ship.getHardFluxLevel()));
        if (hardFlux == 0) return;
        //hard flux sometimes exceeds soft flux
        try { renderInlineIndicator(hardFlux, edges, radius, offsetsThickness, offsetLength); }
        catch (ArrayIndexOutOfBoundsException ignored) {
            final Vector2f[][] hardFluxEdges = getDiamondEdges(ship.getShieldCenterEvenIfNoShield(),
                    hardFlux, radius, offsetLength);
            renderInlineIndicator(hardFlux, hardFluxEdges, radius, offsetsThickness, offsetLength);
        }
    }

    private void resetOffsets(final float viewMult) {
        final float edgeThickness = getEdgeThickness(viewMult),
                    offsetThickness = getOffsetThickness(edgeThickness);
        offsetLength = getOffsetLength(edgeThickness);
        offsetRadius = getOffsetRadius(offsetThickness);
        offsetsThickness = getThicknessOffsets(offsetThickness);
    }

    private void indicateStatus(final ShipAPI ship, final Color hullColor, final float alpha) {
        OpenGLUtils.glColor(hullColor);
        final float radiusHull = getInnermostRadius(ship);
        indicateHull(ship, radiusHull, offsetsThickness, offsetLength);
        OpenGLUtils.glColor(Colors.getFlux(), alpha);
        indicateFlux(ship, radiusHull + offsetRadius, offsetsThickness, offsetLength);
    }

    private void indicateStatuses(final List<ShipAPI> ships,
                                  final Color colorHull,
                                  final float alpha)
    {
        final List<Float> radiiHull = new ArrayList<>();
        OpenGLUtils.glColor(colorHull, alpha);
        for (int i = 0; i < ships.size(); i++) {
            radiiHull.add(getInnermostRadius(ships.get(i)));
            indicateHull(ships.get(i), radiiHull.get(i), offsetsThickness, offsetLength);
        }
        OpenGLUtils.glColor(Colors.getFlux(), alpha);
        for (int i = 0; i < ships.size(); i++)
            indicateFlux(ships.get(i), radiiHull.get(i) + offsetRadius, offsetsThickness,
                    offsetLength);
    }

    @Override
    public void render(final ViewportAPI viewport) {
        if (viewport.getViewMult() != viewMult) {
            viewMult = viewport.getViewMult();
            resetOffsets(viewMult);
            alpha = Colors.getAlpha(viewport.getViewMult());
        }

        final ShipAPI player = Global.getCombatEngine().getPlayerShip(),
                      target = player == null ? null : player.getShipTarget();
        final List<ShipAPI> allies = new ArrayList<>(), enemies = new ArrayList<>();
        for (final ShipAPI ship : Global.getCombatEngine().getShips())
            if (isStatusToBeIndicated(ship))
                if (!(ship == player || ship == target))
                    if (ship.getOwner() == 0) allies.add(ship); else enemies.add(ship);

        OpenGLUtils.setupRendering();
        indicateStatuses(enemies, Colors.getEnemy(), alpha);
        if (target != null) indicateStatus(target, Colors.getTarget(), alpha);
        if (Global.getCombatEngine().isUIShowingHUD()) OpenGLUtils.finishRendering();
        else {
            indicateStatuses(allies, Colors.getFriendly(), alpha);
            if (player != null) indicateStatus(player, Colors.getPlayer(), alpha);
            OpenGLUtils.finishRendering();
        }
    }
}

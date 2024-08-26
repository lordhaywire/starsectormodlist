package RealisticCombat.renderers;

import RealisticCombat.calculation.Vector;
import RealisticCombat.settings.Colors;
import RealisticCombat.util.DrawUtils;
import RealisticCombat.util.OpenGLUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Indicates the momentum of a ship.
 *
 * The momentum indicator points along the velocity of the ship, and its width
 * increases with the log10 of the ship mass.
 */
public final class Projectile implements Indication {

    private static final int projectileIndicatorFrequency = 5;

    private static final float weakProjectileDamageThreshold = 200;

    private static final Vector2f MAP_CENTER = new Vector2f(0, 0);


    private final HashMap<WeaponAPI, Integer> weaponToShotCountHashMap = new HashMap<>();

    private final HashMap<WeaponAPI, HashMap<DamagingProjectileAPI, Integer>>
            weaponToWeakProjectileToNumberHashMap = new HashMap<>();


    private static float getRescaleFactor(final float viewMult) {
        return (float) Math.sqrt(viewMult);
    }

    private static Iterator<Object> getAllObjects() {
        return Global.getCombatEngine().getAllObjectGrid().getCheckIterator(
                MAP_CENTER,
                Global.getCombatEngine().getMapWidth(),
                Global.getCombatEngine().getMapHeight()
        );
    }

    private static boolean isGone(final DamagingProjectileAPI projectile) {
        return projectile.didDamage() || projectile.isFading() || projectile.isExpired();
    }

    private static boolean isStrong(final DamagingProjectileAPI projectile) {
        return projectile.getDamageAmount() > weakProjectileDamageThreshold
                || projectile instanceof MissileAPI;
    }

    private static Vector2f[][] getTrianglesBallistic(final Vector2f center,
                                                      final Vector2f velocity,
                                                      final float width,
                                                      final float length)
    {
        final Vector2f
                backward = Misc.getUnitVector(velocity, new Vector2f()),
                leftward = Misc.getPerp(backward),
                forward = new Vector2f(-backward.getX(), -backward.getY()),
                rightward = new Vector2f(-leftward.getX(), -leftward.getY()),

                bodyEdgeLeft = new Vector2f(width / 2 * leftward.getX(),
                                            width / 2 * leftward.getY()),
                bodyEdgeRight = new Vector2f(width / 2 * rightward.getX(),
                                             width / 2 * rightward.getY()),
                bodyEdgeRear = new Vector2f(length * backward.getX(),
                                            length * backward.getY()),

                nosePoint = Vector.sum(center, new Vector2f(length / 4 * forward.getX(),
                                                            length / 4 * forward.getY())),
                frontLeft = Vector.sum(center, bodyEdgeLeft),
                frontRight = Vector.sum(center, bodyEdgeRight),
                bodyCenterRear = Vector.sum(center, bodyEdgeRear),
                rearLeft = Vector.sum(bodyCenterRear, bodyEdgeLeft),
                rearRight = Vector.sum(bodyCenterRear, bodyEdgeRight);

        final Vector2f[]
                nose = { nosePoint, frontLeft, frontRight },
                squareBottomLeft = { frontLeft, rearLeft, rearRight },
                squareUpperRight = { frontLeft, frontRight, rearRight };

        return new Vector2f[][] { nose, squareBottomLeft, squareUpperRight };
    }

    private static Vector2f[][] getTrianglesMissile(final Vector2f center,
                                                    final Vector2f velocity,
                                                    final float width,
                                                    final float length)
    {
        final Vector2f
                backward = Misc.getUnitVector(velocity, new Vector2f()),
                leftward = Misc.getPerp(backward),
                forward = new Vector2f(-backward.getX(), -backward.getY()),
                rightward = new Vector2f(-leftward.getX(), -leftward.getY()),

                bodyEdgeLeft = new Vector2f(width / 2 * leftward.getX(),
                                            width / 2 * leftward.getY()),
                bodyEdgeRight = new Vector2f(width / 2 * rightward.getX(),
                                             width / 2 * rightward.getY()),
                bodyEdgeRear = new Vector2f(length * backward.getX(),
                                            length * backward.getY()),

                nosePoint = Vector.sum(center, new Vector2f(0.75f * length * forward.getX(),
                                                            0.75f * length * forward.getY())),
                frontLeft = Vector.sum(center, bodyEdgeLeft),
                frontRight = Vector.sum(center, bodyEdgeRight),
                bodyCenterRear = Vector.sum(center, bodyEdgeRear),
                rearLeft = Vector.sum(bodyCenterRear, bodyEdgeLeft),
                rearRight = Vector.sum(bodyCenterRear, bodyEdgeRight),

                finTipLeft = new Vector2f(width / 3 * leftward.getX(),
                                          width / 3 * leftward.getY()),
                finTipRight = new Vector2f(width / 3 * rightward.getX(),
                                           width / 3 * rightward.getY()),
                finTip = new Vector2f(length / 3 * forward.getX(),
                                      length / 3 * forward.getY()),
                finRoot = new Vector2f(length / 3 * backward.getX(),
                                       length / 3 * backward.getY()),

                forwardFinRoot = Vector.sum(center, finRoot),
                forwardLeftFinRoot = Vector.sum(forwardFinRoot, bodyEdgeLeft),
                forwardRightFinRoot = Vector.sum(forwardFinRoot, bodyEdgeRight),
                forwardLeftFinTip = Vector.sum(forwardLeftFinRoot, finTipLeft),
                forwardRightFinTip = Vector.sum(forwardRightFinRoot, finTipRight),

                rearFinLead = Vector.sum(bodyCenterRear, finTip),
                rearLeftFinLead = Vector.sum(rearFinLead, bodyEdgeLeft),
                rearRightFinLead = Vector.sum(rearFinLead, bodyEdgeRight),
                rearLeftFinTip = Vector.sum(rearLeft, finTipLeft),
                rearRightFinTip = Vector.sum(rearRight, finTipRight);

        final Vector2f[]
                nose = { nosePoint, frontLeft, frontRight },
                bodyRearLeft = { frontLeft, rearLeft, rearRight },
                bodyFrontRight = { frontLeft, frontRight, rearRight },
                forwardLeftFin = { frontLeft, forwardLeftFinRoot, forwardLeftFinTip },
                upperRightFin = { frontRight, forwardRightFinRoot, forwardRightFinTip },
                rearLeftFin = { rearLeft, rearLeftFinLead, rearLeftFinTip },
                rearRightFin = { rearRight, rearRightFinLead, rearRightFinTip };

        return new Vector2f[][] { nose, bodyRearLeft, bodyFrontRight, forwardLeftFin, upperRightFin,
                                  rearLeftFin, rearRightFin };
    }

    private static Vector2f[][] getTrianglesEnergy(final Vector2f center, final float radius) {
        final int segments = RealisticCombat.settings.Projectile.getEnergySegments();
        final float THETA = 2f * 3.1415926f / segments,
                    COS = (float) Math.cos(THETA), SIN = (float) Math.sin(THETA);
        Vector2f[][] triangles = new Vector2f[segments][3];
        float tmp, x = radius, y = 0; // Start at angle = 0
        for (int i = 0; i < segments; i++) {
            triangles[i][0] = center;
            triangles[i][1] = new Vector2f(x + center.x, y + center.y); // Output vertex
            tmp = x;
            x = x * COS - y * SIN; y = tmp * SIN + y * COS; // Apply the rotation matrix
            triangles[i][2] = new Vector2f(x + center.x, y + center.y);
        } return triangles;
    }

    @Override
    public void render(final ViewportAPI viewport) {
        final float
                alpha = Colors.getAlpha(viewport.getViewMult()),
                scale = getRescaleFactor(viewport.getViewMult()),
                widthBallisticStrong =
                        scale * RealisticCombat.settings.Projectile.getBallisticWidth(true),
                lengthBallisticStrong =
                        scale * RealisticCombat.settings.Projectile.getBallisticLength(true),
                widthMissileStrong =
                        scale * RealisticCombat.settings.Projectile.getMissileWidth(true),
                lengthMissileStrong =
                        scale * RealisticCombat.settings.Projectile.getMissileLength(true),
                radiusEnergyStrong =
                        scale * RealisticCombat.settings.Projectile.getEnergyRadius(true),
                widthBallisticWeak =
                        scale * RealisticCombat.settings.Projectile.getBallisticWidth(false),
                lengthBallisticWeak =
                        scale * RealisticCombat.settings.Projectile.getBallisticLength(false),
                widthMissileWeak =
                        scale * RealisticCombat.settings.Projectile.getMissileWidth(false),
                lengthMissileWeak =
                        scale * RealisticCombat.settings.Projectile.getMissileLength(false),
                radiusEnergyWeak =
                        scale * RealisticCombat.settings.Projectile.getEnergyRadius(false);
        OpenGLUtils.setupRendering();

        for (final Iterator<Object> it = getAllObjects(); it.hasNext();) {
            final Object object = it.next();
            if (!(object instanceof DamagingProjectileAPI)) continue;
            final DamagingProjectileAPI projectile = (DamagingProjectileAPI) object;
            final WeaponAPI weapon = projectile.getWeapon();
            if (weapon == null) continue;
            final boolean missile = projectile instanceof MissileAPI;
            if (missile && ((MissileAPI) projectile).isFlare()) continue;
            boolean strong = false, tracer = false;

            if (isGone(projectile) && weaponToWeakProjectileToNumberHashMap.containsKey(weapon))
                weaponToWeakProjectileToNumberHashMap.get(weapon).remove(projectile);
            else if (isStrong(projectile)) strong = true;
            else if (weaponToWeakProjectileToNumberHashMap.containsKey(weapon)) {
                int shotCount = weaponToShotCountHashMap.containsKey(weapon)
                                ? weaponToShotCountHashMap.get(weapon) : 0;
                if (weaponToWeakProjectileToNumberHashMap.get(weapon).containsKey(projectile)
                    && weaponToWeakProjectileToNumberHashMap.get(weapon).get(projectile)
                        % projectileIndicatorFrequency == 0)
                    tracer = true;
                else {
                    weaponToShotCountHashMap.put(weapon, ++shotCount);
                    weaponToWeakProjectileToNumberHashMap.get(weapon).put(projectile, shotCount);
                }
            } else {
                weaponToWeakProjectileToNumberHashMap.put(weapon,
                        new HashMap<DamagingProjectileAPI, Integer>() {{ put(projectile, 0); }});
                weaponToShotCountHashMap.put(weapon, 0);
            }

            if (!(tracer || strong)) continue;
            final Vector2f location = projectile.getLocation(), velocity = projectile.getVelocity();
            final Vector2f[][]
                triangles = missile
                    ? getTrianglesMissile(location, velocity,
                                          strong ? widthMissileStrong : widthMissileWeak,
                                          strong ? lengthMissileStrong : lengthMissileWeak)
                    : projectile.getDamageType() == DamageType.ENERGY
                        ? getTrianglesEnergy(location, strong ? radiusEnergyStrong
                                                              : radiusEnergyWeak)
                        : getTrianglesBallistic(location, velocity,
                                                strong ? widthBallisticStrong
                                                       : widthBallisticWeak,
                                                strong ? lengthBallisticStrong
                                                       : lengthBallisticWeak);
            OpenGLUtils.glColor(Colors.getColor(projectile), alpha);
            for (final Vector2f[] triangle : triangles) DrawUtils.drawTriangle(triangle);
        } OpenGLUtils.finishRendering();
    }
}

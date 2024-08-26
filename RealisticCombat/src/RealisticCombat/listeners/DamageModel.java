package RealisticCombat.listeners;

import RealisticCombat.calculation.*;
import RealisticCombat.com.fs.starfarer.api.impl.campaign.skills.ArmorAngling;
import RealisticCombat.com.fs.starfarer.api.impl.campaign.skills.WeakpointFamiliarization;
import RealisticCombat.plugins.Announcer;
import RealisticCombat.scripts.DamageReportManagerV1;
import RealisticCombat.scripts.DamageReportV1;
import RealisticCombat.scripts.Ricochet;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CombatDamageData;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import static RealisticCombat.calculation.Damage.getDamageType;
import static RealisticCombat.settings.DamageModel.*;


public final class DamageModel implements DamageTakenModifier {

    private static ShipAPI getSource(final Object object) {
        final ShipAPI ship;
        if (object instanceof DamagingProjectileAPI) {
            final DamagingProjectileAPI projectile = (DamagingProjectileAPI) object;
            ship = projectile.getSource();
        } else { ship = ((BeamAPI) object).getSource(); }
        return (!(ship.isFighter() || ship.isDrone())
                || ship.getWing() == null
                || ship.getWing().getSourceShip() == null) ? ship : ship.getWing().getSourceShip();
    }

    private static boolean isProhibited(final Object object,
                                        final CombatEntityAPI combatEntity,
                                        final Vector2f point)
    {
        return (object == null || combatEntity == null || point == null)
                || !(object instanceof DamagingProjectileAPI || object instanceof BeamAPI);
    }

    /**
     * @return {@link ShipEngineAPI} nearest this {@link Vector2f} point aboard
     *         this {@link ShipAPI}
     */
    private static ShipEngineAPI getNearestEngine(final ShipAPI ship, final Vector2f point) {
        ShipEngineAPI nearestEngine = null;
        float distanceSquaredClosest = Float.POSITIVE_INFINITY;
        for (ShipEngineAPI engine : ship.getEngineController().getShipEngines()) {
            float distance = Misc.getDistanceSq(engine.getLocation(), point);
            if (distance < distanceSquaredClosest) {
                nearestEngine = engine; distanceSquaredClosest = distance;
            }
        } return nearestEngine;
    }

    /**
     * @return {@link WeaponAPI} nearest this {@link Vector2f} point aboard
     *         this {@link ShipAPI}
     */
    private static WeaponAPI getNearestWeapon(final ShipAPI ship, final Vector2f point) {
        WeaponAPI nearestWeapon = null;
        float distanceSquaredClosest = Float.POSITIVE_INFINITY;
        for (final WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative()) continue;
            float distance = Misc.getDistanceSq(weapon.getLocation(), point);
            if (distance < distanceSquaredClosest) {
                nearestWeapon = weapon; distanceSquaredClosest = distance;
            }
        } return nearestWeapon;
    }

    /**
     * Return the {@link ShipEngineAPI} or {@link WeaponAPI}, if any, nearest
     * this {@link Vector2f} point aboard this {@link ShipAPI}
     */
    private static Object getNearestWorkingModule(final ShipAPI ship, final Vector2f point) {
        final ShipEngineAPI nearestEngine = getNearestEngine(ship, point);
        final WeaponAPI nearestWeapon = getNearestWeapon(ship, point);
        if (nearestEngine == null && nearestWeapon == null) return null;
        if (nearestEngine != null && nearestWeapon != null) {
            if (Misc.getDistanceSq(nearestEngine.getLocation(), point)
                < Misc.getDistanceSq(nearestWeapon.getLocation(), point)) {
                if (!nearestEngine.isDisabled()) return nearestEngine;
            } if (!nearestWeapon.isDisabled()) return nearestWeapon;
        } if (nearestEngine == null && !nearestWeapon.isDisabled()) return nearestWeapon;
        if (nearestWeapon == null && !nearestEngine.isDisabled()) return nearestEngine;
        return null;
    }

    /**
     * Return the compartment center {@link Vector2f} in world coordinates
     *
     * @param x {@code int} horizontal {@link ArmorGridAPI} coordinate of
     *          the compartment
     * @param y {@code int} vertical {@link ArmorGridAPI} coordinate of the
     *          compartment
     * @param compartments {@link ArmorGridAPI} containing the compartments
     *
     * @return compartment center {@link Vector2f} in world coordinates
     */
    private static Vector2f getCompartmentCenter(final int x,
                                                 final int y,
                                                 final ArmorGridAPI compartments)
    {
        final Vector2f upperRightCorner = compartments.getLocation(x, y);
        final float halfSideLength = compartments.getCellSize() / 2;
        return new Vector2f(upperRightCorner.x - halfSideLength,
                            upperRightCorner.y - halfSideLength);
    }

    private static void setDamage(final Object object, final float amount) {
        if (object instanceof BeamAPI) ((BeamAPI) object).getDamage().setDamage(amount);
        else ((DamagingProjectileAPI) object).setDamageAmount(amount);
    }

    private static float getMinimumDamage(final Object object) {
        return 0.1f;
    }

    private static float getSkillFactor(final PersonAPI shooter, final PersonAPI evader)
    {
        final boolean analyzed = shooter != null && shooter.getStats().hasSkill("target analysis"),
                      mitigated = evader != null && evader.getStats().hasSkill("impact mitigation");
        return analyzed && mitigated ? 1
                : analyzed ? ArmorAngling.OBLIQUE_ANGLE_FACTOR
                           : mitigated ? WeakpointFamiliarization.OBLIQUE_ANGLE_FACTOR : 1;
    }

    private static String getWeaponName(final Object object) {
        final WeaponAPI weapon = object instanceof DamagingProjectileAPI
                ? ((DamagingProjectileAPI) object).getWeapon() : ((BeamAPI) object).getWeapon();
        final ShipAPI ship = object instanceof DamagingProjectileAPI
                ? ((DamagingProjectileAPI) object).getSource() : ((BeamAPI) object).getSource();
        final String weaponName = weapon == null ? "unnamed weapon" : weapon.getDisplayName();
        return ((ship.isFighter() || ship.isDrone())
                && ship.getHullSpec() != null
                && ship.getHullSpec().getHullName() != null)
                 ? ship.getHullSpec().getHullName() + " (" + weaponName + ")" : weaponName;
    }

    private static void disableOrDestroy(final ShipAPI target,
                                         final int[] hitCompartment,
                                         final Vector2f point)
    {
        target.setHitpoints(1);
        target.getArmorGrid().setArmorValue(hitCompartment[0], hitCompartment[1], 0);
        Global.getCombatEngine().applyDamage(target, point, 100, DamageType.OTHER, 0,
                false, false, null);
        //TODO: Add Announcer's voice
        /*final boolean destroyed = true;
        Announcer.requestAnnouncement(
                destroyed ? target.getOwner() == 0
                            ? RealisticCombat.settings.Announcer.EVENT_TYPE.DESTROYED_FRIENDLY
                            : RealisticCombat.settings.Announcer.EVENT_TYPE.DESTROYED_ENEMY
                          : target.getOwner() == 0
                            ? RealisticCombat.settings.Announcer.EVENT_TYPE.DISABLED_FRIENDLY
                            : RealisticCombat.settings.Announcer.EVENT_TYPE.DISABLED_ENEMY);*/
    }

    private static void displayDamageText(final ShipAPI target,
                                          final ShipAPI source,
                                          final Vector2f point,
                                          final float compartment,
                                          final float hull)
    {
        Global.getCombatEngine().addFloatingDamageText(
                Vector.sum(point, getFloatingTextOffsetCompartmentDamage()),
                compartment, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, source);
        Global.getCombatEngine().addFloatingDamageText(
                Vector.sum(point, getFloatingTextOffsetHullDamage()), hull,
                Misc.FLOATY_HULL_DAMAGE_COLOR, target, source);
    }

    private static void hitShield(final BeamAPI beam,
                                  final ShipAPI target,
                                  final DamageTracker damageTracker)
    {
        damageTracker.compartment = 0; damageTracker.hull = 0; damageTracker.emp = 0;
        damageTracker.shield = Damage.getShield(target, beam);
        if (beam.getWeapon().getDamage().isForceHardFlux())
            beam.getDamage().setForceHardFlux(true);
        setDamage(beam, damageTracker.shield);
    }

    private static void hitShield(final DamagingProjectileAPI projectile,
                                  final ShipAPI target,
                                  final DamageTracker damageTracker)
    {
        damageTracker.compartment = 0; damageTracker.hull = 0; damageTracker.emp = 0;
        damageTracker.shield = Damage.getShield(target, projectile);
        projectile.getDamage().setForceHardFlux(true);
        setDamage(projectile, damageTracker.shield);
    }

    private static void hitCompartments(final ShipAPI target,
                                        final int[] hitCompartment,
                                        final ArmorGridAPI compartments,
                                        final float compartmentPotential,
                                        final DamageTracker damageTracker) {
        damageTracker.compartment = 0;
        for (int i = 0; i < getPossibleNearbyCompartments().length; i++) {
            final int x = hitCompartment[0] + getPossibleNearbyCompartments()[i][0],
                      y = hitCompartment[1] + getPossibleNearbyCompartments()[i][1];
            final float integrity = compartments.getArmorValue(x, y),
                        potential = getCompartmentDamageDistribution()[i] * compartmentPotential,
                        inflicted = Math.min(integrity, potential);
            if (Collision.isPointWithinBounds(getCompartmentCenter(x, y, compartments), target)) {
                damageTracker.compartment += inflicted;
                final float overflow = getCompartmentDamageOverflowFactor()
                                       * Math.max(potential - integrity, 0);
                damageTracker.hull += inflicted + overflow;
            }
            compartments.setArmorValue(x, y, integrity - inflicted);
        }
    }

    private static void hitCitadel(final float potential,
                                   final DamageTracker damageTracker)
    {
        damageTracker.hull = potential;
    }

    private static void causeCriticalMalfunction(final ShipAPI target,
                                                 final Vector2f point,
                                                 final DamageTracker damageTracker)
    {
        damageTracker.hull += target.getBaseCriticalMalfunctionDamage();
        final Object module = getNearestWorkingModule(target, point);
        if (module == null) return;
        if (module instanceof ShipEngineAPI) ((ShipEngineAPI) module).disable();
        else ((WeaponAPI) module).disable();
    }

    private static void hitShip(final DamagingProjectileAPI projectile,
                                final ShipAPI target,
                                final ShipAPI source,
                                final Vector2f point,
                                final DamageTracker damageTracker)
    {
        damageTracker.emp = Damage.getEmp(projectile); damageTracker.shield = 0;

        final ArmorGridAPI compartments = target.getArmorGrid();
        final int[] hitCompartment = compartments.getCellAtLocation(point);

        if (hitCompartment == null) {
            damageTracker.compartment = 0; damageTracker.hull = 0;
            if (Ricochet.isCaused(projectile, target)) Ricochet.occur(projectile, target);
        } else {
            final PersonAPI sourceCaptain = source.getCaptain(),
                            targetCaptain = target.getCaptain();
            final float penetration = Penetration.getPenetration(projectile),
                        thicknessFactor =
                                RealisticCombat.settings.DamageModel.getArmorThicknessFactor(
                                        getDamageType(projectile)),
                        surfaceArmor = Armor.getSurfaceArmor(target.getArmorGrid())
                                       * thicknessFactor,
                        obliqueAngle = Collision.getObliqueAngle(projectile, target),
                        skillFactor = getSkillFactor(sourceCaptain, targetCaptain),
                        effectiveAngle = Math.max(0, Math.min(90, obliqueAngle * skillFactor)),
                        angleFactor = Armor.getAngleFactor(effectiveAngle);

            if (penetration > angleFactor * surfaceArmor
                || penetration > RealisticCombat.settings.DamageModel.getArmorOvermatchFactor()
                                 * surfaceArmor) {
                final float potential = Damage.getPotential(projectile),
                            compartmentPotential = getCompartmentDamageFactor() * potential;
                hitCompartments(target, hitCompartment, compartments, compartmentPotential,
                                damageTracker);
                final float totalArmor = Armor.getTotalArmor(target.getArmorGrid())
                                         * thicknessFactor;
                if (penetration > angleFactor * totalArmor
                    || penetration > RealisticCombat.settings.DamageModel.getArmorOvermatchFactor()
                                     * totalArmor) {
                    hitCitadel(potential, damageTracker);
                    causeCriticalMalfunction(target, point, damageTracker);
                } if (damageTracker.hull >= target.getHitpoints())
                    disableOrDestroy(target, hitCompartment, point);
                else {
                    target.setHitpoints(target.getHitpoints() - damageTracker.hull);
                    displayDamageText(target, source, point, damageTracker.compartment,
                                      damageTracker.hull);
                }
            } else {
                damageTracker.compartment = 0; damageTracker.hull = 0;
                if (Ricochet.isCaused(projectile, target)) Ricochet.occur(projectile, target);
            }
        }

        setDamage(projectile, getMinimumDamage(projectile));
    }

    private static void hitShip(final BeamAPI beam,
                                final ShipAPI target,
                                final ShipAPI source,
                                final Vector2f point,
                                final DamageTracker damageTracker)
    {
        damageTracker.emp = Damage.getEmp(beam); damageTracker.shield = 0;

        final ArmorGridAPI compartments = target.getArmorGrid();
        final int[] hitCompartment = compartments.getCellAtLocation(point);

        if (hitCompartment == null) {
            damageTracker.compartment = 0; damageTracker.hull = 0;
        } else {
            final PersonAPI sourceCaptain = source.getCaptain(),
                            targetCaptain = target.getCaptain();
            final float penetration = Penetration.getPenetration(beam),
                        thicknessFactor =
                                RealisticCombat.settings.DamageModel.getArmorThicknessFactor(
                                        getDamageType(beam)),
                        surfaceArmor = Armor.getSurfaceArmor(target.getArmorGrid())
                                       * thicknessFactor,
                        obliqueAngle = Collision.getObliqueAngle(beam, target),
                        skillFactor = getSkillFactor(sourceCaptain, targetCaptain),
                        effectiveAngle = Math.max(0, Math.min(90, obliqueAngle * skillFactor)),
                        angleFactor = Armor.getAngleFactor(effectiveAngle);

            if (penetration > angleFactor * surfaceArmor
                || penetration > RealisticCombat.settings.DamageModel.getArmorOvermatchFactor()
                                 * surfaceArmor) {
                final float potential = Damage.getPotential(beam),
                            compartmentPotential = getCompartmentDamageFactor() * potential;
                hitCompartments(target, hitCompartment, compartments, compartmentPotential,
                        damageTracker);
                if (damageTracker.hull >= target.getHitpoints())
                    disableOrDestroy(target, hitCompartment, point);
                else {
                    target.setHitpoints(target.getHitpoints() - damageTracker.hull);
                    displayDamageText(target, source, point, damageTracker.compartment,
                            damageTracker.hull);
                }
            } else {
                damageTracker.compartment = 0; damageTracker.hull = 0;
            }
        }

        setDamage(beam, getMinimumDamage(beam));
    }

    private static void reportDamage(final Object object,
                                     final ShipAPI target,
                                     final ShipAPI source,
                                     final DamageTracker damageTracker)
    {
        final DamageType damageType = getDamageType(object);
        DamageReportManagerV1.getDamageReportManager().addDamageReport(new DamageReportV1(
                damageTracker.compartment, damageTracker.hull, damageTracker.emp,
                damageTracker.shield, damageType, source, target, getWeaponName(object)));
        final FleetMemberAPI fleetMember = source.getFleetMember();
        if (fleetMember == null) return;
        final CombatDamageData data = Global.getCombatEngine().getDamageData();
        final CombatDamageData.DealtByFleetMember dealt = data.getDealtBy(source.getFleetMember());
        dealt.addHullDamage(target.getFleetMember(), damageTracker.hull);
    }

    private static class DamageTracker {
        private float shield = 0, emp = 0, compartment = 0, hull = 0;
        DamageTracker() {}
    }

    /**
     * Modify damage taken by a {@link ShipAPI} from a {@link BeamAPI},
     * {@link MissileAPI}, or {@link DamagingProjectileAPI}.
     * <p></p>
     * @param object {@link Object} hitting something and damaging it
     * @param combatEntity {@link CombatEntityAPI} receiving the damage
     */
    @Override
    public String modifyDamageTaken(final Object object,
                                    final CombatEntityAPI combatEntity,
                                    final DamageAPI damageAPI,
                                    final Vector2f point,
                                    final boolean shieldHit)
    {
        if (isProhibited(object, combatEntity, point)) return null;
        final ShipAPI source = getSource(object), target = (ShipAPI) combatEntity;
        final DamageTracker damageTracker = new DamageTracker();
        if (shieldHit) {
            if (object instanceof DamagingProjectileAPI)
                hitShield((DamagingProjectileAPI) object, target, damageTracker);
            else hitShield((BeamAPI) object, target, damageTracker);
        } else {
            if (object instanceof DamagingProjectileAPI)
                hitShip((DamagingProjectileAPI) object, target, source, point, damageTracker);
            else
                hitShip((BeamAPI) object, target, source, point, damageTracker);
        }
        reportDamage(object, target, source, damageTracker);
        return null;
    }
}

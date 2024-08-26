package RealisticCombat.scripts;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;

/**
 * Contains the {@code float} armor damage and {@code float} hull damage a
 * {@link DamagingProjectileAPI}, {@link MissileAPI}, or {@link BeamAPI} has inflicted.
 * <p></p>
 * Create a DamageReport after calculating the armor and hull damage and pass it to
 * DamageReportManagerV1.getDamageReportManager().addDamageReport() for DetailedCombatResults to record and consume.
 */

public final class DamageReportV1 {

    private final float armorDamage, hullDamage, empDamage, shieldDamage;

    private final DamageType damageType;

    private final CombatEntityAPI source, target;

    private final String weaponName;


    public DamageReportV1(final float armorDamage,
                          final float hullDamage,
                          final float empDamage,
                          final float shieldDamage,
                          final DamageType damageType,
                          final CombatEntityAPI source,
                          final CombatEntityAPI target,
                          final String weaponName)
    {
        this.armorDamage = armorDamage;
        this.hullDamage = hullDamage;
        this.empDamage = empDamage;
        this.shieldDamage = shieldDamage;
        this.damageType = damageType;
        this.source = source;
        this.target = target;
        this.weaponName = weaponName;
    }


    public static Object[] serialize(final DamageReportV1 report){
        return new Object[]{
                report.armorDamage,
                report.hullDamage,
                report.empDamage,
                report.shieldDamage,
                report.damageType,
                report.source,
                report.target,
                report.weaponName,
        };
    }

    public static DamageReportV1 deserialize(final Object[] raw){
        return new DamageReportV1(
                (float) raw[0],
                (float) raw[1],
                (float) raw[2],
                (float) raw[3],
                (DamageType) raw[4],
                (CombatEntityAPI) raw[5],
                (CombatEntityAPI) raw[6],
                (String) raw[7]
        );
    }

    /**
     * Returns the {@code float} armor damage a {@link DamagingProjectileAPI},
     * {@link MissileAPI}, or {@link BeamAPI} has inflicted.
     */
    public float getArmorDamage() { return armorDamage; }

    /**
     * Returns the {@code float} hull damage a {@link DamagingProjectileAPI},
     * {@link MissileAPI}, or {@link BeamAPI} has inflicted.
     */
    public float getHullDamage() { return hullDamage; }

    /**
     * Returns the {@code float} emp damage a {@link DamagingProjectileAPI},
     * {@link MissileAPI}, or {@link BeamAPI} has inflicted.
     */
    public float getEmpDamage() { return empDamage; }

    public float getShieldDamage() { return shieldDamage; }

    public DamageType getDamageType() { return damageType; }

    /**
     * Returns the {@link DamagingProjectileAPI}, {@link MissileAPI}, or {@link BeamAPI}
     * that inflicted the {@code float} armor damage and {@code float} hull damage of
     * this {@link DamageReportV1}.
     */
    public CombatEntityAPI getSource() { return source; }

    public CombatEntityAPI getTarget() { return target; }

    public String getWeaponName() { return weaponName; }
}
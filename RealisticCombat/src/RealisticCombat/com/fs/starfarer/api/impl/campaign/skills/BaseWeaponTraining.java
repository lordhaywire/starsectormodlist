package RealisticCombat.com.fs.starfarer.api.impl.campaign.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;

import java.util.HashSet;

public class BaseWeaponTraining {
    protected static HashSet<WeaponAPI.WeaponType> getMountableTypes(final ShipHullSpecAPI ship) {
        final HashSet<WeaponAPI.WeaponType> possible = new HashSet<>();
        for (final WeaponSlotAPI slot : ship.getAllWeaponSlotsCopy())
            possible.add(slot.getWeaponType());
        for (final String weaponId : ship.getBuiltInWeapons().values())
            possible.add(Global.getSettings().getWeaponSpec(weaponId).getType());
        return possible;
    }

    /**
     * @return {@link HashSet<String>} of skills of this {@link PersonAPI}
     */
    protected static HashSet<String> getSkillIds(final PersonAPI captain) {
        final HashSet<String> skills = new HashSet<>();
        if (captain == null) return skills;
        final MutableCharacterStatsAPI stats = captain.getStats();
        if (stats == null) return skills;
        for (final MutableCharacterStatsAPI.SkillLevelAPI skill : stats.getSkillsCopy())
            if (skill.getLevel() > 0) skills.add(skill.getSkill().getId());
        return skills;
    }
}

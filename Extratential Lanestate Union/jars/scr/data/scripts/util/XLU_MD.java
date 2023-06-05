package data.scripts.util;

import com.fs.starfarer.api.Global;

public class XLU_MD {
    public static String base(String key) {
        return Global.getSettings().getString("xlu_hullmod", key);
    }
    public static String wep_over(String key) {
        return Global.getSettings().getString("xlu_mod_wep_over", key);
    }
    public static String breezer(String key) {
        return Global.getSettings().getString("xlu_mod_breezer", key);
    }
    public static String crew(String key) {
        return Global.getSettings().getString("xlu_mod_crew", key);
    }
    public static String armorclad(String key) {
        return Global.getSettings().getString("xlu_mod_clad", key);
    }
    public static String lithoframe(String key) {
        return Global.getSettings().getString("xlu_mod_litho", key);
    }
    public static String bulkthrust(String key) {
        return Global.getSettings().getString("xlu_mod_bulkthrust", key);
    }
    public static String trusty(String key) {
        return Global.getSettings().getString("xlu_mod_trusty", key);
    }
}

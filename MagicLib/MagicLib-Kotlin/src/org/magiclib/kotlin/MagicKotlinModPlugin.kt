package org.magiclib.kotlin

import com.fs.starfarer.api.Global
import org.magiclib.kotlin.TestMagicFleetBuilder.testMagicFleetBuilder

object MagicKotlinModPlugin {

    fun onGameLoad(newGame: Boolean) {
        if (Global.getSettings().isDevMode
            && Global.getSector().playerPerson.nameString.equals("ML Test", ignoreCase = true)
        ) {
            testMagicFleetBuilder()
        }
    }
}
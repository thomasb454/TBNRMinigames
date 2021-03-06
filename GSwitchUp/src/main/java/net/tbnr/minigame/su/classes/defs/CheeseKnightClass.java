/*
 * Copyright (c) 2014.
 * CogzMC LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.minigame.su.classes.defs;

import net.tbnr.gearz.game.GearzGame;
import net.tbnr.gearz.game.classes.GearzClassMeta;
import net.tbnr.manager.TBNRPlayer;
import net.tbnr.minigame.su.classes.SwitchUpClass;

@GearzClassMeta(
        name = "Cheese Knight",
        key = "su_cheeseknight"
)
public final class CheeseKnightClass extends SwitchUpClass {
    public CheeseKnightClass(TBNRPlayer player, GearzGame game) {
        super(player, game);
    }

    @Override
    protected String getFilename() {
        return "cheeseknight.json";
    }
}

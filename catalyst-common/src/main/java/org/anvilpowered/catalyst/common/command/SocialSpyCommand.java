/*
 *     Copyright (C) 2020 STG_Allen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.catalyst.common.command;

import com.google.inject.Inject;
import com.mojang.brigadier.context.CommandContext;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.anvil.api.util.UserService;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;
import org.anvilpowered.catalyst.api.service.PrivateMessageService;

import java.util.UUID;

public class SocialSpyCommand<
    TString,
    TPlayer extends TCommandSource,
    TCommandSource> {

    @Inject
    private PluginMessages<TString> pluginMessages;

    @Inject
    private TextService<TString, TCommandSource> textService;

    @Inject
    private UserService<TPlayer, TPlayer> userService;

    @Inject
    private PrivateMessageService<TString> privateMessageService;

    public int execute(CommandContext<TCommandSource> context, Class<?> playerClass) {
        if (!playerClass.isAssignableFrom(context.getSource().getClass())) {
            textService.send(textService.of("Player only command!"), context.getSource());
            return 0;
        }
        UUID playerUUID = userService.getUUID((TPlayer) context.getSource());
        if (privateMessageService.socialSpySet().contains(playerUUID)) {
            privateMessageService.socialSpySet().remove(playerUUID);
            textService.send(pluginMessages.getSocialSpy(false), context.getSource());
        } else {
            privateMessageService.socialSpySet().add(playerUUID);
            textService.send(pluginMessages.getSocialSpy(true), context.getSource());
        }
        return 1;
    }
}

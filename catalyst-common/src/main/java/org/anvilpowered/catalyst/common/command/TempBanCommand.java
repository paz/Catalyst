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
import org.anvilpowered.anvil.api.registry.Registry;
import org.anvilpowered.anvil.api.util.PermissionService;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.anvil.api.util.UserService;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.member.MemberManager;
import org.anvilpowered.catalyst.api.plugin.PluginMessages;

public class TempBanCommand<
    TString,
    TPlayer extends TCommandSource,
    TCommandSource> {

    @Inject
    private PermissionService permissionService;

    @Inject
    private TextService<TString, TCommandSource> textService;

    @Inject
    private UserService<TPlayer, TPlayer> userService;

    @Inject
    private PluginMessages<TString> pluginMessages;

    @Inject
    private MemberManager<TString> memberManager;

    @Inject
    private Registry registry;

    public void execute(TCommandSource source, String[] args) {
        if (!permissionService.hasPermission(source,
            registry.getOrDefault(CatalystKeys.TEMP_BAN_PERMISSION))) {
            textService.send(pluginMessages.getNoPermission(), source);
            return;
        }

        if (args.length < 2) {
            textService.send(pluginMessages.getNotEnoughArgs(), source);
            textService.send(pluginMessages.tempBanCommandUsage(), source);
            return;
        }

        String userName = args[0];
        String duration = args[1];

        if (userService.get(userName).isPresent()) {
            if (permissionService.hasPermission(userService.get(userName),
                registry.getOrDefault(CatalystKeys.BAN_EXEMPT_PERMISSION))) {
                textService.send(pluginMessages.getBanExempt(), source);
                return;
            }
        }
        if (args.length == 2) {
            memberManager.tempBan(userName, duration).thenAcceptAsync(m -> textService.send(m, source));
        } else {
            String reason = String.join(" ", args)
                .replace(userName + " ", "").replace(duration, "");
            memberManager.tempBan(userName, duration, reason).thenAcceptAsync(m -> textService.send(m, source));
        }
    }

    public int withoutReason(CommandContext<TCommandSource> context) {
        execute(context.getSource(), new String[]{
            context.getArgument("target", String.class),
            context.getArgument("duration", String.class)
        });
        return 1;
    }

    public int withReason(CommandContext<TCommandSource> context) {
        execute(context.getSource(), new String[]{
            context.getArgument("target", String.class),
            context.getArgument("duration", String.class),
            context.getArgument("reason", String.class)
        });
        return 1;
    }
}

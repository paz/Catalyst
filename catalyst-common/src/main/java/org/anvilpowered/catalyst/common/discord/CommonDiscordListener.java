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

package org.anvilpowered.catalyst.common.discord;

import com.google.inject.Inject;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.anvilpowered.anvil.api.registry.Registry;
import org.anvilpowered.anvil.api.util.PermissionService;
import org.anvilpowered.anvil.api.util.TextService;
import org.anvilpowered.anvil.api.util.UserService;
import org.anvilpowered.catalyst.api.data.key.CatalystKeys;
import org.anvilpowered.catalyst.api.service.EmojiService;
import org.anvilpowered.catalyst.api.service.DiscordCommandService;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.stream.Collectors;

public class CommonDiscordListener<
    TUser,
    TString,
    TPlayer,
    TCommandSource>
    extends ListenerAdapter {

    @Inject
    private Registry registry;

    @Inject
    private UserService<TUser, TPlayer> userService;

    @Inject
    private DiscordCommandService discordCommandService;

    @Inject
    private TextService<TString, TCommandSource> textService;

    @Inject
    private PermissionService permissionService;

    @Inject
    private Logger logger;

    @Inject
    private EmojiService emojiService;

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())
            || event.isWebhookMessage()
            || event.getMember().isFake()) {
            return;
        }

        if (event.getChannel().getId().equals(registry.getOrDefault(CatalystKeys.DISCORD_MAIN_CHANNEL))) {
            String message = EmojiParser.parseToAliases(event.getMessage().getContentDisplay());
            String messageRaw = event.getMessage().toString();
            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)
                && messageRaw.contains("!cmd")) {
                String command = event.getMessage().getContentRaw().replace("!cmd ", "");
                discordCommandService.executeDiscordCommand(command);
                return;
            } else if (messageRaw.contains("!players")
                || messageRaw.contains("!online")
                || messageRaw.contains("!list")) {
                final Collection<TPlayer> onlinePlayers = userService.getOnlinePlayers();
                final String playerNames;
                if (onlinePlayers.size() == 0) {
                    playerNames = "```There are currently no players online!```";
                } else {
                    playerNames = "**Online Players:**```"
                        + userService.getOnlinePlayers().stream()
                        .map(p -> userService.getUserName((TUser) p))
                        .collect(Collectors.joining(", ")) + "```";
                }
                event.getChannel().sendMessage(playerNames).queue();
                return;
            } else {
                if (registry.getOrDefault(CatalystKeys.EMOJI_ENABLE)) {
                    for (String key : registry.getOrDefault(CatalystKeys.EMOJI_MAP).keySet()) {
                        message = message.replace(key, emojiService.getEmojis().get(key).toString());
                    }
                }
                String finalMessage = registry.getOrDefault(CatalystKeys.DISCORD_CHAT_FORMAT)
                    .replace("%name%", event.getMember().getEffectiveName())
                    .replace("%message%", message);
                userService.getOnlinePlayers().forEach(p ->
                    textService.builder()
                        .append(textService.deserialize(finalMessage))
                        .onClickOpenUrl(registry.getOrDefault(CatalystKeys.DISCORD_URL))
                        .onHoverShowText(textService.of(registry.getOrDefault(CatalystKeys.DISCORD_HOVER_MESSAGE)))
                        .sendTo((TCommandSource) p)
                );
            }
            logger.info("[Discord] " + event.getMember().getEffectiveName() + " : " + EmojiParser.parseToAliases(event.getMessage().getContentDisplay()));
        }

        if (event.getChannel().getId().equals(registry.getOrDefault(CatalystKeys.DISCORD_STAFF_CHANNEL))) {
            String message = EmojiParser.parseToAliases(event.getMessage().getContentDisplay());
            if (registry.getOrDefault(CatalystKeys.EMOJI_ENABLE)) {
                for (String key : emojiService.getEmojis().keySet()) {
                    message = message.replace(key, emojiService.getEmojis().get(key).toString());
                }
            }
            String finalMessage = registry.getOrDefault(CatalystKeys.DISCORD_STAFF_FORMAT)
                .replace("%name%", event.getMember().getEffectiveName())
                .replace("%message%", message);
            userService.getOnlinePlayers().forEach(p -> {
                if (permissionService.hasPermission(p, registry.getOrDefault(CatalystKeys.STAFFCHAT_PERMISSION))) {
                    textService.builder()
                        .append(textService.deserialize(finalMessage))
                        .onClickOpenUrl(registry.getOrDefault(CatalystKeys.DISCORD_URL))
                        .onHoverShowText(textService.of(registry.getOrDefault(CatalystKeys.DISCORD_HOVER_MESSAGE)))
                        .sendTo((TCommandSource) p);
                }
            });
            logger.info("[Discord][STAFF] " + event.getMember().getEffectiveName() + " : " + EmojiParser.parseToAliases(event.getMessage().getContentDisplay()));
        }

    }
}

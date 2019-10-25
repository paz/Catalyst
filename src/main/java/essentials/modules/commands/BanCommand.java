package essentials.modules.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.UuidUtils;
import essentials.MSEssentials;
import essentials.modules.Config.PlayerConfig;
import essentials.modules.PluginMessages;
import essentials.modules.PluginPermissions;
import net.kyori.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BanCommand implements Command {

    @Override
    public void execute(CommandSource source, @NonNull String[] args)
    {
        String reason;

        if (args[1] != null) {
            reason = args[1];
        } else {
            reason = "The ban hammer has spoken!";
        }
        if (args.length == 0) {
            source.sendMessage(PluginMessages.notEnoughArgs);
            return;
        }

        if(MSEssentials.getServer().getPlayer(args[0]).isPresent()) {
            Optional<Player> target = MSEssentials.getServer().getPlayer(args[0]);
            UUID targetID = target.get().getUniqueId();

            if (args[0].equalsIgnoreCase(target.get().getUsername())) {
                if (source instanceof Player) {
                    Player src = (Player) source;
                    if (src.hasPermission(PluginPermissions.BAN)) {
                        PlayerConfig.addBan(target.get().getUsername(), reason);
                        src.sendMessage(TextComponent.of("Successfully banned " + target.get().getUsername()));
                        target.get().disconnect(TextComponent.of(PlayerConfig.getBanReason(target.get().getUsername())));
                    }

                }
                source.sendMessage(TextComponent.of("Successfully banned " + target.get().getUsername()));
                PlayerConfig.addBan(target.get().getUsername(), reason);
                target.get().disconnect(TextComponent.of(PlayerConfig.getBanReason(target.get().getUsername())));
                return;
            }
            return;
        }
        UUID offlineUUID = UuidUtils.generateOfflinePlayerUuid(args[0]);
        source.sendMessage(TextComponent.of("Successfully banned " + args[0]));
        source.sendMessage(TextComponent.of(offlineUUID.toString()));
        PlayerConfig.addBan(args[0], reason);
        return;

    }

    @Override
    public List<String> suggest(CommandSource src, String[] args)
    {
        if(args.length ==1)
        {
            return MSEssentials.getServer().matchPlayer(args[0]).stream().map(Player::getUsername).collect(Collectors.toList());
        }
        return Arrays.asList();
    }
}

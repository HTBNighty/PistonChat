package net.pistonmaster.pistonchat.events;

import net.pistonmaster.pistonchat.api.PistonChatEvent;
import net.pistonmaster.pistonchat.utils.CommonTool;
import net.pistonmaster.pistonchat.utils.ConfigTool;
import net.pistonmaster.pistonchat.utils.IgnoreTool;
import net.pistonmaster.pistonchat.utils.TempDataTool;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatEvent implements Listener {
    // Mute plugins should have a lower priority to work!
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatter = event.getPlayer();
        PistonChatEvent pistonChatEvent = new PistonChatEvent(chatter, event.getMessage());

        event.getRecipients().clear();

        Bukkit.getPluginManager().callEvent(pistonChatEvent);

        event.setCancelled(pistonChatEvent.isCancelled());

        if (!pistonChatEvent.isCancelled()) {
            String message = pistonChatEvent.getMessage();

            for (Player receiver : Bukkit.getOnlinePlayers()) {
                if (!IgnoreTool.isIgnored(chatter, receiver) && TempDataTool.isChatEnabled(receiver)) {
                    ComponentBuilder builder = new ComponentBuilder(CommonTool.getFormat(chatter));

                    if (receiver.hasPermission("pistonchat.playernamereply")) {
                        builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/w " + ChatColor.stripColor(chatter.getDisplayName()) + " "));

                        String hoverText = ConfigTool.getPluginConfig().getString("hovertext");

                        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder(
                                        ChatColor.translateAlternateColorCodes('&',
                                                hoverText.replace("%player%",
                                                        ChatColor.stripColor(chatter.getDisplayName())
                                                )
                                        )
                                ).create()
                        ));
                    }

                    builder.append(" ").reset();

                    builder.append(new TextComponent(TextComponent.fromLegacyText(message)));

                    builder.color(CommonTool.getChatColorFor(message, chatter));

                    receiver.spigot().sendMessage(builder.create());
                }
            }
        }
    }
}
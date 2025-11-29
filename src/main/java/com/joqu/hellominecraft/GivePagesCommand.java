package com.joqu.hellominecraft;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;

public class GivePagesCommand implements CommandExecutor {

    private final HelloMinecraft plugin;
    private final PagedChest pagedChest;

    public GivePagesCommand(HelloMinecraft plugin, PagedChest pagedChest) {
        this.plugin = plugin;
        this.pagedChest = pagedChest;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hellominecraft.givepages") && !sender.isOp()) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("Usage: /givepages <player> <count>");
            return true;
        }

        String targetName = args[0];
        int count;
        try {
            count = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Page count must be a number.");
            return true;
        }

        int clampedCount = Math.max(1, Math.min(count, 20));
        if (clampedCount != count) {
            sender.sendMessage("Page count adjusted to within 1-20 range.");
        }
        count = clampedCount;

        Player online = Bukkit.getPlayerExact(targetName);
        UUID uuid;
        if (online != null) {
            uuid = online.getUniqueId();
        } else {
            OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
            uuid = op.getUniqueId();
        }

        PlayerPages pp = PlayerPages.load(plugin.getDataFolder(), uuid);
        pp.setPages(count);
        try {
            pp.save(plugin.getDataFolder());
        } catch (IOException e) {
            sender.sendMessage("Failed to save player data: " + e.getMessage());
            return true;
        }

        sender.sendMessage("Set pages for " + targetName + " to " + count + ".");
        if (online != null) {
            online.sendMessage("Your personal chest page limit has been set to " + count + ".");
        }

        return true;
    }
}

package com.joqu.hellominecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChestCommand implements CommandExecutor {

    private final PagedChest pagedChest;

    public ChestCommand(PagedChest pagedChest) {
        this.pagedChest = pagedChest;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open their personal chest.");
            return true;
        }
        Player player = (Player) sender;
        pagedChest.openChest(player);
        return true;
    }
}

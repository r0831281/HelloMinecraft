package com.joqu.hellominecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements CommandExecutor {

    // This method is called when a player or console executes the command
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        sender.sendMessage("Pong!!!!!!!!!");

        // Return true to indicate the command was handled successfully
        return true;
    }
}

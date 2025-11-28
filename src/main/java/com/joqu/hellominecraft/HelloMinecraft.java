package com.joqu.hellominecraft;

import org.bukkit.plugin.java.JavaPlugin;

public class HelloMinecraft extends JavaPlugin {

    private PagedChest pagedChest;

    @Override
    public void onEnable() {
        // This code runs when the server starts the plugin
        getLogger().info("---------------------------------------");
        getLogger().info("   Hello World! The plugin is alive!   ");
        getLogger().info("---------------------------------------");

        // --- NEW: Register the /ping command ---
        // We link the command defined in plugin.yml ("ping") to our new handler class (PingCommand)
        this.getCommand("ping").setExecutor(new PingCommand());

        // Setup paged chest feature
        this.pagedChest = new PagedChest(this);
        getServer().getPluginManager().registerEvents(pagedChest, this);

        // Register commands for chest and admin page management
        if (this.getCommand("chest") != null) this.getCommand("chest").setExecutor(new ChestCommand(pagedChest));
        if (this.getCommand("givepages") != null) this.getCommand("givepages").setExecutor(new GivePagesCommand(this, pagedChest));
    }

    @Override
    public void onDisable() {
        // This code runs when the server stops
        getLogger().info("HelloWorld plugin is shutting down!");
    }
}

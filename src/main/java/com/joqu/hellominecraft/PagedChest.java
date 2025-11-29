package com.joqu.hellominecraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player paged chest UIs. This implementation stores only the page contents in memory
 * for the current server run and persists page count using PlayerPages files.
 */
public class PagedChest implements Listener {

    private final HelloMinecraft plugin;

    private final Map<UUID, Integer> openPage = new HashMap<>();

    public static final String TITLE_PREFIX = "Personal Chest - ";

    public PagedChest(HelloMinecraft plugin) {
        this.plugin = plugin;
    }

    public void openChest(Player player) {
        PlayerPages pp = PlayerPages.load(plugin.getDataFolder(), player.getUniqueId());
        int pages = pp.getPages();
        int page = 1;

        // Save current page if open
        saveOpenInventory(player);

        Inventory inv = createInventoryFor(player.getUniqueId(), page, pages);
        openPage.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }

    private Inventory createInventoryFor(UUID uuid, int page, int pages) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE_PREFIX + "Page " + page + "/" + pages));

        // Fill with previously saved content if present
        PlayerPages pp = PlayerPages.load(plugin.getDataFolder(), uuid);
        ItemStack[] items = pp.getPageContents(page);
        if (items != null) inv.setContents(items);

        // Add navigation items in the bottom row
        inv.setItem(45, createNavItem(Material.ARROW, "Previous"));
        inv.setItem(49, createNavItem(Material.BARRIER, "Close"));
        inv.setItem(53, createNavItem(Material.ARROW, "Next"));

        return inv;
    }
    }

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            it.setItemMeta(meta);
        }
        return it;
    }

    private ItemStack[] getPageContents(UUID uuid, int page) {
        PlayerPages pp = PlayerPages.load(plugin.getDataFolder(), uuid);
        return pp.getPageContents(page);
    }

    private void savePageContents(UUID uuid, int page, ItemStack[] items) {
        PlayerPages pp = PlayerPages.load(plugin.getDataFolder(), uuid);
        pp.setPageContents(page, items);
        try {
            pp.save(plugin.getDataFolder());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save player pages for " + uuid + ": " + e.getMessage());
        }
    }

    private void saveOpenInventory(Player player) {
        if (player.getOpenInventory() == null) return;
        var view = player.getOpenInventory();
        String title = view.getTitle();
        if (title == null || !title.startsWith(TITLE_PREFIX)) return;
        var inv = view.getTopInventory();
        // parse page from title
        try {
            String after = title.substring(TITLE_PREFIX.length()); // "Page X/Y"
            String[] parts = after.replace("Page ", "").split("/");
            int page = Integer.parseInt(parts[0]);
            savePageContents(player.getUniqueId(), page, inv.getContents());
        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (title == null || !title.startsWith(TITLE_PREFIX)) return;
        Player p = (Player) event.getPlayer();
        // save contents
        saveOpenInventory(p);
        openPage.remove(p.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title == null || !title.startsWith(TITLE_PREFIX)) return;

        int slot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        var top = event.getView().getTopInventory();

        // parse current page and total
        int current = 1;
        int total = 1;
        try {
            String after = title.substring(TITLE_PREFIX.length()); // "Page X/Y"
            String[] parts = after.replace("Page ", "").split("/");
            current = Integer.parseInt(parts[0]);
            total = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {
        }

        if (slot == 45) { // Previous
            event.setCancelled(true);
            if (current > 1) {
                // save current
                savePageContents(uuid, current, top.getContents());
                int nextPage = current - 1;
                Inventory nextInv = createInventoryFor(uuid, nextPage, total);
                openPage.put(uuid, nextPage);
                player.openInventory(nextInv);
            }
        } else if (slot == 53) { // Next
            event.setCancelled(true);
            if (current < total) {
                savePageContents(uuid, current, top.getContents());
                int nextPage = current + 1;
                Inventory nextInv = createInventoryFor(uuid, nextPage, total);
                openPage.put(uuid, nextPage);
                player.openInventory(nextInv);
            }
        } else if (slot == 49) { // Close
            event.setCancelled(true);
            player.closeInventory();
        } else {
            // allow normal interactions for other slots (do not cancel)
        }
    }
}

package com.joqu.hellominecraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Per-player data holder. One file per player is stored under <plugin data folder>/players/<uuid>.yml
 * Stores page count and optional per-page ItemStack contents.
 */
public class PlayerPages {

    private final UUID uuid;
    private int pages = 2; // default

    // optional persisted contents per page (1-based page numbers)
    private final Map<Integer, ItemStack[]> contents = new HashMap<>();

    public PlayerPages(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        if (pages < 1) pages = 1;
        this.pages = pages;
    }

    public ItemStack[] getPageContents(int page) {
        return contents.get(page);
    }

    public void setPageContents(int page, ItemStack[] items) {
        if (page < 1) return;
        if (items == null) {
            contents.remove(page);
        } else {
            contents.put(page, items);
        }
    }

    public File getFile(File dataFolder) {
        File dir = new File(dataFolder, "players");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, uuid.toString() + ".yml");
    }

    public void save(File dataFolder) throws IOException {
        File f = getFile(dataFolder);
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("uuid", uuid.toString());
        cfg.set("pages", pages);

        // Persist contents per page (if any)
        for (Map.Entry<Integer, ItemStack[]> e : contents.entrySet()) {
            int page = e.getKey();
            ItemStack[] items = e.getValue();
            if (items != null) {
                // convert to list for yaml
                List<ItemStack> list = Arrays.stream(items).collect(Collectors.toList());
                cfg.set("pagesData." + page + ".contents", list);
            }
        }

        cfg.save(f);
    }

    @SuppressWarnings("unchecked")
    public static PlayerPages load(File dataFolder, UUID uuid) {
        PlayerPages pp = new PlayerPages(uuid);
        File f = pp.getFile(dataFolder);
        if (!f.exists()) return pp; // defaults
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        int p = cfg.getInt("pages", pp.pages);
        pp.setPages(p);

        // load pagesData
        if (cfg.isConfigurationSection("pagesData")) {
            var section = cfg.getConfigurationSection("pagesData");
            for (String key : section.getKeys(false)) {
                try {
                    int page = Integer.parseInt(key);
                    List<?> list = cfg.getList("pagesData." + key + ".contents", Collections.emptyList());
                    if (!list.isEmpty()) {
                        // attempt to cast to ItemStack objects
                        ItemStack[] arr = list.stream().filter(Objects::nonNull).map(o -> (ItemStack) o).toArray(ItemStack[]::new);
                        pp.setPageContents(page, arr);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return pp;
    }
}

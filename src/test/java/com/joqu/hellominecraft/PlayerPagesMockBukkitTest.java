package com.joqu.hellominecraft;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerPagesMockBukkitTest {

    private HelloMinecraft plugin;
    private org.mockbukkit.mockbukkit.ServerMock server;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(HelloMinecraft.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void saveAndLoadItemStacksRoundTrip() throws Exception {
        org.bukkit.entity.Player player = server.addPlayer();
        UUID id = player.getUniqueId();

        PlayerPages pp = new PlayerPages(id);
        ItemStack[] items = new ItemStack[54];
        items[0] = new ItemStack(Material.DIAMOND, 3);
        items[10] = new ItemStack(Material.OAK_PLANKS, 16);

        pp.setPageContents(1, items);
        pp.save(plugin.getDataFolder());

        PlayerPages loaded = PlayerPages.load(plugin.getDataFolder(), id);
        ItemStack[] loadedItems = loaded.getPageContents(1);

        assertNotNull(loadedItems, "Loaded items should not be null");
        assertEquals(54, loadedItems.length, "Loaded inventory length should match double chest size");
        assertNotNull(loadedItems[0], "Slot 0 should contain the diamond stack");
        assertEquals(Material.DIAMOND, loadedItems[0].getType());
        assertEquals(3, loadedItems[0].getAmount());
        assertNotNull(loadedItems[10], "Slot 10 should contain the planks stack");
        assertEquals(Material.OAK_PLANKS, loadedItems[10].getType());
        assertEquals(16, loadedItems[10].getAmount());
    }
}

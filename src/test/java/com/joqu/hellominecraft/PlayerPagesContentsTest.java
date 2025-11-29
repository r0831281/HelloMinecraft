package com.joqu.hellominecraft;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerPagesContentsTest {

    @Test
    public void saveAndLoadContents() throws Exception {
        Path tmp = Files.createTempDirectory("pmtest-contents");
        var dataFolder = tmp.toFile();

        UUID id = UUID.randomUUID();
        PlayerPages pp = new PlayerPages(id);
        ItemStack[] pageItems = new ItemStack[54];
        pageItems[0] = new ItemStack(Material.DIAMOND, 3);
        pageItems[1] = new ItemStack(Material.OAK_PLANKS, 10);
        pp.setPageContents(1, pageItems);
        pp.save(dataFolder);

        PlayerPages reloaded = PlayerPages.load(dataFolder, id);
        ItemStack[] loaded = reloaded.getPageContents(1);
        assertNotNull(loaded, "Loaded page contents should not be null");
        assertEquals(54, loaded.length, "Loaded array should have same length");
        assertNotNull(loaded[0]);
        assertEquals(Material.DIAMOND, loaded[0].getType());
        assertEquals(3, loaded[0].getAmount());
        assertNotNull(loaded[1]);
        assertEquals(Material.OAK_PLANKS, loaded[1].getType());
        assertEquals(10, loaded[1].getAmount());
    }

    @Test
    public void clampPagesMinimum() throws Exception {
        UUID id = UUID.randomUUID();
        PlayerPages pp = new PlayerPages(id);
        pp.setPages(0); // should clamp to 1
        assertEquals(1, pp.getPages(), "Pages should be clamped to minimum of 1");

        pp.setPages(-5);
        assertEquals(1, pp.getPages(), "Negative pages should be clamped to 1");
    }
}

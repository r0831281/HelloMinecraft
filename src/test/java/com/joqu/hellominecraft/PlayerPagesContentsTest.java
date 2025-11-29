package com.joqu.hellominecraft;

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
        // Creating real ItemStack instances requires a live Paper/Bukkit registry which
        // isn't available in unit tests. Instead store an array of nulls to ensure
        // the save/load path handles empty/placeholder contents without throwing.
        ItemStack[] pageItems = new ItemStack[54];
        // leave entries null to avoid using Material/ItemStack constructors
        pp.setPageContents(1, pageItems);
        pp.save(dataFolder);

        PlayerPages reloaded = PlayerPages.load(dataFolder, id);
        ItemStack[] loaded = reloaded.getPageContents(1);
        // We expect the loaded array to exist but contain no non-null ItemStacks because
        // the original array contained only nulls; the YAML loader filters nulls.
        assertNotNull(loaded, "Loaded page contents should not be null");
        assertEquals(54, loaded.length, "Loaded array should preserve inventory size even if all entries are null");
        for (ItemStack stack : loaded) {
            assertNull(stack, "All stacks should remain null when nothing was stored");
        }
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

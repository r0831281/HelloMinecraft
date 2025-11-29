package com.joqu.hellominecraft;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PagedChestIntegrationTest {

    private ServerMock server;
    private HelloMinecraft plugin;
    private PagedChest pagedChest;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(HelloMinecraft.class);
        pagedChest = extractPagedChest(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void navigatingBetweenPagesPersistsItems() throws Exception {
        PlayerMock player = server.addPlayer();
        UUID id = player.getUniqueId();
        Path playerFile = plugin.getDataFolder().toPath().resolve("players").resolve(id + ".yml");
        Files.createDirectories(playerFile.getParent());
        Files.deleteIfExists(playerFile);

        try {
            PlayerPages pages = PlayerPages.load(plugin.getDataFolder(), id);
            pages.setPages(3);
            pages.save(plugin.getDataFolder());

            // Open the chest and populate page 1
            pagedChest.openChest(player);
            Inventory pageOne = player.getOpenInventory().getTopInventory();
            pageOne.setItem(0, new ItemStack(Material.DIAMOND, 5));
            pageOne.setItem(1, new ItemStack(Material.EMERALD, 2));

            // Move to page 2 via the Next navigation slot
            clickSlot(player, 53);
            Inventory pageTwo = player.getOpenInventory().getTopInventory();
            pageTwo.setItem(10, new ItemStack(Material.GOLD_INGOT, 7));

            // Close the chest to trigger persistence
            InventoryView closingView = player.getOpenInventory();
            pagedChest.onInventoryClose(new InventoryCloseEvent(closingView));

            // Reopen and verify page 1 contents restored
            pagedChest.openChest(player);
            Inventory reopenedPageOne = player.getOpenInventory().getTopInventory();
            ItemStack firstSlot = reopenedPageOne.getItem(0);
            assertNotNull(firstSlot, "Diamond stack should persist on page one");
            assertEquals(5, firstSlot.getAmount());
            ItemStack secondSlot = reopenedPageOne.getItem(1);
            assertNotNull(secondSlot, "Emerald stack should persist on page one");
            assertEquals(Material.EMERALD, secondSlot.getType());

            // Navigate back to page 2 and ensure the item was persisted
            clickSlot(player, 53);
            Inventory reopenedPageTwo = player.getOpenInventory().getTopInventory();
            ItemStack persisted = reopenedPageTwo.getItem(10);
            assertNotNull(persisted, "Gold ingot stack should persist on page two");
            assertEquals(Material.GOLD_INGOT, persisted.getType());
            assertEquals(7, persisted.getAmount());

            PlayerPages reloaded = PlayerPages.load(plugin.getDataFolder(), id);
            assertEquals(3, reloaded.getPages(), "Page count should remain unchanged");
            assertNotNull(reloaded.getPageContents(1), "Page one data should be saved to disk");
            assertNotNull(reloaded.getPageContents(2), "Page two data should be saved to disk");
        } finally {
            Files.deleteIfExists(playerFile);
        }
    }

    @Test
    void previousPageNavigationSavesOutgoingPage() throws Exception {
        PlayerMock player = server.addPlayer();
        UUID id = player.getUniqueId();
        Path playerFile = plugin.getDataFolder().toPath().resolve("players").resolve(id + ".yml");
        Files.createDirectories(playerFile.getParent());
        Files.deleteIfExists(playerFile);

        try {
            PlayerPages pages = PlayerPages.load(plugin.getDataFolder(), id);
            pages.setPages(2);
            pages.save(plugin.getDataFolder());

            pagedChest.openChest(player); // page 1
            clickSlot(player, 53); // go to page 2
            Inventory pageTwo = player.getOpenInventory().getTopInventory();
            pageTwo.setItem(5, new ItemStack(Material.IRON_INGOT, 9));

            clickSlot(player, 45); // go back to page 1 (should save page 2)
            Inventory pageOne = player.getOpenInventory().getTopInventory();
            pageOne.setItem(2, new ItemStack(Material.COPPER_INGOT, 4));

            pagedChest.onInventoryClose(new InventoryCloseEvent(player.getOpenInventory()));

            pagedChest.openChest(player);
            clickSlot(player, 53); // forward to page 2 again
            Inventory reopenedPageTwo = player.getOpenInventory().getTopInventory();
            ItemStack persisted = reopenedPageTwo.getItem(5);
            assertNotNull(persisted, "Previous-page navigation should persist outgoing page contents");
            assertEquals(Material.IRON_INGOT, persisted.getType());
            assertEquals(9, persisted.getAmount());

            PlayerPages reloaded = PlayerPages.load(plugin.getDataFolder(), id);
            ItemStack[] savedPageTwo = reloaded.getPageContents(2);
            assertNotNull(savedPageTwo);
            assertEquals(Material.IRON_INGOT, savedPageTwo[5].getType());
        } finally {
            Files.deleteIfExists(playerFile);
        }
    }

    private void clickSlot(PlayerMock player, int slot) {
        InventoryClickEvent event = new InventoryClickEvent(
                player.getOpenInventory(),
                InventoryType.SlotType.CONTAINER,
                slot,
                ClickType.LEFT,
                InventoryAction.PICKUP_ALL
        );
        pagedChest.onInventoryClick(event);
    }

    private PagedChest extractPagedChest(HelloMinecraft plugin) {
        try {
            Field field = HelloMinecraft.class.getDeclaredField("pagedChest");
            field.setAccessible(true);
            return (PagedChest) field.get(plugin);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to access PagedChest instance", e);
        }
    }
}

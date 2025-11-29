package com.joqu.hellominecraft;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GivePagesCommandTest {

    private ServerMock server;
    private HelloMinecraft plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(HelloMinecraft.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void commandClampsMaximumPages() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.setOp(true);
        PlayerMock target = server.addPlayer("Target");

        admin.performCommand("givepages Target 99");

        PlayerPages reloaded = PlayerPages.load(plugin.getDataFolder(), target.getUniqueId());
        assertEquals(20, reloaded.getPages(), "givepages should clamp maximum to 20");
    }

    @Test
    void commandClampsMinimumPages() {
        PlayerMock admin = server.addPlayer("Admin");
        admin.setOp(true);
        Player target = server.addPlayer("Target");

        admin.performCommand("givepages Target 0");

        PlayerPages reloaded = PlayerPages.load(plugin.getDataFolder(), target.getUniqueId());
        assertEquals(1, reloaded.getPages(), "givepages should clamp minimum to 1");
    }
}

package com.joqu.hellominecraft;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerPagesTest {

    @Test
    public void saveAndLoadPageCount() throws Exception {
        Path tmp = Files.createTempDirectory("pmtest");
        var dataFolder = tmp.toFile();

        UUID id = UUID.randomUUID();
        PlayerPages pp = new PlayerPages(id);
        pp.setPages(5);
        pp.save(dataFolder);

        PlayerPages loaded = PlayerPages.load(dataFolder, id);
        assertEquals(5, loaded.getPages(), "Loaded page count should match saved value");
    }
}

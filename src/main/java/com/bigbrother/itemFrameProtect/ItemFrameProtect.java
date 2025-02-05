package com.bigbrother.itemFrameProtect;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemFrameProtect extends JavaPlugin {
    @Getter
    private ItemFrameCommand itemFrameCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.itemFrameCommand = new ItemFrameCommand(this);
        Bukkit.getPluginManager().registerEvents(new ItemFrameListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

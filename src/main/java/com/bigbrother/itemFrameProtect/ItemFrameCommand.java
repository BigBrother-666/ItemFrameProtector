package com.bigbrother.itemFrameProtect;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemFrameCommand implements CommandExecutor {
    @Getter
    private final Set<UUID> markingPlayers = new HashSet<>();  // 存储处于标记模式的玩家

    public ItemFrameCommand(ItemFrameProtect plugin) {
        PluginCommand command = plugin.getCommand("itemframe");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(new ItemFrameCommandTabCompleter());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("只有玩家可以使用此指令！", NamedTextColor.RED));
            return true;
        }

        if (!commandSender.hasPermission("itemframe.protect.mark")) {
            player.sendMessage(Component.text("没有权限执行此指令！", NamedTextColor.RED));
            return false;
        }

        if (args.length < 1) {
            commandSender.sendMessage(Component.text("指令格式错误！", NamedTextColor.RED));
            commandSender.sendMessage(Component.text("使用 /itemframe mark 或 /if mark 进入标记模式", NamedTextColor.RED));
            return false;
        }

        if (args[0].equalsIgnoreCase("mark")) {
            if (markingPlayers.contains(player.getUniqueId())) {
                markingPlayers.remove(player.getUniqueId());
                player.sendMessage(Component.text("已退出标记模式", NamedTextColor.GREEN));
            } else {
                markingPlayers.add(player.getUniqueId());
                player.sendMessage(Component.text("已进入标记模式，左键点击展示框以设置保护，右键点击展示框取消保护，展示框中需要有物品。", NamedTextColor.GREEN));
            }
            return true;
        } else {
            commandSender.sendMessage(Component.text("指令格式错误！", NamedTextColor.RED));
            return false;
        }
    }
}

class ItemFrameCommandTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            if (args.length == 1) {
                List<String> completerList = new ArrayList<>();
                if (commandSender.hasPermission("itemframe.protect.mark")) {
                    completerList.add("mark");
                }
                return completerList;
            }
        }
        return List.of();
    }
}

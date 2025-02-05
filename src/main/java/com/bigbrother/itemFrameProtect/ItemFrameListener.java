package com.bigbrother.itemFrameProtect;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.UUID;

public class ItemFrameListener implements Listener {
    private final NamespacedKey keyUuid;
    private final NamespacedKey keyName;
    private final ItemFrameProtect plugin;

    public ItemFrameListener(ItemFrameProtect plugin) {
        this.plugin = plugin;
        this.keyUuid = new NamespacedKey(plugin, "owner_uuid");
        this.keyName = new NamespacedKey(plugin, "owner_name");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame itemFrame) {
            Optional<UUID> ownerUuid = getItemFrameOwnerUuid(itemFrame);
            Player player = event.getPlayer();

            if (plugin.getItemFrameCommand().getMarkingPlayers().contains(player.getUniqueId())) {
                event.setCancelled(true);
                // 处于标记模式 移除保护
                if (ownerUuid.isEmpty()) {
                    player.sendMessage(Component.text("该展示框没有设置保护！", NamedTextColor.YELLOW));
                } else if (player.getUniqueId().toString().equals(ownerUuid.get().toString()) || player.hasPermission("itemframe.protect.bypass")) {
                    itemFrame.setFixed(false);
                    Location location = itemFrame.getLocation();
                    if (player.hasPermission("itemframe.protect.bypass")) {
                        Optional<String> itemFrameOwnerName = getItemFrameOwnerName(itemFrame);
                        itemFrameOwnerName.ifPresent(s -> player.sendMessage(Component.text("已移除位于 %d,%d,%d 的由 %s 创建的展示框保护！".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ(), s), NamedTextColor.AQUA)));
                    } else {
                        player.sendMessage(Component.text("已移除位于 %d,%d,%d 的展示框保护！".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ()), NamedTextColor.AQUA));
                    }
                    itemFrame.getPersistentDataContainer().remove(keyUuid);
                    itemFrame.getPersistentDataContainer().remove(keyName);
                } else {
                    Optional<String> itemFrameOwnerName = getItemFrameOwnerName(itemFrame);
                    if (itemFrameOwnerName.isPresent()) {
                        player.sendMessage(Component.text("该展示框由 %s 设置保护，不能移除！".formatted(itemFrameOwnerName.get()), NamedTextColor.YELLOW));
                    } else {
                        player.sendMessage(Component.text("该展示框由其他玩家设置保护，不能移除！", NamedTextColor.YELLOW));
                    }
                }
                return;
            }

            if (ownerUuid.isPresent()) {
                event.setCancelled(true);
                if (itemFrame.getItem().hasItemMeta()) {
                    openBook(itemFrame, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && getItemFrameOwnerUuid(frame).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && getItemFrameOwnerUuid(frame).isPresent()) {
            event.setCancelled(true);
        }

        if (event.getDamager() instanceof Player player && plugin.getItemFrameCommand().getMarkingPlayers().contains(player.getUniqueId()) && player.getTargetEntity(5) instanceof ItemFrame itemFrame) {
            event.setCancelled(true);
            // 左键物品展示框 添加保护
            Optional<UUID> ownerUuid = getItemFrameOwnerUuid(itemFrame);
            if (ownerUuid.isEmpty()) {
                itemFrame.setFixed(true);
                Location location = itemFrame.getLocation();
                itemFrame.getPersistentDataContainer().set(keyUuid, PersistentDataType.STRING, player.getUniqueId().toString());
                itemFrame.getPersistentDataContainer().set(keyName, PersistentDataType.STRING, player.getName());
                player.sendMessage(Component.text("已设置位于 %d,%d,%d 的展示框保护！".formatted(location.getBlockX(), location.getBlockY(), location.getBlockZ()), NamedTextColor.GREEN));
            } else if (player.getUniqueId().toString().equals(ownerUuid.get().toString())) {
                player.sendMessage(Component.text("该展示框已设置保护！", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("该展示框已被其他玩家设置保护！", NamedTextColor.YELLOW));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && getItemFrameOwnerUuid(frame).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && getItemFrameOwnerUuid(frame).isPresent()) {
            event.setCancelled(true);
        }
    }

    private void openBook(ItemFrame frame, Player player) {
        if (frame.getItem().getItemMeta() instanceof BookMeta meta) {
            Component author = meta.author();
            Component title = meta.title();
            if (author == null) {
                author = Component.text("UnKnown", NamedTextColor.BLACK);
            }
            if (title == null) {
                title = Component.text("UnKnown", NamedTextColor.BLACK);
            }
            player.openBook(Book.book(title, author, meta.pages()));
        }
    }

    private Optional<UUID> getItemFrameOwnerUuid(ItemFrame frame) {
        String ownerUuid = frame.getPersistentDataContainer().get(keyUuid, PersistentDataType.STRING);
        if (ownerUuid == null) {
            return Optional.empty();
        }
        UUID uuid = UUID.fromString(ownerUuid);
        return Optional.of(uuid);
    }

    private Optional<String> getItemFrameOwnerName(ItemFrame frame) {
        String ownerName = frame.getPersistentDataContainer().get(keyName, PersistentDataType.STRING);
        if (ownerName == null) {
            return Optional.empty();
        }
        return Optional.of(ownerName);
    }
}

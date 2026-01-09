package dev.Fjc.ultraBans.builders.permission;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PermissionBuilder {

    public enum Perms {
        ADMIN(new Permission("ultrabans.admin")),
        BAN(new Permission("ultrabans.admin.ban")),
        MUTE(new Permission("ultrabans.admin.mute")),
        WARN(new Permission("ultrabans.admin.warn")),
        IMMUNITY(new Permission("ultrabans.immunity"));

        private final Permission permission;

        Perms(Permission permission) {
            this.permission = permission;
        }

        public Permission getPermission() {
            return this.permission;
        }
    }
}

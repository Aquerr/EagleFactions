package io.github.aquerr.eaglefactions;

import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class PluginPermissions
{
    //Relations
    public static final String TRUCE_COMMAND = "player.truce";
    public static final String ALLY_COMMAND = "player.ally";
    public static final String ENEMY_COMMAND = "player.enemy";

    //General
    public static final String CREATE_COMMAND = "player.create";
    public static final String DISBAND_COMMAND = "player.disband";
    public static final String RENAME_COMMAND = "player.rename";
    public static final String TAG_COMMAND = "player.tag";
    public static final String DESCRIPTION_COMMAND = "player.description";
    public static final String MOTD_COMMAND = "player.motd";
    public static final String INVITE_COMMAND = "player.invite";
    public static final String JOIN_COMMAND = "player.join";
    public static final String LEAVE_COMMAND = "player.leave";
    public static final String LIST_COMMAND = "player.list";
    public static final String KICK_COMMAND = "player.kick";
    public static final String CHAT_COMMAND = "player.chat";
    public static final String TOP_COMMAND = "player.top";
    public static final String PUBLIC_COMMAND = "player.public";

    public static final String HELP_COMMAND = "player.help";
    public static final String PLAYER_COMMAND = "player.player";
    public static final String INFO_COMMAND = "player.info";
    public static final String INFO_COMMAND_SELF = "player.info.self";
    public static final String INFO_COMMAND_OTHERS = "player.info.others";
    public static final String VERSION_COMMAND = "player.version";

    public static final String DEBUG_COMMAND = "player.debug";

    //Claiming
    public static final String CLAIMS_LIST_COMMAND = "player.claims";
    public static final String CLAIM_COMMAND = "player.claim";
    public static final String RADIUS_CLAIM_COMMAND = "player.radiusclaim";
    public static final String COMMAND_FILL_CLAIM_COMMAND = "player.fillclaim";
    public static final String AUTO_CLAIM_COMMAND = "player.autoclaim";
    public static final String UNCLAIM_COMMAND = "player.unclaim";
    public static final String UNCLAIM_ALL_COMMAND = "player.unclaimall";

    //Promoting/Demoting
    public static final String SET_LEADER_COMMAND = "player.setleader";
    public static final String PROMOTE_COMMAND = "player.promote";
    public static final String DEMOTE_COMMAND = "player.demote";
    public static final String SET_HOME_COMMAND = "player.sethome";

    //Home
    public static final String HOME_COMMAND = "player.home";
    public static final String HOME_COMMAND_ADMIN_TELEPORT_TO_OTHERS = "admin.home.teleport_to_others";
    public static final String HOME_COMMAND_ADMIN_NO_DELAY = "admin.home.nodelay";

    //Access
    public static final String ACCESS_COMMAND = "player.access";
    public static final String ACCESS_PLAYER_COMMAND = "player.access.player";
    public static final String ACCESS_FACTION_COMMAND = "player.access.faction";
    public static final String ACCESS_OWNED_BY_COMMAND = "player.access.ownedby";
    public static final String ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND = "player.access.notaccessiblebyfaction";

    //Map
    public static final String MAP_COMMAND = "player.map";
    public static final String AUTO_MAP_COMMAND = "player.automap";

    public static final String COORDS_COMMAND = "player.coords";
    public static final String ATTACK_COMMAND = "player.attack";
    public static final String PERMS_COMMAND = "player.perms";
    public static final String TAG_COLOR_COMMAND = "player.tagcolor";
    public static final String CHEST_COMMAND = "player.chest";

    //Admin
    public static final String CONSTANT_ADMIN_MODE = "constant.adminmode";

    public static final String ADMIN_MODE_COMMAND = "admin.adminmode";

    public static final String SET_FACTION_COMMAND = "admin.setfaction";

    public static final String SET_POWER_COMMAND = "admin.setpower";
    public static final String MAX_POWER_COMMAND = "admin.maxpower";
    public static final String MAX_POWER_FOR_EVERYONE_COMMAND = "admin.maxpowerforeveryone";

    public static final String FEATHER_COMMAND = "admin.feather";

    public static final String REGEN_COMMAND = "admin.regen";
    public static final String FLAGS_COMMAND = "admin.flags";
    public static final String SET_FLAG_COMMAND = "admin.setflag";
    public static final String RELOAD_COMMAND = "admin.reload";
    public static final String VERSION_NOTIFY = "admin.version.notify";

    //Build & Interaction
    public static final String SAFE_ZONE_BUILD = "safezone.build";
    public static final String WAR_ZONE_BUILD = "warzone.build";
    public static final String SAFE_ZONE_INTERACT = "safezone.interact";
    public static final String WAR_ZONE_INTERACT = "warzone.interact";
    public static final String BACKUP_COMMAND = "admin.backup.create";
    public static final String RESTORE_BACKUP_COMMAND = "admin.backup.restore";
    private PluginPermissions()
    {

    }

    public static List<PermissionNode<?>> getAsPermissionNodesList()
    {
        List<PermissionNode<?>> permissionNodes = new ArrayList<>();
        
        // I am too lazy putting all permissions by hand in the list so...
        try
        {
            Field[] fields = PluginPermissions.class.getDeclaredFields();
            for (final Field field : fields)
            {
                permissionNodes.add(new PermissionNode<>(PluginInfo.ID, (String) field.get(null), PermissionTypes.STRING, (player, playerUUID, context) -> ""));
            }
        }
        catch (Exception exception)
        {
            throw new IllegalStateException(exception);
        }
        return permissionNodes;
    }
}

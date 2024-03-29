package io.github.aquerr.eaglefactions;

public final class PluginPermissions
{
    //Relations
    public static final String TRUCE_COMMAND = "eaglefactions.player.truce";
    public static final String ALLY_COMMAND = "eaglefactions.player.ally";
    public static final String ENEMY_COMMAND = "eaglefactions.player.enemy";
    public static final String LIST_RELATIONS_COMMAND = "eaglefactions.player.relations.list";
    public static final String LIST_RELATIONS_SELF_COMMAND = "eaglefactions.player.relations.list.self";
    public static final String LIST_RELATIONS_OTHERS_COMMAND = "eaglefactions.player.relations.list.others";
    public static final String RELATION_LIST_PERMISSIONS_COMMAND = "eaglefactions.player.relations.permission.list";
    public static final String SET_RELATION_PERMISSION_COMMAND = "eaglefactions.player.relations.permission.set";

    //General
    public static final String CREATE_COMMAND = "eaglefactions.player.create";
    public static final String DISBAND_COMMAND = "eaglefactions.player.disband";
    public static final String RENAME_COMMAND = "eaglefactions.player.rename";
    public static final String TAG_COMMAND = "eaglefactions.player.tag";
    public static final String DESCRIPTION_COMMAND = "eaglefactions.player.description";
    public static final String MOTD_COMMAND = "eaglefactions.player.motd";
    public static final String INVITE_COMMAND = "eaglefactions.player.invite";
    public static final String JOIN_COMMAND = "eaglefactions.player.join";
    public static final String LEAVE_COMMAND = "eaglefactions.player.leave";
    public static final String LIST_COMMAND = "eaglefactions.player.list";
    public static final String KICK_COMMAND = "eaglefactions.player.kick";
    public static final String CHAT_COMMAND = "eaglefactions.player.chat";
    public static final String TOP_COMMAND = "eaglefactions.player.top";
    public static final String PUBLIC_COMMAND = "eaglefactions.player.public";

    public static final String HELP_COMMAND = "eaglefactions.player.help";
    public static final String PLAYER_COMMAND = "eaglefactions.player.player";
    public static final String INFO_COMMAND = "eaglefactions.player.info";
    public static final String INFO_COMMAND_SELF = "eaglefactions.player.info.self";
    public static final String INFO_COMMAND_OTHERS = "eaglefactions.player.info.others";
    public static final String VERSION_COMMAND = "eaglefactions.player.version";

    public static final String DEBUG_COMMAND = "eaglefactions.player.debug";

    //Claiming
    public static final String CLAIMS_LIST_COMMAND = "eaglefactions.player.claims";
    public static final String CLAIM_COMMAND = "eaglefactions.player.claim";
    public static final String SQUARE_CLAIM_COMMAND = "eaglefactions.player.squareclaim";
    public static final String COMMAND_FILL_CLAIM_COMMAND = "eaglefactions.player.fillclaim";
    public static final String AUTO_CLAIM_COMMAND = "eaglefactions.player.autoclaim";
    public static final String UNCLAIM_COMMAND = "eaglefactions.player.unclaim";
    public static final String UNCLAIM_ALL_COMMAND = "eaglefactions.player.unclaimall";

    // Ranks
    public static final String SET_LEADER_COMMAND = "eaglefactions.player.setleader";
//    public static final String PROMOTE_COMMAND = "eaglefactions.player.promote";
//    public static final String DEMOTE_COMMAND = "eaglefactions.player.demote";

    public static final String RANK_COMMANDS = "eaglefactions.player.rank";
    public static final String LIST_RANKS_COMMAND = "eaglefactions.player.rank.list_ranks";
    public static final String CREATE_RANK_COMMAND = "eaglefactions.player.rank.create";
    public static final String SET_RANK_PERMISSION_COMMAND = "eaglefactions.player.rank.set_permission";
    public static final String SET_RANK_DISPLAY_NAME_COMMAND = "eaglefactions.player.rank.set_display_name";
    public static final String LIST_RANK_PERMISSIONS_COMMAND = "eaglefactions.player.rank.list_permissions";
    public static final String SET_RANK_POSITION_COMMAND = "eaglefactions.player.rank.set_ladder_position";
    public static final String SET_RANK_DISPLAY_IN_CHAT_COMMAND = "eaglefactions.player.rank.set_display_in_chat";
    public static final String ASSIGN_RANK_COMMAND = "eaglefactions.player.rank.assign";
    public static final String DELETE_RANK = "eaglefactions.player.rank.delete";

    public static final String SET_HOME_COMMAND = "eaglefactions.player.rank.set-default-rank";

    //Home
    public static final String HOME_COMMAND = "eaglefactions.player.home";
    public static final String HOME_COMMAND_ADMIN_TELEPORT_TO_OTHERS = "eaglefactions.admin.home.teleport_to_others";
    public static final String HOME_COMMAND_ADMIN_NO_DELAY = "eaglefactions.admin.home.nodelay";

    //Access
    public static final String ACCESS_COMMAND = "eaglefactions.player.access";
    public static final String ACCESS_PLAYER_COMMAND = "eaglefactions.player.access.player";
    public static final String ACCESS_FACTION_COMMAND = "eaglefactions.player.access.faction";
    public static final String ACCESS_OWNED_BY_COMMAND = "eaglefactions.player.access.ownedby";
    public static final String ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND = "eaglefactions.player.access.notaccessiblebyfaction";

    //Map
    public static final String MAP_COMMAND = "eaglefactions.player.map";
    public static final String AUTO_MAP_COMMAND = "eaglefactions.player.automap";

    public static final String COORDS_COMMAND = "eaglefactions.player.coords";
    public static final String ATTACK_COMMAND = "eaglefactions.player.attack";
    public static final String PERMS_COMMAND = "eaglefactions.player.perms";
    public static final String TAG_COLOR_COMMAND = "eaglefactions.player.tagcolor";
    public static final String CHEST_COMMAND = "eaglefactions.player.chest";

    //Admin
    public static final String CONSTANT_ADMIN_MODE = "eaglefactions.constant.adminmode";

    public static final String ADMIN_MODE_COMMAND = "eaglefactions.admin.adminmode";

    public static final String SET_FACTION_COMMAND = "eaglefactions.admin.setfaction";

    public static final String SET_POWER_COMMAND = "eaglefactions.admin.setpower";
    public static final String MAX_POWER_COMMAND = "eaglefactions.admin.maxpower";
    public static final String MAX_POWER_FOR_EVERYONE_COMMAND = "eaglefactions.admin.maxpowerforeveryone";

    public static final String FEATHER_COMMAND = "eaglefactions.admin.feather";

    public static final String REGEN_COMMAND = "eaglefactions.admin.regen";
    public static final String FLAGS_COMMAND = "eaglefactions.admin.flags";
    public static final String SET_FLAG_COMMAND = "eaglefactions.admin.setflag";
    public static final String RELOAD_COMMAND = "eaglefactions.admin.reload";
    public static final String VERSION_NOTIFY = "eaglefactions.admin.version.notify";

    //Build & Interaction
    public static final String SAFE_ZONE_BUILD = "eaglefactions.safezone.build";
    public static final String WAR_ZONE_BUILD = "eaglefactions.warzone.build";
    public static final String SAFE_ZONE_INTERACT = "eaglefactions.safezone.interact";
    public static final String WAR_ZONE_INTERACT = "eaglefactions.warzone.interact";
    public static final String BACKUP_COMMAND = "eaglefactions.admin.backup.create";
    public static final String RESTORE_BACKUP_COMMAND = "eaglefactions.admin.backup.restore";

    private PluginPermissions()
    {

    }
}

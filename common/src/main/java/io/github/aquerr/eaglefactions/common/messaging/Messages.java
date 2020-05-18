package io.github.aquerr.eaglefactions.common.messaging;

import com.google.inject.Singleton;

@Singleton
//@ConfigSerializable
public class Messages
{
    //    @Setting(value = "THERE_IS_NO_FACTION_CALLED_FACTION_NAME")
//    public static TextTemplate THERE_IS_NO_FACTION_CALLED_FACTION_NAME;
    public static String THERE_IS_NO_FACTION_CALLED_FACTION_NAME;

    public static String SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES;
    public static String YOU_ARE_IN_WAR_WITH_THIS_FACTION;
    public static String YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS;
    public static String YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND;
    public static String ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND;
    public static String YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS;
    public static String FACTION_HAS_BEEN_ADDED_TO_THE_ENEMIES;
    public static String ENEMIES;

    public static String FACTION;

    public static String WRONG_COMMAND_ARGUMENTS;
    public static String USAGE;

    public static String TO_ACCEPT_INVITATION_OR_TYPE;

    //Admin Mode
    public static String ADMIN_MODE_HAS_BEEN_TURNED_ON;
    public static String ADMIN_MODE_HAS_BEEN_TURNED_OFF;

    //Attacking
    public static String YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT;
    public static String YOU_CANT_ATTACK_THIS_FACTION;
    public static String ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED;
    public static String STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_CLAIM_IT;
    public static String SECONDS;
    public static String THEIR_POWER_IS_TO_HIGH;
    public static String YOU_ARE_IN_THE_SAME_ALLIANCE;
    public static String YOU_CANT_ATTACK_YOURSELF;
    public static String THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE;
    public static String CLAIM_DESTROYED;
    public static String ONE_OF_YOUR_CLAIMS_HAS_BEEN_DESTROYED_BY_AN_ENEMY;
    public static String STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_DESTROY_IT;

    //Chat
    public static String CHANGED_CHAT_TO;
    public static String GLOBAL_CHAT;
    public static String ALLIANCE_CHAT;
    public static String FACTION_CHAT;

    public static String ALLIANCE_CHAT_PREFIX;
    public static String FACTION_CHAT_PREFIX;

    public static String LEADER_PREFIX;
    public static String OFFICER_PREFIX;
    public static String MEMBER_PREFIX;
    public static String RECRUIT_PREFIX;

    //Chest
    public static String FACTION_CHESTS_ARE_DISABLED;
//    public static TextTemplate YOU_OPENED_FACTION_CHEST;
    public static String YOU_OPENED_FACTION_CHEST;

    //Claiming
    public static String LAND;
    public static String HAS_BEEN_SUCCESSFULLY;
    public static String CLAIMED;
    public static String CLAIMS_NEED_TO_BE_CONNECTED;
    public static String YOUR_FACTION_IS_UNDER_ATTACK;
    public static String YOU_NEED_TO_WAIT;
    public static String YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS;
    public static String THIS_PLACE_IS_ALREADY_CLAIMED;
    public static String YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CLAIM_A_TERRITORY;
    public static String YOU_MOVED_FROM_THE_CHUNK;
    public static String AUTO_CLAIM_HAS_BEEN_TURNED_ON;
    public static String AUTO_CLAIM_HAS_BEEN_TURNED_OFF;
    public static String YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD;
    public static String YOU_NEED_TO_WAIT_NUMBER_MINUTES_TO_BE_ABLE_TO_CLAIM_AGAIN;
    public static String THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION;

    //Coords
    public static String TEAM_COORDS;
    public static String FACTIONS_HOME;
    public static String LEADER;
    public static String OFFICER;
    public static String MEMBER;
    public static String RECRUIT;

    //Creation
    public static String YOU_CANT_USE_THIS_FACTION_NAME;
    public static String PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN;
    public static String PROVIDED_FACTION_TAG_IS_TOO_LONG;
    public static String PROVIDED_FACTION_TAG_IS_TOO_SHORT;
    public static String PROVIDED_FACTION_NAME_IS_TOO_LONG;
    public static String PROVIDED_FACTION_NAME_IS_TOO_SHORT;
    public static String MAX;
    public static String MIN;
    public static String CHARS;
    public static String FACTION_HAS_BEEN_CREATED;
    public static String FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS;
    public static String YOU_ARE_ALREADY_IN_A_FACTION;
    public static String YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION;

    //Disbanding (and Regenerating)
    public static String FACTION_HAS_BEEN_DISBANDED;
    public static String SOMETHING_WENT_WRONG;
    public static String FACTION_HAS_BEEN_REMOVED_DUE_TO_INACTIVITY_TIME;
    public static String REGEN_WARNING_CONFIRMATION_REQUIRED;
    public static String FACTION_HAS_BEEN_REGENERATED;
    public static String THIS_FACTION_CANNOT_BE_DISBANDED;

    //Perms
    public static String SET_TO;
    public static String CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE;
    public static String PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS;
    public static String PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS;
    public static String PLAYERS_WITH_YOUR_RANK_CANT_UNCLAIM_LANDS;
    public static String PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION;
    public static String HAS_NOT_PERMISSIONS_FOR;
    public static String HAS_PERMISSIONS_FOR;

    //Help
    public static String EAGLEFACTIONS_COMMAND_LIST;

    //Home
    public static String HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN;
    public static String TO_BE_ABLE_TO_USE_IT_AGAIN;
    public static String YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND;
    public static String STAND_STILL_FOR;
    public static String FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD;
    public static String FACTIONS_HOME_IS_NOT_SET;
    public static String YOU_WERE_TELEPORTED_TO_FACTIONS_HOME;
    public static String YOU_MOVED;
    public static String TELEPORTING_HAS_BEEN_CANCELLED;
    public static String COULD_NOT_SPAWN_AT_FACTIONS_HOME_HOME_MAY_NOT_BE_SET;

    //Info
    public static String YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_YOUR_FACTION;
    public static String YOU_DONT_HAVE_PERMISSIONS_FOR_VEWING_INFO_ABOUT_OTHER_FACTIONS;
    public static String YOU_DONT_HAVE_PERMISSIONS_TO_USE_THIS_COMMAND;
    public static String NAME;
    public static String TAG;
    public static String PUBLIC;
    public static String DESCRIPTION;
    public static String MOTD;
    public static String OFFICERS;
    public static String TRUCES;
    public static String ALLIANCES;
    public static String MEMBERS;
    public static String RECRUITS;
    public static String POWER;
    public static String CLAIMS;
    public static String FACTION_INFO;
    public static String LAST_ONLINE;
    public static String NOW;

    //Invite
    public static String PLAYER_IS_ALREADY_IN_A_FACTION;
    public static String YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION;
    public static String FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED;
    public static String YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT;
    public static String YOU_INVITED;
    public static String TO_YOUR_FACTION;
    public static String CLICK_HERE;
    public static String FACTION_HAS_SENT_YOU_AN_INVITE;

    //Join
    public static String SUCCESSFULLY_JOINED_FACTION;
    public static String YOU_CANT_JOIN_THIS_FACTION_BECAUSE_IT_REACHED_ITS_PLAYER_LIMIT;
    public static String YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION;

    //Kick
    public static String YOU_KICKED_PLAYER_FROM_THE_FACTION;
    public static String YOU_WERE_KICKED_FROM_THE_FACTION;
    public static String YOU_CANT_KICK_THIS_PLAYER;
    public static String THIS_PLAYER_IS_NOT_IN_YOUR_FACTION;

    //Leave
    public static String YOU_LEFT_FACTION;
    public static String YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER;
    public static String DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER;

    //List
    public static String FACTIONS_LIST;

    //Map
    public static String FACTIONS_MAP_HEADER;
    public static String FACTIONS_MAP_FOOTER;
    public static String YOUR_FACTION;
    public static String FACTIONS;
    public static String CURRENTLY_STANDING_AT_CLAIM_WHICH_IS_CLAIMED_BY;
    public static String LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED;
    public static String YOU_CANT_VIEW_MAP_IN_THIS_WORLD;
    public static String AUTO_MAP_HAS_BEEN_TURNED_OFF;
    public static String AUTO_MAP_HAS_BEEN_TURNED_ON;

    //Maxpower
    public static String YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS;
    public static String PLAYERS_MAXPOWER_HAS_BEEN_CHANGED;

    //Description
    public static String DESCRIPTION_IS_TOO_LONG;
    public static String FACTION_DESCRIPTION_HAS_BEEN_UPDATED;

    //Motd
    public static String FACTION_MESSAGE_OF_THE_DAY_HAS_BEEN_UPDATED;

    //Promotion/Demotion
    public static String YOU_PROMOTED_PLAYER_TO_MEMBER_TYPE;
    public static String YOU_DEMOTED_PLAYER_TO_MEMBER_TYPE;
    public static String YOU_CANT_PROMOTE_THIS_PLAYER_MORE;
    public static String YOU_CANT_DEMOTE_THIS_PLAYER_MORE;

    //Player
    public static String PLAYER_INFO;
    public static String LAST_PLAYED;
    public static String PLAYER_STATUS;

    //Reload
    public static String CONFIG_HAS_BEEN_RELOADED;

    //Rename
    public static String SUCCESSFULLY_RENAMED_FACTION_TO_FACTION_NAME;

    //Truce
    public static String FACTION_HAS_BEEN_ADDED_TO_THE_TRUCE;
    public static String YOU_DISBANDED_YOUR_TRUCE_WITH_FACTION;
    public static String DISBAND_TRUCE_FIRST_TO_INVITE_FACTION_TO_THE_ALLIANCE;
    public static String YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_TRUCE;
    public static String YOU_CANNOT_INVITE_YOURSELF_TO_THE_TRUCE;
    public static String YOU_HAVE_INVITED_FACTION_TO_THE_TRUCE;
    public static String FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE;
    public static String FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_TRUCE;
    public static String DISBAND_TRUCE_FIRST_TO_DECLARE_A_WAR;
    public static String THIS_FACTION_IS_IN_TRUCE_WITH_YOU;

    //Ally
    public static String FACTION_HAS_BEEN_ADDED_TO_THE_ALLIANCE;
    public static String YOU_DISBANDED_YOUR_ALLIANCE_WITH_FACTION;
    public static String FACTION_ACCEPTED_YOUR_INVITE_TO_THE_ALLIANCE;
    public static String YOU_HAVE_ACCEPTED_AN_INVITATION_FROM_FACTION;
    public static String YOU_HAVE_ALREADY_INVITED_THIS_FACTION_TO_THE_ALLIANCE;
    public static String YOU_CANNOT_INVITE_YOURSELF_TO_THE_ALLIANCE;
    public static String YOU_HAVE_INVITED_FACTION_TO_THE_ALLIANCE;
    public static String FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_ALLIANCE;
    public static String DISBAND_ALLIANCE_FIRST_TO_DECLARE_A_WAR;
    public static String DISBAND_ALLIANCE_FIRST_TO_INVITE_FACTION_TO_THE_TRUCE;
    public static String THIS_FACTION_IS_YOUR_ALLY;

    //Enemy
    public static String YOU_REMOVED_WAR_STATE_WITH_FACTION;
    public static String YOU_HAVE_ACCEPTED_ARMISTICE_REQUEST_FROM_FACTION;
    public static String TO_ACCEPT_IT_OR_TYPE;
    public static String YOU_REQUESTED_ARMISTICE_WITH_FACTION;
    public static String YOUR_FACTION_IS_NOW_ENEMIES_WITH_FACTION;
    public static String FACTION_HAS_HAS_DECLARED_YOU_A_WAR;
    public static String FACTION_ACCEPTED_YOUR_ARMISTICE_REQUEST;
    public static String YOU_HAVE_ALREADY_SENT_ARMISTICE_REQUEST;
    public static String FACTION_HAS_SENT_YOU_AN_ARMISTICE_REQUEST;
    public static String YOU_CANNOT_BE_IN_WAR_WITH_YOURSELF;

    //Home
    public static String FACTION_HOME_HAS_BEEN_SET;
    public static String FACTION_HOME_MUST_BE_PLACED_INSIDE_FACTION_TERRITORY;
    public static String TELEPORTING_TO_FACTION_HOME;
    public static String MISSING_OR_CORRUPTED_HOME;
    public static String THIS_FACTION_DOES_NOT_HAVE_ITS_HOME_SET;
    public static String YOU_CANT_TELEPORT_TO_THIS_FACTION_HOME_ALLIANCE_NEEDED;

    //SetLeader
    public static String YOU_SET_PLAYER_AS_YOUR_NEW_LEADER;
    public static String YOU_ALREADY_ARE_THE_LEADER_OF_THIS_FACTION;

    //Setpower
    public static String PLAYERS_POWER_HAS_BEEN_CHANGED;

    //Unclaim
    public static String SUCCESSFULLY_REMOVED_ALL_CLAIMS;

    //Motd
    public static String FACTION_MESSAGE_OF_THE_DAY;

    //World
    public static String THIS_LAND_BELONGS_TO_SOMEONE_ELSE;
    public static String YOU_DONT_HAVE_ACCESS_TO_DO_THIS;
    public static String YOU_CANT_ENTER_THIS_FACTION;
    public static String NONE_OF_THIS_FACTIONS_PLAYERS_ARE_ONLINE;
    public static String YOU_CANT_ENTER_SAFEZONE_WHEN_YOU_ARE_IN_WARZONE;
    public static String YOU_HAVE_ENTERED_FACTION;
    public static String YOU_CANT_USE_COMMAND_WHILE_BEING_IN_A_FIGHT;
    public static String TIME_LEFT_NUMBER_SECONDS;

    public static String PVPLOGGER_HAS_TURNED_ON;
    public static String PVPLOGGER_HAS_TURNED_OFF;
    public static String YOU_WILL_DIE_IF_YOU_DISCONNECT_IN;
    public static String YOU_CAN_NOW_DISCONNECT_SAFELY;

    //PowerFlow
    public static String YOUR_POWER_HAS_BEEN_DECREASED_BY;
    public static String YOUR_POWER_HAS_BEEN_INCREASED_BY;
    public static String CURRENT_POWER;

    //VERSION
    public static String VERSION;
    public static String A_NEW_VERSION_OF;
    public static String IS_AVAILABLE;

    //Tag
    public static String FACTION_TAG_HAS_BEEN_SUCCESSFULLY_CHANGED_TO;
    public static String TAG_COLOR_HAS_BEEN_CHANGED;
    public static String TAG_COLORING_IS_TURNED_OFF_ON_THIS_SERVER;

    //Debug
    public static String DEBUG_MODE_HAS_BEEN_TURNED_ON;
    public static String DEBUG_MODE_HAS_BEEN_TURNED_OFF;
}

package com.seetong5.app.seetong.comm;

import com.custom.etc.EtcInfo;

/**
 * Created by Administrator on 2014-07-02.
 */
public class Define {
    public static final int MSG_SHOW_TOAST = 100;
    public static final int MSG_SEARCH_DEVICE_RESP = 101;
    public static final int MSG_STATUS_EVENT = 102;
    public static final int MSG_RECEIVER_MEDIA_FIRST_FRAME = 103;
    public static final int MSG_FRONT_END_RECORD = 104;
    public static final int MSG_CLOUD_END_RECORD = 105;
    public static final int MSG_PARSE_DEV_LIST = 106;
    public static final int MSG_PLAY_SINGLE_VIDEO = 107;
    public static final int MSG_PLAY_MULTI_VIDEO = 108;
    public static final int MSG_UPDATE_DEV_LIST = 109;
    public static final int MSG_UPDATE_SCREENSHOT_LIST = 110;
    public static final int MSG_UPDATE_DEV_ALIAS = 111;
    public static final int MSG_UPDATE_VIDEORECORD_LIST = 112;
    public static final int MSG_SHOW_PICTURE_FRAGMENT = 113;
    public static final int MSG_NVR_RECORD = 107;
    public static final int MSG_DEVICE_CONFIG = 60004;


    public static final String appName = EtcInfo.appName;

    public static final int LOGIN_TYPE_NONE = -1;
    public static final int LOGIN_TYPE_DEVICE = 0;
    public static final int LOGIN_TYPE_USER = 1;
    public static final int LOGIN_TYPE_DEMO = 2;

    public static final int SUB_STREAM_TYPE = 1;
    public static final int MAIN_STREAM_TYPE = 0;

    /**
     * 默认存储根目录，一般为：/sdcard/PackageName
     */
    public static final String RootDirPath = (Tools.getExternalStoragePath() + "/").replaceAll("//", "/") + appName;

    public static final String SPLIT_CHAR = "@@";
    public static final String VideoExts = Tools.concatString(SPLIT_CHAR, ".h264", ".mp4");
    public static final String ImageExts = Tools.concatString(SPLIT_CHAR, ".jpg", ".jpeg", ".png", ".gif");

    public static final String DEVICE_TYPE_CAMERA = "NVS-DM36X-HD";
    public static final String DEVICE_TYPE_NVR = "NVR";
    public static final String DEVICE_TYPE_DECODE = "MDEC-DM365";

    //------------------------键值对常量-------------------------------
    /**
     * user_name-user_password<br>
     * --string save_state-device_state-plaform_state<br>
     * --boolean server_types<br>
     * --int node_nums-node_content<br>
     * --int-_@_@_ node-->ip port gallery
     */
    public static final String SAVED_VERSION = "saved_version";
    public static final String LOGIN_TYPE = "login_type";	//0:none 1:user 2:device
    public static final String ENCRYPT_TYPE = "encrypt_type"; //0:none 1:aes 2:base
    public static final String USR_NAME = "user_name";
    public static final String USR_PSW = "user_password";
    public static final String DEV_ID = "dev_id";
    public static final String DEV_NAME = "dev_name";
    public static final String DEV_PSW = "dev_password";
    public static final String IS_SAVE_DATA = "is_save_data";
    public static final String IS_FIRST_ENTER = "is_first_enter";
    public static final String IS_SAVE_PWD = "is_save_pwd";
    public static final String IS_AUTO_LOGIN = "is_auto_login";
    public static final String IS_AUTO_PLAY = "is_auto_play";
    public static final String SERVER_URL = "server_url";
    public static final String SERVER_PORT = "server_port";
    public static final String PTZ_SPEED = "ptz_speed";	//0-10
    public static final String VIDEO_VIEW_NUMS = "video_view_nums"; //0:1 1:4
    public static final String CFG_ENABLE_ALARM = "cfg_enable_alarm";
    public static final String CFG_ALARM_SOUND_INDEX = "cfg_alarm_sound_index";
    public static final String CFG_NOT_PROMPT_MODIFY_PASSWORD = "cfg_not_prompt_modify_password";
    public static final String CFG_IN_CALL_MODE = "cfg_in_call_mode";
    public static final String CFG_SHOW_VIDEO_INFO = "cfg_show_video_info";

    /** xml文件名 */
    public static final String LOGIN_USER_CONFIG_FILE = "lu_config";
    public static final String LOGIN_DEVICE_CONFIG_FILE = "ld_config";
    public static final String LOGIN_ALL_CONFIG_FILE = "login_all_config";
    public static final String DEVICE_LIST_CONFIG_FILE = "device_list_config";
    public static final String SEETONG_CONFIG_FILE = "st_config";
    public static final String TMP_CONFIG_FILE = "tmp_config";
    public static final String LIVE_LIST_CONFIG_FILE = "ll_config";
    public static final String SYSTEM_CONFIG_FILE = "system_config";
    public static final String CORE_CONFIG_FILE = "core_config";

    public static final String INTENT_ACTION_ALARM_EVENT = "com.seetong.app.seetong.ALARM_EVENT";

    //------------------------Handler MSG ID-------------------------------
    /** 最小的消息ID */
    public static final int MIN_MSG_ID = -1;
    public static final int RESTART_REQUEST = MIN_MSG_ID - 100;
    public static final int START_REQUEST = MIN_MSG_ID + 2;
    public static final int END_REQUEST = MIN_MSG_ID + 3;
    public static final int STOPPED_REQUEST = MIN_MSG_ID + 4;

    public static final int CLEAR_BACKGROUND = MIN_MSG_ID + 5;
    public static final int START_VIDEO_REQUEST = MIN_MSG_ID + 6;
    public static final int END_VIDEO_REQUEST = MIN_MSG_ID + 7;

    public static final int FALIED = MIN_MSG_ID + 8;
    public static final int SUCCESS = MIN_MSG_ID + 9;

    public static final int IO_ERROR = MIN_MSG_ID + 10;

    public static final int NET_OK = MIN_MSG_ID + 11;
    public static final int NET_ERROR = MIN_MSG_ID + 12;

    public static final int CHECK_FAILED = MIN_MSG_ID + 13;
    public static final int CHECK_SUCCESS = MIN_MSG_ID + 14;

    public static final int SERVER_TIMEOUT = MIN_MSG_ID + 15;
    public static final int SERVER_OFF = MIN_MSG_ID + 16;
    public static final int SERVER_ERROR = MIN_MSG_ID + 17;

    public static final int LOGIN_FAILED = MIN_MSG_ID + 18;
    public static final int LOGIN_SUCCESS = MIN_MSG_ID + 19;

    /** 网络不稳定 */
    public static final int SERVER_ASTABLE = MIN_MSG_ID + 20;
    public static final int STOP_VIDEO = MIN_MSG_ID + 21;
    public static final int CHECK_NET = MIN_MSG_ID + 22;
    public static final int BACK_PRE_TIP = MIN_MSG_ID + 23;
    /** Java层栈内存溢出*/
    public static final int MEMORY_OVERFLOW = MIN_MSG_ID + 24;
    /** 从平台获取设备信息失败*/
    public static final int GET_DEV_INFO_ERROR = MIN_MSG_ID + 25;
    /** 最大的消息ID */
    public static final int MAX_MSG_ID = GET_DEV_INFO_ERROR + 1;

    /** 进度条消息标识 */
    public static final int MSG_ID_NONE = -1;
    public static final int MSG_ID_EXIT_PROCESS = MSG_ID_NONE + 1;
    public static  final int MSG_ID_SHOW_SERIAL_DILOG = MSG_ID_NONE + 2;
    public static  final int MSG_ID_SERIAL_IMPORT_OK = MSG_ID_NONE + 3;
    public static  final int MSG_ID_SERIAL_FILE_NOT_EXIT = MSG_ID_NONE + 4;
    public static  final int MSG_ID_SERIAL_ILLEGAL = MSG_ID_NONE + 5;
    //------------------------Handler MSG ID-------------------------------

    /**
     * 主、次码流的标识 main:1280x720 sub:320x176 third:176x144<br>
     * 1/3/5 video&audio 2/4/6 video<br>
     */
    public static final int MAIN_STREAM_AV = 1;
    public static final int MAIN_STREAM_V = 2;
    public static final int SUB_STREAM_AV = 3;
    public static final int SUB_STREAM_V = 4;
    public static final int Third_STREAM_AV = 5;
    public static final int Third_STREAM_V = 6;

    public static final int DEFAULT_STREAM_TYPES = SUB_STREAM_V;
    public static final int DEFAULT_GALLERY = 0;

    /** Scoket最大读取错误次数 */
    public static final int MAX_SOCKET_ERROR_NUMS = 10;

    /** 默认Socket连接超时时间为30秒*/
    public static int TIME_OUT = 30*1000;

    /** 心跳间隔时间,默认周期为30秒*/
    public static  int PANT_TIME = 30*1000;

    public static final int SELECT_DEFAULT = 1;
    public static final int SELECT_INI = SELECT_DEFAULT + 1;
    public static final int SELECT_TXT = SELECT_DEFAULT + 2;
    public static final int SELECT_DAT = SELECT_DEFAULT + 3;
    public static final int SELECT_ALL = SELECT_DEFAULT + 10;

    /** 备份文件名称长度 */
    public static final int GHOST_NAEM_LENGTH = 20;
    /** 分组名称长度 */
    public static final int GROUP_NAEM_LENGTH = 12;
    /** 设备名称长度*/
    public static final int DEVICE_NAEM_LENGTH = 16;
    /** 设备密码长度*/
    public static final int DEVICE_PWD_LENGTH = 32;
}

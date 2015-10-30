package com.seetong5.app.seetong.sdk.impl;

import com.seetong5.app.seetong.Global;
import com.seetong5.app.seetong.R;
import ipc.android.sdk.com.SDK_CONSTANT;

/**
 * Created by csw on 2014/5/5.
 */
public class ConstantImpl extends SDK_CONSTANT {
    public static String getTPSErrText(int errNO) {
        return getTPSErrText(errNO, false);
    }

    public static String getTPSErrText(int errNO, boolean isShowErrorID) {
        String str = "";
        switch (errNO) {
            case ERR_NONE:
                str = Global.m_res.getString(R.string.ipc_err_none);
                break;
            case ERR_OUTOFF_MEMORY:
                str = Global.m_res.getString(R.string.ipc_err_outoff_memory);
                break;
            case ERR_INVALID_ADDR:
                str = Global.m_res.getString(R.string.ipc_err_invalid_addr);
                break;
            case ERR_SOCKET:
                str = Global.m_res.getString(R.string.ipc_err_socket);
                break;
            case ERR_NOT_FIND_DEV:
                str = Global.m_res.getString(R.string.ipc_err_not_find_dev);
                break;
            case ERR_DEV_LOCK:
                str = Global.m_res.getString(R.string.ipc_err_dev_lock);
                break;
            case ERR_USER_PASSWORD:
                str = Global.m_res.getString(R.string.ipc_err_user_password);
                break;
            case ERR_RTSP_REALPLAY:
                str = Global.m_res.getString(R.string.ipc_err_rtsp_realplay);
                break;
            case ERR_RTSP_STOPPLAY:
                str = Global.m_res.getString(R.string.ipc_err_rtsp_stopplay);
                break;
            case ERR_INVALID_XML:
                str = Global.m_res.getString(R.string.ipc_err_login_server_timeout);//ipc_err_login_server_timeout-ipc_err_invalid_xml
                break;
            case ERR_P2P_SVR_LOGIN_FAILED:
                str = Global.m_res.getString(R.string.ipc_err_p2p_svr_in_the_login);
                break;
            case ERR_P2P_DISCONNECTED:
                str = Global.m_res.getString(R.string.ipc_err_p2p_disconnected);
                break;
            case ERR_P2P_AUTH_FAILED:
                str = Global.m_res.getString(R.string.ipc_err_p2p_auth_failed);
                break;
            case ERR_UPNP_DISCONNECT:
                str = Global.m_res.getString(R.string.ipc_err_upnp_disconnect);
                break;
            case ERR_UPNP_DEV_AUTH_FAILED:
                str = Global.m_res.getString(R.string.ipc_err_upnp_dev_auth_failed);
                break;
            case ERR_PLAY_FAILED:
                str = Global.m_res.getString(R.string.ipc_err_play_failed);
                break;
            case ERR_AUDIO_NOT_START:
                str = Global.m_res.getString(R.string.ipc_err_audio_not_start);
                break;
            case ERR_AUTHCODE_NULL:
                str = Global.m_res.getString(R.string.ipc_err_auth_code_null);
                break;
            case ERR_INVALID_SESSION:
                str = Global.m_res.getString(R.string.ipc_err_invalid_session);
                break;
            case ERR_INVALID_RANDOM_DATA:
                str = Global.m_res.getString(R.string.ipc_err_invalid_random_data);
                break;
            case ERR_RSP_TIMEOUT:
                str = Global.m_res.getString(R.string.ipc_err_rsp_timeout);
                break;
            case ERR_P2P_DEV_NOT_ALLOW_REPLAY:
                str = Global.m_res.getString(R.string.ipc_err_p2p_dev_not_allow_replay);
                break;
            case ERR_NVR_CHANNEL_OFFLINE:
                str = Global.m_res.getString(R.string.ipc_nvr_channel_offline);
                break;
            case ERR_SOCKET_SEND:
                str = Global.m_res.getString(R.string.ipc_err_send_error);
                break;
            case ERR_SOCKET_RECV:
                str = Global.m_res.getString(R.string.ipc_err_recv_error);
                break;
            case ERR_UNKOWN:
                str = Global.m_res.getString(R.string.ipc_err_unkown);
                break;
            default:
                str = Global.m_res.getString(R.string.ipc_err_unkown);
                break;
        }
        if (errNO != ERR_NONE && isShowErrorID) str += "(" + errNO + ")";
        return str;
    }

    public static String getRegErrText(int errNO, boolean isShowErrorID) {
        String str = "";
        switch (errNO) {
            case reg_error_null:
                str = Global.m_res.getString(R.string.reg_error_null);
                break;
            case reg_error_user:
                str = Global.m_res.getString(R.string.reg_error_user);
                break;
            case reg_error_password:
                str = Global.m_res.getString(R.string.reg_error_password);
                break;
            case reg_error_code:
                str = Global.m_res.getString(R.string.reg_error_code);
                break;
            case reg_error_user_length:
                str = Global.m_res.getString(R.string.reg_error_user_length);
                break;
            case reg_error_psw_length:
                str = Global.m_res.getString(R.string.reg_error_psw_length);
                break;
            case reg_error_mail:
                str = Global.m_res.getString(R.string.reg_error_mail);
                break;
            case reg_error_phone:
                str = Global.m_res.getString(R.string.reg_error_phone);
                break;
            case reg_error_user_exist:
                str = Global.m_res.getString(R.string.reg_error_user_exist);
                break;
            default:
                str = Global.m_res.getString(R.string.reg_error_other);
                break;
        }
        if (errNO != ERR_NONE && isShowErrorID) str += "(" + errNO + ")";
        return str;
    }

    public static String getAddDevErrText(int errNO, boolean isShowErrorID) {
        String str = "";
        switch (errNO) {
            case ad_error_null:
                str = Global.m_res.getString(R.string.ad_error_null);
                break;
            case ad_error_notlogin:
                str = Global.m_res.getString(R.string.ad_error_notlogin);
                break;
            case ad_error_id:
                str = Global.m_res.getString(R.string.ad_error_id);
                break;
            case ad_error_dev_exist:
                str = Global.m_res.getString(R.string.ad_error_dev_exist);
                break;
            case ad_error_dev_lock:
                str = Global.m_res.getString(R.string.ad_error_dev_lock);
                break;
            case ad_error_user_psw:
                str = Global.m_res.getString(R.string.ad_error_user_psw);
                break;
            default:
                str = Global.m_res.getString(R.string.ad_error_other);
                break;
        }
        if (errNO != ERR_NONE && isShowErrorID) str += "(" + errNO + ")";
        return str;
    }

    public static String getAlarmTypeDesc(int type) {
        switch (type) {
            case TPS_ALARM_NONE:
                return "";
            case TPS_ALARM_FIRE:
                return Global.m_res.getString(R.string.tps_alarm_fire);
            case TPS_ALARM_SMOKE:
                return Global.m_res.getString(R.string.tps_alarm_smoke);
            case TPS_ALARM_INFRARED:
                return Global.m_res.getString(R.string.tps_alarm_infrared);
            case TPS_ALARM_GAS:
                return Global.m_res.getString(R.string.tps_alarm_gas);
            case TPS_ALARM_TEMPERATURE:
                return Global.m_res.getString(R.string.tps_alarm_temperature);
            case TPS_ALARM_GATING:
                return Global.m_res.getString(R.string.tps_alarm_gating);
            case TPS_ALARM_MANUAL:
                return Global.m_res.getString(R.string.tps_alarm_manual);
            case TPS_ALARM_FRAME_LOST:
                return Global.m_res.getString(R.string.tps_alarm_frame_lost);
            case TPS_ALARM_MOTION:
                return Global.m_res.getString(R.string.tps_alarm_motion);
            case TPS_ALARM_MASKED:
                return Global.m_res.getString(R.string.tps_alarm_masked);
            case TPS_ALARM_LINKDOWN:
                return Global.m_res.getString(R.string.tps_alarm_linkdown);
            case TPS_ALARM_LINKUP:
                return Global.m_res.getString(R.string.tps_alarm_linkup);
            case TPS_ALARM_USB_PLUG:
                return Global.m_res.getString(R.string.tps_alarm_usb_plug);
            case TPS_ALARM_USB_UNPLUG:
                return Global.m_res.getString(R.string.tps_alarm_usb_unplug);
            case TPS_ALARM_SD0_PLUG:
                return Global.m_res.getString(R.string.tps_alarm_sd0_plug);
            case TPS_ALARM_SD0_UNPLUG:
                return Global.m_res.getString(R.string.tps_alarm_sd0_unplug);
            case TPS_ALARM_SD1_PLUG:
                return Global.m_res.getString(R.string.tps_alarm_sd1_plug);
            case TPS_ALARM_SD1_UNPLUG:
                return Global.m_res.getString(R.string.tps_alarm_sd1_unplug);
            case TPS_ALARM_USB_FREESPACE_LOW:
                return Global.m_res.getString(R.string.tps_alarm_usb_freespace_low);
            case TPS_ALARM_SD0_FREESPACE_LOW:
                return Global.m_res.getString(R.string.tps_alarm_sd0_freespace_low);
            case TPS_ALARM_SD1_FREESPACE_LOW:
                return Global.m_res.getString(R.string.tps_alarm_sd1_freespace_low);
            case TPS_ALARM_VIDEO_LOST:
                return Global.m_res.getString(R.string.tps_alarm_video_lost);
            case TPS_ALARM_VIDEO_COVERD:
                return Global.m_res.getString(R.string.tps_alarm_video_coverd);
            case TPS_ALARM_MOTION_DETECT:
                return Global.m_res.getString(R.string.tps_alarm_motion_detect);
            case TPS_ALARM_GPIO3_HIGH2LOW:
                return Global.m_res.getString(R.string.tps_alarm_gpio3_high2low);
            case TPS_ALARM_GPIO3_LOW2HIGH:
                return Global.m_res.getString(R.string.tps_alarm_gpio3_low2high);
            case TPS_ALARM_STORAGE_FREESPACE_LOW:
                return Global.m_res.getString(R.string.tps_alarm_storage_freespace_low);
            case TPS_ALARM_RECORD_START:
                return Global.m_res.getString(R.string.tps_alarm_record_start);
            case TPS_ALARM_RECORD_FINISHED:
                return Global.m_res.getString(R.string.tps_alarm_record_finished);
            case TPS_ALARM_RECORD_FAILED:
                return Global.m_res.getString(R.string.tps_alarm_record_failed);
            case TPS_ALARM_GPS_INFO:
                return Global.m_res.getString(R.string.tps_alarm_gps_info);
            case TPS_ALARM_EMERGENCY_CALL:
                return Global.m_res.getString(R.string.tps_alarm_emergency_call);
            case TPS_ALARM_JPEG_CAPTURED:
                return Global.m_res.getString(R.string.tps_alarm_jpeg_captured);
            case TPS_ALARM_RS485_DATA:
                return Global.m_res.getString(R.string.tps_alarm_rs485_data);
            case TPS_ALARM_SAME_IP:
                return Global.m_res.getString(R.string.tps_alarm_same_ip);
            case TPS_ALARM_TST_NO:
                return Global.m_res.getString(R.string.tps_alarm_tst_no);
            case TPS_ALARM_TST_DISKFULL:
                return Global.m_res.getString(R.string.tps_alarm_tst_diskfull);
            case TPS_ALARM_TST_DISKERROR:
                return Global.m_res.getString(R.string.tps_alarm_tst_diskerror);
            case TPS_ALARM_TST_RETICLEDISCONNECT:
                return Global.m_res.getString(R.string.tps_alarm_tst_reticledisconnect);
            case TPS_ALARM_TST_IPCONFLICT:
                return Global.m_res.getString(R.string.tps_alarm_tst_ipconflict);
            case TPS_ALARM_TST_ILLEGEACCESS:
                return Global.m_res.getString(R.string.tps_alarm_tst_illegeaccess);
            case TPS_ALARM_TST_VIDEOSTANDARDEXCEPTION:
                return Global.m_res.getString(R.string.tps_alarm_tst_videostandardexception);
            case TPS_ALARM_TST_VIDEOEXCEPTION:
                return Global.m_res.getString(R.string.tps_alarm_tst_videoexception);
            case TPS_ALARM_TST_ENCODEERROR:
                return Global.m_res.getString(R.string.tps_alarm_tst_encodeerror);
            case TPS_ALARM_TST_TST_NO:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_no);
            case TPS_ALARM_TST_TST_IN:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_in);
            case TPS_ALARM_TST_TST_MOTION:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_motion);
            case TPS_ALARM_TST_TST_VIDEOLOSS:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_videoloss);
            case TPS_ALARM_TST_TST_EXCEPION:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_excepion);
            case TPS_ALARM_TST_TST_MASK:
                return Global.m_res.getString(R.string.tps_alarm_tst_tst_mask);
            default:
                return "";
        }
    }

    public static String getCloudAlarmTypeDesc(int type) {
        switch (type) {
            case 1:
                return getAlarmTypeDesc(TPS_ALARM_MOTION);
            case 2:
                return getAlarmTypeDesc(TPS_ALARM_GPIO3_HIGH2LOW);
            default:
                return "";
        }
    }

    public static String getModifyDevNameErrText(int err) {
        switch (err) {
            case md_error_id_null:
                return Global.m_res.getString(R.string.md_error_id_null);
            case md_error_name_null:
                return Global.m_res.getString(R.string.md_error_name_null);
            case md_error_dev_notfind:
                return Global.m_res.getString(R.string.md_error_dev_notfind);
            case md_error_user_psw:
                return Global.m_res.getString(R.string.md_error_user_psw);
            case md_error_connect_failed:
                return Global.m_res.getString(R.string.md_error_connect_failed);
            case md_error_other:
                return Global.m_res.getString(R.string.md_error_other);
            default:
                return "";
        }
    }

    public static String getDelDeviceErrText(int err) {
        switch (err) {
            case del_error_null:
                return Global.m_res.getString(R.string.del_error_null);
            case del_error_notlogin:
                return Global.m_res.getString(R.string.del_error_notlogin);
            case del_error_id:
                return Global.m_res.getString(R.string.del_error_id);
            case del_error_connect_failed:
                return Global.m_res.getString(R.string.del_error_connect_failed);
            case del_error_user_disabled:
                return Global.m_res.getString(R.string.del_error_user_disabled);
            case del_error_user_no_auth:
                return Global.m_res.getString(R.string.del_error_user_no_auth);
            case del_error_other:
                return Global.m_res.getString(R.string.del_error_other);
            default: return "";
        }
    }

    public static String getSearchDevAlarmErrText(int err) {
        switch (err) {
            case sa_error_id_null:
                return Global.m_res.getString(R.string.sa_error_id_null);
            case sa_error_dev_notfind:
                return Global.m_res.getString(R.string.sa_error_dev_notfind);
            case sa_error_no_right:
                return Global.m_res.getString(R.string.md_error_user_psw);
            case sa_error_page:
                return Global.m_res.getString(R.string.md_error_connect_failed);
            case sa_error_time:
                return Global.m_res.getString(R.string.md_error_connect_failed);
            case sa_error_other:
                return Global.m_res.getString(R.string.md_error_other);
            default:
                return "";
        }
    }

    public static String getAddFriendErrText(int err) {
        switch (err) {
            case af_error_friend_not_login:
                return Global.m_res.getString(R.string.af_error_friend_not_login);
            case af_error_friend_not_find:
                return Global.m_res.getString(R.string.af_error_friend_not_find);
            case af_error_friend_null:
                return Global.m_res.getString(R.string.af_error_friend_null);
            case af_error_friend_add:
                return Global.m_res.getString(R.string.af_error_friend_add);
            case af_error_netword:
                return Global.m_res.getString(R.string.af_error_netword);
            case af_error_other:
                return Global.m_res.getString(R.string.af_error_other);
            default:
                return "";
        }
    }

    public static String getDelFriendErrText(int err) {
        switch (err) {
            case df_error_friend_invalidate:
                return Global.m_res.getString(R.string.df_error_friend_invalidate);
            case df_error_not_friend:
                return Global.m_res.getString(R.string.df_error_not_friend);
            case df_error_netword:
                return Global.m_res.getString(R.string.df_error_netword);
            case df_error_other:
                return Global.m_res.getString(R.string.df_error_other);
            default: return "";
        }
    }

    public static String getConfirmAddFriendErrText(int err) {
        switch (err) {
            case ac_error_user_not_login:
                return Global.m_res.getString(R.string.ac_error_user_not_login);
            case ac_error_auth_invalidate:
                return Global.m_res.getString(R.string.ac_error_auth_invalidate);
            case ac_error_friend_null:
                return Global.m_res.getString(R.string.ac_error_friend_null);
            case ac_error_netword:
                return Global.m_res.getString(R.string.ac_error_netword);
            case ac_error_other:
                return Global.m_res.getString(R.string.ac_error_other);
            default:
                return "";
        }
    }

    public static String getSendOfflineMsgErrText(int err) {
        switch (err) {
            case sm_error_user_not_login:
                return Global.m_res.getString(R.string.sm_error_user_not_login);
            case sm_error_friend_invalidate:
                return Global.m_res.getString(R.string.sm_error_friend_invalidate);
            case sm_error_friend_null:
                return Global.m_res.getString(R.string.sm_error_friend_null);
            case sm_error_msg_null:
                return Global.m_res.getString(R.string.sm_error_msg_null);
            case sm_error_network:
                return Global.m_res.getString(R.string.sm_error_network);
            case sm_error_other:
                return Global.m_res.getString(R.string.sm_error_other);
            default:
                return "";
        }
    }

    public static String getDeleteOfflineMsgErrText(int err) {
        switch (err) {
            case dm_error_user_not_login:
                return Global.m_res.getString(R.string.dm_error_user_not_login);
            case dm_error_network:
                return Global.m_res.getString(R.string.dm_error_network);
            case dm_error_other:
                return Global.m_res.getString(R.string.dm_error_other);
            default:
                return "";
        }
    }

    public static String getOssErrText(int err) {
        switch (err) {
            case oss_error_dev_not_find:
                return Global.m_res.getString(R.string.oss_error_dev_not_find);
            case oss_error_dev_not_pay:
                return Global.m_res.getString(R.string.oss_error_dev_not_pay);
            case oss_error_param_invalidate:
                return Global.m_res.getString(R.string.oss_error_param_invalidate);
            case oss_error_req_failed:
                return Global.m_res.getString(R.string.oss_error_req_failed);
            case oss_error_stop_failed:
                return Global.m_res.getString(R.string.oss_error_stop_failed);
            case oss_error_search_failed:
                return Global.m_res.getString(R.string.oss_error_search_failed);
            case oss_error_download_failed:
                return Global.m_res.getString(R.string.oss_error_download_failed);
            case oss_error_del_failed:
                return Global.m_res.getString(R.string.oss_error_del_failed);
            case oss_error_set_replaypos_failed:
                return Global.m_res.getString(R.string.oss_error_set_replaypos_failed);
            default:
                return "";
        }
    }

    public static String getRegNumberErrText(int err) {
        switch (err) {
            case get_reg_number_error_null:
                return "";
            case get_reg_number_error_param:
                return Global.m_res.getString(R.string.get_reg_number_error_param);
            case get_reg_number_error_quik:
                return Global.m_res.getString(R.string.get_reg_number_error_quik);
            case get_reg_number_error_sendmsg:
                return Global.m_res.getString(R.string.get_reg_number_error_sendmsg);
            case get_reg_number_error_phonemail_used:
                return Global.m_res.getString(R.string.get_reg_number_error_phonemail_used);
            case get_reg_number_error_other:
                return Global.m_res.getString(R.string.get_reg_number_error_other);
            default:
                return "";
        }
    }

    public static String getForgetPasswordErrText(int err) {
        switch (err) {
            default:
                return "";
        }
    }
}

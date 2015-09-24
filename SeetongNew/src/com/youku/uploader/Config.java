package com.youku.uploader;

public class Config {

	protected static final String LOGIN_URL = "https://openapi.youku.com/v2/oauth2/token";
	protected static final String CREATE_URL = "https://openapi.youku.com/v2/uploads/create.json";
	protected static final String CREATE_FILE_URL = "http://upload_server_uri/create_file";
	protected static final String NEW_SLICE_URL = "http://upload_server_uri/new_slice";
	protected static final String UPLOAD_SLICE_URL = "http://upload_server_uri/upload_slice";
	protected static final String CHECK_URL = "http://upload_server_uri/check";
	protected static final String COMMIT_URL = "https://openapi.youku.com/v2/uploads/commit.json";
	protected static final String CANCEL_URL = "https://openapi.youku.com/v2/uploads/cancel.json";

	// version update
	protected static final String VERSION_UPDATE_URL = "http://open.youku.com/sdk/version_update";
	protected static String VERSION = "13112114";

	protected static Boolean DEBUG = false;

	/**
	 * 分片最大长度KB
	 */
	protected static final int SLICE_LENGTH = 1024;

	/**
	 * 一般接口请求 timeout
	 */
	protected static final int TIMEOUT = 10 * 1000;

	/**
	 * upload slice 接口 timeout
	 */
	protected static final int TIMEOUT_UPLOAD_DATA = 2 * 60 * 1000;

	/**
	 * check 2、3时 sleep
	 */
	protected static final int SLEEPTIME = 20000;

	// protected static final int UPLOAD_SLICE_MAX_THREAD = 2;

	/**
	 * upload response handler
	 */
	protected static final int RES_START = 0;
	protected static final int RES_SUCCESS = 1;
	protected static final int RES_FAILURE = 2;
	protected static final int RES_PROGRESS_UPDATE = 3;
	protected static final int RES_FINISHED = 4;
	protected static final int RES_UPLOADING = 5;

	/**
	 * error code ( 仅以下特殊code 返回JSONObject，其他均通过接口返回，更多查看主站提供error code 文档 )
	 */
	protected static final String ERROR_1002 = "Service exception occured";
	protected static final String ERROR_1012 = "Necessary parameter missing";
	protected static final String ERROR_1013 = "Invalid parameter";
	protected static final String ERROR_120020001 = "The video clip does not exist";

	/**
	 * 自定义 custom
	 */
    public static final String ERROR_50001 = "upload task only one thread";
    public static final String ERROR_50002 = "connect exception";

    public static final String ERROR_TYPE_FILE_NOT_FOUND = "FileNotFoundException";
    public static final String ERROR_TYPE_SYSTEM = "SystemException";
    public static final String ERROR_TYPE_UPLOAD_TASK = "UploadTaskException";
    public static final String ERROR_TYPE_CONNECT = "ConnectException";
}

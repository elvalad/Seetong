package com.seetong.app.seetong.ui.ext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @declaration 自定义弹出对话框
 * @author nilbounds@gmail.com
 * 2012-10-17 下午10:27:42
 */
public class MyTipDialog {
	public interface IDialogMethod{
		public void sure();
//		public void cancel();
	}
	
	public static AlertDialog getMyDialog(Context context, int titleResID, int msgResID, int okBtnTextResID, int cancelBtnTextResID, final IDialogMethod md){
		String title = context.getString(titleResID);
		String msg = context.getString(msgResID);
		String okBtnText = context.getString(okBtnTextResID);
		String cancelBtnText = context.getString(cancelBtnTextResID);
		AlertDialog dlg = getMyDialog(context, msg, okBtnText, cancelBtnText, md);
		dlg.setTitle(title);
		return dlg;
	}
	
	public static AlertDialog getMyDialog(Context context, String title, String msg, String okBtnText, String cancelBtnText, final IDialogMethod md){
		AlertDialog dlg = getMyDialog(context, msg, okBtnText, cancelBtnText, md);
		dlg.setTitle(title);
		return dlg;
	}
	
	public static AlertDialog getMyDialog(Context context, String msg, String okBtnText, String cancelBtnText, final IDialogMethod md){
		AlertDialog dlg = new AlertDialog.Builder(context)
		.setMessage(msg)
		.setPositiveButton(okBtnText,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,int which) {
						dialog.dismiss();
						if(md != null) md.sure();
					}
				})
		.setNegativeButton(cancelBtnText,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).create();
		return dlg;
	}
	
	public static void popDialog(Context context, String msg, String okBtnText, String cancelBtnText, final IDialogMethod md){
		getMyDialog(context, msg, okBtnText, cancelBtnText, md).show();
	}
	
	public static void popDialog(Context context, String msg, int okBtnResID, int cancelBtnResID, final IDialogMethod md){
		popDialog(context, msg, context.getResources().getString(okBtnResID),
				context.getResources().getString(cancelBtnResID), md);
	}
	
	public static void popDialog(Context context, int msgID, int okBtnResID, int cancelBtnResID, final IDialogMethod md){
		popDialog(context, context.getResources().getString(msgID),
				context.getResources().getString(okBtnResID), context
						.getResources().getString(cancelBtnResID), md);
	}
	
	public static void popDialog(Context context, String title, String msg, int okBtnResID, int cancelBtnResID, final IDialogMethod md){
		popDialog(context, title, msg, context.getResources()
				.getString(okBtnResID),
				context.getResources().getString(cancelBtnResID), md);
	}
	
	public static void popDialog(Context context, String title, int msgID, int okBtnResID, int cancelBtnResID, final IDialogMethod md){
		popDialog(context, title, context.getResources().getString(msgID),
				context.getResources().getString(okBtnResID), context
						.getResources().getString(cancelBtnResID), md);
	}
	
	public static void popDialog(Context context, String title, String msg, String okBtnText, String cancelBtnText, final IDialogMethod md){
		getMyDialog(context, title, msg, okBtnText, cancelBtnText, md).show();
	}

    public static void popDialog(Context context, int titleID, String msg, int okBtnResID, int cancelBtnResID, final IDialogMethod md) {
        popDialog(context, context.getResources().getString(titleID), msg,
                context.getResources().getString(okBtnResID), context
                        .getResources().getString(cancelBtnResID), md);
    }

	public static void popDialog(Context context, Object titleID, Object msgID, Object cancelBtnResID){
		String title = getMyResString(context, titleID);
		String msg = getMyResString(context, msgID);
		String cancelBtnText = getMyResString(context, cancelBtnResID);
		
		AlertDialog dlg = new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(msg)
				.setCancelable(false)
				.setNegativeButton(cancelBtnText,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create();
		dlg.show();
	}
	
	public static String getMyResString(Context context, Object resID){
		String msg = "";
		if(resID instanceof Integer){
			msg =  context.getString((Integer)resID);
		}else if(resID instanceof String){
			msg = (String) resID;
		}
		return msg;
	}
}

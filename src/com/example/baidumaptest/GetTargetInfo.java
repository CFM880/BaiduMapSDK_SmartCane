package com.example.baidumaptest;

public class GetTargetInfo {
	/*
	 * 判断是否是已知格式@Location(39.963175, 116.400244) 
	 */
	
	public boolean isTargetSMS(String message){
		String regex =  "(@Location+['(])+\\d+['.]+\\d+[',]+\\s+\\d+['.]+\\d+([')])";
		if(message.matches(regex)){
			return true;
		}else {
			return false;
		}
	}
	

	public String getTargetLatLng(String message) {
		int beginIndex = message.indexOf('(') + 1;
		int endIndex = message.indexOf(')');
		String locInfo = message.substring(beginIndex, endIndex);
		return locInfo;
	}
	
	public float targrtLnt(String locInfo){
		String[] list = locInfo.split(", ");
		float lnt = Float.parseFloat(list[0]);
		
		return lnt;
	}
	
	public float targrtLng(String locInfo){
		String[] list = locInfo.split(", ");
		float lng = Float.parseFloat(list[1]);
		
		return lng;
	}
}

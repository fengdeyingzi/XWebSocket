package com.xl.util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
//import android.os.Handler;
//import android.os.Message;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TranslateThread extends Thread
{
    public int CONNENT_TIMEOUT = 5000;
    public int READ_TIMEOUT = 7000;
    public String url = "";
    public String text;
    //	Handler handler;
    OnPostGetText listener;
    long time;

    public TranslateThread(String text, final OnPostGetText listener){
        this.text = text;
        this.listener = listener;

    }

    public String getFanyiNew(){
        //时间戳
        long time = System.currentTimeMillis();
        //一位随机数
        int i = (int) ((Math.random()*10)+1);
        String ua = "Dalvik/1.6.0 (Linux; U; Android 4.4.4; MI 4LTE MIUI/V6.6.2.0.KXDCNCF)";
        String textcode = "";
        String c = "ebSeFb%=XZ%T[KZ)c(sy!";
        String d = "@6f#X3=cCuncYssPsuRUE";
        try
        {
            textcode = URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        //
        String params =
                "q="+textcode
                        +"&keyfrom=luoluofanyi"
                        +"&key=2094706909"
                        +"&type=data&doctype=jsonp"
                        //时间 r+parseInt(10*Math.random(),10)
                        +"&version=1.1"
                        +"&callback=jQuery18307398728175361471_1562915087602"
                ;
//			+"&typoResult=false";
        String url = "http://fanyi.youdao.com/openapi.do"+"?"+params;

        String result = "";
        BufferedReader in = null;
        try
        {
            String urlName = url+params ;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            //conn.setRequestProperty("accept", "*/*");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("user-agent", "Dalvik/1.6.0 (Linux; U; Android 4.4.4; MI 4LTE MIUI/V6.6.2.0.KXDCNCF)");
            conn.setRequestProperty("Referer","http://fanyi.youdao.com/");
//			conn.setRequestProperty("Cookie","OUTFOX_SEARCH_USER_ID_NCOO=1537643834.9570553; OUTFOX_SEARCH_USER_ID=1799185238@10.169.0.83; fanyi-ad-id=43155; fanyi-ad-closed=1; JSESSIONID=aaaBwRanNsqoobhgvaHmw; _ntes_nnid=07e771bc10603d984c2dc8045a293d30,1525267244050; ___rl__test__cookies=" + String.valueOf(time));
            conn.setRequestProperty("Cookie","P_INFO=ruianrunxiang@163.com|1557127817|0|other|00&99|zhj&1556199810&mail_client#zhj&330300#10#0#0|&0||ruianrunxiang@163.com; UM_distinctid=16b539eb4a41f7-03411560d712de-e353165-1fa400-16b539eb4a5141; OUTFOX_SEARCH_USER_ID=-2002790370@183.138.62.87; JSESSIONID=aaageaBflOG3ogfBTcFVw; OUTFOX_SEARCH_USER_ID_NCOO=1075685848.894828; ___rl__test__cookies=" + String.valueOf(time));

            conn.setConnectTimeout(CONNENT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            //conn.setRequestProperty("user-agent","MQQBrowser\nQ-UA2: QV=3&PL=ADR&PR=QB&PP=com.tencent.mtt&PPVN=6.5.0.2170&TBSVC=26001&CO=BK&COVC=036504&PB=GE&VE=GA&DE=PHONE&CHID=0&LCID=9678&MO= MI4LTE &RL=1080*1920&OS=4.4.4&API=19");
            // 建立实际的连接
            //conn.connect();
            // 发送POST请求必须设置如下两行
//            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
//            OutputStream out = conn.getOutputStream();
//            // 发送请求参数
//            out.write(params.getBytes("utf-8"));
//            // flush输出流的缓冲
//            out.flush();
            // 获取所有响应头字段
			/*
			 Map<String, List<String>> map = conn.getHeaderFields();
			 // 遍历所有的响应头字段
			 for (String key : map.keySet())
			 {
			 System.out.println(key + "--->" + map.get(key));
			 }
			 */
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null)
            {
                result += "\n" + line;
            }
        }
        catch (Exception e)
        {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
//		Message m = new Message();

//		m.obj = result;
		/*
		try
		{
			m.obj = new YouDao().fanyi(text);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		*/
//		m.what = 1;
//		handler.sendMessage(m);
        return result;

    }


    public String getFanyi(){
        //时间戳
        long time = System.currentTimeMillis();
        //一位随机数
        int i = (int) ((Math.random()*10)+1);
        String ua = "Dalvik/1.6.0 (Linux; U; Android 4.4.4; MI 4LTE MIUI/V6.6.2.0.KXDCNCF)";
        String textcode = "";
        String c = "ebSeFb%=XZ%T[KZ)c(sy!";
        String d = "@6f#X3=cCuncYssPsuRUE";
        try
        {
            textcode = URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        //
        String params =
                "i="+textcode
                        +"&from=AUTO"
                        +"&to=AUTO"
                        +"&smartresult=dict&client=fanyideskweb"
                        //时间 r+parseInt(10*Math.random(),10)
                        +"&salt="+time+i
                        //sign = +e+i (i为salt

                        +"&sign="+ MD5.GetMD5Code("fanyideskweb"+text+ time+i+ d)
                        //时间 (new Date()).getTime()
                        +"&ts="+time
                        //ua的md5
                        +"&bv="+ MD5.GetMD5Code(ua)
                        +"&doctype=json"
                        +"&version=2.1"
                        +"&keyfrom=fanyi.web"
                        +"&action=FY_BY_CLICKBUTTION";
//			+"&typoResult=false";
        String url = "http://fanyi.youdao.com/translate_o?smartresult=dict&smartresult=rule";

        String result = "";
        BufferedReader in = null;
        try
        {





            String urlName = url+params ;
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            //conn.setRequestProperty("accept", "*/*");
            //conn.setRequestProperty("connection", "Keep-Alive");
            //conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("user-agent", "Dalvik/1.6.0 (Linux; U; Android 4.4.4; MI 4LTE MIUI/V6.6.2.0.KXDCNCF)");
            conn.setRequestProperty("Referer","http://fanyi.youdao.com/");
//			conn.setRequestProperty("Cookie","OUTFOX_SEARCH_USER_ID_NCOO=1537643834.9570553; OUTFOX_SEARCH_USER_ID=1799185238@10.169.0.83; fanyi-ad-id=43155; fanyi-ad-closed=1; JSESSIONID=aaaBwRanNsqoobhgvaHmw; _ntes_nnid=07e771bc10603d984c2dc8045a293d30,1525267244050; ___rl__test__cookies=" + String.valueOf(time));
            conn.setRequestProperty("Cookie","P_INFO=ruianrunxiang@163.com|1557127817|0|other|00&99|zhj&1556199810&mail_client#zhj&330300#10#0#0|&0||ruianrunxiang@163.com; UM_distinctid=16b539eb4a41f7-03411560d712de-e353165-1fa400-16b539eb4a5141; OUTFOX_SEARCH_USER_ID=-2002790370@183.138.62.87; JSESSIONID=aaageaBflOG3ogfBTcFVw; OUTFOX_SEARCH_USER_ID_NCOO=1075685848.894828; ___rl__test__cookies=" + String.valueOf(time));

            conn.setConnectTimeout(CONNENT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            //conn.setRequestProperty("user-agent","MQQBrowser\nQ-UA2: QV=3&PL=ADR&PR=QB&PP=com.tencent.mtt&PPVN=6.5.0.2170&TBSVC=26001&CO=BK&COVC=036504&PB=GE&VE=GA&DE=PHONE&CHID=0&LCID=9678&MO= MI4LTE &RL=1080*1920&OS=4.4.4&API=19");
            // 建立实际的连接
            //conn.connect();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            OutputStream out = conn.getOutputStream();
            // 发送请求参数
            out.write(params.getBytes("utf-8"));
            // flush输出流的缓冲
            out.flush();
            // 获取所有响应头字段
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null)
            {
                result += "\n" + line;
            }
        }
        catch (Exception e)
        {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return result;

    }


    public interface OnPostGetText{
        public void onPostGetText(String text);
    }



    @Override
    public void run() {
        if(listener!=null){
                String text = getFanyiNew();
            StringBuffer buffer = new StringBuffer();
            int type = 0;
            for(int i=0;i<text.length();i++){
                char c = text.charAt(i);
                switch (type) {
                    case 0:
                        if(c=='('){
                            type= 1;
                        }
                        break;
                    case 1:
                        buffer.append(c);
                        break;

                    default:
                        break;
                }

            }
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(buffer.toString());
                JSONArray array = jsonObject.getJSONArray("translation");
                String translation= null;
                for(int i=0;i<array.length();i++){
                    translation = array.getString(i);
                }
                listener.onPostGetText(translation);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }






}


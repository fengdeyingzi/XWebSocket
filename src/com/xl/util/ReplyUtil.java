package com.xl.util;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
用于自动回复
 */
public class ReplyUtil {
    ArrayList<String> list_text;
   public ReplyUtil(File file){
        this.list_text = new ArrayList();
       try {
           String text = FileUtils.read(file, "UTF-8");
           String[] list_temp = text.split("\n");
           for(int i=0;i<list_temp.length;i++){
               list_text.add(list_temp[i]);
//               System.out.println(list_temp[i]);
           }
       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   private String getHead(String text){
       for(int i=0;i<text.length();i++){
           if(text.charAt(i)==' '){
               return text.substring(0, i);
           }
       }
       return text;
   }

    private String getBody(String text){
        for(int i=0;i<text.length();i++){
            if(text.charAt(i)==' '){
                return text.substring(i,text.length());
            }
        }
        text = text.replace("【换行】","\n");
        text = text.replace("\\n","\n");
        return text.trim();
    }

    private String doBody(String text){
       text = text.replace("【换行】","\n");
       text = text.replace("\\n","\n");
       return text;
    }

   //通过自动回复获取一个列表
    public ArrayList<String> getList(String msg){
       ArrayList<String> list_temp = new ArrayList<String>();
       for(int i=0;i<list_text.size();i++){
            if(list_text.get(i).indexOf('*')>=0){
                //获取前面的关键词
String head = getHead(list_text.get(i));
                String regex = head; //"(.*a.*b.*c.*)";
                try{
                    Pattern pattern = Pattern.compile(regex,Pattern.MULTILINE);
                    Matcher matcher = pattern.matcher(msg);
                    if(matcher.matches()){
                        String item = getBody(list_text.get(i));
                        list_temp.add(item);
                    }
//                    while(matcher.find()){
//                        System.out.println(matcher.group());
//                    }
                } catch (Exception e){
//                    System.out.println("正则匹配失败："+list_text.get(i));
                }

            }else{
                if(list_text.get(i).startsWith(msg)){
                    String item = list_text.get(i).substring(msg.length()).trim();
                    if(item.endsWith("\r")){
                        item = item.substring(0,item.length()-1);
                    }
                    item = item.replace("【换行】","\n");
                    item = item.replace("\\n","\n");
                    list_temp.add(item);
                }
            }

       }
       return list_temp;
    }
   //获取一句自动回复
    public String getListOne(String msg){
       ArrayList<String> list_temp = getList(msg);
       if(list_temp.size()>0){
           int max=list_temp.size()-1,min=0;
           int ran2 = (int) (Math.random()*(max-min)+min);
           return doBody(list_temp.get(ran2));
       }else{
           return null;
       }
    }

}

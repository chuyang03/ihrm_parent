package com.ihrm.common.utils;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;

import java.util.Date;

public class QiniuUploadUtil {

    private static final String accessKey = "KUbKSi-Rnm6v9HIWPhuKeTT1WdnqrHm8kQWkKxyd";
    private static final String secretKey = "RgOpL0CoepBDxuKfx6Ly3Urrxa3GriSMqNi-5DAh";
    private static final String bucket = "ihrm-bucket-cy";
    private static final String prix = "http://q9g77v4jc.bkt.clouddn.com/";
    private UploadManager manager;

    public QiniuUploadUtil() {
        //初始化基本配置
        Configuration cfg = new Configuration(Zone.zone0());
        //创建上传管理器
        manager = new UploadManager(cfg);
    }

	//文件名 = key
	//文件的byte数组
    public String upload(String imgName , byte [] bytes) {
        Auth auth = Auth.create(accessKey, secretKey);
        //第二个参数能够保证，构造覆盖上传token
        String upToken = auth.uploadToken(bucket,imgName);
        try {
            //第一个参数：文件内容，第二个参数：文件名称，也就是key，第三个参数就是token
            Response response = manager.put(bytes, imgName, upToken);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            //返回请求地址,加了一个时间字符串，防止访问图片地址的时候出现浏览器缓存
            return prix+putRet.key+"?t="+new Date().getTime();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //如果是大文件的话可以选择断点续传，直接去七牛云查找sdk

}

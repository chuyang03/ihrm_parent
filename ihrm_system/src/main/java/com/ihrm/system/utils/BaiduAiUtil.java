package com.ihrm.system.utils;

import com.baidu.aip.face.AipFace;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class BaiduAiUtil {

    @Value("${ai.appId}")
    private String APP_ID;
    @Value("${ai.apiKey}")
    private String API_KEY;
    @Value("${ai.secretKey}")
    private String SECRET_KEY;
    @Value("${ai.imageType}")
    private String IMAGE_TYPE;
    @Value("${ai.groupId}")
    private String groupId;

    private AipFace client;

    private HashMap<String, String> options = new HashMap<String, String>();

    public BaiduAiUtil() {
        options.put("quality_control", "NORMAL");
        options.put("liveness_control", "LOW");
    }

    @PostConstruct
    public void init() {
        client = new AipFace(APP_ID, API_KEY, SECRET_KEY);
    }

    //判断用户的头像是否已经注册到人脸库了
    public Boolean faceExist(String userId){

        //从人脸库中查询是否有这个头像
        JSONObject res = client.getUser(userId, groupId, null);

        //错误编码，如果错误编码等于0，相当于说明查询用户成功，人脸库中存在该头像
        Integer error_code = res.getInt("error_code");

        return error_code == 0 ? true: false;
    }

    /**
     *  人脸注册 ：将用户照片存入人脸库中
     */
    public Boolean faceRegister(String userId, String image) {
        // 人脸注册
        JSONObject res = client.addUser(image, IMAGE_TYPE, groupId, userId, options);
        Integer errorCode = res.getInt("error_code");

        //如果errorCode==0,表示人脸注册成功，照片存入人脸库中
        return errorCode == 0 ? true : false;
    }

    /**
     *  人脸更新 ：更新人脸库中的用户照片
     */
    public Boolean faceUpdate(String userId, String image) {
        // 人脸更新
        JSONObject res = client.updateUser(image, IMAGE_TYPE, groupId, userId, options);
        Integer errorCode = res.getInt("error_code");
        return errorCode == 0 ? true : false;
    }


    /**
     *
     * {
     *   "result": {
     *     "face_num": 1,
     *     "face_list": [{
     *       "angle": {
     *         "roll": -14.42,
     *         "pitch": 15.56,
     *         "yaw": -5.89
     *       },
     *       "face_token": "15e6795ebca1e7208f39620c665bf976",
     *       "location": {
     *         "top": 562.01,
     *         "left": 199.5,
     *         "rotation": -15,
     *         "width": 582,
     *         "height": 574
     *       },
     *       "face_probability": 1
     *     }]
     *   },
     *   "log_id": 4500125947500,
     *   "error_msg": "SUCCESS",
     *   "cached": 0,
     *   "error_code": 0,
     *   "timestamp": 1588689618
     * }
     *
     *
     * 人脸检测：判断上传图片中是否具有面部头像
     */
    public Boolean faceCheck(String image) {
        JSONObject res = client.detect(image, IMAGE_TYPE, options);

        if (res.has("error_code") && res.getInt("error_code") == 0) {
            JSONObject resultObject = res.getJSONObject("result");

            //表示检测到的人脸数量，进行人脸登陆必须是一个人脸，所以如果由多个人脸，将判断检测人脸失败
            Integer faceNum = resultObject.getInt("face_num");
            return faceNum == 1?true:false;
        }else{
            return false;
        }
    }

    /**
     * {
     *   "result": {
     *     "face_token": "15e6795ebca1e7208f39620c665bf976",
     *     "user_list": [{
     *       "score": 100,
     *       "group_id": "itcast",
     *       "user_id": "1000",
     *       "user_info": ""
     *     }]
     *   },
     *   "log_id": 1565157535159,
     *   "error_msg": "SUCCESS",
     *   "cached": 0,
     *   "error_code": 0,
     *   "timestamp": 1588689706
     * }
     *
     *
     *  人脸查找：查找人脸库中最相似的人脸并返回数据
     *          处理：用户的匹配得分（score）大于80分，即可认为是同一个用户
     */
    public String faceSearch(String image) {
        JSONObject res = client.search(image, IMAGE_TYPE, groupId, options);
        if (res.has("error_code") && res.getInt("error_code") == 0) {
            JSONObject result = res.getJSONObject("result");

            //user_list  是一个数组，表示人脸库中与当前需要登陆的人脸相似度最高的几个图片，
            // 按照相似度从高到低排列，只取第一个相似度最高的
            JSONArray userList = result.getJSONArray("user_list");
            if (userList.length() > 0) {
                //数组第一个为相似度最高
                JSONObject user = userList.getJSONObject(0);
                double score = user.getDouble("score");

                //将相似度评分高于80分的，认为是同一个人，并获取到这个人的id
                if(score > 80) {
                    return user.getString("user_id");
                }
            }
        }
        return null;
    }



}

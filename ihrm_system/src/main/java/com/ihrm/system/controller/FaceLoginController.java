package com.ihrm.system.controller;

import com.baidu.aip.util.Base64Util;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.system.response.FaceLoginResult;
import com.ihrm.domain.system.response.QRCode;
import com.ihrm.system.service.FaceLoginService;
import com.ihrm.system.utils.BaiduAiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sys/faceLogin")
public class FaceLoginController {


    @Autowired
    private FaceLoginService faceLoginService;
    @Autowired
    private BaiduAiUtil baiduAiUtil;
    /*
      将人脸照片注册到人脸库的功能，是需要在添加用户上传头像的时候完成的，所以是在UserController中实现的
     */


    /**
     * 获取刷脸登录二维码
     */
    @RequestMapping(value = "/qrcode", method = RequestMethod.GET)
    public Result qrcode() throws Exception {

        QRCode qrCode = faceLoginService.getQRCode();
        return new Result(ResultCode.SUCCESS,qrCode);
    }

    /**
     * 检查二维码：登录页面轮询调用此方法，根据唯一标识code判断用户登录情况
     */
    @RequestMapping(value = "/qrcode/{code}", method = RequestMethod.GET)
    public Result qrcodeCeck(@PathVariable(name = "code") String code) throws Exception {
        FaceLoginResult result = faceLoginService.checkQRCode(code);
        return new Result(ResultCode.SUCCESS,result);
    }

    /**
     * 人脸登录：根据落地页随机拍摄的面部头像进行登录
     *          根据拍摄的图片调用百度云AI进行检索查找
     *
     *          扫面二维码的页面只需要知道登陆成功还是失败即可，不需要返回数据，登陆页面跳转是在系统页面跳转
     */
    @RequestMapping(value = "/{code}", method = RequestMethod.POST)
    public Result loginByFace(@PathVariable(name = "code") String code, @RequestParam(name = "file") MultipartFile attachment) throws Exception {
		
        //人脸登陆成功返回userid
        String userId = faceLoginService.loginByFace(code, attachment);
        //表示登陆成功
        if (userId != null) {
            return new Result(ResultCode.SUCCESS);
        } else {
            //表示登陆失败
            return new Result(ResultCode.FAIL);
        }
    }


    /**
     * 图像检测，判断图片中是否存在面部头像,接受的参数是一张图片
     */
    @RequestMapping(value = "/checkFace", method = RequestMethod.POST)
    public Result checkFace(@RequestParam(name = "file") MultipartFile attachment) throws Exception {

        String imgBase64 = Base64Util.encode(attachment.getBytes());
        //是否包含人脸
        Boolean aBoolean = baiduAiUtil.faceCheck(imgBase64);

        if (aBoolean) {
            return new Result(ResultCode.SUCCESS);
        } else {

            return new Result(ResultCode.FAIL);
        }
    }

}

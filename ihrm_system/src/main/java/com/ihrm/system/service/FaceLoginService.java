package com.ihrm.system.service;

import com.baidu.aip.util.Base64Util;
import com.ihrm.common.utils.IdWorker;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.FaceLoginResult;
import com.ihrm.domain.system.response.QRCode;
import com.ihrm.system.dao.UserDao;
import com.ihrm.system.utils.BaiduAiUtil;
import com.ihrm.system.utils.QRCodeUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.concurrent.TimeUnit;

@Service
public class FaceLoginService {

    @Value("${qr.url}")
    private String url;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private BaiduAiUtil baiduAiUtil;

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

	//创建二维码
    public QRCode getQRCode() throws Exception {
        //创建二维码的唯一标识
        String code = idWorker.nextId() + "";
        //二维码的内容，其实是一个url地址
        String content = url + "?code=" + code;
        //生成二维码文件
        String qrCode = qrCodeUtil.crateQRCode(content);
        //将二维码的状态存入redis，状态就是，这个二维码是否登陆，是否失败
        FaceLoginResult faceLoginResult = new FaceLoginResult("-1");
        //getCacheKey(code)存入redis的key，faceLoginResult 表示存入redis的value
        redisTemplate.boundValueOps(getCacheKey(code)).set(faceLoginResult,10, TimeUnit.MINUTES);

        System.out.println(qrCode);
        return new QRCode(code,qrCode);
    }

	//根据唯一标识，查询用户是否登录成功
    public FaceLoginResult checkQRCode(String code) {
        //得到二维码存入redis的唯一key
        String key = getCacheKey(code);
        //如果这个用户不在redis中，那么默认的FaceLoginResult中的属性state为0，登陆失败
        return (FaceLoginResult) redisTemplate.opsForValue().get(key);
    }

    @Autowired
    private UserDao userDao;

	//扫描二维码之后，使用拍摄照片进行登录
    //登陆成功之后返回用户id，登陆失败返回null
    public String loginByFace(String code, MultipartFile attachment) throws Exception {

        //调用百度云ai查询当前用户是否注册过,如果人脸库中有这个人，可以允许登陆
        String userId = baiduAiUtil.faceSearch(Base64Util.encode(attachment.getBytes()));
        //给定二维码状态初始值未登陆
        FaceLoginResult result = new FaceLoginResult("0");
        if (userId != null) {
            //模拟自己登陆，这个地方可以加一个判断，如果获取不到对象可以抛出异常进行处理，因为这个地方没有判断，加入人脸库中的头像id没有在这个数据库中就会出错
            User user = userDao.findById(userId).get();
            if (user != null) {
                //获取subject
                Subject subject = SecurityUtils.getSubject();
                //调用该方法会自动先去执行自定义的realm域
                subject.login(new UsernamePasswordToken(user.getMobile(), user.getPassword()));
                //获取token
                String token = subject.getSession().getId() + "";
                result = new FaceLoginResult("1",token,userId);

            }
        }
        //设置过期时间10分钟，redis中value的值是一个对象
        redisTemplate.boundValueOps(getCacheKey(code)).set(result,10,TimeUnit.MINUTES);
        return userId;

    }

	//构造缓存key,这个唯一key就是存入redis的key，value就是二维码的标识
    private String getCacheKey(String code) {
        return "qrcode_" + code;
    }
}

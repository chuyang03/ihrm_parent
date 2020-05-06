package com.ihrm.system.controller;


import com.ihrm.common.controller.BaseController;
import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.common.exception.CommonException;
import com.ihrm.common.utils.JwtUtils;
import com.ihrm.common.utils.PermissionConstants;
import com.ihrm.domain.system.Permission;
import com.ihrm.domain.system.Role;
import com.ihrm.domain.system.User;
import com.ihrm.domain.system.response.ProfileResult;
import com.ihrm.domain.system.response.UserResult;
import com.ihrm.system.feignclient.DepartmentFeignClient;
import com.ihrm.system.service.PermissionService;
import com.ihrm.system.service.UserService;
import io.jsonwebtoken.Claims;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//1.解决跨域
@CrossOrigin
@RestController
//2.设置父路径
@RequestMapping(value = "/sys")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private DepartmentFeignClient departmentFeignClient;

    //上传用户图片
    @RequestMapping(value = "/user/upload/{id}", method = RequestMethod.POST)
    public Result upload(@PathVariable(value = "id")String id,
                         @RequestParam(value = "file")MultipartFile file) throws IOException {

        //调用service保存图片，并返回一个图片访问地址，以便于上传图片的时候将图片
        //String imgUrl = userService.uploadImage(id, file);
        String imgUrl = userService.uploadImageQiniu(id, file);

        return new Result(ResultCode.SUCCESS, imgUrl);

    }

    /**
     *
     * 导入excel，批量添加用户
     */
    @RequestMapping(value = "/user/import", method = RequestMethod.POST)
    public Result importUser(@RequestParam(value = "file")MultipartFile file) throws IOException {

        Workbook wb = new XSSFWorkbook(file.getInputStream());

        Sheet sheet = wb.getSheetAt(0);
        List<User> list = new ArrayList<>();

        //获取单元格，行号需要小于等于索引
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            //获取表单中第几行
            Row row = sheet.getRow(rowNum);
            //每一行数据都是一个用户对象数据，所以数据封装在一个数组中
            Object[] values = new Object[row.getLastCellNum()];

            for (int cellNum = 1; cellNum < row.getLastCellNum(); cellNum++) {
                //获取第几列单元格
                Cell cell = row.getCell(cellNum);
                Object value = getCellValue(cell);

                values[cellNum] = value;
            }
            //将excel中每一行的数据封装导user对象中,采用构造函数的方式将数据注入到对象中
            User user = new User(values);
            list.add(user);

        }
        //批量保存用户
        userService.saveAll(list, companyId, companyName);

        return new Result(ResultCode.SUCCESS);
    }

    public static Object getCellValue(Cell cell){

        Object value = null;
        //获取单元格数据类型
        CellType cellType = cell.getCellType();
        switch (cellType){
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                //如果单元格数据格式是日期格式
                if (DateUtil.isCellDateFormatted(cell)){
                    value = cell.getDateCellValue();
                }else {
                    //单元格数据是数字格式
                    value = cell.getNumericCellValue();
                }
                break;
            case FORMULA:
                value = cell.getCellFormula();
                break;
            default:
                break;


        }

        return value;
    }

    /**
     * 测试Feign组件
     *
     * 调用系统微服务的/test的接口传递id，通过Feign调用部门微服务获取部门信息
     */
    @RequestMapping(value = "/test/{id}", method = RequestMethod.GET)
    public Result testFeign(@PathVariable(value = "id") String id){

        Result result = departmentFeignClient.findById(id);
        return result;
    }

    /**
     * 分配角色
     *
     */
    @RequestMapping(value = "/user/assignRoles", method = RequestMethod.PUT)
    public Result save(@RequestBody Map<String, Object> map){

        //用户id
        String userId = (String)map.get("id");
        List<String> roleIds = (List<String>)map.get("roleIds");
        userService.assignRoles(userId, roleIds);
        return new Result(ResultCode.SUCCESS);

    }

    //保存用户
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public Result save(@RequestBody User user){

        //设置保存的企业id
        //String companyId = "1";

        //这里的companyId使用的是父类的属性
        user.setCompanyId(companyId);
        userService.save(user);
        return new Result(ResultCode.SUCCESS);
    }

    //查询企业的用户列表
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Result findAll(int page, int size, @RequestParam Map map){ //page 表示当前页

        //获取企业id
        map.put("companyId", companyId);
        Page<User> pageUser = userService.findAll(map, page, size);
        //构造返回结果
        //pageUser.getTotalElements()  获得数据总条数
        //pageUser.getContent()  获取数据list集合
        PageResult pageResult = new PageResult(pageUser.getTotalElements(), pageUser.getContent());

        return new Result(ResultCode.SUCCESS, pageResult);
    }

    //根据id查询用户
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id){
        User user = userService.findById(id);

        //对用户分配角色的面板进行数据回显，也就是当给这个用户分配过一个角色之后，瞎猜给该用户分配角色的时候，那个分配过的角色已经被勾选了
        UserResult userResult = new UserResult(user);
        return new Result(ResultCode.SUCCESS, userResult);

    }

    //修改用户
    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody User user){

        //因为接收的dempartment是表单信息，不带有id，需要根据传入的部门id，将该属性设置到user中，然后根据这个id去数据库查询，修改更改的数据
        user.setId(id);
        userService.update(user);
        return new Result(ResultCode.SUCCESS);

    }

    //根据id删除用户,name = "API-USER-DELETE" 这个name属性用来对用户权限访问控制的时候与用户所具有的用户权限做比较
    @RequiresPermissions("API-USER-DELETE")  //这个注解表示具有这个权限才可以访问该接口
    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE, name = "API-USER-DELETE")
    public Result delete(@PathVariable(value = "id") String id){

        userService.deleteById(id);
        return new Result(ResultCode.SUCCESS);
    }

    public static void main(String[] args) {
        String password = new Md5Hash("123456", "13800000001", 3).toString();
        System.out.println(password);
    }

    /**
     * 用户登录
     *  1.通过service根据mobile(登陆账号)查询用户
     *  2.比较password
     *  3.生成jwt信息
     *
     */
    //使用shiro进行登陆验证
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Result login(@RequestBody Map<String, String> loginMap) {

        String mobile = loginMap.get("mobile");
        String password = loginMap.get("password");

        try {
            //1.构造登陆令牌
            //对密码进行加密
            password = new Md5Hash(password, mobile, 3).toString(); //第一个参数密码，2。登陆手机号，3。加密次数
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(mobile, password);
            //2.获取subject
            Subject subject = SecurityUtils.getSubject();
            //3.调用login方法登陆，进去自定义realm域进行认证
            subject.login(usernamePasswordToken);
            //4.获取sessionId
            String sessionId = (String) subject.getSession().getId();
            //登陆成功，返回sessionId

            return new Result(ResultCode.SUCCESS, sessionId);
        } catch (Exception e) {
            //登陆失败
            return new Result(ResultCode.MOBILEORPASSWORDERROR);
        }
    }

    //用户登录成功之后，获取用户信息
    //1.获取用户id
    //2.根据用户id查询用户
    //3.构建返回值对象
    //4.响应
    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public Result profile(HttpServletRequest request) throws Exception {

        //得到用户主体，通过subject获取安全数据
        Subject subject = SecurityUtils.getSubject();

        PrincipalCollection principals = subject.getPrincipals();
        ProfileResult result = (ProfileResult) principals.getPrimaryPrincipal();

        return new Result(ResultCode.SUCCESS, result);

    }


    //这是以token的方式进行登陆验证
//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public Result login(@RequestBody Map<String, String> loginMap){
//
//        String mobile = loginMap.get("mobile");
//        String password = loginMap.get("password");
//
//        User user = userService.findByMobile(mobile);
//
//        if (user == null || !user.getPassword().equals(password)){
//
//            //登陆失败
//            return new Result(ResultCode.MOBILEORPASSWORDERROR);
//        }else {
//
//            //登陆成功￿
//
//            /**
//             * 这里有一个需求：
//             *      用户登陆成功后返回的token中包含所有该用户能够访问的api权限，这样就可以控制该用户可以访问那些api接口了
//             *
//             */
//            StringBuilder sb = new StringBuilder();
//            //获取到所有的api权限
//            for (Role role : user.getRoles()) {
//                for (Permission permission : role.getPermissions()) {
//
//                    if (permission.getType() == PermissionConstants.PERMISSION_API){
//                        sb.append(permission.getCode()).append(",");
//                    }
//                }
//
//            }
//
//            Map<String, Object> map = new HashMap<>();
//            map.put("apis", sb.toString()); //可访问的api权限字符串
//            map.put("companyId", user.getCompanyId());
//            map.put("companyName", user.getCompanyName());
//
//            String token = jwtUtils.createJwt(user.getId(), user.getUsername(), map);
//
//            return new Result(ResultCode.SUCCESS, token);
//
//        }
//
//    }


    /**
     *
     * 前后端约定：前端请求微服务时需要添加头信息Authorization，内容为Bearer+空格+token
     *
     * 用户登录成功之后，获取用户信息
     *      1.获取用户id
     *      2.根据用户id查询用户
     *      3.构建返回值对象
     *      4.响应
     */
//    @RequestMapping(value = "/profile", method = RequestMethod.POST)
//    public Result profile(HttpServletRequest request) throws Exception {
////        //直接从请求域中获取claims
////        Claims claims = (Claims) request.getAttribute("user_claims");
//
//        //可以在BaseController中设置获取claims，那在这个子类中可以直接使用父类的成员变量
//
//        String userid = claims.getId();
//        User user = userService.findById(userid);
//
//        //需要根据用户判断用户级别，如果是saas管理员就是全部权限，
//        // 如果是企业管理员就是所有企业权限，如果是企业用户就是用户所具有的角色对应的权限
//        ProfileResult result = null;
//        if ("user".equals(user.getLevel())){
//            result = new ProfileResult(user);
//        }else {
//
//            Map map = new HashMap();
//            //如果登陆用户是企业管理员
//            if ("coAdmin".equals(user.getLevel())){
//                //这个enVisible的值等于1，表示查询用户所有的企业权限
//                map.put("enVisible", "1");
//            }
//            //根据map中的内容查询权限
//            List<Permission> list = permissionService.findAll(map);
//
//            result = new ProfileResult(user, list);
//        }
//
//
//        return new Result(ResultCode.SUCCESS, new ProfileResult(user));
//
//    }


}

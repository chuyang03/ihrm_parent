package com.ihrm.system.controller;

import com.ihrm.common.entity.PageResult;
import com.ihrm.common.entity.Result;
import com.ihrm.common.entity.ResultCode;
import com.ihrm.domain.system.Permission;
import com.ihrm.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//1.解决跨域
@CrossOrigin
@RestController
//2.设置父路径
@RequestMapping(value = "/sys")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    //保存权限信息，使用map接收传递过来的数据
    @RequestMapping(value = "/permission", method = RequestMethod.POST)
    public Result save(@RequestBody Map<String, Object> map) throws Exception {

        permissionService.save(map);
        return new Result(ResultCode.SUCCESS);
    }

    //查询权限列表
    @RequestMapping(value = "/permission", method = RequestMethod.GET)
    public Result findAll(@RequestParam Map map){ //page 表示当前页

        List<Permission> list = permissionService.findAll(map);
        return new Result(ResultCode.SUCCESS, list);
    }

    //根据id查询权限
    @RequestMapping(value = "/permission/{id}", method = RequestMethod.GET)
    public Result findById(@PathVariable(value = "id") String id) throws Exception {

        Map<String, Object> map = permissionService.findById(id);

        return new Result(ResultCode.SUCCESS, map);

    }

    //修改权限
    @RequestMapping(value = "/permission/{id}", method = RequestMethod.PUT)
    public Result update(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map) throws Exception {

        //因为接收的是表单信息，不带有id，需要根据传入的权限id，将该属性设置到permission中，然后根据这个id去数据库查询，修改更改的数据
        map.put("id", id);

        permissionService.update(map);

        return new Result(ResultCode.SUCCESS);

    }

    //根据id删除权限
    //1。首先删除权限
    //2。删除权限包含的资源，菜单，按钮，api
    @RequestMapping(value = "/permission/{id}", method = RequestMethod.DELETE)
    public Result delete(@PathVariable(value = "id") String id) throws Exception {

        permissionService.deleteById(id);

        return new Result(ResultCode.SUCCESS);
    }
}

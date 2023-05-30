package com.register.server.web.controller;

import com.google.common.collect.Lists;
import com.register.agent.req.RegisterAgentInfo;
import com.register.server.core.RegisterAgentFactory;
import com.register.server.web.domain.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("rv-manage")
public class ManageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("appNameSet", RegisterAgentFactory.getAppNameSet());
        return "register";
    }

    @ResponseBody
    @RequestMapping("getRegisterAgentInfo")
    public Result getRegisterAgentInfo(String appName,
                                       @RequestParam(required = false, defaultValue = "1") int page,
                                       @RequestParam(required = false, defaultValue = "10") int limit){
        Map<String, List<RegisterAgentInfo>> map = RegisterAgentFactory.getRegisterAgentListMap();
        List<RegisterAgentInfo> result;
        if(StringUtils.isNotBlank(appName)){
            result = map.get(appName);
        }else{
            result = map.values()
                    .stream().reduce((one, next) -> {
                        one.addAll(next);
                        return one;
                    }).orElseGet(ArrayList::new);
        }
        int startIndex = (page - 1) * limit;
        int endIndex = page * limit;
        if(result.size() <= startIndex){
            return Result.ok();
        }
        return Result.ok(Lists.newArrayList(result.subList(startIndex, Math.min(endIndex, result.size()))));
    }


    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("appNameSet", RegisterAgentFactory.getAppNameSet());
        return "home";
    }

    @ResponseBody
    @RequestMapping("getAppNameSet")
    public Set<String> getAppNameSet(){
        return RegisterAgentFactory.getAppNameSet();
    }

    @ResponseBody
    @RequestMapping("add")
    public Result add(String appName, String mappingPath, String serverPath){
        return Result.ok(jdbcTemplate.update(
                "INSERT INTO t_appname_path(app_name, mapping_path, server_path, create_time) VALUES (?, ?, ?, now())",
                appName, mappingPath, serverPath));
    }
    @ResponseBody
    @RequestMapping("delete")
    public Result delete(int id){
        return Result.ok(jdbcTemplate.update("DELETE FROM t_appname_path where id = ?", id));
    }
    @ResponseBody
    @RequestMapping("update")
    public Result update(int id, String appName, String mappingPath, String serverPath){
        return Result.ok(jdbcTemplate.update("UPDATE t_appname_path set app_name = ?, mapping_path = ?, server_path = ? where id = ?",
                appName, mappingPath, serverPath, id));
    }
    @ResponseBody
    @RequestMapping("select")
    public Result select(String appName, String mappingPath,
                         @RequestParam(required = false, defaultValue = "1") int page,
                         @RequestParam(required = false, defaultValue = "10") int limit){
        String sql = "SELECT * FROM t_appname_path WHERE 1=1";
        if(StringUtils.isNotBlank(appName)){
            sql += " and app_name = '" + appName + "'";
        }
        if(StringUtils.isNotBlank(mappingPath)){
            sql += " and mapping_path = '" + mappingPath + "'";
        }
        Integer count  = jdbcTemplate.queryForObject("select count(*) from (" + sql + ")", Integer.class);

        List<Map<String, Object>> res = new ArrayList<>();
        if(count != 0){
            int offset = (page -1) * limit;
            res = jdbcTemplate.queryForList(sql + " order by id desc limit " + offset + "," + limit );
        }
        return Result.okPage(count, res);
    }






}

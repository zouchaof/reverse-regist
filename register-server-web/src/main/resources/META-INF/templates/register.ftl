<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>注册信息</title>
    <link rel="stylesheet" href="/static/layui/css/layui.css">
</head>
<body>

<#include "left.ftl">

<div class="layui-container" style="margin-left: 210px;width: 80%">

    <div class="layui-progress" style="margin: 15px 0 30px;">
        <div class="layui-progress-bar" lay-percent="100%"></div>
    </div>

    <form class="layui-form search_filter2" action="">
        <div class="layui-form-item">
<#--            <div class="layui-inline" style="width: 310px">-->
<#--                <label class="layui-form-label" style="width: 70px">匹配路径:</label>-->
<#--                <div class="layui-input-inline" style="width: 200px;">-->
<#--                    <input type="text" id="mappingPath" lay-verify="mappingPath"-->
<#--                           class="layui-input" placeholder="匹配路径">-->
<#--                </div>-->
<#--            </div>-->

            <div class="layui-inline">
                <label class="layui-form-label" style="width: 70px">应用名称:</label>
                <div class="layui-input-inline" style="width: 170px;">
                    <select id="appName" lay-filter="appName">
                        <option value="">全部</option>
                        <#if appNameSet??> 
                        <#list appNameSet as appName>  
                        <option value="${appName}"> ${appName} </option>
                        </#list> 
                        </#if> 
                    </select>
                </div>
            </div>

            <div class="layui-inline">
                <button class="layui-btn" lay-filter="qry" lay-submit="">&nbsp;&nbsp;查询&nbsp;&nbsp;</button>
                <button type="reset" id="reset" class="layui-btn">&nbsp;&nbsp;重置&nbsp;&nbsp;</button>
            </div>
        </div>
    </form>


    <div class="layui-form">
        <table class="layui-hide" id="app-table" lay-filter="app-table"></table>
    </div>

    <blockquote class="layui-elem-quote" style="margin-top: 30px;">
        <div class="layui-text">
            <ul>
                <li>网络请求反转服务</li>
                <li>这是一个演示服务功能，请遵守国家相关法律法规</li>
            </ul>
        </div>
    </blockquote>
</div>

<!-- body 末尾处引入 layui -->
<script src="/static/layui/layui.js"></script>
<script type="text/html" id="barDemo">
    <a class="layui-btn layui-btn-primary layui-btn-xs layui-btn-disabled" lay-event="underLine">下线</a>
</script>

<script>
    layui.use(function(){
        var layer = layui.layer;
        var table = layui.table;
        var form = layui.form;
        var $ = layui.jquery;
        var element = layui.element;

        $("#register-tab").addClass("layui-this");

        table.render({
            elem: '#app-table',
            url: '/rv-manage/getRegisterAgentInfo', // 此处为静态模拟数据，实际使用时需换成真实接口
            cols: [[
                {type:'numbers', title: '序号', width:80, align:'center'},
                {field:'appName', title: '应用名称', width:100, align:'center'},
                {field:'lastRegisterTime', title: '最近注册时间', align:'center'},
                {field:'lastUseTime', title: '最近使用时间',align:'center'},
                {field:'registerIp', title: '注册节点ip', align:'center'},
                {title: '操作', width: 150, align:'center', toolbar: '#barDemo'}
            ]],
            // width:1078,
            loading: true,
            even: true,
            where: {
                appName: $("#appName").val(),
            },
            page: true
        });

        // 监听查询事件
        form.on('submit(qry)', function (data) {
            loadTable();
            return false;
        });

        function loadTable() {
            table.reload('app-table', {
                where: {
                    appName: $("#appName").val(),
                }
                , page: {
                    curr: 1
                }
            });
        }


        // 单元格工具事件
        table.on('tool(app-table)', function(obj){
            var data = obj.data; // 获得当前行数据
            var layEvent = obj.event; // 获得 lay-event 对应的值

            if(layEvent === 'underLine'){
                layer.msg('下线操作');
            }
        });

    });
</script>
</body>
</html>

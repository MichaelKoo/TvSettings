# TvSettings

logging for work

网络模块
1.初始化wifi监听类，并开始监听WiFi；
2.初始化PPPOE监听；
3.生成布局并显示；
    3.1 生成3个主项，WLAN，有线网络，PPPOE
    3.2 监听网络回调，刷新网络列表
    3.3 添加网络更改的监听会更合适(network change)
    3.4 显示布局并扫描无线网络，显示扫描到网络(记录网络类型)
类文件说明：
    Layout.java
        Node,LayoutRow,
        有刷新，有get，DrawableGetter,StringGetter
        LayoutGetter,获取布局,Header 有子项布局的总类
        Action,Status,Static，继承Node，

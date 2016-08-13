# TvSettings
<br/><br/>
logging for work
<br/><br/>
网络模块<br/>
1.初始化wifi监听类，并开始监听WiFi；<br/>
2.初始化PPPOE监听；<br/>
3.生成布局并显示；<br/>
    3.1 生成3个主项，WLAN，有线网络，PPPOE<br/>
    3.2 监听网络回调，刷新网络列表<br/>
    3.3 添加网络更改的监听会更合适(network change)<br/>
    3.4 显示布局并扫描无线网络，显示扫描到网络(记录网络类型)<br/>
类文件说明：<br/>
    Layout.java<br/><ul>
        <li>Node,LayoutRow,</li>
        <li>有刷新，有get，DrawableGetter,StringGetter </li>
        <li>LayoutGetter,获取布局,Header 有子项布局的总类 </li>
        <li>Action,Status,Static，继承Node，</li>
        </ul>
    SettingsLayoutFragment.java </br><ul>
    <li></li>
    </ul>
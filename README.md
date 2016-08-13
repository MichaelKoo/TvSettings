# TvSettings
<br/>
    <h2>ogging for work</h2>
<br/>
<h3>网络模块</h3>
&nbsp;&nbsp;1.初始化wifi监听类，并开始监听WiFi；<br/>
&nbsp;&nbsp;2.初始化PPPOE监听；<br/>
&nbsp;&nbsp;3.生成布局并显示；<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.1 生成3个主项，WLAN，有线网络，PPPOE<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.2 监听网络回调，刷新网络列表<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.3 添加网络更改的监听会更合适(network change)<br/>
&nbsp;&nbsp;&nbsp;&nbsp;3.4 显示布局并扫描无线网络，显示扫描到网络(记录网络类型)<br/>
<h3>类文件说明</h3>
    Layout.java<br/>
        <ul>
            <li>Node,LayoutRow,</li>
            <li>有刷新，有get，DrawableGetter,StringGetter </li>
            <li>LayoutGetter,获取布局,Header 有子项布局的总类 </li>
            <li>Action,Status,Static，继承Node，</li>
        </ul>
    SettingsLayoutFragment.java <br/>
    <ul>
        <li>onCreate()生成布局，显示页面</li>
        <li>createLayout()，此类没有显示，留给具体的子类实现，如：NetworkActivity.java</li>
        <li></li>
    </ul>
    WifiConnectionActivity.java <br/>
    <ul>
        <li>createIntent()生成回调的Intent</li>
        <li>onPageComplete() -->CONNECT-->RESULT_ETHERNET_CONNECTED</li>
        <li>WifiMultiPagedFormActivity.java -->addPage()</li>
    </ul>
    ConnectivityListener.java<br/>
    <p>
        在监听器中添加两个动作：<br/>
        ConnectivityManager.ACTION_DATA_ACTIVITY_CHANGE,<br/>
        ConnectivityManager.ACTION_TETHER_STATE_CHANGED
    </p>
    <h3>网络变化和页面更新</h3>
    <ul>
        <li>由NetworkActivity的onCreate()方法可知,Listener接口由NetworkActivity类实现了</li>
        <li>查看NetworkActivity的onConnectivityChange方法可知，具体的页面刷新是由布局Layout的具体实现类实现，刷新方法为实现类的refreshView()</li>
        <li>查看以太网的mEthernetConnectedDescription的实现，这里的实现只有两个方法，其中一个就是刷新布局方法refreshView()，其中方法实现里面有mConnectivityListener.getConnectivityStatus().isEthernetConnected() !=lastIsEthernetConnected的判断，到这里就切换到ConnectivityListener中去查看</li>
        <li>查看ConnectivityListener可知是updateConnectivityStatus判断并改变改变网络类型，而updateConnectivityStatus的实现则由ConnectivityManager.getActiveNetworkInfo()决定</li>
        <li>由updateConnectivityStatus可以决定后续的布局刷新，只要网络有改变，布局就会刷新</li>
    </ul>

<h3>网络热点</h3>
<p>
    宿主界面类：TetherSettingsActivity.java<br/>
    主要实现界面类：TetherSettings.java <br/>
    热点设置信息实现类：WifiApDialog.java
</p>

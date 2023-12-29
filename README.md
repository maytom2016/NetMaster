# NetMaster


[![LICENSE](https://img.shields.io/github/license/mashape/apistatus.svg?style=flat-square&label=LICENSE)](https://github.com/maytom2016/NetMaster/blob/master/LICENSE)
![GitHub Stars](https://img.shields.io/github/stars/maytom2016/NetMaster.svg?style=flat-square&label=Stars&logo=github)
![GitHub Forks](https://img.shields.io/github/forks/maytom2016/NetMaster.svg?style=flat-square&label=Forks&logo=github)

## 用途
该软件由May Tom制作并免费发布。
该软件利用低层iptables实现对APP内外网流量进行分离管理，
由于安卓系统的特性，部分功能需要请求root权限才可以正常使用。
本软件不需要联网权限，如果弹出联网权限提示选项直接拒绝即可。
软件不收集任何使用者隐私信息。虽然作者已经经过测试没有展示出对系统的破坏性，
但还是不排除会发生如数据丢失、系统崩溃、硬件损坏等后果。
如果因为使用该APP导致的任何损失软件作者不因此而承担责任，
继续使用软件意味着同意该协议。

## 用法
已ROOT的安卓手机直接安装，在提示授权时，授予相关权限就可以使用，底层使用的iptables，经本人测试无法在普通权限下增加任何iptables出站filter规则，所以必须ROOT权限下才可以使用。

## 参考
因为本人不会安卓开发，所以代码都是抄网上的，感谢以下开发者提供的代码和思路，因为第一次开发周期查阅资料较多，难免有所遗漏，若遗漏了您的贡献，请与我联系qwerty448qwe@163.com。
### iptables过滤APP流量想法来源
@B站 小狗爱喝冰汽水
https://b23.tv/VNF1G2J
### iptables规则底层逻辑
https://b23.tv/IcnvbmM
### OnbackPressed废弃替代方案
https://www.droidcon.com/2022/12/05/migrate-the-deprecated-onbackpressed-function-android-13/
### 文件读写参考
https://www.cnblogs.com/chen110xi/p/6629558.html
https://blog.csdn.net/wanghonghongkx/article/details/132837613
https://blog.51cto.com/u_16175442/7044685
### progressbar加载动画参考
https://blog.csdn.net/qq_26500807/article/details/109116623
###  progressbar显示操作参考
https://wenku.csdn.net/answer/c00a82ff6cae48e18ecd86a0b36be174#
### recycleview子itemview的checkbox改变选定状态事件绑定参考
https://cloud.tencent.com/developer/ask/sof/131209
### 拼接字符串参考
https://blog.csdn.net/Tefuir111/article/details/123686854
### 函数多返回值参考
[https://juejin.cn/s/kotlin 多个返回值]
### root权限参考
https://blog.csdn.net/Allen_ww/article/details/90701794
### 变量非空参考
https://blog.csdn.net/guangdeshishe/article/details/100933456
### 执行命令回显参考
https://www.mianshigee.com/tutorial/EasyKotlin/spilt.7.15.kotlin%20%E6%96%87%E4%BB%B6io%E6%93%8D%E4%BD%9C%E4%B8%8E%E5%A4%9A%E7%BA%BF%E7%A8%8B-%E7%AC%AC15%E7%AB%A0%20kotlin%20%E6%96%87%E4%BB%B6io%E6%93%8D%E4%BD%9C%E4%B8%8E%E5%A4%9A%E7%BA%BF%E7%A8%8B.md
### AlertDialog 样式参考
https://www.jianshu.com/p/a94755fc7978
https://www.cnblogs.com/xunevermore/p/16058450.html
### Recylerview刷新参考
https://github.com/ananananzhuo-blog/RecycleViewSample

https://blog.51cto.com/u_16213367/7338847
### searchview焦点相关
https://www.jianshu.com/p/7c0eab3f4480
###  APK打包
https://www.cnblogs.com/YZFHKMS-X/p/12045664.html
https://blog.csdn.net/mimica247706624/article/details/88086250
###  退出back二次
https://blog.csdn.net/weixin_33739627/article/details/91951965
https://blog.csdn.net/wenyingzhi/article/details/97396917
### Action打包APK
https://blog.csdn.net/ZZL23333/article/details/115798615

https://juejin.cn/post/7234418257759846455

https://juejin.cn/post/6908427616298991629

https://github.com/marketplace/actions/automated-build-android-app-with-github-action

https://github.com/marketplace/actions/sign-android-release-2

### 开机启动方案：依赖magisk

https://github.com/topjohnwu/Magisk/blob/master/docs/guides.md#boot-scripts

在/data/adb/service.d放置执行脚本

使用echo写入脚本，并且授权运行权限。

### 安卓请求权限
请求权限代码直接抄的
https://github.com/2dust/v2rayNG
他用的库是RxPermissions

### 开发早期安卓开始入门参考
https://b23.tv/1l37uSC

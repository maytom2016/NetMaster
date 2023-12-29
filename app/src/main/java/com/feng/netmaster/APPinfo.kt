package com.feng.netmaster
import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable


class AppInfo {
    var uid = 0
    var label: String? = null //应用名称
    var package_name: String? = null //应用包名
    var icon: Drawable? = null //应用icon
    var connectlocalnet : Boolean =true  //连接本地网络
    var connectinternet : Boolean =true  //连接互联网
    var classify : toolbarchecked = toolbarchecked.USER
//    fun AppInfo() {
//
//    }

    fun getUidKotlin(): Int {
        return uid
    }

    fun setUidKotlin(uid: Int) {
        this.uid = uid
    }

    fun getLabelKotlin(): String? {
        return label
    }

    fun setLabelKotlin(label: String?) {
        this.label = label
    }

    fun getPackage_nameKotlin(): String? {
        return package_name
    }

    fun setPackage_nameKotlin(package_name: String?) {
        this.package_name = package_name
    }

    fun getIconKotlin(): Drawable? {
        return icon
    }
    @JvmName("setIconKotlin")
    fun setIconKotlin(icon: Drawable?) {
        this.icon = icon
    }
    fun setlocalnet(isallow : Boolean)
    {
        connectlocalnet=isallow
    }
    fun setinternet(isallow : Boolean)
    {
        connectinternet=isallow
    }
    fun setclassify(enum:toolbarchecked)
    {
        classify=enum
    }
    fun tobesave(): Appinfo_Save
    {
        var save=Appinfo_Save(this.uid,
            this.label,
            this.package_name,
            this.connectlocalnet,
            this.connectinternet,
            this.classify)
        return save
    }
    fun tobeself(save:Appinfo_Save,drawable: Drawable):AppInfo
    {
//        val self:AppInfo=AppInfo()
        this.uid=save.uid
        this.label=save.label
        this.package_name=save.package_name
        this.icon=drawable
        this.connectlocalnet=save.connectlocalnet
        this.connectinternet=save.connectinternet
        this.classify=save.classify
        return this
    }
}
@Serializable
data class Appinfo_Save(
    var uid:Int,
    var label: String?,  //应用名称
    var package_name: String?, //应用包名
    var connectlocalnet : Boolean, //连接本地网络
    var connectinternet : Boolean,  //连接互联网
    var classify : toolbarchecked,
)
@Serializable
data class  Ruleresult(val strlist:List<String>,val dropstrlist:List<String>)



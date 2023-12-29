package com.feng.netmaster


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings.Global.getString
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.encodeToString
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

class menutoolbarvm : ViewModel() {
    //工具栏筛选用户APP按钮状态
    var userappcheck : Boolean =true
    //工具栏筛选系统APP按钮状态
    var sysappcheck : Boolean =true
    //appfragment与mainactivity的筛选功能需要通信
    var currentlist : List<AppInfo> = mutableListOf<AppInfo>()
    //使用查找功能时存储的查询总表
    var currentlist_all:List<AppInfo> = mutableListOf<AppInfo>()
    //appfragment与firstfragment的列表项通信的限制APP总表
    var limitedlist : List<AppInfo>  =  mutableListOf<AppInfo>()
    //appfragment与firstfragment的列表项通信的限制APP显示表
    var currentlist2 : MutableList<AppInfo>  =  mutableListOf<AppInfo>()
    //firstfragment多选列表，用于存储被选中项是哪些，与currentlist2显示表同步对应
    var cur2sellist:  MutableList<AppInfo>   = mutableListOf<AppInfo>()
    //adapter的选项，必须公开，以便activity使用
    var mutaselcted: MutableSet<Int>  =mutableSetOf<Int>()
    //APP启动后首次加载标志
    var firstload=true
    fun containitem( list:List<AppInfo>,app: AppInfo):Boolean
    {
        for (i in list)
        {
            if(i.package_name==app.package_name) {
                return true
            }
        }
        return false
    }
    fun tobesave(list:List<AppInfo> ):List<Appinfo_Save>
    {
       var savelist= mutableListOf<Appinfo_Save>()
        for (p in list)
        {
            savelist+=p.tobesave()
        }
        return savelist
    }
   fun updatechecked(package_name:String,bool:Boolean,type:net)
   {
       val finditem=limitedlist.find{it.package_name== package_name}
       if(type.name==net.LOCAL.name){
           if(finditem!=null)finditem.connectlocalnet=bool
       }
       else if(type.name==net.INTERNET.name) {
           if(finditem!=null)finditem.connectinternet = bool
       }
   }
    fun getres():Ruleresult
    {
        val rulmag=rulemanager()
        val reallimitedlist=rulmag.fliteappsneedrules(limitedlist)
        val res=rulmag.generaterules(reallimitedlist)
        return res
    }

}

enum class toolbarchecked {
    USER,SYSTEM
}
enum class net{
    LOCAL,INTERNET
}

//更新recycleview的DiffUtil.Callback
class DifferCallback(val oldDatas: List<AppInfo>, val newDatas: List<AppInfo>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDatas.size
    override fun getNewListSize(): Int = newDatas.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldDatas[oldItemPosition].package_name == newDatas[newItemPosition].package_name)
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        val a=oldDatas[oldItemPosition]
//        val b=newDatas[newItemPosition]
//        if(!(oldDatas[oldItemPosition].package_name == newDatas[newItemPosition].package_name))
//            return false
//        if(!(oldDatas[oldItemPosition].label == newDatas[newItemPosition].label))
//            return false
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }

}
class FileManage {
    fun savefile(context: Context, FILENAME: String, filestr: String) {

        var fos: FileOutputStream? = null
        try {
            fos = context.openFileOutput(FILENAME, AppCompatActivity.MODE_PRIVATE)
            fos.write(filestr.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun savecfgfile(context: Context,listjson: String)
    {
        savefile(context,"appinfo.json",listjson)
    }
    fun loadfile(context: Context, FILENAME: String):String {
        var listjson:String? = null
        try {
            var fis = context.openFileInput(FILENAME)
            val b = ByteArray(fis.available())
            fis.read(b)
            listjson = String(b)
            fis.close()
            return listjson

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "error"
    }
    fun loadcfg(context: Context):String
    {
        return loadfile(context,"appinfo.json")
    }
    fun jsonlisttolist(ctx:Context,listjsonstring:String):List<AppInfo>
    {
        val savelist:List<Appinfo_Save> = Json.decodeFromString(listjsonstring)
        val packageManager = ctx.packageManager
        val list = packageManager.getInstalledPackages(0)
        var nt: PackageInfo? =null
        var applist:List<AppInfo> = mutableListOf<AppInfo>()
        var app:AppInfo=AppInfo()
        for (p in savelist) {
            nt = list.find { it.applicationInfo.packageName == p.package_name }
            if (nt != null) {
                app = app.tobeself(p, nt.applicationInfo.loadIcon(packageManager))
                applist += app
                app = AppInfo()
            }
        }
        return applist
    }
    fun saverule(context: Context,ruleresult:Ruleresult){
        val json = Json.encodeToString(ruleresult)
        savefile(context,"ruleresult.json",json)
    }
    fun readrule(context: Context):Ruleresult
    {
        var ruleresult=Ruleresult(listOf(),listOf())
        if(fileexistinerstorage(context,"ruleresult.json")) {
        val rulestr=loadfile(context,"ruleresult.json")
            ruleresult= Json.decodeFromString(rulestr)
        }
        return ruleresult
    }
    fun fileexistinerstorage(context: Context,filename:String):Boolean
    {
        val mct=context as MainActivity
        val str=mct.fileList().find{it==filename}
        return str!=null
    }
    fun deleterules(context: Context)
    {
        context.deleteFile("ruleresult.json")
    }
    fun checkpathlegal(path:String?):Boolean{
        if (path != null) {
            if (path.contains(".json"))return true
        }
        return false
    }
}

class rulemanager {

    fun fliteappsneedrules(list:List<AppInfo>):List<AppInfo>{
        var res:List<AppInfo> = mutableListOf<AppInfo>()
        for (p in list)
        {
            if(p.connectinternet && p.connectlocalnet) {}
            else
            {
                res+=p
            }
        }
        return res
    }
    fun generaterules(list:List<AppInfo>):Ruleresult
    {
        var str=mutableListOf<String>()
        var dropstr=mutableListOf<String>()
        val strfd="iptables -I "
        val dropstrfd="iptables -D "
        for(p in list)
        {
            if(p.connectinternet==true&&p.connectlocalnet==false)
            {
//                str+= strfd+generateinternetstr(p.uid,"ACCEPT")
                str+= strfd+generatelocalstr(p.uid,"DROP")
//                dropstr+=dropstrfd+generateinternetstr(p.uid,"ACCEPT")
                dropstr+= dropstrfd+generatelocalstr(p.uid,"DROP")
            }
            if(p.connectinternet==false&&p.connectlocalnet==true)
            {
                str+= strfd+generateinternetstr(p.uid,"DROP")
                str+= strfd+generatelocalstr(p.uid,"ACCEPT")
                dropstr+= dropstrfd+generateinternetstr(p.uid,"DROP")
                dropstr+= dropstrfd+generatelocalstr(p.uid,"ACCEPT")

            }
            if(p.connectinternet==false&&p.connectlocalnet==false)
            {
                str+= strfd+generateinternetstr(p.uid,"DROP")
                dropstr+= dropstrfd+generateinternetstr(p.uid,"DROP")
            }
        }
        return Ruleresult(str,dropstr)
    }
    fun generatelocalstr(uid:Int,action:String):String
    {
        return "OUTPUT -d 192.168.0.0/16,172.16.0.0/12,10.0.0.0/8 -m owner --uid-owner=$uid -j $action"
    }
    fun generateinternetstr(uid:Int,action:String):String
    {
        return "OUTPUT  -m owner --uid-owner=$uid -j $action"
    }



}


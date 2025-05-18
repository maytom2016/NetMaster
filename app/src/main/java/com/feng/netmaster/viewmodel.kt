package com.feng.netmaster


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

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
    var addlimitedlist: List<AppInfo>  =  mutableListOf<AppInfo>()
    //准备要增加到limitedlist的表项，用户可能取消，所以需要此表进行缓存。
    var currentlist2 : MutableList<AppInfo>  =  mutableListOf<AppInfo>()
    //firstfragment多选列表，用于存储被选中项是哪些，与currentlist2显示表同步对应
    var cur2sellist:  MutableList<AppInfo>   = mutableListOf<AppInfo>()
    //firstfragmen当前选中项序号
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

        val reallimitedlist=rulemanager.fliteappsneedrules(limitedlist)
        val res=rulemanager.generaterules(reallimitedlist)
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
            val savelist:List<Appinfo_Save>
            // 先验证JSON格式有效性
            try{
                savelist=Json.decodeFromString(listjsonstring)
            } catch (e: JSONException) {
                Log.e("JSON", "Invalid JSON: ${e.message}")
                return emptyList() // 返回空列表或抛出异常
            } catch (e: Exception) {
                Log.e("JSON", "Unexpected error", e)
                return emptyList()
            }
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

object rulemanager {

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

object RootCommandExecutor {
    private var hasRootPermission = false
    private var rootProcess: Process? = null

    fun getRootPermission(): Boolean {
        if (!hasRootPermission) {
            try {
                val builder = ProcessBuilder("su")
                rootProcess = builder.start()
                val outputStream = rootProcess!!.outputStream
                outputStream.write("id\n".toByteArray())
                outputStream.flush()
//                outputStream.close()
                val inputStream = rootProcess!!.inputStream
                val buffer = ByteArray(1024)
                val length = inputStream.read(buffer)
                val result = String(buffer, 0, length)
                println("testlog:$result")
                println("process is:$rootProcess")
                if (result.contains("uid=0(root)")) {
                    hasRootPermission = true
                    println("testlog:here is~~")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return hasRootPermission
    }

    fun executeCommand(command: String): String {
        var result = ""
        if (getRootPermission()) {
            if (rootProcess == null) {
//                try {
                    val builder = ProcessBuilder("su")
                    rootProcess = builder.start()
//                } catch (e: java.io.IOException) {
//                    e.printStackTrace()
//                }
            }
            if(rootProcess !=null) {
                println("process is:$rootProcess")
                val outputStream = rootProcess!!.outputStream
                outputStream.write("$command\n".toByteArray())
                outputStream.flush()
//                outputStream.close()
                val inputStream = rootProcess!!.inputStream
                val buffer = ByteArray(1024)
                val length = inputStream.read(buffer)
                result = String(buffer, 0, length)
            }
        } else {
            result = "无法获取root权限，无法执行命令"
        }
        return result
    }
    fun executeCommands(commands: List<String>): List<String> {
        var result = mutableListOf<String>()
        if (getRootPermission()) {
            if (rootProcess == null) {
                val builder = ProcessBuilder("su")
                rootProcess = builder.start()
            }
            if (rootProcess != null) {
                println("process is: $rootProcess")
                val outputStream = rootProcess!!.outputStream
                val reader = BufferedReader(InputStreamReader(rootProcess!!.inputStream))
                for (command in commands) {
                    outputStream.write("$command\n".toByteArray())
                }
                outputStream.flush()

                var line: String
                while(reader.ready()) {
                    line=reader.readLine()
                    result.add(line)
                }
            }
        } else {
            result.add("无法获取root权限，无法执行命令")
        }
        return result
    }
    suspend fun executeCommands_suspd(commands: List<String>,channel: Channel<String>): String {
        var result = ""
        if (getRootPermission()) {
            if (rootProcess == null) {
                val builder = ProcessBuilder("su")
                rootProcess = builder.start()
            }
            if (rootProcess != null) {
                println("process is: $rootProcess")
                val outputStream = rootProcess!!.outputStream
                for (command in commands) {
                    outputStream.write("$command\n".toByteArray())
                }
                outputStream.flush()
                // 不再需要手动关闭outputStream，因为不会影响BufferedReader的读取
                var boollineread=true
                val reader = BufferedReader(InputStreamReader(rootProcess!!.inputStream))
                var line: String?
                do{
                    line=reader.readLine()
                    result += line
                    if(line.contains("/system/bin/sh"))boollineread=false
                    line?.let { channel.trySend(it) }
                    //println("line is$line")
//                    println("boollineread is $boollineread")
                }while (reader.ready()or boollineread)

//                reader.useLines { lines ->
//                    lines.forEach {
//                    println("line is" + it)
//                     }
//                }


                channel.close()
                println("channel is closed")

            }
        } else {
            result = "无法获取root权限，无法执行命令"
        }

        return result
    }
    fun getRootProcess(): Process {
        if (rootProcess == null) {
            try {
                val builder = ProcessBuilder("su")
                rootProcess = builder.start()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }
        }
        return rootProcess!!
    }

    fun destroyRootProcess() {
        rootProcess?.let { process ->
            try {
                val reader: BufferedReader=BufferedReader(InputStreamReader(rootProcess?.inputStream))
                reader.close()
                val inputStream: InputStream? = process.inputStream
                inputStream?.close()
                val outputStream: OutputStream? = process.outputStream
                outputStream?.close()
                val errorStream: InputStream? = process.errorStream
                errorStream?.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
            process.destroy()
            rootProcess = null
        }
    }
}

object FileUtils {
    private const val REQUEST_CODE_DIR = 1001

    fun pickOutputDirectory(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        activity.startActivityForResult(intent, REQUEST_CODE_DIR)
    }

    fun handleActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Uri? {
        if (requestCode == REQUEST_CODE_DIR && resultCode == Activity.RESULT_OK) {
            return data?.data?.also { uri ->
                activity.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
        return null
    }

    fun saveToDirectory(context: Context, dirUri: Uri, fileName: String, content: String): Boolean {
        return try {
            val dir = DocumentFile.fromTreeUri(context, dirUri)
            dir?.createFile("application/json", fileName)?.uri?.let { fileUri ->
                context.contentResolver.openOutputStream(fileUri)?.use {
                    it.write(content.toByteArray())
                    true
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
object MarkDownManager{
    //转换数组为markdown的表格，第一项数组为表头，其余为列表项
    fun convertArrayToMarkdownTable(data: ArrayList<Array<String>>): String {
        val markdownTable = StringBuilder()

        // 遍历二维数组
        for (row in data) {
            markdownTable.append("| ")
            for (cell in row) {
                markdownTable.append(cell).append(" | ")
            }
            markdownTable.append("\n")
        }

        // 添加表头分隔符
        if (data.isNotEmpty()) {
            val headerSeparator = data[0].joinToString(separator = " | ", prefix = "| ", postfix = " |\n") { ":---:" }
            markdownTable.insert(markdownTable.indexOf("\n") + 1, headerSeparator)
        }

        return markdownTable.toString()
    }
}
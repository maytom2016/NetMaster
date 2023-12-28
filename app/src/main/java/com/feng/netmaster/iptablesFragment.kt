package com.feng.netmaster

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.feng.netmaster.databinding.FragmentFirstBinding
import com.feng.netmaster.databinding.FragmentIptablesBinding
import evalBash
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import runCommand
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader


class iptablesFragment : Fragment() {
    private var _binding: FragmentIptablesBinding? = null
    private val binding get() = _binding!!
    private var output:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        output+="--------------------------------------------------\n"
//        context?.let {
//            output+=execrootshell("iptables -L OUTPUT --line-numbers", it)
//            output+="开机启动配置rules.sh\n"
//            output+="--------------------------------------------------\n"
//            output+=execrootshell("ls /data/adb/service.d/rules.sh -l", it)
//            output+=execrootshell("cat /data/adb/service.d/rules.sh", it)
//        }
        val ctx = context as MainActivity
        ctx.hidefabimexforiptablesFragment()
        val roottest = kotlin.runCatching { ctx.getroot() }
//        ctx.toast(roottest.getOrThrow().message)
        if (roottest.isSuccess) {
            val cmds = listOf(
                "iptables -L OUTPUT --line-numbers\n",
                "cd /data/adb/service.d/",
                "ls rules.sh -l",
                "cat rules.sh"
            )
            val resstr = execmultiplerootshell(cmds)
            if(resstr.size>0){
                output += "Iptables OUTPUT规则表(不包括默认规则)\n"
                output = output.addline()
                var i = 0
                while (!resstr[i].contains(("oem_out"))) {
                    output = output + "\n" + resstr[i]
                    i++
                }
                output += "\n\n开机启动配置rules.sh\n"
                output = output.addline()
                for (p in i + 4..resstr.size - 1) {
                    output = output + "\n" + resstr[p]
                }
            }
            else ctx.toast( getString(R.string.no_root2))

        } else
            ctx.toast(getString(R.string.no_root))
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIptablesBinding.inflate(inflater, container, false)
        binding.textView.text=output
        return binding.root
    }
//    fun String.execute(): Process {
//        val runtime = Runtime.getRuntime()
//        return runtime.exec(this)
//    }
//    fun Process.text(): String {
//        var output = ""
//        //    输出 Shell 执行的结果
//        val isr = InputStreamReader(this.inputStream)
//        val reader = BufferedReader(isr)
//        var line: String? = ""
////        while (line != null) {
//            line = reader.readLine()
//            output += line + "\n"
////        }
//        return output
//    }
    fun String.addline():String
    {
        return "$this\n--------------------------------------------------\n"
    }
    fun execrootshell(cmd:String,context: Context): String {
    val ctx =context as MainActivity
    var res=""
    val roottest = kotlin.runCatching {ctx.getroot()}
        if (roottest.isSuccess) {
            val process = Runtime.getRuntime().exec("su -c $cmd")
            process.waitFor()
            val isr = InputStreamReader(process.inputStream)
            val reader = BufferedReader(isr)
            res += reader.readText() + "\n"
            reader.close()
            isr.close()
        }
         else
            res = "error"
    return  res
    }
    fun execmultiplerootshell(cmds:List<String>):List<String>
    {
        val process = Runtime.getRuntime().exec("su")
        val isr = InputStreamReader(process.inputStream)
        val reader = BufferedReader(isr)
        val os = DataOutputStream(process.outputStream)
        for (cmd in cmds) {
            os.writeBytes("$cmd\n")
        }
        os.writeBytes("exit\n")
        os.flush()
        process.waitFor()
        var k:List<String>
        k=reader.useLines { it.toList()}
        reader.close()
        isr.close()
        os.close()
        process.destroy()
        return k

    }

}
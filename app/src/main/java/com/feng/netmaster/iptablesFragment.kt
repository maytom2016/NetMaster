package com.feng.netmaster

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.feng.netmaster.databinding.FragmentIptablesBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.regex.Pattern


class iptablesFragment : Fragment() {
    private var _binding: FragmentIptablesBinding? = null
    private val binding get() = _binding!!
    private var finnaloutput: SpannableStringBuilder =SpannableStringBuilder("")
    private val textrecycle = Channel<String>(Channel.UNLIMITED)
    private val menutoolbarvm: menutoolbarvm by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIptablesBinding.inflate(inflater, container, false)
        val ctx = context as MainActivity
        ctx.hidefabimexforiptablesFragment()
        if (RootCommandExecutor.getRootPermission()) {
            val cmds = listOf(
                "iptables -L OUTPUT --line-numbers\n",
                "cd /data/adb/service.d/",
                "ls rules.sh -l",
                "cat rules.sh"
            )

            val product=lifecycleScope.launch {
                RootCommandExecutor.executeCommands_suspd(cmds,textrecycle)
            }
            val costomer=lifecycleScope.launch {
                finnaloutput.addmessage("Iptables OUTPUT规则表(不包括默认规则)\n")
                finnaloutput.addline()
                for(value in textrecycle) {
                    if (!value.contains(("oem_out"))) {
                        var uid =finduidfromtext(value)
                        if (uid!=0 && uid!=null){
                            var formatspace:String
                            val appname=getappnamefromlimitedlist(uid)
                            if (appname?.containsChinese() == true) {
                                val num = 10 - ((appname?.length) ?: 10)
                                formatspace = "\u3000".repeat(num)
                            }
                            else
                            {
                                val num = 10 - ((appname?.length) ?: 10)
                                formatspace = "\u3000\u3000".repeat(num)
//                                formatspace = "  ".repeat(num)+" ".repeat(3)
                            }
                            val spannableString = SpannableStringBuilder(appname + formatspace + value)
                            val endIndex =appname?.length?: 0
                            val foregroundColorSpan = ForegroundColorSpan(Color.GREEN)
                            spannableString.setSpan(foregroundColorSpan,
                                0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            finnaloutput.append( "\n")
                            finnaloutput.append(spannableString)
                        }
//                        println(value)
                    }
                    if(value.contains("firewall")){
                        val str="\n\n开机启动配置rules.sh\n"
                        finnaloutput.addmessage(str)
                        finnaloutput.addline()
                    }
                    if(value.contains("destination")){
                        val formatspace="应用中文名"+"\u3000".repeat(5)
                        finnaloutput.append(SpannableStringBuilder(formatspace+value))
                    }
                }
            }
            MainScope().launch {
                joinAll(costomer,product)
                binding.textView.text=finnaloutput
            }


        } else
            ctx.toast(getString(R.string.no_root))

        return binding.root
    }
    fun String.containsChinese(): Boolean {
        return this.any { it.code in 0x4E00..0x9FA5 }
    }
    fun SpannableStringBuilder.addmessage(messages:String)
    {
        val spannableString=SpannableStringBuilder(messages)
        val foregroundColorSpan = ForegroundColorSpan(Color.rgb(255,0,255))
        spannableString.setSpan(foregroundColorSpan,
            0, messages.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.append(spannableString)
    }
    fun SpannableStringBuilder.addline()
    {
        val str="\n----------------------------------------------------------------------------------------------------------------------------\n"
        val spannableString=SpannableStringBuilder(str)
        val foregroundColorSpan = ForegroundColorSpan(Color.WHITE)
        spannableString.setSpan(foregroundColorSpan,
            0, str.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.append(spannableString)
    }

    fun getappnamefromlimitedlist(uid: Int):String?
    {
        val app=menutoolbarvm.limitedlist.find({ it.uid == uid})
        println("匹配到的appname: ${app?.label}")
        return app?.label
    }
    fun finduidfromtext(text:String):Int?
    {
        var pattern = Pattern.compile("--uid-owner=(\\d+)")
        var matcher = pattern.matcher(text)
        if (matcher.find()) {
            val uid = matcher.group(1)?.toInt()
            println("匹配到的UID: $uid")
            return uid
        }
        else
        {
            pattern = Pattern.compile("u0_a(\\d+)")
            matcher = pattern.matcher(text)
            if (matcher.find()) {
                val uid = matcher.group(1)?.toInt()
                if (uid != null) {
                    println("匹配到的UID: $uid")
                    return uid+10000
                }
            }
        }
        return 0
    }

}
package com.feng.netmaster

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.feng.netmaster.databinding.FragmentIptablesBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.math.abs


class iptablesFragment : Fragment() {
    private var _binding: FragmentIptablesBinding? = null
    private val binding get() = _binding!!
    private var appnamelist : SpannableStringBuilder =SpannableStringBuilder("")
    private var finnaloutput: SpannableStringBuilder =SpannableStringBuilder("")
    private val textrecycle = Channel<String>(Channel.UNLIMITED)
    private val menutoolbarvm: menutoolbarvm by activityViewModels()
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
fun isFontMonospace(textView: TextView): Boolean {
    val paint = textView.paint
    val widths = floatArrayOf(0f, 0f)

    // 测量窄字符(i)和宽字符(W)的宽度
    paint.getTextWidths("iW", widths)

    // 如果两个字符宽度差异小于10%，则认为是等宽字体
    return abs(widths[0] - widths[1]) < paint.textSize * 0.1f
}
    fun printFontInfo(textView: TextView) {
        val typeface = textView.typeface
        val metrics = textView.paint.fontMetrics

        Log.d("FontDebug", """
        Typeface: $typeface
        Font Metrics:
        - Ascent: ${metrics.ascent}
        - Descent: ${metrics.descent}
        - Leading: ${metrics.leading}
        Char Widths:
        - 'i': ${textView.paint.measureText("i")}px
        - 'W': ${textView.paint.measureText("W")}px
        - '中': ${textView.paint.measureText("中")}px
    """.trimIndent())
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        Log.d("FontCheck", isFontMonospace(binding.textView).toString())
////        Log.d("FontCheck", printFontInfo(binding.textView).toString())
//        printFontInfo(binding.textView)
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
                "cd "+ctx.getString(R.string.magisk_delta_boot),
                "ls rules.sh -l",
                "cat rules.sh"
            )

            val product=lifecycleScope.launch {
                RootCommandExecutor.executeCommands_suspd(cmds,textrecycle)
            }
            val costomer=lifecycleScope.launch {
                finnaloutput.addmessage("Iptables OUTPUT 规则表(不包括默认规则)")
                finnaloutput.addline(50)
                for(value in textrecycle) {
                    if (!value.contains(("oem_out"))) {
                        val uid =finduidfromtext(value)
                        if (uid!=0 && uid!=null) {
                            var appname= getappnamefromlimitedlist(uid).toString()
                            if (appname.length>=6 && appname.containsChinese())
                            {
                                appname=appname.substring(0, 5)+"..."
                            }
                            if (appname.length>=6 && !appname.containsChinese())
                            {
                                appname=appname.substring(0, 7)+"..."
                            }
                            appnamelist.addmessage(appname,Color.GREEN)
                            appnamelist.append( "\n")
                            val str=value.replace("            owner UID match","").replace("         ","")+"  "
                            finnaloutput.append(str)
                            finnaloutput.append( "\n")
                        }
                    }
                    if(value.contains("firewall")){
                        appnamelist.addline(8)
                        appnamelist.addmessage("应用名")
                        appnamelist.addline(8)

                        finnaloutput.addline(50)
                        val str="开机启动配置rules.sh"
                        finnaloutput.addmessage(str)
                        finnaloutput.addline(50)
                    }
                    if(value.contains("destination")){
                        appnamelist.addline(8)
                        appnamelist.addmessage("应用名\n")
                        finnaloutput.addmessage(value.replace("         ","")+"\n")
                    }

                }
            }
            MainScope().launch {
                joinAll(costomer,product)
                binding.textView.text= appnamelist
                binding.textView1.text=finnaloutput
            }


        } else
            ctx.toast(getString(R.string.no_root))

        return binding.root
    }

    fun String.toDBC():String {
        val c = this.toCharArray()
        for (i in c.indices) {
            if (c[i].code == 12288) {
                c[i] = 32.toChar()
                continue
            }
            if (c[i].code > 65280 && c[i].code < 65375) c[i] = (c[i].code - 65248).toChar()
        }
        return String(c)
    }
    fun String.containsChinese(): Boolean {
        return this.any { it.code in 0x4E00..0x9FA5 }
    }
    fun SpannableStringBuilder.addmessage(message: String, color: Int = Color.rgb(255, 0, 255))  // 默认颜色为 (255, 0, 255)
    {
        val spannableString = SpannableStringBuilder(message)
        val foregroundColorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(foregroundColorSpan, 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.append(spannableString)
    }
    fun SpannableStringBuilder.addline(width: Int = 80,  color: Int = Color.WHITE) {
        val line = "\n" + "-".repeat(width) + "\n"
        val spannableString = SpannableStringBuilder(line)
        val foregroundColorSpan = ForegroundColorSpan(color)
        spannableString.setSpan(foregroundColorSpan, 0, line.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        this.append(spannableString)
    }
    fun getappnamefromlimitedlist(uid: Int):String?
    {
        val app=menutoolbarvm.limitedlist.find{ it.uid == uid}
        println("匹配到的appname: ${app?.label}")
        return app?.label
    }
    fun finduidfromtext(text:String):Int?
    {
        var pattern = Pattern.compile("--uid-owner=(\\d+)")
        var matcher = pattern.matcher(text)
        if (matcher.find()) {
            val uid = matcher.group(1)?.toInt()
//            println("匹配到的UID: $uid")
            return uid
        }
        else
        {
            pattern = Pattern.compile("u0_a(\\d+)")
            matcher = pattern.matcher(text)
            if (matcher.find()) {
                val uid = matcher.group(1)?.toInt()
                if (uid != null) {
//                    println("匹配到的UID: $uid")
                    return uid+10000
                }
            }
        }
        return 0
    }

}
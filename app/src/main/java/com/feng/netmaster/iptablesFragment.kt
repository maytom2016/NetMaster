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
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.feng.netmaster.databinding.FragmentIptablesBinding
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.RenderProps
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.tag.SimpleTagHandler
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.recycler.MarkwonAdapter
import io.noties.markwon.recycler.table.TableEntry
import io.noties.markwon.recycler.table.TableEntryPlugin
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.node.FencedCodeBlock
import java.util.regex.Pattern
import kotlin.math.abs


class iptablesFragment : Fragment() {
    private var _binding: FragmentIptablesBinding? = null
    private val binding get() = _binding!!
//    private var finnaloutput: SpannableStringBuilder =SpannableStringBuilder("")
    private var finnaloutput:String=""
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
    private fun setupMarkDown(mdstr:String) {
        // 清除现有视图（如果有）
        val ctx=context as MainActivity
        var md = """
         this is first line!
         
        |Name    | Occupation   |
        |:-------:|:-----------:|
        | Alice  |  EngineerEngineerThis is a table of people and their occupations   |
        | Bob    |Designer  |
        | Charlie |Developer  |

        this is end line!
    """.trimIndent()
        val tableTheme = TableTheme.Builder()
            .tableBorderWidth(2)  // 改为1像素边框
            .tableCellPadding(8)   // 减少内边距
            .tableBorderColor(Color.WHITE)
            .build()
        val markwon = Markwon.builder(ctx)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(TableEntryPlugin.create(tableTheme))
            .usePlugin(HtmlPlugin.create{ plugin ->
                //此处对font字体颜色进行处理
                plugin.addHandler(object : SimpleTagHandler() {
                    override fun supportedTags(): MutableList<String> = mutableListOf("font")
                    override fun getSpans(
                        configuration: MarkwonConfiguration,
                        renderProps: RenderProps,
                        tag: HtmlTag
                    ): Any? {
                        val color = tag.attributes()["color"] ?: return null
                        return ForegroundColorSpan(color.toColorInt())
                    }
                })
            })
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(ctx))
            .usePlugin(object : AbstractMarkwonPlugin() {
                //                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
//                    builder.imageDestinationProcessor(GithubImageDestinationProcessor())

                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    builder.on(FencedCodeBlock::class.java) { visitor, fencedCodeBlock ->
                        // 我们不会在这里应用代码跨度，因为我们的自定义视图会绘制背景并应用等宽字体
                        // 注意对字面量的trim操作（因为代码末尾会有换行符）
                        val code = visitor.configuration()
                            .syntaxHighlight()
                            .highlight(fencedCodeBlock.info, fencedCodeBlock.literal.trim())
                        visitor.builder().append(code)
                    }
                }
            })
            .build()
        /*
        layout说明，view_table_entry_cell是表格内的textview，可以直接控制表格内文字颜色。
        adapter_node_table_block表格总布局，使用tablelayout对齐每一个表格块
        adapter_node外部普通块的textview
         */
        val adapter = MarkwonAdapter.builderTextViewIsRoot(R.layout.adapter_node)
//            .include(FencedCodeBlock::class.java, SimpleEntry.create(R.layout.adapter_node_code_block, R.id.text_view))
            .include(TableBlock::class.java, TableEntry.create { builder ->
                builder
                    .tableLayout(R.layout.adapter_node_table_block, R.id.table_layout)
                    .textLayoutIsRoot(R.layout.view_table_entry_cell)
            })
            .build()

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        adapter.setMarkdown(markwon, mdstr)
        adapter.notifyDataSetChanged()
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
//                finnaloutput.addmessage("Iptables OUTPUT 规则表(不包括默认规则)")
                finnaloutput+=makecolorfont( "Iptables OUTPUT 规则表(不包括默认规则)",Color.CYAN)
                finnaloutput+="\n\n"
                var tableData =  arrayListOf<Array<String>>()
                tableData.add(arrayOf<String>("应用名", "num  target     prot opt source     destination   uid        &"))
                for (value in textrecycle) {
                    if (!value.contains(("oem_out"))) {
                        val uid = finduidfromtext(value)
                        if (uid != 0 && uid != null) {
                            var appname= getappnamefromlimitedlist(uid).toString()
                            val iptablesrule=value.replace("            owner UID match","").replace("         ","")
                            tableData.add(arrayOf<String>(appname,iptablesrule))
                        }
                    }
                    if(value.contains("firewall")){
                        finnaloutput+=MarkDownManager.convertArrayToMarkdownTable(tableData)+"\n\n"
                        tableData.clear()
                        finnaloutput+=makecolorfont("开机启动配置rules.sh",Color.CYAN)+"\n\n"
                    }
                }
                finnaloutput+=MarkDownManager.convertArrayToMarkdownTable(tableData)
            }
            MainScope().launch {
                joinAll(costomer,product)
//                binding.textView.text= appnamelist
//                binding.textView1.text=finnaloutput
                var tableData =  arrayListOf(
                    arrayOf<String>("Age", "Gender", "City"),
                    arrayOf<String>("25", "Male", "1    DROP       all  --  anywhere             anywhere             owner UID match u0_a168"),
                    arrayOf<String>("30", "Female", "London"),
                    arrayOf<String>("<font color='#00FF00'>应用名</font>", "Male", "Tokyo")
                )
                tableData.add(arrayOf("40", "Male", "Paris"))
//                finnaloutput.addline(50)
//                finnaloutput.addmessage()
                val md=MarkDownManager.convertArrayToMarkdownTable(tableData)+"\n<font color='#00FF00'>应用名</font>"
                setupMarkDown(finnaloutput)
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
    /**
     * @param message 要添加的消息内容
     * @param color 文字颜色，默认为品红色 (RGB: 255, 0, 255)
     * @return 合并后的字符串，包含带颜色标签的消息
     */
    fun makecolorfont(message: String, color: Int = Color.rgb(255, 0, 255)):String {
        // 将颜色值转换为6位十六进制格式（去除alpha通道）
        val hexColor = String.format("#%06X", 0xFFFFFF and color)
        // 包装消息并追加到原始字符串
        return  "<font color='$hexColor'>$message</font>"
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
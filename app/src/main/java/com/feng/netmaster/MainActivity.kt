package com.feng.netmaster

//import com.google.android.material.snackbar.Snackbar
//import com.feng.netmaster.R




import android.Manifest
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.feng.netmaster.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.tbruyelle.rxpermissions3.RxPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import kotlin.properties.Delegates



class MainActivity : AppCompatActivity(){
    private  var time_last:Long=1
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var menutoolbarvm: menutoolbarvm
    private lateinit var ruleresult:Ruleresult
//    private lateinit var thefirsttimebootapp:Boolean

//private val openDocumentLauncher = registerForActivityResult(
//    ActivityResultContracts.OpenDocument()
//) { uri: Uri? ->
//    // 处理所选择的文件的URI
//    if (uri!=null){
//        println(uri)
//    }
//}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
//侧边导航栏
        val drawerLayout: DrawerLayout = binding.drawerLayout

        val navView: NavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph,drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


//toolbar的显示应用数据传递到viewmodel类中
        menutoolbarvm=menutoolbarvm()
        menutoolbarvm = ViewModelProvider(this)[menutoolbarvm::class.java]
        binding.fab.setOnClickListener {
            val controller = findNavController(this@MainActivity, R.id.nav_host_fragment_content_main)
            controller.setGraph(R.navigation.nav_graph)
            controller.navigate(R.id.action_FirstFragment_to_AppFragment)
            hidefabimex()
//            onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
        }
        binding.searchView.setOnQueryTextListener(/* listener = */ object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(p0: String?): Boolean {
            searchbytext(p0)
//            toast("test!")
            return false
        }
        override fun onQueryTextChange(p0: String?): Boolean {
            searchbytext(p0)
            return false
        }


    })
    binding.searchView.setOnQueryTextFocusChangeListener { view, b ->
        println("abc"+b.toString())
        if(!b)
        {
            setsearchviewvisiable(View.GONE)
        }

    }

        val fma=FileManage()
        if(!(fma.fileexistinerstorage(this,"firstboot.conf")))
        {
            showlicencedialog()
        }


    }
    fun searchbytext(p0: String?){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val app = navHostFragment.childFragmentManager.fragments[0]
        if(app.javaClass.name=="com.feng.netmaster.appFragment") {
            val fragment = app as appFragment
            CoroutineScope(Dispatchers.Main).async {
//                    if (p0 != null) {
                async { p0?.let { fragment.refleshfliterview(it) } }
//                    }
            }
        }
        else if(app.javaClass.name=="com.feng.netmaster.FirstFragment")
        {
            val fragment = app as FirstFragment
            CoroutineScope(Dispatchers.Main).async{
                async { p0?.let { fragment.refleshfliterview(it) } }
            }

        }
    }
    fun Letsearchviewlosefocus()
    {
        val id: Int = binding.searchView.getContext().getResources()
            .getIdentifier("android:id/search_src_text", null, null)
        val mSearchSrcTextView = binding.searchView.findViewById(id) as EditText
        mSearchSrcTextView.clearFocus()
    }
    fun limitedsearchgap(second:Int):Boolean
    {
        var time2=System.currentTimeMillis()/1000
        toast(time2.toString())
        if (time_last== 1.toLong())
        {
            time_last=time2
            return false
        }
        else{
            if((time2.minus(time_last)) >second)
            {
                time_last=time2
                return true
            }
        }
        return false
    }
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        return super.onCreateView(name, context, attrs)
    }
    fun showlicencedialog(){
        val builder = AlertDialog.Builder(this,R.style.BlurDialogTheme)
        builder.setIcon(R.drawable.ic_licence)
            .setTitle("APP使用协议")
            .setPositiveButton("我同意，继续", DialogInterface.OnClickListener { dialog, id ->
                val fma=FileManage()
                fma.savefile(this,"firstboot.conf","false")
            })
            .setNeutralButton("我不同意", DialogInterface.OnClickListener { dialog, id ->
                finishAffinity()
            })
            .setMessage(getString(R.string.about_licence))
            .setCancelable(false)
            .create()
            .show()
    }
    fun updateappchecked(checked: Boolean,enum :toolbarchecked) {

        when(enum)
        {
            toolbarchecked.USER ->menutoolbarvm.userappcheck=checked
            else-> menutoolbarvm.sysappcheck=checked
        }
    }
    fun updatelist()
    {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val app=navHostFragment.childFragmentManager.fragments[0]
        if(app.javaClass.name=="com.feng.netmaster.appFragment"){
            val fragment = app as appFragment
            CoroutineScope(Dispatchers.Main).async{
                fragment.refleshrecycleview(menutoolbarvm.currentlist)
            }
        }
        else if(app.javaClass.name=="com.feng.netmaster.FirstFragment")
        {
            val fragment = app as FirstFragment
            CoroutineScope(Dispatchers.Main).async{
                fragment.refleshrecycleview(menutoolbarvm.currentlist2)
            }
            //fragment.refleshrecycleview( menutoolbarvm.currentlist2)
        }
    }
    fun deleteitem()
    {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val app=navHostFragment.childFragmentManager.fragments[0]
        if(app.javaClass.name=="com.feng.netmaster.appFragment"){
            //val fragment = app as appFragment

        }
        else if(app.javaClass.name=="com.feng.netmaster.FirstFragment")
        {
            val fragment = app as FirstFragment
            fragment.deletemutiitem(menutoolbarvm.currentlist2)
        }
    }

//    fun setapplyvisiable(boolean: Boolean){
//        binding.toolbar.menu.findItem(R.id.action_apply).isVisible = boolean
//    }
//    fun setdeletevisiable(boolean: Boolean){
//        binding.toolbar.menu.findItem(R.id.action_delete).isVisible = boolean
//    }
    fun setvisiablebyid(id:Int,boolean: Boolean)
    {
        binding.toolbar.menu.findItem(id).isVisible = boolean
    }
    fun setsearchviewvisiable(viewvisable:Int)
    {
        binding.searchView.visibility=viewvisable
        //防止隐藏后仍然对用户操作进行反应
        if(viewvisable==GONE)
        binding.searchView.isEnabled=false
        else
        {
            binding.searchView.isEnabled=true
        }
    }
    fun loadappfromcfg():List<AppInfo> {
        val path="appinfo.json"
       // val file = File(this.getFilesDir().getPath()+"/"+path)
        if (FileManage().fileexistinerstorage(this,path)) {
            val fma = FileManage()
            val listjsonstring = fma.loadcfg(this)
            return fma.jsonlisttolist(this,listjsonstring)
        }
        else
        {
            return mutableListOf<AppInfo>()
        }
    }
    //
    fun mergelimitedList() {
        val newItems = menutoolbarvm.addlimitedlist.filter { it!in menutoolbarvm.limitedlist }
        val newLimitedList = (menutoolbarvm.limitedlist + newItems).toMutableList() as List<AppInfo>
        menutoolbarvm.limitedlist = newLimitedList
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //val submenu: SubMenu = menu.getItem(1).subMenu(1)//(1, Menu.NONE, 2, "New Form")

        menuInflater.inflate(R.menu.menu_main, menu)
        //在转换视图的时候保持筛选值
        binding.toolbar.menu.getItem(0).subMenu!!.getItem(0).isChecked=menutoolbarvm.userappcheck
        binding.toolbar.menu.getItem(0).subMenu!!.getItem(1).isChecked=menutoolbarvm.sysappcheck
//        val checkBoxMenuItem = menu.findItem(R.id.show_user_app)
//        checkBoxMenuItem.setChecked(true)

//        menuInflater.inflate(R.menu.menu_fliter, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
//            R.id.action_settings -> Toast.makeText(this, "设置", Toast.LENGTH_LONG).show()
//            R.id.action_fliter -> Toast.makeText(this, "筛选", Toast.LENGTH_LONG).show()
            R.id.show_user_app -> {
                when (item.isChecked) {
                    true -> item.isChecked = false
                    false -> item.isChecked = true

                }
                updateappchecked(item.isChecked, toolbarchecked.USER)
                updatelist()
            }

            R.id.show_system_app -> {
//                Toast.makeText(this, "显示系统应用", Toast.LENGTH_LONG).show()
                when (item.isChecked) {
                    true -> item.isChecked = false
                    false -> item.isChecked = true

                }
                updateappchecked(item.isChecked, toolbarchecked.SYSTEM)
                updatelist()
//                var fragment= supportFragmentManager.findFragmentById(R.id.appFragment) as appFragment
//                if (fragment==null)
//                fragment = appFragment()
//                var fragment = appFragment().getinstance()


            }

            R.id.action_exit -> finishAffinity()

            R.id.action_apply -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigateUp(appBarConfiguration)
                showfabimex()
                mergelimitedList()
                //添加后自动保存一次设置，避免没有手动保存退出后被杀后台丢失
                saveconf()
                setvisiablebyid(R.id.action_apply,false)
            }

            R.id.action_delete -> {
                deleteitem()
                setvisiablebyid(R.id.action_delete,false)
            }

            R.id.action_save ->
            {
                saveconf()
            }
            R.id.action_import ->
            {
                ImportConfigFile()
            }
            R.id.action_import_ext->
            {
                //openDocumentLauncher.launch(arrayOf("*/*"))
            }
            R.id.action_export ->
            {
                ExportConfigFile()
            }
            R.id.action_copy ->
            {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                val app=navHostFragment.childFragmentManager.fragments[0]
                if(app.javaClass.name=="com.feng.netmaster.SecondFragment"){
                    val fragment = app as SecondFragment
                    val text=fragment.gettextviewtext()
                    val clipmag: ClipboardManager = (this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                    val lipData =ClipData.newPlainText("Label", text)
                    clipmag.setPrimaryClip(lipData)

                }
            }
            R.id.apply_all_rules ->
            {
                //val a=RootCommandExecutor.getRootPermission()
//                var text=RootCommandExecutor.executeCommand("date")
//                toast("root permitsiion is"+text)

                val filerules=requireruleresult("file")
                val listrules=requireruleresult("list")
                println("filerules$listrules")
                println("listrules$listrules")
                if (filerules.strlist==listrules.strlist) {
                    //不需要应用新规则
                    ruleresult=filerules
                    toast("没有规则需要应用")
                }
                else {
//                    val roottest = kotlin.runCatching { getroot() }
//                    if (roottest.isSuccess) {

                    if (RootCommandExecutor.getRootPermission()) {
                        ruleresult = listrules
                        //需要应用新规则
//                        val notestr = "规则已还原"
                        //擦除先前规则
                        var res=true
                        //还原规则只有一种情况下会影响后面应用新规则，那就是还原失败，res会有错误文字，还原规则为空等情况不影响应用新规则。
                        if (!filerules.dropstrlist.isEmpty()) {
                            res = manage_rules(filerules.dropstrlist, "")
                        }
                        //应用新规则
                        if (res) {
                            val notestr2 = "增加规则成功"
                            manage_rules(listrules.strlist, notestr2)
                            //开机启动新规则
                            addrulestoboot(listrules.strlist)
                            //保存新规则
                            saveconf()
                            FileManage().saverule(this, ruleresult)
                        }
                    }
                    //隐藏ROOT权限或者未ROOT跳转此处
                    else toast("没有获取到root权限，无法还原规则")
                }

            }
            R.id.clear_all_rules->
            {
//                val roottest = kotlin.runCatching { getroot() }
//                if (roottest.isSuccess) {
                if (RootCommandExecutor.getRootPermission()){
                    ruleresult = requireruleresult("file")
                    if (ruleresult.dropstrlist.isNotEmpty()) {
                        val notestr = "规则已还原"
                        val res = manage_rules(ruleresult.dropstrlist, notestr)
                        if(res) {
                            FileManage().deleterules(this)
                            removerulesfromboot()
                        }
                    } else {
                        toast("没有规则需要还原")
                    }
                }
                //隐藏ROOT权限或者未ROOT跳转此处
                else toast("没有获取到root权限，无法还原规则")
            }
            R.id.search_view->
            {
                binding.searchView.visibility=View.VISIBLE
            }
        }
        return false
    }
    @Throws
    fun getroot():Exception{
        val p=Runtime.getRuntime().exec("su")
        return NullPointerException()
    }
    fun addrulestoboot(rulestr: List<String>) {
//        val path = getString(R.string.magisk_boot_general) + "/rules.sh"
        val path2 = getString(R.string.magisk_delta_boot) + "/rules.sh"
//        val roottest = kotlin.runCatching { getroot() }
//        if (roottest.isSuccess) {
        if (RootCommandExecutor.getRootPermission()) {
//            var process = Runtime.getRuntime().exec("su")
            val process=RootCommandExecutor.getRootProcess()
            if(process!=null) {
                val os = DataOutputStream(process.outputStream)
                val shell = getString(R.string.shell)
//                os.writeBytes("echo  \"$shell\">$path\n")
                os.writeBytes("echo  \"$shell\">$path2\n")
                for (p in rulestr) {
//                    os.writeBytes("echo  $p>>$path\n")
                    os.writeBytes("echo  $p>>$path2\n")
                }
//                os.writeBytes("chmod +x $path\n")
                os.writeBytes("chmod +x $path2\n")
//                os.writeBytes("exit\n")
                os.flush()
//                process.waitFor()
//                os.close()
            }
        }
    }
    fun removerulesfromboot()
    {
        val path = getString(R.string.magisk_boot_general) + "/rules.sh"
//        val roottest = kotlin.runCatching { getroot() }
//        if (roottest.isSuccess) {
//            var process = Runtime.getRuntime().exec("su")
        if (RootCommandExecutor.getRootPermission()) {
            val process=RootCommandExecutor.getRootProcess()
            val os = DataOutputStream(process.outputStream)
            val shell = getString(R.string.shell)
            os.writeBytes("echo \"\">$path\n")
//            os.writeBytes("exit\n")
            os.flush()
//            process.waitFor()
//            os.close()
        }
    }
    private fun getMimeType(filePath: String): String? {
        val ext = MimeTypeMap.getFileExtensionFromUrl(filePath)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }
    fun saveconf()
    {
        val fma=FileManage()
//        if(menutoolbarvm.limitedlist.size>0) {
            val list=menutoolbarvm.tobesave(menutoolbarvm.limitedlist)
            val json = Json.encodeToString(list)
            fma.savecfgfile(this,json)
            toast("保存成功")
//        }
//        else{
//            toast("没有需要保存的规则")
//        }
    }
    fun requireruleresult(where:String):Ruleresult
    {
        var rules:Ruleresult
        if(where=="file") {
            rules = FileManage().readrule(this)
        }
        else
        {
            rules=menutoolbarvm.getres()
        }
        return rules

    }
    fun manage_rules(managelist:List<String>,notestr:String):Boolean
    {

//        var process = Runtime.getRuntime().exec("su")

//        val os = DataOutputStream(process.outputStream)
//        val reader = BufferedReader(InputStreamReader(process.inputStream))
//        for (p in managelist) {
//            os.writeBytes(p + "\n")
//        }
//若用户拒绝root权限，该条命令不会输出字符，可以防止因为拒绝ROOT权限导致判断错误。
//        os.writeBytes("date\n")
//        os.writeBytes("exit\n")
//        os.flush()
//        process.waitFor()
//        val text=reader.useLines { it.toList()}
//                println("text size is"+text.size.toString())
//        os.close()
//        process.destroy()
//
        val isroot=RootCommandExecutor.getRootPermission()
        if(isroot && managelist.size>0)
        {
            var text=RootCommandExecutor.executeCommands(managelist)

            if(text.size>0)
            {
                //执行失败时有错误提示
                return false
            }
            if (notestr.length!=0) {
                toast(notestr)
            }
        }
        //拒绝ROOT权限会跳转此处
        else {
            toast("没有获取到root权限，无法还原规则")
        }
        //执行成功时不会有回显示
        return true
    }
    fun ExportConfigFile(){
        val path = Environment.getExternalStorageDirectory().absolutePath+"/Download/appinfo.json"
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        val RX= RxPermissions(this).request(permission).subscribe {
            if (it) {
                try {
                    val myfile = File(path)
                    val list=menutoolbarvm.tobesave(menutoolbarvm.limitedlist)
                    val json = Json.encodeToString(list)
                    myfile.writeText(json)
                    toast("配置文件已经导出在"+path)
//                    toast(getMimeType(path))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        if(RX.toString().contains("0"))
        {
            toast(getString(R.string.toast_permission_denied))
        }
        RX.dispose()

    }

    private fun ImportConfigFile() {
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/json"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val downloadsDirUri = Uri.parse(
            "${DocumentsContract.EXTRA_INITIAL_URI.toString()}/primary:${Environment.DIRECTORY_DOWNLOADS}"
        )
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,downloadsDirUri)
        chooseFile.launch(Intent.createChooser(intent, getString(R.string.action_import)))
    }
    val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        val uri = it.data?.data
        val fma=FileManage()
        if (it.resultCode == RESULT_OK && uri != null && fma.checkpathlegal(uri.path)) {
            val listjsonstring=readContentFromUri(uri)
            if(listjsonstring!="error") {
                //清空当前列表项
                menutoolbarvm.currentlist2.clear()
                //清空选中列表项
                menutoolbarvm.cur2sellist.clear()
                //清空选中项序号
                menutoolbarvm.mutaselcted.clear()
                //新导入项默认不选中，无须显示删除按钮
                setvisiablebyid(R.id.action_delete,false)

                menutoolbarvm.limitedlist = fma.jsonlisttolist(this, listjsonstring)
                updatelist()
            }
            else
            {
                toast("没有外部存储权限读取配置文件")
            }

        }
        else
        {
            toast("无效配置文件")
        }
    }
    private fun readContentFromUri(uri: Uri):String {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        var res="error"
        val RX=RxPermissions(this).request(permission).subscribe {
                if (it) {
                    try {
                        contentResolver.openInputStream(uri).use { input ->
                           res=input?.bufferedReader()?.readText().toString()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    toast(getString(R.string.toast_permission_denied))
                }
            }
        RX.dispose()
        return res
    }
    fun toast(meg:String?)
    {
        Toast.makeText(this, meg, Toast.LENGTH_LONG).show()
    }
    fun showfabimex()
    {
        binding.fab.show()
        setvisiablebyid(R.id.action_import,true)
        setvisiablebyid(R.id.action_export,true)
        setvisiablebyid(R.id.action_save,true)
        setvisiablebyid(R.id.action_fliter,true)
        setvisiablebyid(R.id.action_rulemanager,true)
        setvisiablebyid(R.id.action_copy,false)
        setvisiablebyid(R.id.action_apply,false)

    }
    fun hidefabimex()
    {
        binding.fab.hide()
        setvisiablebyid(R.id.action_copy,false)
        setvisiablebyid(R.id.action_import,false)
        setvisiablebyid(R.id.action_export,false)
        setvisiablebyid(R.id.action_rulemanager,false)
        setvisiablebyid(R.id.action_delete,false)
    }
    fun hidefabimexforiptablesFragment()
    {
        binding.fab.hide()
        setvisiablebyid(R.id.action_copy,false)
        setvisiablebyid(R.id.action_import,false)
        setvisiablebyid(R.id.action_export,false)
        setvisiablebyid(R.id.action_rulemanager,false)
        setvisiablebyid(R.id.action_fliter,false)
        setvisiablebyid(R.id.action_delete,false)
        setvisiablebyid(R.id.action_apply,false)
        setsearchviewvisiable(View.GONE)
    }
    fun hidefabimexfragment2()
    {
        binding.fab.hide()
        setvisiablebyid(R.id.action_copy,true)
        setvisiablebyid(R.id.action_rulemanager,true)
        setvisiablebyid(R.id.action_import,false)
        setvisiablebyid(R.id.action_export,false)
        setvisiablebyid(R.id.action_fliter,false)
        setvisiablebyid(R.id.action_delete,false)
        setvisiablebyid(R.id.action_apply,false)
        setsearchviewvisiable(View.GONE)
    }


    //返回导航
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        showfabimex()
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
//    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//
//            //
//        }
//    }
    val name: String by Delegates.observable("初始化值") { pre, old, new ->
        println("以前的值$old 新设置的值$new")
    }
    private var backPressedTime by Delegates.observable(0L) { pre, old, new ->
        // 2次的时间间隔小于2秒就退出了
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val app = navHostFragment.childFragmentManager.fragments[0]
        if (app.javaClass.name == "com.feng.netmaster.FirstFragment") {
            if (new - old < 2000) {
                finish()
            } else {

            }
            toast("再按返回鍵退出")
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigateUp(appBarConfiguration)
            showfabimex()
            backPressedTime = System.currentTimeMillis()
        }
        return false

    }

    override fun onDestroy() {
        super.onDestroy()
        println("MainActivity"+"onDestroy: Activity is being destroyed")
        // 同样可以进行资源释放等操作
        RootCommandExecutor.destroyRootProcess()
    }
//    override fun onBackPressed() {
//
//        super.onBackPressed()
//    }

}



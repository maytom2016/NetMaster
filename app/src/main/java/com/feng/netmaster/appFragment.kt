package com.feng.netmaster

//import com.feng.netmaster.R

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feng.netmaster.databinding.FragmentAppBinding
import com.feng.netmaster.databinding.ItemViewLinearBinding
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


private lateinit var appi :List<AppInfo>
//private lateinit var appi_cache :List<AppInfo>

class appFragment : Fragment(R.layout.fragment_app) {
    private var _binding: FragmentAppBinding? = null
    private val binding get() = _binding!!

    private val menutoolbarvm: menutoolbarvm by activityViewModels()
    //private lateinit var deferred : Deferred<List<AppInfo>>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAppBinding.inflate(layoutInflater)
        //显示加载动画
        binding.progressBar.visibility = View.VISIBLE
//        progressDialog = CustomProgressDialog(context, R.style.progressDialog)
//        progressDialog.setCanceledOnTouchOutside(false)
//        progressDialog.show()
        //让I0线程去处理数据加载
        CoroutineScope(Dispatchers.Main).async{
            //deferred = async { Loadappicache() }
            async {
                Loadappi()
            }
        }
        //空白表丢给UI线程
            appi= ArrayList<AppInfo>()
        return binding.root
    }
    fun filterappbyname(searchname:String):List<AppInfo>
    {
        var fliterlist=mutableListOf<AppInfo>()
        if(menutoolbarvm.currentlist_all.isNotEmpty()) {
            for (p in menutoolbarvm.currentlist_all) {
                val a=p.package_name.toString().indexOf(searchname,0,true)>-1
                val b=p.label.toString().indexOf(searchname,0,true)>-1
                if (a||b)
                {
                    fliterlist.add(p)
                }

            }
            return fliterlist
        }
        return menutoolbarvm.currentlist
    }
    fun getAllAppInfo(ctx: Context, isFilterSystem: Boolean,isFilterUserApp : Boolean): ArrayList<AppInfo> {
        val appBeanList: ArrayList<AppInfo> = ArrayList<AppInfo>()
        var bean: AppInfo?
        val packageManager = ctx.packageManager
//        val list = packageManager.getInstalledPackages(0)
        val list = packageManager.getInstalledPackages(0)
        for (p in list) {
            bean = AppInfo()
            bean.setIconKotlin(p.applicationInfo.loadIcon(packageManager))
            bean.setLabelKotlin(packageManager.getApplicationLabel(p.applicationInfo).toString())
            bean.setPackage_nameKotlin(p.applicationInfo.packageName)
            bean.setUidKotlin(p.applicationInfo.uid)
            val flags = p.applicationInfo.flags

            if (flags and ApplicationInfo.FLAG_SYSTEM !== 0 && !isFilterSystem) {

                //           bean.setSystem(true)
            }
            else if(flags and ApplicationInfo.FLAG_SYSTEM == 0 && !isFilterUserApp){

            }
            else if(menutoolbarvm.containitem(menutoolbarvm.limitedlist,bean)){
            }
            else {
                if(flags and ApplicationInfo.FLAG_SYSTEM == 0) {bean.setclassify(toolbarchecked.USER)}
                else{bean.setclassify(toolbarchecked.SYSTEM)}
                appBeanList.add(bean)
            }
        }
        //更新viewmodel中的列表值，用于列表更新时mainactivity访问
        menutoolbarvm.currentlist=appBeanList
        //该项只为筛选列表保存筛选前的状态
        menutoolbarvm.currentlist_all=appBeanList
        return appBeanList
    }
        suspend fun Loadappi(){
            withContext(Dispatchers.IO) {
                appi =getAllAppInfo(requireContext(), menutoolbarvm.sysappcheck,menutoolbarvm.userappcheck)
//                Thread.sleep(3000)
            }

//                val diffResult =
//                    DiffUtil.calculateDiff(DifferCallback(ArrayList<AppInfo>(), appi))
//                diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
            safetyuseDiffUtil(ArrayList<AppInfo>())

            binding.progressBar.visibility = View.GONE
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = MyAdapter(appi,menutoolbarvm)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    suspend fun refleshfliterview(searchname: String){
        val old=appi
        binding.progressBar.visibility = View.VISIBLE
        withContext(Dispatchers.IO) {
            appi = filterappbyname(searchname)
        }
        if(old.size<appi.size) {
        binding.recyclerView.adapter?.notifyDataSetChanged()
        }
        if(old.size>appi.size){
//            val diffResult = DiffUtil.calculateDiff(DifferCallback(old, appi))
//            diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
//            binding.recyclerView.adapter?.let { diffResult.dispatchUpdatesTo(it) }
            safetyuseDiffUtil(old)
        }
//

        binding.progressBar.visibility = View.GONE
    }
    fun safetyuseDiffUtil(old:List<AppInfo>){
        try {
            val diffResult = DiffUtil.calculateDiff(DifferCallback(old, appi))
            diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun refleshrecycleview(old : List<AppInfo>){

        binding.progressBar.visibility = View.VISIBLE
        if(menutoolbarvm.sysappcheck or  menutoolbarvm.userappcheck) {
            withContext(Dispatchers.IO) {
                appi = getAllAppInfo(
                    requireContext(),
                    menutoolbarvm.sysappcheck,
                    menutoolbarvm.userappcheck
                )
            }
        }
        else
        {
            appi= mutableListOf()
        }
//        val diffResult = DiffUtil.calculateDiff(DifferCallback(old, appi))
//        diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
        safetyuseDiffUtil(old)
        binding.progressBar.visibility = View.GONE
    }
    class MyAdapter(private val appList: List<AppInfo>,private val menutoolbarvm: menutoolbarvm) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private lateinit var itemView : ItemViewLinearBinding
        private lateinit var ctx:MainActivity
        //多选列表，用于存储被选中项是哪些
        private var mutilSelectedList = mutableSetOf<Int>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //val itemView:View =LayoutInflater.from(context).inflate(R.layout.item_view_linear_vertical_selectview,parent,false)
            //val itemViewL:View =ItemViewLinearBinding.inflate(LayoutInflater.from(context)).root
            //val itemView =ItemViewLinearBinding.inflate(LayoutInflater.from(context))
            //itemView.itemImage.setImageResource()

            itemView = ItemViewLinearBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ctx=parent.context as MainActivity
            return MyViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return appi.count()
        }
        fun setbuttonvisiable()
        {
            if( mutilSelectedList.size>0)
            {
//                val main =itemView.view.context as MainActivity
                ctx.setapplyvisiable(true)
            }
            else
            {
                ctx.setapplyvisiable(false)
            }
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val app: AppInfo = appi[position]
            holder.itemView.isSelected=mutilSelectedList.contains(position)
            holder.itemView.setOnClickListener{

                menutoolbarvm.limitedlist
                if (mutilSelectedList.contains(position)) {
                    mutilSelectedList.remove(position)
                    holder.itemView.isSelected= false
                    menutoolbarvm.limitedlist-=app
                    //Toast.makeText(itemView.view.context, "取消选中${position}", Toast.LENGTH_SHORT).show()
                } else {
                    mutilSelectedList.add(position)
                    holder.itemView.isSelected = true
                    menutoolbarvm.limitedlist+=app
                    //Toast.makeText(itemView.view.context, "选中${position}", Toast.LENGTH_SHORT).show()
                }
                ctx.Letsearchviewlosefocus()
                setbuttonvisiable()
                //Toast.makeText(itemView.view.context, "你点击了${itemView.view.getLayoutParams()}", Toast.LENGTH_SHORT).show()
            }
            holder.bind(app)
            // holder.itemView.item_image.setImageResource(R.drawable.ic_launcher_background)
            //holder.itemName.text = "【{$position}】test"
            // holder.itempackages.text = "test"
        }

        class MyViewHolder(private val itemBinding: ItemViewLinearBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(app: AppInfo) {
                itemBinding.itemName.text = app.getLabelKotlin()
                itemBinding.itemPackages.text = app.getPackage_nameKotlin()
                itemBinding.itemImage.setImageDrawable(app.getIconKotlin())
            }
        }
    }
}




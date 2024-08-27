package com.feng.netmaster

//import android.R
//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.feng.netmaster.databinding.FragmentFirstBinding
import com.feng.netmaster.databinding.ItemViewLinearFirstBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private lateinit var limiteditemfliter : List<AppInfo>

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val menutoolbarvm: menutoolbarvm by activityViewModels()
//    private lateinit var progressDialog : CustomProgressDialog

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        //显示加载动画
//        if(!menutoolbarvm.firstload) {
//            progressDialog = CustomProgressDialog(context, R.style.progressDialog)
//            progressDialog.setCanceledOnTouchOutside(false)
//            progressDialog.show()
//        }
        binding.progressBar.visibility = View.VISIBLE

        if(menutoolbarvm.limitedlist.isEmpty()&&menutoolbarvm.firstload)
        {
            val ctx=context as MainActivity
            menutoolbarvm.limitedlist=ctx.loadappfromcfg()
            menutoolbarvm.firstload=false
        }
        CoroutineScope(Dispatchers.Main).async{
            async { Loadappi(menutoolbarvm.currentlist2) }
        }

        limiteditemfliter=ArrayList<AppInfo>()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = MyAdapter(menutoolbarvm)

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(com.feng.netmaster.R.id.action_FirstFragment_to_SecondFragment)
//        }

    }
    suspend fun Loadappi(old : List<AppInfo>){
        withContext(Dispatchers.IO) {
            limiteditemfliter =fliterapp(menutoolbarvm.sysappcheck,menutoolbarvm.userappcheck)
        }
        reflesh(old)
        binding.progressBar.visibility = View.GONE

    }
    suspend fun refleshrecycleview(old : List<AppInfo>){

        if(menutoolbarvm.sysappcheck or  menutoolbarvm.userappcheck) {
            withContext(Dispatchers.IO) {
                limiteditemfliter = fliterapp(menutoolbarvm.sysappcheck, menutoolbarvm.userappcheck)
            }
        }
        else{
            limiteditemfliter =mutableListOf<AppInfo>()
        }
        reflesh(old)

    }
    fun reflesh(old : List<AppInfo>)
    {
        if(old.size!=limiteditemfliter.size) {
//            val diffResult = DiffUtil.calculateDiff(DifferCallback(old, limiteditemfliter))
//            diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
            safetyuseDiffUtil(old)
        }
        else
        {
            //未增加任何项目时，使用DiffUtil刷新有时无效
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
    }
    fun filterappbyname(searchname:String):List<AppInfo>
    {
        var fliterlist=mutableListOf<AppInfo>()
        if(menutoolbarvm.currentlist2.isNotEmpty()) {
            for (p in menutoolbarvm.currentlist2) {
                val a=p.package_name.toString().indexOf(searchname,0,true)>-1
                val b=p.label.toString().indexOf(searchname,0,true)>-1
                if (a||b)
                {
                    fliterlist.add(p)
                }

            }
            return fliterlist
        }
        return menutoolbarvm.currentlist2
    }
    suspend fun refleshfliterview(searchname: String){
        val old=limiteditemfliter
        binding.progressBar.visibility = View.VISIBLE
        withContext(Dispatchers.IO) {
            limiteditemfliter  = filterappbyname(searchname)
        }
        if(old.size<limiteditemfliter .size) {
            binding.recyclerView.adapter?.notifyDataSetChanged()
        }
        if(old.size>limiteditemfliter .size){
            safetyuseDiffUtil(old)
//                binding.recyclerView.adapter?.let { diffResult.dispatchUpdatesTo(it) }

        }
//
        binding.progressBar.visibility = View.GONE
    }
    fun safetyuseDiffUtil(old:List<AppInfo>){
        try {
            val diffResult = DiffUtil.calculateDiff(DifferCallback(old, limiteditemfliter))
            diffResult.dispatchUpdatesTo(binding.recyclerView.adapter!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun deletemutiitem(old : List<AppInfo>)
    {
        for (p in menutoolbarvm.cur2sellist)
        {
            //Toast.makeText(this, "你点击了${menutoolbarvm.currentlist2[p].package_name}", Toast.LENGTH_SHORT).show()
            menutoolbarvm.limitedlist-=p
            menutoolbarvm.currentlist2-=p
        }
//        if(menutoolbarvm.currentlist2.size>0){
        CoroutineScope(Dispatchers.Main).async{
            refleshrecycleview(old)
        }
//        else {
//            limiteditemfliter = fliterapp(menutoolbarvm.sysappcheck, menutoolbarvm.userappcheck)
//            binding.recyclerView.adapter?.notifyDataSetChanged()
//        }
    }
    fun fliterapp(isFilterSystem: Boolean,isFilterUserApp : Boolean):List<AppInfo>
    {
        var list =menutoolbarvm.currentlist2
        for (p in menutoolbarvm.limitedlist) {
            if (isFilterSystem && p.classify.name == toolbarchecked.SYSTEM.name) {
                if(!menutoolbarvm.containitem(list,p)){list  += p}

            } else if (isFilterUserApp && p.classify.name == toolbarchecked.USER.name) {
                if(!menutoolbarvm.containitem(list,p)){list  += p}
            }
            else
            {
                list -=p
            }
        }
        //更新viwemodel的显示表,以便之后对比刷新
        menutoolbarvm.currentlist2=list
        return list
    }
    class MyAdapter(private val menutoolbarvm: menutoolbarvm) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
        private lateinit var itemView : ItemViewLinearFirstBinding
        private lateinit var ctx:MainActivity
        private var mutaselcted: MutableSet<Int>  =mutableSetOf<Int>()
        fun setbuttonvisiable()
        {
            if(mutaselcted.size>0)
            {
                val main =itemView.view.context as MainActivity
                main.setvisiablebyid(R.id.action_delete,true)
            }
            else
            {
                val main =itemView.view.context as MainActivity
                main.setvisiablebyid(R.id.action_delete,false)
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            itemView =
                ItemViewLinearFirstBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ctx=parent.context as MainActivity
            return MyViewHolder(itemView)
        }
        override fun  getItemCount(): Int {
            return limiteditemfliter.count()
        }
        override fun onBindViewHolder(holder:MyViewHolder, position: Int) {
            val app: AppInfo = limiteditemfliter[position]
            holder.bind(app)
            mutaselcted=menutoolbarvm.mutaselcted
            holder.itemView.isSelected=mutaselcted.contains(position)
            holder.itemView.setOnClickListener {
                if (mutaselcted.contains(position)) {
                    mutaselcted.remove(position)
                    menutoolbarvm.cur2sellist-=(app)
                    holder.itemView.isSelected= false
                } else {
                    mutaselcted.add(position)
                    menutoolbarvm.cur2sellist+=(app)
                    holder.itemView.isSelected = true
                }
                ctx.Letsearchviewlosefocus()
                setbuttonvisiable()

//
            }
            itemView.checkBox.setOnCheckedChangeListener{  buttonView, isChecked ->
                app.package_name?.let { it1 -> menutoolbarvm.updatechecked(it1,isChecked,net.LOCAL) }
            }
            itemView.checkBox2.setOnCheckedChangeListener{  buttonView, isChecked ->
                app.package_name?.let { it1 -> menutoolbarvm.updatechecked(it1,isChecked,net.INTERNET) }
            }
        }
        class MyViewHolder(private val itemBinding: ItemViewLinearFirstBinding) :
            RecyclerView.ViewHolder(itemBinding.root) {
            fun bind(app: AppInfo) {
                itemBinding.itemName.text = app.getLabelKotlin()
                itemBinding.itemPackages.text = app.getPackage_nameKotlin()
                itemBinding.itemImage.setImageDrawable(app.getIconKotlin())
                itemBinding.checkBox.isChecked=app.connectlocalnet
                itemBinding.checkBox2.isChecked=app.connectinternet
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}
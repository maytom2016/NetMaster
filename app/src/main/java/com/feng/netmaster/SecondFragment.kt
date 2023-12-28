package com.feng.netmaster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.feng.netmaster.R
import com.feng.netmaster.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */



class SecondFragment : Fragment() {
    private var _binding: FragmentSecondBinding? = null
    private val menutoolbarvm: menutoolbarvm by activityViewModels()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    fun gettextviewtext(): CharSequence?
    {
        return binding.textView.text
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)

        val ctx=context as MainActivity
        ctx.hidefabimexfragment2()

        //生成iptables规则
        val res=menutoolbarvm.getres()
        var str=""
        for(p in res.strlist)
        {
            str=str+p+"\n"
        }
        binding.textView.text=str
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.ulaugh.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.ulaugh.R
import com.example.ulaugh.databinding.FragmentCodeBinding
import com.example.ulaugh.databinding.FragmentResetPassBinding
import com.example.ulaugh.utils.SharePref
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ResetPassFragment : Fragment() {

    private var _binding: FragmentResetPassBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var tokenManager: SharePref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResetPassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.included2.continueBtn.text = getText(R.string.confirm_pass)
        binding.included.backBtn.setOnClickListener {
            it.findNavController().popBackStack()
        }
        binding.included2.continueBtn.setOnClickListener {
//            it.findNavController().navigate(R.id.action_resetPassFragment_to_loginFragment)
//            it.findNavController()
//                .popBackStack(R.id.action_resetPassFragment_to_loginFragment, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
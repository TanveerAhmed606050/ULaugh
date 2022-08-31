package com.example.ulaugh.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.ulaugh.R
import com.example.ulaugh.controller.ChangeContactActivity
import com.example.ulaugh.controller.HomeActivity
import com.example.ulaugh.databinding.FragmentCodeBinding
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.OTP
import com.example.ulaugh.utils.Constants.PHONE_NO
import com.example.ulaugh.utils.Constants.USER_DATA
import com.example.ulaugh.utils.Constants.USER_TOKEN
import com.example.ulaugh.utils.NetworkResult
import com.example.ulaugh.utils.SharePref
import com.example.ulaugh.viewModel.UserFirebaseVM
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class CodeFragment : Fragment() {

    private var _binding: FragmentCodeBinding? = null
    private val binding get() = _binding!!
    private var token: String = ""

    //    lateinit var firebaseAuth: FirebaseAuth
//    private lateinit var storageReference: StorageReference
//    private lateinit var databaseReference: DatabaseReference
    private lateinit var userRequest: UserRequest
    private var otpCode = ""
    private val userViewModels by activityViewModels<UserFirebaseVM>()

    @Inject
    lateinit var sharePref: SharePref
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        initFirebase()
        getBundle()
        clickListener()
        otpKeyListener()
        bindObservers()
    }

    private fun verifyVerificationCode() {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(token, otpCode)
        //signing the user
        userViewModels.signWithPhoneAuthCredential(credential, userRequest)
    }

    private fun getBundle() {
        if (arguments != null) {
            token = arguments?.getString(USER_TOKEN).toString()
            userRequest = Gson().fromJson(
                arguments?.getString(USER_DATA),
                object : TypeToken<UserRequest>() {}.type
            )
            binding.textView13.text = "${getText(R.string.hint_otp)}  ${userRequest.phone_no}"
        }
    }

    private fun putBundle() {
        val bundle = Bundle()
        bundle.putString(PHONE_NO, userRequest.phone_no)
        bundle.putString(USER_TOKEN, token)
        bundle.putString(OTP, otpCode)
        binding.included2.continueBtn.findNavController()
            .navigate(R.id.action_codeFragment_to_registerFragment, bundle)
    }

    private fun clickListener() {
        binding.included2.continueBtn.setOnClickListener {
            if (checkValidation()) {
                verifyVerificationCode()
                binding.included2.continueBtn.isEnabled = false
            } else
                binding.txtError.text = "Enter complete code"
        }
        binding.included.backBtn.setOnClickListener {
            it.findNavController().popBackStack()
        }
    }

    private fun bindObservers() {
        userViewModels.userResponseLiveData.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = false
            when (it) {
                is NetworkResult.Success -> {
                    val user = it.data
                    if (!user?.firebase_id.isNullOrEmpty())
                        sharePref.writeString(Constants.FIREBASE_ID, user!!.firebase_id)
                    if (user?.email != null)
                        sharePref.writeString(Constants.EMAIL, user.email)
                    if (user?.full_name != null)
                        sharePref.writeString(Constants.FULL_NAME, user.full_name)
                    if (user?.user_name != null)
                        sharePref.writeString(Constants.USER_NAME, user.user_name)
                    if (user?.phone_no != null)
                        sharePref.writeString(PHONE_NO, user.phone_no)
                    if (user?.profile_pic != null)
                        sharePref.writeString(Constants.PROFILE_PIC, user.profile_pic)
                    if (user?.email != null) {
                        (activity)?.finish()
                        startActivity(Intent(activity, HomeActivity::class.java))
                    } else
                        putBundle()
                }
                is NetworkResult.Error -> {
                    binding.included2.continueBtn.isEnabled = true
                    showValidationErrors(it.message.toString())
                }
                is NetworkResult.Loading -> {
                    showValidationErrors("")
                    binding.progressBar.isVisible = true
                }
            }
        }
    }

    private fun showValidationErrors(error: String) {
        binding.txtError.text =
            String.format(resources.getString(R.string.txt_error_message, error))
    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        firebaseAuth.signInWithCredential(credential)
//            .addOnCompleteListener(activity!!) { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(activity!!, "Success", Toast.LENGTH_SHORT).show()
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
//                    databaseReference = FirebaseDatabase.getInstance().getReference("users")
//                        .child(firebaseAuth.currentUser!!.uid)
//                    databaseReference.addValueEventListener(object :
//                        ValueEventListener {
//                        override fun onDataChange(dataSnapshot: DataSnapshot) {
//                            if (dataSnapshot.value != null) {
//                                databaseReference.setValue(userRequest).addOnCompleteListener {
//                                    if (it.isSuccessful){
//                                        startActivity(Intent(activity!!, HomeActivity::class.java))
//                                        (activity!!).finish()
//                                    }else{
//                                        Toast.makeText(activity!!, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
//                                    }
//
//                                }
//                            } else {
//                                putBundle()
//                            }
////                                Log.d(TAG, "onDataChange:value ${dataSnapshot.value}")
////                            Log.d(TAG, "onDataChange ${dataSnapshot}")
//                        }
//
//                        override fun onCancelled(databaseError: DatabaseError) {
//                            Log.d(TAG, "Failed ${databaseError.code}")
//                            println("The read failed: " + databaseError.code)
//                        }
//                    })
//                } else {
//                    // Sign in failed, display a message and update the UI
//                    binding.txtError.text = "Failure: ${task.exception}"
//                    Log.d(TAG, "signInWithCredential:failure", task.exception)
////                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                    // The verification code entered was invalid
////                    }
//                    // Update UI
//                }
//            }
//    }


    private fun checkValidation(): Boolean {
        if (binding.firstDigit1.text.toString().isEmpty() || binding.secondDigit.text.toString()
                .isEmpty() || binding.thirdDigit.text.toString().isEmpty()
            || binding.fourDigit.text.toString().isEmpty() || binding.fiveDigit.text.toString()
                .isEmpty() || binding.sixDigit.text.toString().isEmpty()
        )
            return false

        binding.txtError.text = ""
        otpCode =
            binding.firstDigit1.text.toString() + binding.secondDigit.text.toString() + binding.thirdDigit.text.toString() + binding.fourDigit.text.toString() + binding.fiveDigit.text.toString() + binding.sixDigit.text.toString()
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun otpKeyListener() {
        //GenericTextWatcher here works only for moving to next EditText when a number is entered
//first parameter is the current EditText and second parameter is next EditText
        binding.firstDigit1.addTextChangedListener(
            GenericTextWatcher(
                binding.firstDigit1,
                binding.secondDigit
            )
        )
        binding.secondDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.secondDigit,
                binding.thirdDigit
            )
        )
        binding.thirdDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.thirdDigit,
                binding.fourDigit
            )
        )
        binding.fourDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.fourDigit,
                binding.fiveDigit
            )
        )
        binding.fiveDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.fiveDigit,
                binding.sixDigit
            )
        )
        binding.sixDigit.addTextChangedListener(GenericTextWatcher(binding.sixDigit, null))

//GenericKeyEvent here works for deleting the element and to switch back to previous EditText
//first parameter is the current EditText and second parameter is previous EditText
        binding.firstDigit1.setOnKeyListener(GenericKeyEvent(binding.firstDigit1, null))
        binding.secondDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.secondDigit,
                binding.firstDigit1
            )
        )
        binding.thirdDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.thirdDigit,
                binding.secondDigit
            )
        )
        binding.fourDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.fourDigit,
                binding.thirdDigit
            )
        )
        binding.fiveDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.fiveDigit,
                binding.fourDigit
            )
        )
        binding.sixDigit.setOnKeyListener(GenericKeyEvent(binding.sixDigit, binding.fiveDigit))
    }

    class GenericKeyEvent internal constructor(
        private val currentView: EditText,
        private val previousView: EditText?
    ) : View.OnKeyListener {
        override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (event!!.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.id != R.id.first_digit_1 && currentView.text.isEmpty()) {
                //If current is empty then previous EditText's number will also be deleted
                previousView!!.text = null
                previousView.requestFocus()
                return true
            }
            return false
        }


    }

    class GenericTextWatcher internal constructor(
        private val currentView: View,
        private val nextView: View?
    ) : TextWatcher {
        override fun afterTextChanged(editable: Editable) { // TODO Auto-generated method stub
            val text = editable.toString()
            when (currentView.id) {
                R.id.first_digit_1 -> if (text.length == 1) nextView!!.requestFocus()
                R.id.second_digit -> if (text.length == 1) nextView!!.requestFocus()
                R.id.third_digit -> if (text.length == 1) nextView!!.requestFocus()
                R.id.four_digit -> if (text.length == 1) nextView!!.requestFocus()
                R.id.five_digit -> if (text.length == 1) nextView!!.requestFocus()
                //You can use EditText4 same as above to hide the keyboard
            }
        }

        override fun beforeTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

        override fun onTextChanged(
            arg0: CharSequence,
            arg1: Int,
            arg2: Int,
            arg3: Int
        ) { // TODO Auto-generated method stub
        }

    }
}
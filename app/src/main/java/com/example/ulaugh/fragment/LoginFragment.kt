package com.example.ulaugh.fragment

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Intent
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.ulaugh.R
import com.example.ulaugh.controller.HomeActivity
import com.example.ulaugh.databinding.FragmentLoginBinding
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.Constants
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.utils.Constants.USER_DATA
import com.example.ulaugh.utils.Constants.USER_TOKEN
import com.example.ulaugh.utils.Helper
import com.example.ulaugh.utils.Helper.Companion.hideKeyboard
import com.example.ulaugh.utils.SharePref
import com.example.ulaugh.utils.ValidateStatus
import com.example.ulaugh.viewModel.UserFirebaseVM
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mukesh.countrypicker.CountryPicker
import dagger.hilt.android.AndroidEntryPoint
import java.util.ResourceBundle.getBundle
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var userRequest: UserRequest
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var mVerificationInProgress = false
    private var mVerificationId: String? = null
    private var mResendToken: ForceResendingToken? = null

    //    private var mCallbacks: OnVerificationStateChangedCallbacks? = null
    private var token: String = ""
    private var argumentsData: UserRequest? = null

    @Inject
    lateinit var sharePref: SharePref
    private val userViewModel by activityViewModels<UserFirebaseVM>()
    private val validateStatus = ValidateStatus()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var firebaseAuth: FirebaseAuth
//    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.included2.continueBtn.text = getString(R.string.sign_in)
        getBundle()
        clickListener()
        prepareGoogle()
//        bindObservers()
    }

    private fun clickListener() {
//        binding.flagIv.setImageResource(2131165554)
//        binding.cellCode.text = "+1"
        binding.included2.continueBtn.setOnClickListener {
            hideKeyboard(it)
//            (activity)!!.finish()
//            startActivity(Intent(activity!!, HomeActivity::class.java))
            userRequest = getUserRequest()
            val validationResult = checkValidation()
            if (validationResult == "Email") {
                disableViews(binding.signupBtn, binding.included2.continueBtn, binding.loginGoogle)
                loginWithEmail()
                inVisibleLoginView()
            } else if (validationResult == "Phone" && !binding.otpEt.isVisible) {
                binding.progressBar.visibility = View.VISIBLE
                loginWithPhone(binding.phoneNo.text.toString())
            } else if (binding.otpEt.isVisible && binding.otpEt.text.toString().length == 6) {
                val credential = PhoneAuthProvider.getCredential(
                    mVerificationId!!,
                    binding.otpEt.text.toString()
                )
                signInWithPhoneAuthCredential(credential)
                binding.progressBar.visibility = View.VISIBLE
                disableViews(binding.signupBtn, binding.included2.continueBtn, binding.loginGoogle)
            }
        }
//        binding.countryPicker.setOnClickListener {
//            openCountryPickerDialog()
//        }
//        binding.forgotTv.setOnClickListener {
//            hideKeyboard(it)
//            it.findNavController()
//                .navigate(R.id.action_loginFragment_to_forgotPassFragment)
//        }
        binding.signupBtn.setOnClickListener {
            hideKeyboard(it)
            it.findNavController().popBackStack()
//            it.findNavController()
//                .navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.loginGoogle.setOnClickListener {
            signIn()
        }
    }

    private fun checkValidation(): String {
        if (binding.phoneNo.text.toString().isEmpty()) {
            showValidationErrors("Please provide Email or Phone")
            return ""
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(
                binding.phoneNo.text.toString().trim()
            ) && !Helper.isValidEmail(
                binding.phoneNo.text.toString().trim()
            )
        ) {
            showValidationErrors("Please provide correct email or Phone")
            return ""
        } else if (Helper.isValidEmail(binding.phoneNo.text.toString()))
            return "Email"
        else if (PhoneNumberUtils.isGlobalPhoneNumber(binding.phoneNo.text.toString()))
            return "Phone"
        return ""
    }

    private fun loginWithEmail() {
        binding.etPassword.visibility = View.VISIBLE
        val signInEmail = binding.phoneNo.text.toString().trim()
        val signInPassword = binding.etPassword.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(requireContext(), HomeActivity::class.java))
                        toast("signed in successfully")
                        requireActivity().finish()
                    } else {
                        toast("sign in failed:${signIn.result}")
                    }
                }
        } else {
            toast("Please provide Credential")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun notEmpty(): Boolean =
        binding.phoneNo.text.isNotEmpty() && binding.etPassword.text!!.isNotEmpty()

    private fun getBundle() {
        if (arguments != null) {
            token = arguments?.getString(USER_TOKEN).toString()
            argumentsData = Gson().fromJson(
                arguments?.getString(USER_DATA),
                object : TypeToken<UserRequest>() {}.type
            )
        }
    }

    private fun putBundle() {
        val bundle = Bundle()
        bundle.putString(USER_TOKEN, mVerificationId)
        bundle.putString(USER_DATA, Gson().toJson(userRequest))
        binding.included2.continueBtn.findNavController()
            .navigate(R.id.action_loginFragment_to_codeFragment, bundle)
    }

    private fun loginWithPhone(phoneNo: String) {
//        disableViews(binding.signupBtn, binding.loginGoogle, binding.included2.continueBtn)
//        visibleLoginView()
        binding.txtError.text = ""

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNo)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity!!)
            .requireSmsValidation(false)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verificationId: String,
                    token: ForceResendingToken
                ) {
                    inVisibleLoginView()
                    mVerificationId = verificationId
                    mResendToken = token
                    enableViews(
                        binding.loginGoogle,
                        binding.signupBtn,
                        binding.included2.continueBtn
                    )
                    binding.progressBar.visibility = View.GONE
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    mVerificationInProgress = false
                    val code = phoneAuthCredential.smsCode
                    if (code != null) {
                        binding.otpEt.setText(code)
                        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                        signInWithPhoneAuthCredential(credential)
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    enableViews(
                        binding.signupBtn,
                        binding.loginGoogle,
                        binding.included2.continueBtn
                    )
                    binding.progressBar.visibility = View.GONE
                    visibleLoginView()
                    mVerificationInProgress = false
                    binding.txtError.text = e.message
                }
            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val reference = FirebaseDatabase.getInstance()
                        .getReference(Constants.USERS_REF)
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT)
                                    .show()
//                                    if (!userRequest.email.isNullOrEmpty()) {
//                                        reference.child(Constants.EMAIL).setValue(userRequest.email)
                                sharePref.writeString(
                                    Constants.EMAIL,
                                    dataSnapshot.child(Constants.EMAIL).value.toString()
                                )
//                                    }
//                                    if (!userRequest.user_name.isNullOrEmpty()) {
//                                        reference.child(Constants.USER_NAME)
//                                            .setValue(userRequest.user_name)
                                sharePref.writeString(
                                    Constants.USER_NAME,
                                    dataSnapshot.child(Constants.USER_NAME).value.toString()
                                )
//                                    }
//                                    if (!userRequest.full_name.isNullOrEmpty()) {
//                                        reference.child(Constants.FULL_NAME)
//                                            .setValue(userRequest.full_name)
                                sharePref.writeString(
                                    Constants.FULL_NAME,
                                    dataSnapshot.child(Constants.FULL_NAME).value.toString()
                                )
//                                    }
                                sharePref.writeString(
                                    Constants.PROFILE_PIC,
                                    dataSnapshot.child(Constants.PROFILE_PIC).value.toString()
                                )
                                sharePref.writeBoolean(
                                    Constants.IS_PRIVATE,
                                    dataSnapshot.child(Constants.IS_PRIVATE).value.toString()
                                        .toBoolean()
                                )
                                requireContext().startActivity(
                                    Intent(
                                        requireContext(),
                                        HomeActivity::class.java
                                    )
                                )
                                activity!!.finish()
                            } else
                                Toast.makeText(
                                    requireContext(),
                                    "Error:User not Exist",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            visibleLoginView()
                            binding.progressBar.visibility = View.GONE
                            enableViews(
                                binding.signupBtn,
                                binding.loginGoogle,
                                binding.included2.continueBtn
                            )
                            Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT)
                                .show()
                        }

                    })
//                    userViewModel.signWithPhoneAuthCredential(credential, userRequest)
//                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(
                            requireContext(),
                            "Exception: ${task.exception!!.message.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    // [END sign_in_with_phone]
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                disableViews(binding.signupBtn, binding.included2.continueBtn, binding.loginGoogle)
                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else
                Log.d("TAG", "error:${result.data.toString()} ")
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )

            if (account?.idToken!!.isEmpty()) {
                val socialRequest = UserRequest(
                    account.id!!,
                    account.email!!,
                    "${account.givenName} ${account.familyName}",
                    account.displayName!!,
                )
                userViewModel.socialLogin(socialRequest)
            } else {
                val socialRequest = UserRequest(
                    account.idToken!!,
                    account.email!!,
                    "${account.givenName} ${account.familyName}",
                    account.displayName!!,
                )
                userViewModel.socialLogin(socialRequest)
            }

        } catch (e: ApiException) {
            // Sign in was unsuccessful
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }


    private fun prepareGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(activity!!, gso)
    }

    private fun getUserRequest(): UserRequest {
        return binding.run {
            if (argumentsData != null)
                UserRequest(
                    phoneNo.text.toString()
                )
            else
                UserRequest(
                    phoneNo.text.toString()
                )
        }
    }

    private fun showValidationErrors(error: String) {
        binding.txtError.text =
            String.format(resources.getString(R.string.txt_error_message, error))
    }

    private fun validateUserInput(): Pair<Boolean, String> {
        val phoneNo = binding.phoneNo.text.toString()
//        val password = binding.etPassword.text.toString()
        return validateStatus.validateCredentials(phoneNo, "", "", "", true)
    }

    private fun openCountryPickerDialog() {
        val picker = CountryPicker.newInstance("Select Country") // dialog title

        picker.setListener { name, code, dialCode, flagDrawableResID ->
            // Implement your code here
//            binding.flagIv.setImageResource(flagDrawableResID)
//            binding.cellCode.text = dialCode
            binding.phoneNo.isEnabled = true
            picker.dismiss()
        }
        picker.show(parentFragmentManager, "COUNTRY_PICKER")
    }

//    private fun bindObservers() {
//        userViewModel.userResponseLiveData.observe(viewLifecycleOwner) {
//            binding.progressBar.isVisible = false
//            binding.textView5.visibility = View.VISIBLE
//            when (it) {
//                is NetworkResult.Success -> {
//                    tokenManager.saveFirebaseId(it.data!!.firebase_id)
//                    activity?.finish()
//                    startActivity(Intent(activity, HomeActivity::class.java))
//                }
//                is NetworkResult.Error -> {
//                    enableViews(binding.signupBtn,binding.included2.continueBtn, binding.loginGoogle)
//                    showValidationErrors(it.message.toString())
//                }
//                is NetworkResult.Loading -> {
//                    binding.textView5.visibility = View.INVISIBLE
//                    binding.progressBar.isVisible = true
//                }
//                else -> {
//                    enableViews(binding.signupBtn,binding.included2.continueBtn, binding.loginGoogle)
//                    showValidationErrors(it.message.toString())
//                }
//            }
//        }
//    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    private fun visibleLoginView() {
//        binding.progressBar.visibility = View.INVISIBLE
        binding.loginGoogle.visibility = View.VISIBLE
        binding.signupBtn.visibility = View.VISIBLE
        binding.otpEt.visibility = View.GONE
        binding.phoneNo.visibility = View.VISIBLE
        binding.textView2.text = getText(R.string.phone_email)
        binding.textView5.visibility = View.VISIBLE
        binding.txtError.text = ""
        binding.textView6.visibility = View.GONE
        binding.imageView2.visibility = View.VISIBLE
        binding.textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_3))
    }

    private fun inVisibleLoginView() {
//        binding.progressBar.visibility = View.VISIBLE
        binding.textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_black))
        binding.textView2.text = "${getText(R.string.hint_otp)} ${binding.phoneNo.text}"
        binding.loginGoogle.visibility = View.GONE
        binding.signupBtn.visibility = View.GONE
        binding.textView5.visibility = View.GONE
        binding.phoneNo.visibility = View.GONE
        binding.textView3.visibility = View.GONE
        binding.passwordPassEt.visibility = View.GONE
        binding.txtError.text = ""
        binding.otpEt.visibility = View.VISIBLE
        binding.textView6.visibility = View.VISIBLE
        binding.imageView2.visibility = View.INVISIBLE
    }

//    private fun layoutVisible(vararg views: View) {
//        for (v in views)
//            v.visibility = View.VISIBLE
//    }
//
//    private fun layoutGone(vararg views: View) {
//        for (v in views)
//            v.visibility = View.GONE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
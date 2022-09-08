package com.example.ulaugh.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.ulaugh.R
import com.example.ulaugh.controller.HomeActivity
import com.example.ulaugh.databinding.FragmentRegisterBinding
import com.example.ulaugh.utils.Helper.Companion.hideKeyboard
import com.example.ulaugh.model.UserRequest
import com.example.ulaugh.utils.*
import com.example.ulaugh.utils.Constants.TAG
import com.example.ulaugh.viewModel.UserFirebaseVM
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var userRequest: UserRequest
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var mVerificationInProgress = false
    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    private val userViewModels by activityViewModels<UserFirebaseVM>()

    private val validateStatus = ValidateStatus()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var otp: String? = null
    private var phoneNo: String? = null

    //    @Inject
//    lateinit var authFirebase: AuthFirebase
//    lateinit var authFirebase: FirebaseAuth

    @Inject
    lateinit var sharePref: SharePref
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize phone auth callbacks
        firebaseAuth = FirebaseAuth.getInstance()
        callBacks()
//        getBundle()
        clickListener()
//        bindObservers()
        prepareGoogle()
    }

    private fun clickListener() {
        binding.loginBtn.setOnClickListener {
            it.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.included2.continueBtn.setOnClickListener {
            hideKeyboard(it)

            if (validateUserInput() && !binding.otpEt.isVisible) {
                disableViews(binding.loginBtn, binding.included2.continueBtn, binding.loginGoogle)
                binding.progressBar.visibility = View.VISIBLE
                userRequest = getUserRequest()
                loginWithPhone(binding.phoneNo.text.toString().trim())//+923454568270

            } else if (binding.otpEt.isVisible && binding.otpEt.text.toString().length == 6) {
                val credential = PhoneAuthProvider.getCredential(
                    mVerificationId!!,
                    binding.otpEt.text.toString()
                )
                binding.progressBar.visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
                disableViews(binding.loginBtn, binding.included2.continueBtn, binding.loginGoogle)
            }
        }
        binding.loginGoogle.setOnClickListener {
            socialSign()
        }
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
            .setCallbacks(mCallbacks!!)
//            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                override fun onCodeSent(
//                    verificationId: String,
//                    token: PhoneAuthProvider.ForceResendingToken
//                ) {
//                    mVerificationId = verificationId
//                    mResendToken = token
////                    enableViews(
////                        binding.signupBtn,
////                        binding.loginGoogle,
////                        binding.included2.continueBtn
////                    )
//                    inVisibleLoginView()
////                    putBundle()
//                }
//
//                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
//                    mVerificationInProgress = false
//                    val code = phoneAuthCredential.smsCode
//                    if (code != null) {
//                        binding.otpEt.setText(code)
//                        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
//                        signInWithPhoneAuthCredential(credential)
//                    }
//                }
//
//                override fun onVerificationFailed(e: FirebaseException) {
//                    enableViews(
//                        binding.loginBtn,
//                        binding.loginGoogle,
//                        binding.included2.continueBtn
//                    )
//                    visibleLoginView()
//                    mVerificationInProgress = false
//                    binding.txtError.text = e.message
//                }
//            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val reference = FirebaseDatabase.getInstance()
                        .getReference(Constants.USERS_REF)
                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT)
                                        .show()
                                    userRequest.firebase_id =
                                        FirebaseAuth.getInstance().currentUser!!.uid
//                                    reference
//                                        .setValue(userRequest)
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.IS_PRIVATE).setValue(false)
                                    sharePref.writeBoolean(Constants.IS_PRIVATE, false)
//                                    if (!userRequest.email.isNullOrEmpty()) {
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.EMAIL).setValue(userRequest.email)
                                    sharePref.writeString(
                                        Constants.EMAIL,
                                        userRequest.email
                                    )
//                                    }
//                                    if (!userRequest.user_name.isNullOrEmpty()) {
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.USER_NAME)
                                        .setValue(userRequest.user_name)
                                    sharePref.writeString(
                                        Constants.USER_NAME,
                                        userRequest.user_name
                                    )
//                                    }
//                                    if (!userRequest.full_name.isNullOrEmpty()) {
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.FULL_NAME)
                                        .setValue(userRequest.full_name)
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.FIREBASE_ID)
                                        .setValue(userRequest.firebase_id)
                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child(Constants.PHONE_NO)
                                        .setValue(userRequest.phone_no)

                                    sharePref.writeString(
                                        Constants.FULL_NAME,
                                        userRequest.full_name
                                    )
//                                    }
//                                    if (!userRequest.full_name.isNullOrEmpty()) {
//                                    reference.child(FirebaseAuth.getInstance().currentUser!!.uid)
//                                        .child(Constants.PHONE_NO)
//                                        .setValue(userRequest.phone_no)
//                                    }
                                    sharePref.writeString(
                                        Constants.PROFILE_PIC,
                                        userRequest.profile_pic
                                    )
                                    binding.progressBar.visibility = View.GONE
                                    requireContext().startActivity(
                                        Intent(
                                            requireContext(),
                                            HomeActivity::class.java
                                        )
                                    )
                                    activity!!.finish()
                                } else {
                                    binding.progressBar.visibility = View.GONE
                                    Toast.makeText(
                                        requireContext(),
                                        "Already register",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                visibleLoginView()
                                enableViews(
                                    binding.loginBtn,
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

    private fun signupWithEmail() {

        // identicalPassword() returns true only  when inputs are not empty and passwords are identical
        val userEmail = binding.emailEt.text.toString().trim()
        val userPassword = (binding).etPassword.text.toString().trim()

        /*create a user*/
        firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("created account successfully !")
                    sendEmailVerification(userEmail)
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            HomeActivity::class.java
                        )
                    )
                    requireActivity().finish()
                } else {
                    toast("failed to Authenticate !")
                }
            }

    }

    private fun sendEmailVerification(userEmail: String) {
        firebaseAuth.currentUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("email sent to $userEmail")
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

//    private fun getBundle() {
//        if (arguments != null) {
//            mVerificationId = arguments?.getString(USER_TOKEN).toString()
//            otp = arguments?.getString(OTP).toString()
//            phoneNo = arguments?.getString(PHONE_NO).toString()
//        }
//    }
//
//    private fun putBundle(view: View) {
//        val bundle = Bundle()
//        bundle.putString(USER_DATA, Gson().toJson(userRequest))
//        view.findNavController()
//            .navigate(R.id.action_registerFragment_to_loginFragment, bundle)
//    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                disableViews(binding.loginBtn, binding.included2.continueBtn, binding.loginGoogle)
                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            } else
                Log.d(TAG, "error:${result.data.toString()} ")
        }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(
                ApiException::class.java
            )
            val googleIdToken = account?.idToken ?: ""
            if (googleIdToken.isEmpty()) {
                val socialRequest = UserRequest(
                    "",
                    account.email!!,
                    "${account.givenName} ${account.familyName}",
                    account.displayName!!,
                    account.id!!,
                )
                userViewModels.socialLogin(socialRequest)
            } else {
                val socialRequest = UserRequest(
                    "",
                    account.email!!,
                    "${account.givenName} ${account.familyName}",
                    account.displayName!!,
                    account.idToken!!,
                )
                userViewModels.socialLogin(socialRequest)
            }
        } catch (e: ApiException) {
            binding.txtError.text = e.message
        }
    }

    private fun socialSign() {
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

    private fun validateUserInput(): Boolean {
        val fullName = binding.fullName.text.toString().trim()
        val userName = binding.userName.text.toString().trim()
        val email = binding.emailEt.text.toString().trim()
        if (fullName.isEmpty()) {
            showValidationErrors("Please Enter name")
            return false
        } else if (userName.isEmpty()) {
            showValidationErrors("Please Enter username")
            return false
        } else if (!Helper.isValidEmail(email) && email.isNotEmpty()) {
            showValidationErrors("Please Enter Email")
            return false
        } else if (!PhoneNumberUtils.isGlobalPhoneNumber(binding.phoneNo.text.toString().trim())) {
            showValidationErrors("Please Enter valid Phone number")
            return false
        }
//        validateStatus.validateCredentials(
//            "",
//            userName,
//            fullName,
//            email,
//            false
//        )
        return true
    }

    private fun showValidationErrors(error: String) {
        binding.txtError.text =
            String.format(resources.getString(R.string.txt_error_message, error))
    }

    private fun getUserRequest(): UserRequest {
        return binding.run {
            UserRequest(
                binding.phoneNo.text.toString().trim(),
                emailEt.text.toString().trim(),
                fullName.text.toString().trim(),
                userName.text.toString().trim(),
                "",
                ""
            )
        }
    }

    private fun callBacks() {
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                mVerificationInProgress = false
                enableViews(binding.loginBtn, binding.loginGoogle, binding.included2.continueBtn)
                mVerificationInProgress = false
                val code = phoneAuthCredential.smsCode
//                if (code != null) {
//                    binding.otpEt.setText(code)
//                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
//                    signInWithPhoneAuthCredential(credential)
//                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                mVerificationInProgress = false
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Failed: ${e.message.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                visibleLoginView()
                enableViews(binding.loginBtn, binding.loginGoogle, binding.included2.continueBtn)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.progressBar.visibility = View.GONE
                inVisibleLoginView()
                enableViews(binding.loginBtn, binding.loginGoogle, binding.included2.continueBtn)
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

//    private fun bindObservers() {
//        userViewModels.userResponseLiveData.observe(viewLifecycleOwner) {
//            binding.progressBar.isVisible = false
//            val user = it.data
//            when (it) {
//                is NetworkResult.Success -> {
//                    if (user?.firebase_id != null)
//                        sharePref.saveFirebaseId(user.firebase_id)
//                    if (user?.email != null)
//                        sharePref.saveEmail(user.email)
//                    if (user?.full_name != null)
//                        sharePref.saveFullName(user.full_name)
//                    if (user?.user_name != null)
//                        sharePref.saveUserName(user.user_name)
//                    if (user?.phone_no != null)
//                        sharePref.savePhone(user.phone_no!!)
//                    if (user?.profile_pic != null)
//                        sharePref.saveImage(user.profile_pic)
//
//                    if (user?.email != null) {
//                        (activity)?.finish()
//                        startActivity(Intent(activity, HomeActivity::class.java))
//                    }
//                }
//                is NetworkResult.Error -> {
//                    showValidationErrors(it.message.toString())
//                }
//                is NetworkResult.Loading -> {
//                    showValidationErrors("")
//                    binding.progressBar.isVisible = true
//                }
//            }
//        }
//    }


    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun visibleLoginView() {
//        binding.progressBar.visibility = View.VISIBLE
        binding.loginGoogle.visibility = View.VISIBLE
        binding.loginBtn.visibility = View.VISIBLE
        binding.otpEt.visibility = View.GONE
        binding.textView9.text = getText(R.string.email)
        binding.textView5.visibility = View.VISIBLE

        binding.textView7.visibility = View.VISIBLE
        binding.fullName.visibility = View.VISIBLE
        binding.textView8.visibility = View.VISIBLE
        binding.userName.visibility = View.VISIBLE
        binding.emailEt.visibility = View.VISIBLE
        binding.textView10.visibility = View.VISIBLE
        binding.phoneNo.visibility = View.VISIBLE
        binding.textView12.visibility = View.VISIBLE
        binding.imageView3.visibility = View.VISIBLE
        binding.textView6.visibility = View.GONE
        binding.textView9.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_3))
        binding.txtError.text = ""
        binding.textView18.visibility = View.VISIBLE
        binding.googleIcon.visibility = View.VISIBLE
    }

    private fun inVisibleLoginView() {
//        binding.progressBar.visibility = View.INVISIBLE
        binding.loginGoogle.visibility = View.INVISIBLE
        binding.textView18.visibility = View.INVISIBLE
        binding.googleIcon.visibility = View.INVISIBLE
        binding.loginBtn.visibility = View.INVISIBLE
        binding.otpEt.visibility = View.VISIBLE
        binding.textView6.visibility = View.VISIBLE
        binding.textView9.text = "${getText(R.string.hint_otp)} ${binding.phoneNo.text}"
        binding.textView5.visibility = View.INVISIBLE
        binding.textView9.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        binding.imageView3.visibility = View.INVISIBLE
        binding.textView7.visibility = View.INVISIBLE
        binding.fullName.visibility = View.INVISIBLE
        binding.textView8.visibility = View.INVISIBLE
        binding.userName.visibility = View.INVISIBLE
        binding.emailEt.visibility = View.INVISIBLE
        binding.textView10.visibility = View.INVISIBLE
        binding.phoneNo.visibility = View.INVISIBLE
        binding.textView12.visibility = View.INVISIBLE
        binding.txtError.text = ""

    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
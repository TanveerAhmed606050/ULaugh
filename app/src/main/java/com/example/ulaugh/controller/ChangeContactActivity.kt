package com.example.ulaugh.controller

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ulaugh.R
import com.example.ulaugh.databinding.ActivityChangeContactBinding
import com.example.ulaugh.utils.AuthFirebase
import com.example.ulaugh.utils.SharePref
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.mukesh.countrypicker.CountryPicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class ChangeContactActivity : AppCompatActivity() {
    @Inject
    lateinit var sharePref: SharePref

    @Inject
    lateinit var authFirebase: AuthFirebase
    private var oldPhoneNo: String? = null
    private var newPhoneNo: String? = null

    var otp = ""
    var verificationId = ""
    private var _binding: ActivityChangeContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChangeContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        clickListener()
    }

    private fun clickListener() {
        binding.flagIv.setImageResource(2131165554)
        binding.cellCode.text = "+1"
//        binding.backBtn.setOnClickListener {
//            finish()
//        }
        binding.countryPicker1.setOnClickListener {
            openCountryPickerDialog(true)
        }
        binding.countryPicker2.setOnClickListener {
            openCountryPickerDialog(false)
        }
        binding.included2.continueBtn.setOnClickListener {
//            if (checkValidation()) {
//                binding.included2.continueBtn.isEnabled = false
            oldPhoneNo = "+923456982288"
            newPhoneNo = "+923045465470"
            CoroutineScope(Dispatchers.IO).launch {
                verifyPhoneNo(newPhoneNo!!)
            }
//                otpKeyListener()
//            } else {
//                binding.txtError.text = ""
//            }
        }
        binding.included.included2.continueBtn.setOnClickListener {
            binding.included.included2.continueBtn.isEnabled = false
            if (checkOtpValidation())
                CoroutineScope(Dispatchers.IO).launch {
                    updatePhoneNumber(otp)
                }
            else
                showValidationErrors("Please provide credential")
//            showViews(binding.textView17, binding.textView18, binding.textView19, binding.cellCode, binding.cellCode2,
//                binding.countryPicker2, binding.countryPicker1, binding.oldPh, binding.dropdownIv2, binding.flagIv,
//                binding.flagIv2, binding.dropdownIv, binding.newPh, binding.progressBar, binding.txtError)
//            binding.included2.root.visibility = View.VISIBLE
//            binding.included.root.visibility = View.GONE
        }
    }

    private fun verifyPhoneNo(phoneNo: String) {
        binding.progressBar.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(authFirebase.auth)
            .setPhoneNumber(phoneNo)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)
            .requireSmsValidation(false)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onCodeSent(
                    verification: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = verification
                    hideViews(
                        binding.textView17,
                        binding.textView18,
                        binding.textView19,
                        binding.cellCode,
                        binding.cellCode2,
                        binding.countryPicker2,
                        binding.countryPicker1,
                        binding.oldPh,
                        binding.dropdownIv2,
                        binding.flagIv,
                        binding.flagIv2,
                        binding.dropdownIv,
                        binding.newPh,
                        binding.progressBar,
                        binding.txtError
                    )
                    binding.included2.root.visibility = View.GONE
                    binding.included.root.visibility = View.VISIBLE
                }

                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                }

                override fun onVerificationFailed(e: FirebaseException) {
                }
            }) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun updatePhoneNumber(otp: String) {
        val credential = PhoneAuthProvider.getCredential(newPhoneNo!!, otp)
        FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)!!
            .addOnCompleteListener {
                binding.progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                    Toast.makeText(this, "Contact re-authenticated.", Toast.LENGTH_SHORT).show()
                    // Now change your email address \\
                    //----------------Code for Changing Email Address----------\\

                    // Now change your email address \\
                    //----------------Code for Changing Email Address----------\\
                    val user = FirebaseAuth.getInstance().currentUser
                    user!!.updatePhoneNumber(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ChangeContactActivity,
                                "Phone Changed Successfully.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
//                    delay(1000)
                    finish()
                } else if (it.isCanceled)
                    Toast.makeText(this, "Contact update Canceled", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(
                        this,
                        "Contact update Error ${it.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
            }

    }

    private fun checkValidation(): Boolean {
        if (binding.oldPh.text.toString().isEmpty() || binding.newPh.text.toString().isEmpty()) {
            showValidationErrors("Please provide Credential")
            return false
        }
        oldPhoneNo = binding.cellCode.text.trim().toString() + binding.oldPh.text.trim().toString()
        newPhoneNo =
            binding.cellCode2.text.trim().toString() + binding.newPh.text.toString()
        return true
    }

    private fun checkOtpValidation(): Boolean {
        if (binding.included.firstDigit1.text.toString()
                .isEmpty() || binding.included.secondDigit.text.toString()
                .isEmpty() || binding.included.thirdDigit.text.toString().isEmpty()
            || binding.included.fourDigit.text.toString()
                .isEmpty() || binding.included.fiveDigit.text.toString()
                .isEmpty() || binding.included.sixDigit.text.toString().isEmpty()
        ) {
            binding.txtError.text = "Enter complete code"
            return false
        }
        binding.txtError.text = ""
        otp =
            binding.included.firstDigit1.text.toString() + binding.included.secondDigit.text.toString() + binding.included.thirdDigit.text.toString() + binding.included.fourDigit.text.toString() + binding.included.fiveDigit.text.toString() + binding.included.sixDigit.text.toString()
        return true
    }

    private fun showValidationErrors(error: String) {
        binding.txtError.text =
            String.format(resources.getString(R.string.txt_error_message, error))
    }

    private fun hideViews(vararg views: View) {
        for (v in views)
            v.visibility = View.GONE
    }

    private fun showViews(vararg views: View) {
        for (v in views)
            v.visibility = View.VISIBLE
    }

    private fun openCountryPickerDialog(isOldPhone: Boolean) {
        val picker = CountryPicker.newInstance("Select Country") // dialog title

        picker.setListener { name, code, dialCode, flagDrawableResID ->
            // Implement your code here
            if (isOldPhone) {
                binding.flagIv.setImageResource(flagDrawableResID)
                binding.cellCode.text = dialCode
                binding.oldPh.isEnabled = true
            } else {
                binding.flagIv2.setImageResource(flagDrawableResID)
                binding.cellCode2.text = dialCode
                binding.newPh.isEnabled = true
            }
            picker.dismiss()
        }
        picker.show(supportFragmentManager, "COUNTRY_PICKER")
    }

    private fun otpKeyListener() {
        binding.included.firstDigit1.addTextChangedListener(
            GenericTextWatcher(
                binding.included.firstDigit1,
                binding.included.secondDigit
            )
        )
        binding.included.secondDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.included.secondDigit,
                binding.included.thirdDigit
            )
        )
        binding.included.thirdDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.included.thirdDigit,
                binding.included.fourDigit
            )
        )
        binding.included.fourDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.included.fourDigit,
                binding.included.fiveDigit
            )
        )
        binding.included.fiveDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.included.fiveDigit,
                binding.included.sixDigit
            )
        )
        binding.included.sixDigit.addTextChangedListener(
            GenericTextWatcher(
                binding.included.sixDigit,
                null
            )
        )
        binding.included.firstDigit1.setOnKeyListener(
            GenericKeyEvent(
                binding.included.firstDigit1,
                null
            )
        )
        binding.included.secondDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.included.secondDigit,
                binding.included.firstDigit1
            )
        )
        binding.included.thirdDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.included.thirdDigit,
                binding.included.secondDigit
            )
        )
        binding.included.fourDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.included.fourDigit,
                binding.included.thirdDigit
            )
        )
        binding.included.fiveDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.included.fiveDigit,
                binding.included.fourDigit
            )
        )
        binding.included.sixDigit.setOnKeyListener(
            GenericKeyEvent(
                binding.included.sixDigit,
                binding.included.fiveDigit
            )
        )
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
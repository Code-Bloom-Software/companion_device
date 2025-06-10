package br.com.codebloom.companion_device

import android.app.Activity
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import java.util.concurrent.Executor

private const val SELECT_DEVICE_REQUEST_CODE = 42
private const val TAG = "CompanionDevice"

class CompanionDeviceService(private val context: Activity) {
    private val deviceManager: CompanionDeviceManager by lazy {
        context.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }
    private val executor: Executor = Executor { it.run() }

    fun associate(singleDevice: Boolean, filter: CompanionDeviceFilter) {
        val deviceFilter = filter.toDeviceFilter()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(singleDevice)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            deviceManager.associate(pairingRequest,
                executor,
                object : CompanionDeviceManager.Callback() {
                    override fun onAssociationPending(intentSender: IntentSender) {
                        startIntentSenderForResult(
                            context, intentSender, SELECT_DEVICE_REQUEST_CODE,
                            null, 0, 0, 0, null
                        )
                    }

                    override fun onAssociationCreated(associationInfo: AssociationInfo) {
                        Log.i(TAG, associationInfo.displayName.toString())
                    }

                    override fun onFailure(errorMessage: CharSequence?) {
                        Log.e(TAG, errorMessage.toString())
                    }
                }
            )
        } else {
            deviceManager.associate(pairingRequest,
                object : CompanionDeviceManager.Callback() {

                    @Deprecated("Deprecated in Java")
                    override fun onDeviceFound(chooserLauncher: IntentSender) {
                        startIntentSenderForResult(
                            context, chooserLauncher, SELECT_DEVICE_REQUEST_CODE,
                            null, 0, 0, 0, null
                        )
                    }

                    override fun onFailure(error: CharSequence?) {
                        Log.e(TAG, error.toString())
                    }
                }, null
            )
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.i(TAG, "Device request success in activity result")
                }

                else -> Log.e(TAG, "Device request error in activity result")
            }.also {
                return true
            }

            else -> return false
        }
    }
}
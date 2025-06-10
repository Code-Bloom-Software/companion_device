package br.com.codebloom.companion_device

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.le.ScanResult
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

class CompanionDeviceService(
    private val context: Activity,
    private val result: CompanionDeviceResult
) {
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
                        result.onDeviceFound()
                        startIntentSenderForResult(
                            context, intentSender, SELECT_DEVICE_REQUEST_CODE,
                            null, 0, 0, 0, null
                        )
                    }

                    override fun onAssociationCreated(associationInfo: AssociationInfo) {
                        Log.i(TAG, associationInfo.displayName.toString())
                        val device = AssociatedDevice(associationInfo.id,
                            associationInfo.displayName?.toString(),
                            associationInfo.deviceMacAddress?.toString())
                        result.onDeviceAssociated(device)
                    }

                    override fun onFailure(errorMessage: CharSequence?) {
                        Log.e(TAG, errorMessage.toString())
                        result.onAssociationError(errorMessage.toString())
                    }
                }
            )
        } else {
            deviceManager.associate(pairingRequest,
                object : CompanionDeviceManager.Callback() {

                    @Deprecated("Deprecated in Java")
                    override fun onDeviceFound(chooserLauncher: IntentSender) {
                        result.onDeviceFound()
                        startIntentSenderForResult(
                            context, chooserLauncher, SELECT_DEVICE_REQUEST_CODE,
                            null, 0, 0, 0, null
                        )
                    }

                    override fun onFailure(error: CharSequence?) {
                        Log.e(TAG, error.toString())
                        result.onAssociationError(error.toString())
                    }
                }, null
            )
        }
    }

    fun disassociate(associationId: Int?, deviceAddress: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (associationId != null) {
                deviceManager.disassociate(associationId)
            } else {
                throw Exception("Association Id must be NOT NULL for SDK >= 33")
            }
        } else {
            if (deviceAddress != null) {
                deviceManager.disassociate(deviceAddress)
            } else {
                throw Exception("Mac Address must be NOT NULL for SDK < 33")
            }
        }
    }

    fun fetchAssociations(): List<AssociatedDevice> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            deviceManager.myAssociations.map { AssociatedDevice(it.id, it.displayName?.toString(),
                it.deviceMacAddress?.toString()) }
        } else {
            deviceManager.associations.map { AssociatedDevice(null, null, it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return when (requestCode) {
            SELECT_DEVICE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG, "Device association success in activity result")
                        val intentResult: Any? = data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                        if (intentResult is ScanResult) {
                            val device = intentResult.device
                            val associatedDevice = AssociatedDevice(
                                null,
                                device.name,
                                device.address
                            )
                            result.onDeviceAssociated(associatedDevice)
                        }
                    }
                    else -> {
                        Log.e(TAG, "Device association error in activity result")
                        result.onAssociationError("Device association error. CODE: $resultCode")
                    }
                }
                true
            }
            else -> false
        }
    }

    interface CompanionDeviceResult {
        fun onDeviceFound()
        fun onDeviceAssociated(device: AssociatedDevice)
        fun onAssociationError(description: String)
    }
}
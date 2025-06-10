package br.com.codebloom.companion_device

import android.app.Activity
import android.content.Intent
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "CompanionDevice"

/** CompanionDevicePlugin */
class CompanionDevicePlugin: FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener,
    CompanionDeviceService.CompanionDeviceResult {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel : MethodChannel
    private var activity: Activity? = null
    private lateinit var service: CompanionDeviceService
    private var associateDeviceFoundResult: CompletableDeferred<Unit>? = null
    private var associateResult: CompletableDeferred<AssociatedDevice>? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "companion_device")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val args = call.arguments as? List<*>

        when (call.method) {
            "init" -> {
                val bindActivity = activity ?: return result.error("NO_ACTIVITY",
                    "Plugin not attached to activity", null)
                service = CompanionDeviceService(bindActivity, this)
                result.success(true)
            }
            "associate" -> {
                if (associateResult != null || associateDeviceFoundResult != null) {
                    Log.i(TAG, "associate() is already running", null)
                    return
                }
                val isSingleDevice = args?.getOrNull(0) as? Boolean ?: false
                val timeoutMs = args?.getOrNull(1) as? Int ?: 15000
                val nameRegex = args?.getOrNull(2) as? String
                val deviceAddress = args?.getOrNull(3) as? String
                val uuidList = args?.getOrNull(4) as? List<*>
                val uuids = uuidList?.filterIsInstance<String>() ?: emptyList()
                val filter = CompanionDeviceFilter(nameRegex, deviceAddress, uuids)
                associateDeviceFoundResult = CompletableDeferred()
                associateResult = CompletableDeferred()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        service.associate(isSingleDevice, filter)
                        withTimeout(timeoutMs.toLong()) {
                            associateDeviceFoundResult?.await()
                        }
                        val associatedDevice = associateResult?.await()
                        associatedDevice?.let {
                            result.success(
                                mapOf(
                                    "association_id" to it.associationId,
                                    "name" to it.name,
                                    "mac_address" to it.macAddress
                                )
                            )
                        }
                    } catch (e: TimeoutCancellationException) {
                        result.error("ASSOCIATION_ERROR", "TIMEOUT", null)
                    } catch (e: Exception) {
                        result.error("ASSOCIATION_ERROR", e.message, null)
                    } finally {
                        associateDeviceFoundResult = null
                        associateResult = null
                    }
                }
            }
            "disassociate" -> {
                val associationId = args?.getOrNull(0) as? Int
                val deviceAddress = args?.getOrNull(1) as? String
                service.disassociate(associationId, deviceAddress)
                result.success(true)
            }
            "associations" -> {
                val associations = service.fetchAssociations()
                result.success(associations.map {
                    mapOf(
                        "association_id" to it.associationId,
                        "name" to it.name,
                        "mac_address" to it.macAddress
                    )
                })
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDeviceFound() {
        associateDeviceFoundResult?.complete(Unit)
    }

    override fun onDeviceAssociated(device: AssociatedDevice) {
        associateResult?.complete(device)
    }

    override fun onAssociationError(description: String) {
        associateResult?.completeExceptionally(Exception(description))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return service.handleActivityResult(requestCode, resultCode, data)
    }
}

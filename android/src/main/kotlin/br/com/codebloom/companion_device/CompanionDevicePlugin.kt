package br.com.codebloom.companion_device

import android.app.Activity
import android.content.Intent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener

/** CompanionDevicePlugin */
class CompanionDevicePlugin: FlutterPlugin, MethodCallHandler, ActivityAware, ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var activity: Activity? = null
  private lateinit var service: CompanionDeviceService

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "companion_device")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    val args = call.arguments as? List<*>

    when (call.method) {
        "init" -> {
          val bindActivity = activity ?: return result.error("NO_ACTIVITY", "Plugin not attached to activity", null)
          service = CompanionDeviceService(bindActivity)
          result.success(true)
        }
        "associate" -> {
          val isSingleDevice = args?.getOrNull(0) as? Boolean ?: false
          val nameRegex = args?.getOrNull(1) as? String
          val deviceAddress = args?.getOrNull(2) as? String
          val uuidList = args?.getOrNull(3) as? List<*>
          val uuids = uuidList?.filterIsInstance<String>() ?: emptyList()
          val filter = CompanionDeviceFilter(nameRegex, deviceAddress, uuids)
          service.associate(isSingleDevice, filter)
          result.success(true)
        }
        else -> {
          result.notImplemented()
        }
    }
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

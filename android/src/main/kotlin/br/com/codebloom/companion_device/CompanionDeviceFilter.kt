package br.com.codebloom.companion_device

import android.bluetooth.le.ScanFilter
import android.companion.BluetoothLeDeviceFilter
import android.os.ParcelUuid
import java.util.regex.Pattern

data class CompanionDeviceFilter (
    val nameRegex: String?,
    val deviceAddress: String?,
    val uuids: List<String> = listOf()
)

fun CompanionDeviceFilter.toDeviceFilter(): BluetoothLeDeviceFilter {
    val filterBuilder: BluetoothLeDeviceFilter.Builder = BluetoothLeDeviceFilter.Builder()
    val scanFilterBuilder: ScanFilter.Builder = ScanFilter.Builder()
    nameRegex?.let {
        filterBuilder.setNamePattern(Pattern.compile(it))
    }
    deviceAddress?.let {
        scanFilterBuilder.setDeviceAddress(deviceAddress)
    }
    uuids.forEach {
        scanFilterBuilder.setServiceUuid(ParcelUuid.fromString(it))
    }
    filterBuilder.setScanFilter(scanFilterBuilder.build())

    return filterBuilder.build()
}
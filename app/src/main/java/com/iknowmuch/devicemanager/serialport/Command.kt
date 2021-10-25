package com.iknowmuch.devicemanager.serialport

import java.util.*

/**
 *@author: Chen
 *@createTime: 2021/10/13 17:18
 *@description:
 **/
data class Command(

    val head: UByte = Head,
    //命令类型
    val type: UByte = 0x00.toUByte(),
    //命令
    val cmd: UByte,

    //数据
    val data: Array<UByte>,
    //帧尾
    val tail: UByte = Tail
) {
    val _cmd = when (cmd) {
        CMD.Version.ubyte -> CMD.Version
        CMD.OpenAll.ubyte -> CMD.OpenAll
        CMD.Open.ubyte -> CMD.Open
        CMD.DoorState.ubyte -> CMD.DoorState
        CMD.ProbeState.ubyte -> CMD.ProbeState
        else -> CMD.StopCharging
    }

    //数据长度
    val dataSize: Int = data.size

    private val arrayOfDataSize: Array<UByte> = "%04x".format(dataSize).toHexUBytes()


    //校验和
    val checksum: UByte = (type + cmd + dataSize.toUByte() + getDataSum()).toUByte()

    private fun getDataSum(): UByte {
        var i = 0
        data.forEach {
            i += it.toInt()
        }
        return i.toUByte()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Command

        if (head != other.head) return false
        if (type != other.type) return false
        if (cmd != other.cmd) return false
        if (dataSize != other.dataSize) return false
        if (!data.contentEquals(other.data)) return false
        if (checksum != other.checksum) return false
        if (tail != other.tail) return false

        return true
    }

    override fun hashCode(): Int {
        var result = head.toInt()
        result = 31 * result + type.toInt()
        result = 31 * result + cmd.toInt()
        result = 31 * result + dataSize
        result = 31 * result + data.contentHashCode()
        result = 31 * result + checksum.toInt()
        result = 31 * result + tail.toInt()
        return result
    }

    @ExperimentalUnsignedTypes
    fun toUBytes() = (ubyteArrayOf(head, type, cmd) + arrayOfDataSize + data + ubyteArrayOf(
        checksum,
        tail
    )).toUByteArray()

    sealed class CMD(val ubyte: UByte) {
        //设备版本号获取
        object Version : CMD(0x72.toUByte())

        //设备所有柜门开启
        object OpenAll : CMD(0x12.toUByte())

        //开启柜门
        object Open : CMD(0x14.toUByte())

        //设备柜门当前状态读取
        object DoorState : CMD(0x20.toUByte())

        //设备在线状态
        object ProbeState : CMD(0x16.toUByte())

        //设备对应通道充电时间结束通知
        object StopCharging : CMD(0x17.toUByte())
    }
}

val Head = 0xBB.toUByte()
val Tail = 0x7E.toUByte()
fun String.toHexUBytes(): Array<UByte> {
    if (isNullOrEmpty()) return arrayOf()
    val string = if (this.length % 2 == 0) this else "0$this"
    val bytes = Array(size = string.length / 2) { UByte.MIN_VALUE }
    for (i in string.indices step 2) {
        bytes[i / 2] = ("${string[i]}${string[i + 1]}").toInt(16).toUByte()
    }
    return bytes
}

private const val HEX_FORMATTER = "%02x"
fun Iterable<UByte>.joinToHexString(): String {
    return joinToString("") { ub ->
        HEX_FORMATTER.format(ub.toInt())
            .uppercase(Locale.getDefault())
    }
}

fun Array<UByte>.joinToHexString(): String {
    return joinToString("") { ub ->
        HEX_FORMATTER.format(ub.toInt())
            .uppercase(Locale.getDefault())
    }
}

@ExperimentalUnsignedTypes
fun UByteArray.toCommand(): Command {
    if (size < 7) throw RuntimeException("this u byte array size less than minimum length")
    if (Head != firstOrNull() || Tail != lastOrNull()) throw RuntimeException("this u byte array can't convert to Command class")
    val dataSize = sliceArray(3..4).joinToHexString().toInt(16)
    return Command(
        cmd = get(2),
        data = sliceArray(5 until 5 + dataSize).toTypedArray()
    )
}

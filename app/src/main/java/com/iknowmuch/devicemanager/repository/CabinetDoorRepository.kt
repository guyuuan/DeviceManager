package com.iknowmuch.devicemanager.repository

import com.iknowmuch.devicemanager.bean.CabinetDoor
import com.iknowmuch.devicemanager.db.dao.CabinetDoorDao

/**
 *@author: Chen
 *@createTime: 2021/9/23 11:29
 *@description:
 **/
class CabinetDoorRepository(private val cabinetDoorDao: CabinetDoorDao) {

    suspend fun insertCabinetDoor(data: List<CabinetDoor>) = cabinetDoorDao.insertCabinetDoors(data)

    fun getCabinetDoorFlow() = cabinetDoorDao.getCabinetDoors()

//    private suspend fun updateCabinetDoorInfo(door: CabinetDoor) =
//        cabinetDoorDao.updateCabinetDoor(door)

    private suspend fun getCabinetDoorById(id: Int) = cabinetDoorDao.getCabinetDoorById(id)

//    suspend fun deleteCabinetDoor(door: CabinetDoor) = cabinetDoorDao.deleteCabinetDoor(door)

    suspend fun updateCabinetDoor(id: Int, modifier: (CabinetDoor?) -> CabinetDoor?) {
        modifier(getCabinetDoorById(id))?.let {
            cabinetDoorDao.updateCabinetDoor(it)
        }
    }
}
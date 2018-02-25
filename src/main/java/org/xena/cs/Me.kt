/*
 *    Copyright 2016 Jonathan Beaudoin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.xena.cs

import org.xena.offsets.OffsetManager.clientModule
import org.xena.offsets.OffsetManager.process
import org.xena.offsets.offsets.Offsets.m_bCanReload
import org.xena.offsets.offsets.Offsets.m_dwEntityList
import org.xena.offsets.offsets.Offsets.m_dwLocalPlayer
import org.xena.offsets.offsets.Offsets.m_hActiveWeapon
import org.xena.offsets.offsets.Offsets.m_hMyWeapons
import org.xena.offsets.offsets.Offsets.m_iClip1
import org.xena.offsets.offsets.Offsets.m_iClip2
import org.xena.offsets.offsets.Offsets.m_iCrossHairID
import org.xena.offsets.offsets.Offsets.m_iShotsFired

class Me : Player() {
	
	var activeWeapon = Weapon()
		private set
	
	var target: Player? = null
		private set
	
	var shotsFired: Long = 0
		private set
	
	override fun update() {
		setAddress(clientModule().readUnsignedInt(m_dwLocalPlayer.toLong()))
		super.update()
		
		val activeWeaponIndex = process().readUnsignedInt(address() + m_hActiveWeapon) and 0xFFF
		for (i in 0..weaponIds.size - 1) {
			val currentWeaponIndex = process().readUnsignedInt(address() + m_hMyWeapons.toLong() + ((i - 1) * 0x04).toLong()) and 0xFFF
			val weaponAddress = clientModule().readUnsignedInt(m_dwEntityList + (currentWeaponIndex - 1) * 0x10)
			
			if (weaponAddress > 0 && activeWeaponIndex == currentWeaponIndex) {
				processWeapon(weaponAddress, i, true)
			}
		}
		/*		if (activeWeapon.getWeaponID() == 42 || activeWeapon.getWeaponID() == 516) {
		int modelAddress = process().readInt(address() + m_hViewModel) & 0xFFF;
		long ds = clientModule().readUnsignedInt(m_dwEntityList + (modelAddress - 1) * 0x10);
		process().writeInt(ds + m_nModelIndex, 403);
		process().writeInt(weaponAddress + iViewModelIndex, 403);
		process().writeInt(weaponAddress + iWorldModelIndex, 404);
		process().writeInt(weaponAddress + m_iWorldDroppedModelIndex, 405);
		process().writeInt(weaponAddress + m_iItemDefinitionIndex, 515);
		process().writeInt(weaponAddress + m_iWeaponID, 516);
	}*/
		
		target = null
		val crosshair = process().readUnsignedInt(address() + m_iCrossHairID) - 1
		if (crosshair > -1 && crosshair <= 1024) {
			val entity = entities[clientModule().readUnsignedInt(m_dwEntityList + crosshair * 0x10)]
			if (entity != null) {
				target = entity as Player
			}
		}
		
		shotsFired = process().readUnsignedInt(address() + m_iShotsFired)
	}
	
	override fun processWeapon(weaponAddress: Long, index: Int, active: Boolean): Int {
		val weaponId = super.processWeapon(weaponAddress, index, active)
		if (active) {
			activeWeapon.weaponID = weaponId.toLong()
			activeWeapon.canReload = process().readBoolean(weaponAddress + m_bCanReload)
			activeWeapon.clip1 = process().readUnsignedInt(weaponAddress + m_iClip1)
			activeWeapon.clip2 = process().readUnsignedInt(weaponAddress + m_iClip2)
		}
		return weaponId
	}
}
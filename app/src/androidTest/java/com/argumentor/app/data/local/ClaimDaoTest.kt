package com.argumentor.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.argumentor.app.data.local.dao.ClaimDao
import com.argumentor.app.data.model.Claim
import com.argumentor.app.util.FingerprintUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClaimDaoTest {

    private lateinit var database: ArguMentorDatabase
    private lateinit var claimDao: ClaimDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ArguMentorDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        claimDao = database.claimDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveClaim() = runBlocking {
        val claim = Claim(
            text = "Test Claim",
            stance = Claim.Stance.PRO,
            strength = Claim.Strength.HIGH,
            topics = listOf("topic1"),
            claimFingerprint = FingerprintUtils.generateTextFingerprint("Test Claim")
        )

        claimDao.insertClaim(claim)

        val retrieved = claimDao.getClaimById(claim.id)
        assertNotNull(retrieved)
        assertEquals(claim.text, retrieved?.text)
        assertEquals(claim.stance, retrieved?.stance)
        assertEquals(claim.strength, retrieved?.strength)
    }

    @Test
    fun getAllClaims() = runBlocking {
        val claim1 = Claim(text = "Claim 1")
        val claim2 = Claim(text = "Claim 2")

        claimDao.insertClaim(claim1)
        claimDao.insertClaim(claim2)

        val claims = claimDao.getAllClaims().first()
        assertEquals(2, claims.size)
    }

    @Test
    fun getClaimByFingerprint() = runBlocking {
        val claim = Claim(
            text = "Unique Claim",
            claimFingerprint = FingerprintUtils.generateTextFingerprint("Unique Claim")
        )

        claimDao.insertClaim(claim)

        val retrieved = claimDao.getClaimByFingerprint(claim.claimFingerprint)
        assertNotNull(retrieved)
        assertEquals(claim.id, retrieved?.id)
    }

    @Test
    fun updateClaim() = runBlocking {
        val claim = Claim(text = "Original")
        claimDao.insertClaim(claim)

        val updated = claim.copy(text = "Updated", stance = Claim.Stance.CON)
        claimDao.updateClaim(updated)

        val retrieved = claimDao.getClaimById(claim.id)
        assertEquals("Updated", retrieved?.text)
        assertEquals(Claim.Stance.CON, retrieved?.stance)
    }

    @Test
    fun deleteClaim() = runBlocking {
        val claim = Claim(text = "To Delete")
        claimDao.insertClaim(claim)

        claimDao.deleteClaim(claim)

        val retrieved = claimDao.getClaimById(claim.id)
        assertNull(retrieved)
    }

    @Test
    fun getClaimCount() = runBlocking {
        assertEquals(0, claimDao.getClaimCount())

        claimDao.insertClaim(Claim(text = "Claim 1"))
        claimDao.insertClaim(Claim(text = "Claim 2"))

        assertEquals(2, claimDao.getClaimCount())
    }
}

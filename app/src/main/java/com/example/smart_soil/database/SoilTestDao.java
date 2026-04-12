package com.example.smart_soil.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SoilTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SoilTestEntity soilTest);

    @Update
    void update(SoilTestEntity soilTest);

    @Delete
    void delete(SoilTestEntity soilTest);

    @Query("SELECT * FROM soil_tests WHERE farmId = :farmId ORDER BY testDate DESC")
    LiveData<List<SoilTestEntity>> getTestsForFarm(int farmId);

    @Query("SELECT * FROM soil_tests WHERE isSynced = 0")
    List<SoilTestEntity> getUnsyncedTests();

    @Query("DELETE FROM soil_tests")
    void deleteAll();
}

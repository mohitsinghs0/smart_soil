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
public interface FarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FarmEntity farm);

    @Update
    void update(FarmEntity farm);

    @Delete
    void delete(FarmEntity farm);

    @Query("SELECT * FROM farms WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<FarmEntity>> getAllFarmsByUser(String userId);

    @Query("SELECT * FROM farms ORDER BY createdAt DESC")
    LiveData<List<FarmEntity>> getAllFarms();

    @Query("SELECT * FROM farms WHERE isSynced = 0")
    List<FarmEntity> getUnsyncedFarms();

    @Query("SELECT * FROM farms WHERE id = :id")
    FarmEntity getFarmById(int id);

    @Query("SELECT * FROM farms WHERE serverId = :serverId LIMIT 1")
    FarmEntity getFarmByServerId(String serverId);

    @Query("DELETE FROM farms")
    void deleteAll();
}

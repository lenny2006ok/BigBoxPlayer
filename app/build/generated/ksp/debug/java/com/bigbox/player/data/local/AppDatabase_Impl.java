package com.bigbox.player.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SeriesDao _seriesDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `series_categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `series` (`seriesId` INTEGER NOT NULL, `name` TEXT NOT NULL, `cover` TEXT, `categoryId` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`seriesId`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '761ba99fe73c6dc4a2db491696092630')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `series_categories`");
        _db.execSQL("DROP TABLE IF EXISTS `series`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsSeriesCategories = new HashMap<String, TableInfo.Column>(2);
        _columnsSeriesCategories.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeriesCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSeriesCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSeriesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSeriesCategories = new TableInfo("series_categories", _columnsSeriesCategories, _foreignKeysSeriesCategories, _indicesSeriesCategories);
        final TableInfo _existingSeriesCategories = TableInfo.read(_db, "series_categories");
        if (! _infoSeriesCategories.equals(_existingSeriesCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "series_categories(com.bigbox.player.data.local.SeriesCategoryEntity).\n"
                  + " Expected:\n" + _infoSeriesCategories + "\n"
                  + " Found:\n" + _existingSeriesCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsSeries = new HashMap<String, TableInfo.Column>(5);
        _columnsSeries.put("seriesId", new TableInfo.Column("seriesId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("cover", new TableInfo.Column("cover", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("categoryId", new TableInfo.Column("categoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSeries.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSeries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSeries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSeries = new TableInfo("series", _columnsSeries, _foreignKeysSeries, _indicesSeries);
        final TableInfo _existingSeries = TableInfo.read(_db, "series");
        if (! _infoSeries.equals(_existingSeries)) {
          return new RoomOpenHelper.ValidationResult(false, "series(com.bigbox.player.data.local.SeriesEntity).\n"
                  + " Expected:\n" + _infoSeries + "\n"
                  + " Found:\n" + _existingSeries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "761ba99fe73c6dc4a2db491696092630", "4511826d18f087a948d3133813983e18");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "series_categories","series");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `series_categories`");
      _db.execSQL("DELETE FROM `series`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SeriesDao.class, SeriesDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public SeriesDao seriesDao() {
    if (_seriesDao != null) {
      return _seriesDao;
    } else {
      synchronized(this) {
        if(_seriesDao == null) {
          _seriesDao = new SeriesDao_Impl(this);
        }
        return _seriesDao;
      }
    }
  }
}

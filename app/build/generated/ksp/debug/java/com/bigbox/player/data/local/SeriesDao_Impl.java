package com.bigbox.player.data.local;

import android.database.Cursor;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SeriesDao_Impl implements SeriesDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SeriesCategoryEntity> __insertionAdapterOfSeriesCategoryEntity;

  private final EntityInsertionAdapter<SeriesEntity> __insertionAdapterOfSeriesEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearSeriesByCategory;

  public SeriesDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSeriesCategoryEntity = new EntityInsertionAdapter<SeriesCategoryEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `series_categories` (`id`,`name`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SeriesCategoryEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
      }
    };
    this.__insertionAdapterOfSeriesEntity = new EntityInsertionAdapter<SeriesEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `series` (`seriesId`,`name`,`cover`,`categoryId`,`lastUpdated`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SeriesEntity value) {
        stmt.bindLong(1, value.getSeriesId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        if (value.getCover() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getCover());
        }
        if (value.getCategoryId() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getCategoryId());
        }
        stmt.bindLong(5, value.getLastUpdated());
      }
    };
    this.__preparedStmtOfClearSeriesByCategory = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM series WHERE categoryId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCategories(final List<SeriesCategoryEntity> categories,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSeriesCategoryEntity.insert(categories);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertSeries(final List<SeriesEntity> series,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSeriesEntity.insert(series);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object clearSeriesByCategory(final String catId,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearSeriesByCategory.acquire();
        int _argIndex = 1;
        if (catId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, catId);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfClearSeriesByCategory.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<SeriesCategoryEntity>> getCategories() {
    final String _sql = "SELECT * FROM series_categories";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"series_categories"}, new Callable<List<SeriesCategoryEntity>>() {
      @Override
      public List<SeriesCategoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final List<SeriesCategoryEntity> _result = new ArrayList<SeriesCategoryEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final SeriesCategoryEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item = new SeriesCategoryEntity(_tmpId,_tmpName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<SeriesEntity>> getSeriesByCategory(final String catId) {
    final String _sql = "SELECT * FROM series WHERE categoryId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (catId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, catId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"series"}, new Callable<List<SeriesEntity>>() {
      @Override
      public List<SeriesEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCover = CursorUtil.getColumnIndexOrThrow(_cursor, "cover");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final List<SeriesEntity> _result = new ArrayList<SeriesEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final SeriesEntity _item;
            final int _tmpSeriesId;
            _tmpSeriesId = _cursor.getInt(_cursorIndexOfSeriesId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpCover;
            if (_cursor.isNull(_cursorIndexOfCover)) {
              _tmpCover = null;
            } else {
              _tmpCover = _cursor.getString(_cursorIndexOfCover);
            }
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            _item = new SeriesEntity(_tmpSeriesId,_tmpName,_tmpCover,_tmpCategoryId,_tmpLastUpdated);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

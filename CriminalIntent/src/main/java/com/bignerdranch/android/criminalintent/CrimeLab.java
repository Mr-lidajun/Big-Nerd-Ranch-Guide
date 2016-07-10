package com.bignerdranch.android.criminalintent;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bignerdranch.android.criminalintent.CrimeDbSchema.CrimeTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/5 11:57.
 * @desc: 数据集中存储池，用来存储Crime对象
 */
public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(context).getWritableDatabase();
    }

    public void addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    public void deleteCrime(Crime c) {
        String uuidString = c.getId().toString();
        mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?", new String[] { uuidString });
    }

    /**
     * 返回crime列表
     * @return
     */
    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    /**
     * 获取指定的crime
     * @param id
     * @return
     */
    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() });

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    /**
     * 定位图片文件
     * getExternalFilesDir(String):
     *  获取主外部存储上存放常规文件的文件目录。通过String参数，可访问特定内容类型的子目录。内容类型常量以DIRECTORY_为前缀，
     *  定义在Environment中。例如，用于图像文件的Environment.DIRECTORY_PICTURES
     * @param crime
     * @return
     */
    public File getPhotoFile(Crime crime) {
        File externalFilesDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }

        return new File(externalFilesDir, crime.getPhotoFileName());

    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values, CrimeTable.Cols.UUID + " = ?", new String[]{ uuidString });
    }

    /**
     * 获取ContentValues（负责处理数据库写入和更新操作的辅助类）
     * @param crime
     * @return
     */
    public static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        values.put(CrimeTable.Cols.CONTACT_ID, crime.getContactId());

        return values;
    }

    /**
     * 创建模型层对象-使用cursor封装方法
     * @param whereClause
     * @param whereArgs
     * @return
     */
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor =
                mDatabase.query(CrimeTable.NAME,
                        null, // Columns - null selects all columns
                        whereClause, 
                        whereArgs,
                        null, // groupBy
                        null, // having
                        null); // orderBy
        return new CrimeCursorWrapper(cursor);
    }

}

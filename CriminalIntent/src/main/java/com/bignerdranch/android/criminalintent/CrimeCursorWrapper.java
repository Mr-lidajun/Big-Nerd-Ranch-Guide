package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

import com.bignerdranch.android.criminalintent.CrimeDbSchema.CrimeTable;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/6/26 11:50.
 * @desc: ${todo}
 */
public class CrimeCursorWrapper extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        long date = getLong(getColumnIndex(CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        long contactId = getLong(getColumnIndex(CrimeTable.Cols.CONTACT_ID));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setSolved(isSolved != 0);
        crime.setSuspect(suspect);
        crime.setContactId(contactId);

        return crime;
    }

}

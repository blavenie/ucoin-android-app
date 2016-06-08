package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntitySql.base.AbstractSql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class ContactSql extends AbstractSql<Contact> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(ContactTable.TABLE_NAME+"/").build();
    public static final int CODE = 30;


    public ContactSql(Context context) {
        super(context,URI);
    }

    public String isContact(String uid, String publicKey,long currencyId) {
        String result = null;
        Cursor cursor = query(
                ContactTable.UID + "=? AND "+ContactTable.PUBLIC_KEY + "=? AND "+ContactTable.CURRENCY_ID + "=?",
                new String[]{uid,publicKey,String.valueOf(currencyId)});
        if (cursor.moveToFirst()){
            result = cursor.getString(cursor.getColumnIndex(ContactTable.ALIAS));
        }
        cursor.close();
        return result;
    }

    public Map<String,Contact> findByPublicKey(Currency currency, String publicKey) {
        Map<String,Contact> contacts = new HashMap<>();
        Contact contact;
        Cursor cursor = query(
                ContactTable.CURRENCY_ID + "=? AND " + ContactTable.PUBLIC_KEY + " LIKE ?",
                new String[]{String.valueOf(currency.getId()),"%"+publicKey+"%"},ContactTable.PUBLIC_KEY+" DESC");
        if (cursor.moveToFirst()){
            do {
                contact = fromCursor(cursor);
                contact.setCurrency(currency);
                contacts.put(contact.getPublicKey(),contact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    public Map<String,Contact> findByName(Currency currency,String search) {
        Map<String,Contact> contacts = new HashMap<>();
        Contact contact;
        Cursor cursor = query(
                ContactTable.CURRENCY_ID + "=? AND (" + ContactTable.ALIAS + " LIKE ? OR " + ContactTable.UID + " LIKE ? )",
                new String[]{String.valueOf(currency.getId()),search+"%",search+"%"},ContactTable.ALIAS+" DESC");
        if (cursor.moveToFirst()){
            do {
                contact = fromCursor(cursor);
                contact.setCurrency(currency);
                contacts.put(contact.getPublicKey(),contact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    public Map<String, Contact> findAllInMap(Currency currency) {
        Map<String,Contact> contacts = new HashMap<>();
        Contact contact;
        Cursor cursor = query(
                ContactTable.CURRENCY_ID + "=?",
                new String[]{String.valueOf(currency.getId())},ContactTable.ALIAS+" ASC");
        if (cursor.moveToFirst()){
            do {
                contact = fromCursor(cursor);
                contact.setCurrency(currency);
                contacts.put(contact.getPublicKey(),contact);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + ContactTable.TABLE_NAME + "(" +
                ContactTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                ContactTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                ContactTable.ALIAS + TEXT + NOTNULL + COMMA +
                ContactTable.UID + TEXT + NOTNULL + COMMA +
                ContactTable.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                "FOREIGN KEY (" + ContactTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ")" + COMMA +
                UNIQUE + "(" + ContactTable.CURRENCY_ID + COMMA + ContactTable.UID + ")" + COMMA +
                UNIQUE + "(" + ContactTable.CURRENCY_ID + COMMA + ContactTable.PUBLIC_KEY + ")" +
                ")";
    }

    @Override
    public Contact fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ContactTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(ContactTable.CURRENCY_ID);
        int aliasIndex = cursor.getColumnIndex(ContactTable.ALIAS);
        int uidIndex = cursor.getColumnIndex(ContactTable.UID);
        int publicKeyIndex = cursor.getColumnIndex(ContactTable.PUBLIC_KEY);

        Contact contact = new Contact();
        contact.setId(cursor.getLong(idIndex));
        contact.setUid(cursor.getString(uidIndex));
        contact.setPublicKey(cursor.getString(publicKeyIndex));
        contact.setAlias(cursor.getString(aliasIndex));
        contact.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        contact.setContact(true);

        return contact;
    }

    @Override
    public ContentValues toContentValues(Contact entity) {
        ContentValues values = new ContentValues();
        values.put(ContactTable.CURRENCY_ID, entity.getCurrency().getId());
        if (entity.getAlias()!=null && entity.getAlias().length()>0){
            values.put(ContactTable.ALIAS, entity.getAlias());
        }else{
            values.put(ContactTable.ALIAS,entity.getUid());
        }
        values.put(ContactTable.UID, entity.getUid());
        values.put(ContactTable.PUBLIC_KEY, entity.getPublicKey());
        return values;
    }

    public class ContactTable implements BaseColumns {
        public static final String TABLE_NAME = "contact";

        public static final String CURRENCY_ID = "currency_id";
        public static final String ALIAS = "alias";
        public static final String UID = "uid";
        public static final String PUBLIC_KEY = "public_key";
    }
}

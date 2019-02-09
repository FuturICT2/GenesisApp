package com.example.mcb.genesisapp.Repository.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.mcb.genesisapp.State.StateCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import Features.IFeature;
import Features.IOperation;
import Features.IUnderlying;
import Features.operations.IOperationProof;
import Features.operations.IProof;
import Features.operations.actions.IAction;
import Features.properties.IProperty;
import Features.properties.basic.DecimalsProperty;
import Features.properties.basic.GeneralSupplyProperty;
import Features.properties.basic.GenesisSupplyProperty;
import Features.properties.basic.NameProperty;
import Features.properties.basic.SymbolProperty;
import Repository.IRepository;
import Token.basic.BasicToken;
import Utilities.IAddress;

/**
 * Implementation of IRepository. Connects to a SQLite Database.
 * Created by mcb on 11.02.18.
 */

public class BasicSQLiteRepo implements IRepository<BasicToken>, ISQLiteRepo {


    private Context context;
    private SQLiteDatabase database;
    private BasicOpenHelper basicDBHelper;


    public BasicSQLiteRepo(StateCallback stateCallback) {
        this.context = stateCallback.getContext();
        basicDBHelper = BasicOpenHelper.getInstance(context);
        open();
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    @Override
    public void open() throws SQLException {
        database = basicDBHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        //database.close();
        basicDBHelper.close();
    }

    @Override
    public BasicToken crateNewToken(Collection<IFeature> collection) {
        List<IUnderlying> underlyings = new ArrayList<>();
        List<IProperty> properties = new ArrayList<>();
        List<IOperation> operations = new ArrayList<>();
        for(IFeature feature: collection){
            switch (feature.getFeatureType()){
                case UNDERLYING:
                    underlyings.add((IUnderlying) feature);
                    break;
                case PROPERTY:
                    properties.add((IProperty) feature);
                    break;
                case OPERATION:
                    operations.add((IOperation) feature);
                    break;
            }
        }
        return new BasicToken(operations,properties,underlyings,this);
    }

    @Override
    public long store(BasicToken token) {

        /*
        Check if Token already exists, if yes, return id of token
         */
        //Todo check if condition is enough
        Cursor cursor = database.query(BasicSQLContracts.BasicTokenTable.TABLE_NAME,
                BasicSQLContracts.BasicTokenTable.allColumns, BasicSQLContracts.BasicTokenTable.COLUMN_NAME_NAME +
                        " = ? AND " +
                        BasicSQLContracts.BasicTokenTable.COLUMN_NAME_SYMBOL + " = ? ",
                new String[]{token.getName(),token.getSymbol()},
                null, null, null, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            long id = cursor.getLong(BasicSQLContracts.BasicTokenTable.idColumn);
            cursor.close();
            return id;
        }
        cursor.close();

        /*
        Save new Token
         */
        ContentValues values = new ContentValues();
        values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_NAME,token.getName());
        values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_SYMBOL,token.getSymbol());
        values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_DECIMALS,token.getDecimals());
        if(token.isCapped()) {
            values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_GENERAL_SUPPLY, token.cappedAmount());
        }else{
            values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_GENERAL_SUPPLY,-1);
        }
        if(token.preMined()){
            values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_GENESIS_SUPPLY,token.preMinedAmount());
        }else{
            values.put(BasicSQLContracts.BasicTokenTable.COLUMN_NAME_GENESIS_SUPPLY,-1);
        }

        long id = database.insert(BasicSQLContracts.BasicTokenTable.TABLE_NAME, null, values);


        saveOtherProperties(id, token.getProperties());
        saveOperations(id, token.getOperations());
        saveUnderlyings(id, token.getUnderlyings());

        return id;
    }

    protected void saveUnderlyings(long id, Collection<IUnderlying> underlyings){

    }
    protected void saveOtherProperties(long id, Collection<IProperty> properties){

    }
    protected void saveOperations(long id, Collection<IOperation> operations){

    }

    protected BasicToken fetchToken(Cursor cursor){
        return new BasicToken(cursor.getLong(BasicSQLContracts.BasicTokenTable.idColumn),this);
    }

    @Override
    public BasicToken fetchToken(long l) {
        return new BasicToken(l, this);
    }

    @Override
    public Collection<BasicToken> getAllTokens() {
        Cursor cursor = database.rawQuery("SELECT * FROM "+ BasicSQLContracts.BasicTokenTable.TABLE_NAME,null);
        cursor.moveToFirst();
        List returnList = new ArrayList();
        while (!cursor.isAfterLast()) {
            returnList.add(fetchToken(cursor));
            cursor.moveToNext();
        }
        return returnList;
    }

    @Override
    public Collection<IFeature> getFeatures(BasicToken basicToken) {
        return null;
    }

    @Override
    public Collection<IOperation> getOperations(BasicToken basicToken) {
        return new ArrayList<>();
    }

    @Override
    public Collection<IUnderlying> getUnderlyings(BasicToken basicToken) {
        return new ArrayList<>();
    }

    @Override
    public Collection<IProperty> getProperties(BasicToken basicToken) {

        /*
        Basic properties
         */
        // Get cursor from database.
        Cursor cursor = database.query(
                BasicSQLContracts.BasicTokenTable.TABLE_NAME,
               BasicSQLContracts.BasicTokenTable.allColumns,
                BasicSQLContracts.BasicTokenTable._ID + " = ? ",
                new String[]{String.valueOf(basicToken.getUniqueID())}, null, null, null
        );
        cursor.moveToNext();
        List<IProperty> returnList = new ArrayList();
        if(cursor.isAfterLast())
            return returnList;

        returnList.add(new NameProperty(cursor.getString(BasicSQLContracts.BasicTokenTable.nameColumng)));
        returnList.add(new SymbolProperty(cursor.getString(BasicSQLContracts.BasicTokenTable.symbolColumn)));
        returnList.add(new DecimalsProperty(cursor.getInt(BasicSQLContracts.BasicTokenTable.decimalsColumn)));
        returnList.add(new GeneralSupplyProperty(cursor.getInt(BasicSQLContracts.BasicTokenTable.totalSupplyColumn)));
        returnList.add(new GenesisSupplyProperty(cursor.getInt(BasicSQLContracts.BasicTokenTable.genesisSupplyColumn)));

        // Todo fetch other properties
        return returnList;
    }

    @Override
    public int getCurrentTotalSupply(BasicToken basicToken) {
        return 0;
    }

    @Override
    public int getBalanceOf(BasicToken basicToken, IAddress iAddress) {
        return 0;
    }

    @Override
    public Map<IAddress, Integer> getAllBalances(BasicToken basicToken) {
        return null;
    }

    //Todo: create Transaction Table
    //Todo: create value table
    private boolean transfer(BasicToken basicToken, IAddress iAddress, IAddress iAddress1, int i, IProof iProof) {
        return false;
    }

    /*
        Action BAsed functionalities
     */
    @Override
    public boolean store(IAction iAction, IOperationProof iOperationProof) {
        return false;
    }

    @Override
    public boolean contains(IAction iAction) {
        return false;
    }
}

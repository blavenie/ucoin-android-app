package org.duniter.app.model.EntityServices;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;

import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityJson.TxJson;
import org.duniter.app.model.EntityJson.UdJson;
import org.duniter.app.model.EntityWeb.TxWeb;
import org.duniter.app.model.EntityWeb.UdReceivedWeb;
import org.duniter.app.model.EntityWeb.UdWeb;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.technical.callback.CallbackTx;
import org.duniter.app.technical.callback.CallbackUdReceived;

/**
 * Created by naivalf27 on 02/05/16.
 */
public class TxService {

    public static void getListTx(final Context context, final Wallet wallet, final Map<String,String> mapMember, final CallbackTx callback){
        TxWeb txWeb = new TxWeb(context,wallet.getCurrency(),wallet.getPublicKey());
        txWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code == 200){
                    TxJson txJson = TxJson.fromJson(response);
                    List<Tx> txList = TxJson.fromTx(txJson,wallet,mapMember);
                    List<String> listSource = TxJson.fromTxGetSourcesPending(txJson);
                    if (callback!=null){
                        callback.methode(txList,listSource);
                    }
                }else{
                    Log.d("TX SERVICE", "error get Tx code:"+code);
                }
            }
        });
    }

    public static void getListUd(final Context context, final Wallet wallet, final CallbackUdReceived callback){
        UdReceivedWeb udReceivedWeb = new UdReceivedWeb(context,wallet.getCurrency(),wallet.getPublicKey());
        udReceivedWeb.getData(new WebService.WebServiceInterface() {
            @Override
            public void getDataFinished(int code, String response) {
                if (code == 200){
                    List<Tx> txList = UdJson.fromTx(response,wallet);
                    if (callback!=null){
                        callback.methode(txList);
                    }
                }else{
                    Log.d("TX SERVICE", "error get Tx code:"+code);
                }
            }
        });
    }
}

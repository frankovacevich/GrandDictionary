package com.subtlebit.fran_.croatiandictionary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;


public class Payment{

    private Context context;
    private MainActivity activity;
    private BillingClient BC;
    public String FullVersionSKU = "";
    public String SignatureKey = "";
    public boolean PurchasedFlag = false;

    public Payment(Context c, MainActivity a){
        context = c;
        activity = a;

        FullVersionSKU = activity.getResources().getString(R.string.paySKU);
        //FullVersionSKU = "android.test.purchased";
        SignatureKey = activity.getResources().getString(R.string.paySIGNATURE);



        BC = BillingClient.newBuilder(activity).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
                Log.i("BILLING_TAG", "onPurchasesUpdated() response: " + responseCode);
                if(responseCode == BillingClient.BillingResponse.OK && purchases != null){

                    if(PurchasedFlag) {
                        PurchasedFlag = false;
                        activity.ActivateFullMode();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(activity.getResources().getString(R.string.app_name));
                        builder.setMessage("Thanks for getting the full version of the app!\n\nIf you have some time, we would love to get your opinion on it. Just click on RATE THE APP");
                        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();

                    }
                }
            }
        }).build();
    }

    private boolean VerifyPurchaseID(String signature){
        if(signature.equals(SignatureKey)){
            return true;
        }
        return false;
    }

    public void StartConnection(BillingClientStateListener listener){
        BC.startConnection(listener);
    }

    public boolean RecheckOwnership(){
        List<Purchase> PurchaseList =  BC.queryPurchases("inapp").getPurchasesList();
        if(PurchaseList == null) return false;

        for (Purchase p : PurchaseList){
            if(p.getSku().equals(FullVersionSKU))
                return true;
        }

        return false;
    }

    public void RemoveOwnership(){
        List<Purchase> PurchaseList =  BC.queryPurchases("inapp").getPurchasesList();
        if(PurchaseList == null) return;

        for (Purchase p : PurchaseList){
            if(p.getSku().equals(FullVersionSKU)){

                BC.consumeAsync(p.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        Log.i("OWN","response: " + Integer.toString(responseCode));
                    }
                });
            }
        }
    }

    public void QuerySingleInappSku(String sku, SkuDetailsResponseListener listener){
        List<String> skus = new ArrayList<>();
        skus.add(sku);
        SkuDetailsParams skuDetailsParams = SkuDetailsParams.newBuilder().setType("inapp").setSkusList(skus).build();
        BC.querySkuDetailsAsync(skuDetailsParams,listener);
    }

    public void startPurchaseFlow(final String skuId, final String billingType) {
        BillingFlowParams.Builder BFP = BillingFlowParams.newBuilder()
                .setSku(skuId)
                .setType(billingType);
        PurchasedFlag = true;
        int responseCode = BC.launchBillingFlow(activity, BFP.build());
    }
}

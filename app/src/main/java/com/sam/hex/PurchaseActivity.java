package com.sam.hex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;

public abstract class PurchaseActivity extends FragmentActivity implements OnIabSetupFinishedListener, OnIabPurchaseFinishedListener {
    private static final String KEY = "YOUR_KEY_HERE";
    public static final String ITEM_SKU_BASIC = "bronze_donation";
    public static final String ITEM_SKU_INTERMEDIATE = "silver_donation";
    public static final String ITEM_SKU_ADVANCED = "gold_donation";
    private IabHelper billingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        billingHelper = new IabHelper(this, KEY);
        billingHelper.startSetup(this);
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        if(result.isSuccess()) {
            dealWithIabSetupSuccess();
        }
        else {
            dealWithIabSetupFailure();
        }
    }

    protected abstract void dealWithIabSetupSuccess();

    protected abstract void dealWithIabSetupFailure();

    protected abstract void dealWithPurchaseSuccess(IabResult result, String sku);

    protected abstract void dealWithPurchaseFailed(IabResult result);

    public void purchaseItem(String sku) {
        billingHelper.launchPurchaseFlow(this, sku, 123, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        billingHelper.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * Security Recommendation: When you receive the purchase response from
     * Google Play, make sure to check the returned data signature, the orderId,
     * and the developerPayload string in the Purchase object to make sure that
     * you are getting the expected values. You should verify that the orderId
     * is a unique value that you have not previously processed, and the
     * developerPayload string matches the token that you sent previously with
     * the purchase request. As a further security precaution, you should
     * perform the verification on your own secure server.
     */
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        if(result.isFailure()) {
            dealWithPurchaseFailed(result);
        }
        else {
            dealWithPurchaseSuccess(result, info.getSku());
        }
    }

    @Override
    protected void onDestroy() {
        disposeBillingHelper();
        super.onDestroy();
    }

    private void disposeBillingHelper() {
        if(billingHelper != null) {
            billingHelper.dispose();
        }
        billingHelper = null;
    }
}
